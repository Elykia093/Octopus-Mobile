package com.elykia.octopus.core.di

import com.elykia.octopus.core.data.model.ServerConfig
import com.elykia.octopus.core.data.remote.ServerUrlProvider
import com.google.common.truth.Truth.assertThat
import java.util.concurrent.TimeUnit
import okhttp3.Call
import okhttp3.Connection
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test

class BaseUrlInterceptorTest {
    @Test
    fun rewritesRequestUrlFromCachedServerUrl() {
        val provider = ServerUrlProvider().apply {
            update(ServerConfig(baseUrl = "https://example.com/octopus/"))
        }
        val chain = CapturingChain(
            Request.Builder()
                .url("https://127.0.0.1:8080/api/v1/user/status?x=1")
                .build()
        )

        BaseUrlInterceptor(provider).intercept(chain).close()

        assertThat(chain.proceededRequest?.url.toString())
            .isEqualTo("https://example.com/octopus/api/v1/user/status?x=1")
    }
}

private class CapturingChain(
    private val request: Request,
) : Interceptor.Chain {
    var proceededRequest: Request? = null

    override fun request(): Request = request

    override fun proceed(request: Request): Response {
        proceededRequest = request
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body("".toResponseBody())
            .build()
    }

    override fun connection(): Connection? = null

    override fun call(): Call = error("Call is not used by this test")

    override fun connectTimeoutMillis(): Int = 10_000

    override fun withConnectTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain = this

    override fun readTimeoutMillis(): Int = 10_000

    override fun withReadTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain = this

    override fun writeTimeoutMillis(): Int = 10_000

    override fun withWriteTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain = this
}
