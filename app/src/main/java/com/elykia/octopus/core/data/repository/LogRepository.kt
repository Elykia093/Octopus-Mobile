package com.elykia.octopus.core.data.repository

import com.elykia.octopus.core.common.AppResult
import com.elykia.octopus.core.common.DispatchersProvider
import com.elykia.octopus.core.data.model.LogKeywordMode
import com.elykia.octopus.core.data.model.LogKeywordScope
import com.elykia.octopus.core.data.model.LogListFilter
import com.elykia.octopus.core.data.model.LogSiteActionTargets
import com.elykia.octopus.core.data.model.RelayLog
import com.elykia.octopus.core.data.model.LogStatusFilter
import com.elykia.octopus.core.data.remote.LogApiService
import com.elykia.octopus.core.data.remote.NetworkExecutor
import com.elykia.octopus.core.data.remote.sanitizeErrorMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

data class LogPage(
    val logs: List<RelayLog> = emptyList(),
    val hasMore: Boolean = false,
    val total: Int = 0,
    val warning: String? = null,
    val searchMode: String? = null,
)

sealed interface LogStreamEvent {
    data object Connected : LogStreamEvent
    data class Item(val log: RelayLog) : LogStreamEvent
    data class Error(val message: String) : LogStreamEvent
}

@Singleton
class LogRepository @Inject constructor(
    private val apiService: LogApiService,
    private val executor: NetworkExecutor,
    private val dispatchers: DispatchersProvider,
    private val json: Json,
) {
    suspend fun logs(
        page: Int = 1,
        pageSize: Int = 20,
        filter: LogListFilter = LogListFilter(),
    ): AppResult<LogPage> = withContext(dispatchers.io) {
        when (val result = executor.executeNullable { apiService.logs(
            page = page,
            pageSize = pageSize,
            startTime = filter.startTime.toTimeQuery(),
            endTime = filter.endTime.toTimeQuery(),
            channelIds = filter.channelIds.toChannelIdsQuery(),
            status = filter.status.toStatusQuery(),
            keyword = filter.keyword.toKeywordQuery(),
            keywordScope = filter.keywordScope.toKeywordScopeQuery(),
            keywordMode = filter.keywordMode.toKeywordModeQuery(),
        ) }) {
            is AppResult.Success -> {
                val pageData = result.data
                AppResult.Success(
                    LogPage(
                        logs = pageData?.logs?.map { it.withHiddenContent() } ?: emptyList(),
                        hasMore = pageData?.hasMore ?: false,
                        total = pageData?.total ?: 0,
                        warning = pageData?.warning?.sanitizeErrorMessage(),
                        searchMode = pageData?.searchMode,
                    )
                )
            }
            is AppResult.Error -> result
        }
    }

    suspend fun clearLogs(): AppResult<String?> = withContext(dispatchers.io) {
        executor.executeNullable { apiService.clearLogs() }
    }

    suspend fun logDetail(id: Long): AppResult<RelayLog> = withContext(dispatchers.io) {
        when (val result = executor.execute { apiService.logDetail(id) }) {
            is AppResult.Success -> AppResult.Success(result.data.withVisibleContent())
            is AppResult.Error -> result
        }
    }

    suspend fun siteActionTargets(logIds: List<Long>): AppResult<Map<Long, LogSiteActionTargets>> = withContext(dispatchers.io) {
        val ids = logIds.filter { it > 0 }.distinct().sorted()
        if (ids.isEmpty()) return@withContext AppResult.Success(emptyMap())

        val merged = mutableMapOf<Long, LogSiteActionTargets>()
        for (chunk in ids.chunked(LOG_SITE_ACTION_TARGET_CHUNK_SIZE)) {
            when (val result = executor.executeNullable { apiService.siteActionTargets(chunk.joinToString(",")) }) {
                is AppResult.Success -> {
                    result.data.orEmpty().forEach { (key, value) ->
                        key.toLongOrNull()?.let { id -> merged[id] = value }
                    }
                }
                is AppResult.Error -> return@withContext result
            }
        }
        AppResult.Success(merged)
    }

    fun streamLogs(): Flow<LogStreamEvent> = flow {
        val token = when (val result = executor.execute { apiService.streamToken() }) {
            is AppResult.Success -> result.data.token
            is AppResult.Error -> {
                emit(LogStreamEvent.Error(result.message))
                return@flow
            }
        }
        if (token.isBlank()) {
            emit(LogStreamEvent.Error("实时日志令牌为空。"))
            return@flow
        }

        apiService.streamLogs(token).use { body ->
            emit(LogStreamEvent.Connected)
            val pending = mutableListOf<String>()
            body.charStream().buffered().use { reader ->
                while (currentCoroutineContext().isActive) {
                    val line = reader.readLine() ?: break
                    when {
                        line.startsWith("data:") -> pending += line.removePrefix("data:").trimStart()
                        line.isBlank() -> emitPendingStreamLog(pending)
                    }
                }
                emitPendingStreamLog(pending)
            }
        }
    }.catch { throwable ->
        if (throwable is CancellationException) throw throwable
        emit(LogStreamEvent.Error(throwable.toStreamMessage()))
    }.flowOn(dispatchers.io)

    private suspend fun FlowCollector<LogStreamEvent>.emitPendingStreamLog(
        pending: MutableList<String>,
    ) {
        if (pending.isEmpty()) return
        val payload = pending.joinToString("\n").trim()
        pending.clear()
        if (payload.isBlank()) return
        val log = json.decodeFromString<RelayLog>(payload).withHiddenContent()
        emit(LogStreamEvent.Item(log))
    }
}

private fun String.toStatusQuery(): String? =
    takeIf { it.isNotBlank() && it != LogStatusFilter.All }

private fun Long?.toTimeQuery(): Long? =
    this?.takeIf { it > 0 }

private fun List<Int>.toChannelIdsQuery(): String? =
    asSequence()
        .filter { it > 0 }
        .distinct()
        .sorted()
        .joinToString(",")
        .takeIf { it.isNotBlank() }

private fun String.toKeywordQuery(): String? =
    trim().takeIf { it.isNotBlank() }

private fun String.toKeywordScopeQuery(): String? =
    takeIf { it.isNotBlank() && it != LogKeywordScope.Default }

private fun String.toKeywordModeQuery(): String? =
    takeIf { it.isNotBlank() && it != LogKeywordMode.Default }

internal fun RelayLog.withHiddenContent(): RelayLog = copy(
    requestContent = "",
    responseContent = "",
    error = error.sanitizeErrorMessage(),
    attempts = attempts.map { attempt ->
        attempt.copy(msg = attempt.msg?.sanitizeErrorMessage())
    },
)

private fun RelayLog.withVisibleContent(): RelayLog = copy(
    requestContent = requestContent.sanitizeErrorMessage(),
    responseContent = responseContent.sanitizeErrorMessage(),
    error = error.sanitizeErrorMessage(),
    attempts = attempts.map { attempt ->
        attempt.copy(msg = attempt.msg?.sanitizeErrorMessage())
    },
)

private fun Throwable.toStreamMessage(): String = when (this) {
    is HttpException -> message().takeIf { it.isNotBlank() } ?: "实时日志连接失败，状态码：${code()}"
    is IOException -> message ?: "实时日志网络错误。"
    is SerializationException -> message ?: "实时日志解析失败。"
    else -> message ?: "实时日志连接失败。"
}.sanitizeErrorMessage()

private const val LOG_SITE_ACTION_TARGET_CHUNK_SIZE = 100
