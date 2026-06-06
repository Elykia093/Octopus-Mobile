package com.elykia.octopus.core.data.remote

import com.elykia.octopus.core.data.model.ServerConfig
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ServerUrlProviderTest {
    @Test
    fun currentDefaultsToLocalServerUrl() {
        val provider = ServerUrlProvider()

        assertThat(provider.current().toString()).isEqualTo("https://127.0.0.1:8080/")
    }

    @Test
    fun updateCachesNormalizedServerUrl() {
        val provider = ServerUrlProvider()

        provider.update(ServerConfig(baseUrl = " https://example.com/octopus// "))

        assertThat(provider.current().toString()).isEqualTo("https://example.com/octopus/")
    }
}
