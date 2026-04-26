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

class NetworkExecutorTest {
    private val executor = NetworkExecutor(
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
            isLenient = true
        }
    )

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
    fun executeMapsIoExceptionToReadableError() = runBlocking {
        val result = executor.execute<String> {
            throw IOException("timeout")
        }

        assertThat(result).isInstanceOf(AppResult.Error::class.java)
        assertThat((result as AppResult.Error).message).isEqualTo("timeout")
    }
}
