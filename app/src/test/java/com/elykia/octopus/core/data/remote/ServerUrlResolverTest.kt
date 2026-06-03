package com.elykia.octopus.core.data.remote

import com.elykia.octopus.core.data.model.ServerConfig
import com.google.common.truth.Truth.assertThat
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Test

class ServerUrlResolverTest {
    @Test
    fun `normalize falls back to local default when config is blank`() {
        val result = ServerUrlResolver.normalize(ServerConfig(baseUrl = ""))

        assertThat(result.toString()).isEqualTo("https://127.0.0.1:8080/")
    }

    @Test
    fun `merge preserves base path and request path`() {
        val baseUrl = "https://example.com/octopus/".toHttpUrl()
        val requestUrl = "https://127.0.0.1:8080/api/v1/user/status?x=1".toHttpUrl()

        val result = ServerUrlResolver.merge(baseUrl, requestUrl)

        assertThat(result.toString()).isEqualTo("https://example.com/octopus/api/v1/user/status?x=1")
    }

    @Test
    fun `normalize trims trailing slash and keeps configured path`() {
        val result = ServerUrlResolver.normalize(ServerConfig(baseUrl = " https://example.com/octopus// "))

        assertThat(result.toString()).isEqualTo("https://example.com/octopus/")
    }

    @Test
    fun `normalize rejects cleartext server url`() {
        val result = ServerUrlResolver.normalize(ServerConfig(baseUrl = "http://192.168.1.10:3000/admin/"))

        assertThat(result.toString()).isEqualTo("https://127.0.0.1:8080/")
    }

    @Test
    fun `normalize rejects credentials query and fragment`() {
        assertThat(ServerUrlResolver.normalize(ServerConfig(baseUrl = "https://user:pass@example.com/")).toString())
            .isEqualTo("https://127.0.0.1:8080/")
        assertThat(ServerUrlResolver.normalize(ServerConfig(baseUrl = "https://example.com/?token=secret")).toString())
            .isEqualTo("https://127.0.0.1:8080/")
        assertThat(ServerUrlResolver.normalize(ServerConfig(baseUrl = "https://example.com/#secret")).toString())
            .isEqualTo("https://127.0.0.1:8080/")
    }

    @Test
    fun `merge keeps request query and replaces server origin`() {
        val baseUrl = "https://example.com/admin/".toHttpUrl()
        val requestUrl = "https://127.0.0.1:8080/api/v1/channel/list?page=1".toHttpUrl()

        val result = ServerUrlResolver.merge(baseUrl, requestUrl)

        assertThat(result.toString()).isEqualTo("https://example.com/admin/api/v1/channel/list?page=1")
    }
}
