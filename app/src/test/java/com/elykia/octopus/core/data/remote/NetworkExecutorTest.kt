package com.elykia.octopus.core.data.remote

import com.elykia.octopus.core.common.AppResult
import com.elykia.octopus.core.data.model.ApiEnvelope
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import kotlinx.coroutines.CancellationException

class NetworkExecutorTest {
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        isLenient = true
    }
    private val executor = NetworkExecutor(json)

    @Test
    fun executeReturnsSuccessWhenApiCodeIs200AndDataIsPresent() = runBlocking {
        val result = executor.execute {
            ApiEnvelope(code = 200, message = "success", data = "ok")
        }

        assertThat(result).isEqualTo(AppResult.Success("ok"))
    }

    @Test
    fun executeReturnsApiErrorWhenEnvelopeCodeIsNotSuccessful() = runBlocking {
        val result = executor.execute<String> {
            ApiEnvelope(code = 500, message = "server exploded", data = null)
        }

        assertThat(result).isInstanceOf(AppResult.Error::class.java)
        assertThat((result as AppResult.Error).message).isEqualTo("server exploded")
    }

    @Test
    fun executeInvokesUnauthorizedCallbackWhenEnvelopeCodeIs401() = runBlocking {
        var unauthorized = false
        val executor = NetworkExecutor(json) {
            unauthorized = true
        }

        val result = executor.execute<String> {
            ApiEnvelope(code = 401, message = "session expired", data = null)
        }

        assertThat(result).isInstanceOf(AppResult.Error::class.java)
        assertThat((result as AppResult.Error).message).isEqualTo("session expired")
        assertThat(unauthorized).isTrue()
    }

    @Test
    fun executeNullableAcceptsEmptySuccessfulResponse() = runBlocking {
        val result = executor.executeNullable {
            ApiEnvelope<String?>(code = 200, message = "success", data = null)
        }

        assertThat(result).isEqualTo(AppResult.Success(null))
    }

    @Test
    fun executeNullableAcceptsNonStringEmptySuccessfulResponse() = runBlocking {
        val result = executor.executeNullable {
            ApiEnvelope<Int?>(code = 200, message = "success", data = null)
        }

        assertThat(result).isEqualTo(AppResult.Success(null))
    }

    @Test
    fun executeMapsHttpErrorBodyMessageToReadableError() = runBlocking {
        val errorBody = """{"code":401,"message":"Authentication failed"}"""
            .toResponseBody("application/json".toMediaType())
        val result = executor.execute<String> {
            throw HttpException(Response.error<String>(401, errorBody))
        }

        assertThat(result).isInstanceOf(AppResult.Error::class.java)
        assertThat((result as AppResult.Error).message).isEqualTo("Authentication failed")
    }

    @Test
    fun executeInvokesUnauthorizedCallbackWhenHttpStatusIs401() = runBlocking {
        var unauthorized = false
        val executor = NetworkExecutor(json) {
            unauthorized = true
        }
        val errorBody = """{"code":401,"message":"Authentication failed"}"""
            .toResponseBody("application/json".toMediaType())

        val result = executor.execute<String> {
            throw HttpException(Response.error<String>(401, errorBody))
        }

        assertThat(result).isInstanceOf(AppResult.Error::class.java)
        assertThat((result as AppResult.Error).message).isEqualTo("Authentication failed")
        assertThat(unauthorized).isTrue()
    }

    @Test
    fun executeMapsIoExceptionToReadableError() = runBlocking {
        val result = executor.execute<String> {
            throw IOException("timeout")
        }

        assertThat(result).isInstanceOf(AppResult.Error::class.java)
        assertThat((result as AppResult.Error).message).isEqualTo("timeout")
    }

    @Test
    fun executeRethrowsCancellationException() = runBlocking {
        var thrown: CancellationException? = null

        try {
            executor.execute<String> {
                throw CancellationException("screen left")
            }
        } catch (exception: CancellationException) {
            thrown = exception
        }

        assertThat(thrown).isNotNull()
        assertThat(thrown?.message).isEqualTo("screen left")
    }

    @Test
    fun executeSanitizesSensitiveValuesFromServerMessages() = runBlocking {
        val errorBody = """
            {
              "code": 401,
              "message": "Authorization: Bearer sk-secret123 token=raw-token password=hunter2 channel_key=ck-live-secret channelKey=camel-secret x-api-key=x-secret jwt=aaaaaaaaaaaaaaaaaaaa.bbbbbbbbbbbbbbbbbbbb.cccccccccc"
            }
        """.trimIndent().toResponseBody("application/json".toMediaType())
        val result = executor.execute<String> {
            throw HttpException(Response.error<String>(401, errorBody))
        }

        assertThat(result).isInstanceOf(AppResult.Error::class.java)
        val message = (result as AppResult.Error).message
        assertThat(message).contains("Authorization: Bearer ****")
        assertThat(message).contains("token=****")
        assertThat(message).contains("password=****")
        assertThat(message).contains("channel_key=****")
        assertThat(message).contains("channelKey=****")
        assertThat(message).contains("x-api-key=****")
        assertThat(message).contains("jwt-****")
        assertThat(message).doesNotContain("sk-secret123")
        assertThat(message).doesNotContain("raw-token")
        assertThat(message).doesNotContain("hunter2")
        assertThat(message).doesNotContain("ck-live-secret")
        assertThat(message).doesNotContain("camel-secret")
        assertThat(message).doesNotContain("x-secret")
        assertThat(message).doesNotContain("aaaaaaaaaaaaaaaaaaaa")
    }

    @Test
    fun executeSanitizesPrivateAndAccessKeyNamesFromServerMessages() = runBlocking {
        val errorBody = """
            {
              "code": 500,
              "message": "access_key=ak-secret private_key=pk-secret secret_key=sk-secret clientPrivateKey=client-secret"
            }
        """.trimIndent().toResponseBody("application/json".toMediaType())
        val result = executor.execute<String> {
            throw HttpException(Response.error<String>(500, errorBody))
        }

        assertThat(result).isInstanceOf(AppResult.Error::class.java)
        val message = (result as AppResult.Error).message
        assertThat(message).contains("access_key=****")
        assertThat(message).contains("private_key=****")
        assertThat(message).contains("secret_key=****")
        assertThat(message).contains("clientPrivateKey=****")
        assertThat(message).doesNotContain("ak-secret")
        assertThat(message).doesNotContain("pk-secret")
        assertThat(message).doesNotContain("sk-secret")
        assertThat(message).doesNotContain("client-secret")
    }

    @Test
    fun executeDoesNotExposeOversizedHttpErrorBody() = runBlocking {
        val errorBody = """{"message":"${"sk-secret123".repeat(7000)}"}"""
            .toResponseBody("application/json".toMediaType())
        val result = executor.execute<String> {
            throw HttpException(Response.error<String>(500, errorBody))
        }

        assertThat(result).isInstanceOf(AppResult.Error::class.java)
        val message = (result as AppResult.Error).message
        assertThat(message).doesNotContain("sk-secret123")
    }
}
