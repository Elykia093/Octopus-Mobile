package com.elykia.octopus.feature.proxypool

import com.elykia.octopus.core.data.model.ProxyConfiguration
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ProxyPoolEditorModelsTest {
    @Test
    fun validationAcceptsSupportedProxySchemes() {
        val supported = listOf(
            "http://127.0.0.1:8080",
            "https://proxy.example.com",
            "socks4://127.0.0.1:1080",
            "socks5://user:pass@proxy.example.com:1080",
        )

        supported.forEach { url ->
            assertThat(canSubmitProxyConfiguration(ProxyPoolEditorValues(name = "Proxy", url = url), submitting = false))
                .isTrue()
        }
    }

    @Test
    fun validationRejectsMissingNameOrUnsupportedUrl() {
        assertThat(canSubmitProxyConfiguration(ProxyPoolEditorValues(name = "", url = "socks5://127.0.0.1:1080"), false))
            .isFalse()
        assertThat(canSubmitProxyConfiguration(ProxyPoolEditorValues(name = "Proxy", url = "ftp://proxy.example.com"), false))
            .isFalse()
        assertThat(canSubmitProxyConfiguration(ProxyPoolEditorValues(name = "Proxy", url = "not a url"), false))
            .isFalse()
        assertThat(canSubmitProxyConfiguration(ProxyPoolEditorValues(name = "Proxy", url = "socks5://127.0.0.1:1080"), true))
            .isFalse()
    }

    @Test
    fun requestsTrimTextFields() {
        val values = ProxyPoolEditorValues(
            name = "  Main proxy  ",
            url = "  socks5://127.0.0.1:1080  ",
            enabled = false,
            remark = "  local  ",
        )

        val create = values.toCreateRequest()
        val update = values.toUpdateRequest(ProxyConfiguration(id = 9, name = "Old", url = "http://old.example.com"))

        assertThat(create.name).isEqualTo("Main proxy")
        assertThat(create.url).isEqualTo("socks5://127.0.0.1:1080")
        assertThat(create.enabled).isFalse()
        assertThat(create.remark).isEqualTo("local")
        assertThat(update.id).isEqualTo(9)
        assertThat(update.name).isEqualTo("Main proxy")
        assertThat(update.url).isEqualTo("socks5://127.0.0.1:1080")
    }

    @Test
    fun maskProxyUrlHidesPassword() {
        assertThat(maskProxyUrl("socks5://user:secret@proxy.example.com:1080"))
            .isEqualTo("socks5://user:***@proxy.example.com:1080")
    }
}
