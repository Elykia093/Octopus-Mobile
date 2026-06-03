package com.elykia.octopus.core.data.remote

import com.elykia.octopus.core.common.AppResult
import com.elykia.octopus.core.data.model.ApiEnvelope
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.coroutines.CancellationException
import okhttp3.ResponseBody
import retrofit2.HttpException
import java.io.ByteArrayOutputStream
import java.io.IOException
import javax.inject.Singleton

@Singleton
class NetworkExecutor(
    private val json: Json,
    private val onUnauthorized: () -> Unit = {},
) {
    suspend fun <T> execute(block: suspend () -> ApiEnvelope<T>): AppResult<T> {
        return runAppCatching {
            val response = block().requireSuccess()
            val data = response.data
            if (data != null) {
                AppResult.Success(data)
            } else {
                AppResult.Error((response.message.takeIf { !it.isNullOrBlank() } ?: "响应为空。").sanitizeErrorMessage())
            }
        }
    }

    suspend fun <T> executeNullable(block: suspend () -> ApiEnvelope<T?>): AppResult<T?> {
        return runAppCatching {
            val response = block().requireSuccess()
            AppResult.Success(response.data)
        }
    }

    private inline fun <T> runAppCatching(block: () -> AppResult<T>): AppResult<T> {
        return try {
            block()
        } catch (exception: ApiException) {
            if (exception.code == UNAUTHORIZED_CODE) {
                onUnauthorized()
            }
            AppResult.Error(exception.message.sanitizeErrorMessage(), exception)
        } catch (exception: HttpException) {
            if (exception.code() == UNAUTHORIZED_CODE) {
                onUnauthorized()
            }
            AppResult.Error(exception.toReadableMessage(), exception)
        } catch (exception: IOException) {
            AppResult.Error((exception.message ?: "网络错误。").sanitizeErrorMessage(), exception)
        } catch (exception: SerializationException) {
            AppResult.Error((exception.message ?: "数据解析失败。").sanitizeErrorMessage(), exception)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            AppResult.Error((exception.message ?: "发生未知错误。").sanitizeErrorMessage(), exception)
        }
    }

    private fun <T> ApiEnvelope<T>.requireSuccess(): ApiEnvelope<T> {
        val responseCode = code ?: 200
        if (responseCode in 200..299) {
            return this
        }

        throw ApiException(
            code = responseCode,
            message = message.takeIf { !it.isNullOrBlank() } ?: "请求失败，状态码：$responseCode",
        )
    }

    private fun HttpException.toReadableMessage(): String {
        val errorBody = runCatching {
            response()?.errorBody()?.readUtf8Limited(MAX_ERROR_BODY_BYTES).orEmpty()
        }.getOrDefault("")
        val serverMessage = parseServerMessage(errorBody)
        if (!serverMessage.isNullOrBlank()) {
            return serverMessage.sanitizeErrorMessage()
        }

        return (message().takeIf { it.isNotBlank() } ?: "请求失败，状态码：${code()}").sanitizeErrorMessage()
    }

    private fun parseServerMessage(body: String): String? {
        if (body.isBlank()) {
            return null
        }

        return runCatching {
            json.parseToJsonElement(body)
                .jsonObject["message"]
                ?.jsonPrimitive
                ?.contentOrNull
                ?.takeIf { it.isNotBlank() }
        }.getOrNull()
    }
}

internal fun ResponseBody.readUtf8Limited(maxBytes: Int): String {
    return use { body ->
        val output = ByteArrayOutputStream()
        body.byteStream().use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var total = 0
            while (true) {
                val read = input.read(buffer)
                if (read == -1) break
                total += read
                if (total > maxBytes) {
                    throw IOException("错误响应过大。")
                }
                output.write(buffer, 0, read)
            }
        }
        output.toString(Charsets.UTF_8.name())
    }
}

internal fun String.sanitizeErrorMessage(): String {
    var sanitized = this

    sanitized = AUTHORIZATION_PATTERN.replace(sanitized) { match ->
        val prefix = match.groups[1]?.value.orEmpty()
        val scheme = match.groups[2]?.value?.trim()?.takeIf { it.isNotBlank() }?.let { "$it " }.orEmpty()
        "$prefix$scheme****"
    }
    sanitized = BEARER_PATTERN.replace(sanitized, "Bearer ****")
    sanitized = SENSITIVE_ASSIGNMENT_PATTERN.replace(sanitized) { match ->
        "${match.groups[1]?.value.orEmpty()}****${match.groups[3]?.value.orEmpty()}"
    }
    sanitized = SENSITIVE_QUERY_PATTERN.replace(sanitized) { match ->
        "${match.groups[1]?.value.orEmpty()}****"
    }
    sanitized = API_KEY_PATTERN.replace(sanitized, "sk-****")
    sanitized = JWT_PATTERN.replace(sanitized, "jwt-****")

    return sanitized
}

private val AUTHORIZATION_PATTERN = Regex(
    pattern = """(?i)\b(authorization\s*[:=]\s*)(bearer\s+)?([^\s,;}\]]+)""",
)
private val BEARER_PATTERN = Regex(
    pattern = """(?i)\bBearer\s+[A-Za-z0-9._~+/\-=]+""",
)
private val SENSITIVE_ASSIGNMENT_PATTERN = Regex(
    pattern = """(?i)(["']?(?:api[_-]?key|x[_-]?api[_-]?key|channel[_-]?key|access[_-]?key|private[_-]?key|secret[_-]?key|client[_-]?private[_-]?key|access[_-]?token|refresh[_-]?token|token|password|passwd|secret|client[_-]?secret)["']?\s*[:=]\s*["']?)([^"'\s&,;}]+)(["']?)""",
)
private val SENSITIVE_QUERY_PATTERN = Regex(
    pattern = """(?i)(\b(?:api[_-]?key|x[_-]?api[_-]?key|channel[_-]?key|access[_-]?key|private[_-]?key|secret[_-]?key|client[_-]?private[_-]?key|access[_-]?token|refresh[_-]?token|token|password|passwd|secret|client[_-]?secret)=)([^&\s]+)""",
)
private val API_KEY_PATTERN = Regex(
    pattern = """\bsk-[A-Za-z0-9_-]{3,}\b""",
)
private val JWT_PATTERN = Regex(
    pattern = """\b[A-Za-z0-9_-]{20,}\.[A-Za-z0-9_-]{20,}\.[A-Za-z0-9_-]{10,}\b""",
)

private const val MAX_ERROR_BODY_BYTES = 64 * 1024
private const val UNAUTHORIZED_CODE = 401
