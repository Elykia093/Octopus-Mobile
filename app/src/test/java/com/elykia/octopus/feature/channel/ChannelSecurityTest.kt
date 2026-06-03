package com.elykia.octopus.feature.channel

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ChannelSecurityTest {
    @Test
    fun channelBaseUrlMustBeHttpsWhenPresent() {
        assertThat(hasValidChannelBaseUrl("")).isTrue()
        assertThat(hasValidChannelBaseUrl(" https://api.example.com/v1 ")).isTrue()
        assertThat(hasValidChannelBaseUrl("http://api.example.com/v1")).isFalse()
    }

    @Test
    fun channelBaseUrlRejectsCredentialsQueryAndFragment() {
        assertThat(hasValidChannelBaseUrl("https://user:pass@api.example.com/v1")).isFalse()
        assertThat(hasValidChannelBaseUrl("https://api.example.com/v1?token=secret")).isFalse()
        assertThat(hasValidChannelBaseUrl("https://api.example.com/v1#secret")).isFalse()
    }
}
