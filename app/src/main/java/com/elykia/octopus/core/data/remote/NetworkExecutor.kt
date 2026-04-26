package com.elykia.octopus.core.data.remote

import com.elykia.octopus.core.common.AppResult
import com.elykia.octopus.core.data.model.ApiEnvelope
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkExecutor @Inject constructor(
    private val json: Json,
) {
    suspend fun <T> execute(block: suspend () -> ApiEnvelope<T>): AppResult<T> {
        return runAppCatching {
            val response = block().requireSuccess()
            val data = response.data
            if (data != null) {
                AppResult.Success(data)
            } else {
                AppResult.Error(response.message.takeIf { !it.isNullOrBlank() } ?: "响应为空。")
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
            AppResult.Error(exception.message, exception)
        } catch (exception: HttpException) {
            AppResult.Error(exception.toReadableMessage(), exception)
        } catch (exception: IOException) {
            AppResult.Error(exception.message ?: "网络错误。", exception)
        } catch (exception: SerializationException) {
            AppResult.Error(exception.message ?: "数据解析失败。", exception)
        } catch (exception: Exception) {
            AppResult.Error(exception.message ?: "发生未知错误。", exception)
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
        val errorBody = response()?.errorBody()?.string().orEmpty()
        val serverMessage = parseServerMessage(errorBody)
        if (!serverMessage.isNullOrBlank()) {
            return serverMessage
        }

        return message().takeIf { it.isNotBlank() } ?: "请求失败，状态码：${code()}"
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
