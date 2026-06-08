package com.elykia.octopus.core.data.repository

import com.elykia.octopus.core.common.AppResult
import com.elykia.octopus.core.common.DispatchersProvider
import com.elykia.octopus.core.data.model.RelayLog
import com.elykia.octopus.core.data.remote.LogApiService
import com.elykia.octopus.core.data.remote.NetworkExecutor
import com.elykia.octopus.core.data.remote.sanitizeErrorMessage
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

data class LogPage(
    val logs: List<RelayLog> = emptyList(),
    val hasMore: Boolean = false,
    val total: Int = 0,
    val warning: String? = null,
)

@Singleton
class LogRepository @Inject constructor(
    private val apiService: LogApiService,
    private val executor: NetworkExecutor,
    private val dispatchers: DispatchersProvider,
) {
    suspend fun logs(page: Int = 1, pageSize: Int = 20): AppResult<LogPage> = withContext(dispatchers.io) {
        when (val result = executor.executeNullable { apiService.logs(page, pageSize) }) {
            is AppResult.Success -> {
                val pageData = result.data
                AppResult.Success(
                    LogPage(
                        logs = pageData?.logs?.map { it.withHiddenContent() } ?: emptyList(),
                        hasMore = pageData?.hasMore ?: false,
                        total = pageData?.total ?: 0,
                        warning = pageData?.warning?.sanitizeErrorMessage(),
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
}

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
