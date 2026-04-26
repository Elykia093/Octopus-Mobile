package com.elykia.octopus.core.data.remote

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class NetworkErrorsTest {
    @Test
    fun httpExceptionUsesServerMessageFromJsonBody() {
        val body = """{"code":401,"message":"Authentication failed"}"""
            .toResponseBody("application/json".toMediaType())
        val exception = HttpException(Response.error<String>(401, body))

        assertEquals("Authentication failed", exception.toUserMessage())
    }

    @Test
    fun exceptionUsesFallbackWhenMessageIsEmpty() {
        val exception = RuntimeException("")

        assertEquals("加载失败", exception.toUserMessage("加载失败"))
    }
}
