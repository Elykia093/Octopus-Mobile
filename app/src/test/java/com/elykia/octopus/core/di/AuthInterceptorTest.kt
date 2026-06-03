package com.elykia.octopus.core.di

import com.elykia.octopus.core.data.local.SessionManager
import com.elykia.octopus.core.data.model.AuthState
import com.google.common.truth.Truth.assertThat
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Test

class AuthInterceptorTest {
    @Test
    fun authInterceptorAddsBearerTokenWhenSessionExists() {
        val server = MockWebServer().apply {
            enqueue(MockResponse().setResponseCode(200))
            start()
        }
        val sessionManager = SessionManager().apply {
            update(AuthState(token = "session-token", serverUrl = server.url("/").toString().trimEnd('/')))
        }

        try {
            val client = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(sessionManager) {})
                .build()

            client.newCall(Request.Builder().url(server.url("/status")).build()).execute().close()

            assertThat(server.takeRequest().getHeader("Authorization")).isEqualTo("Bearer session-token")
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun authInterceptorClearsSessionOnUnauthorizedResponseWithToken() {
        var unauthorizedCleared = false
        val server = MockWebServer().apply {
            enqueue(MockResponse().setResponseCode(401))
            start()
        }
        val sessionManager = SessionManager().apply {
            update(AuthState(token = "expired-token", serverUrl = server.url("/").toString().trimEnd('/')))
        }

        try {
            val client = OkHttpClient.Builder()
                .addInterceptor(
                    AuthInterceptor(sessionManager) {
                        unauthorizedCleared = true
                        sessionManager.clear()
                    }
                )
                .build()

            client.newCall(Request.Builder().url(server.url("/status")).build()).execute().close()

            assertThat(unauthorizedCleared).isTrue()
            assertThat(sessionManager.currentAuth().token).isEmpty()
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun authInterceptorDoesNotClearSessionWhenUnauthorizedResponseHasNoToken() {
        val sessionManager = SessionManager()
        var unauthorizedCleared = false
        val server = MockWebServer().apply {
            enqueue(MockResponse().setResponseCode(401))
            start()
        }

        try {
            val client = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(sessionManager) { unauthorizedCleared = true })
                .build()

            client.newCall(Request.Builder().url(server.url("/status")).build()).execute().close()

            assertThat(unauthorizedCleared).isFalse()
            assertThat(server.takeRequest().getHeader("Authorization")).isNull()
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun authInterceptorDoesNotSendTokenToDifferentServer() {
        val sessionManager = SessionManager().apply {
            update(AuthState(token = "session-token", serverUrl = "https://old.example.com"))
        }
        val server = MockWebServer().apply {
            enqueue(MockResponse().setResponseCode(200))
            start()
        }

        try {
            val client = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(sessionManager) {})
                .build()

            client.newCall(Request.Builder().url(server.url("/status")).build()).execute().close()

            assertThat(server.takeRequest().getHeader("Authorization")).isNull()
        } finally {
            server.shutdown()
        }
    }
}
