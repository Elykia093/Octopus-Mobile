package com.elykia.octopus.feature.apikey

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.Locale
import java.util.TimeZone

class ApiKeyDisplayFormatTest {
    @Test
    fun expireAtRawReturnsBlankForNeverExpireValues() {
        assertThat(formatApiKeyExpireAtRaw(null)).isEmpty()
        assertThat(formatApiKeyExpireAtRaw(0)).isEmpty()
        assertThat(formatApiKeyExpireAtRaw(-1)).isEmpty()
    }

    @Test
    fun expireAtRawFormatsUnixSeconds() {
        val previousLocale = Locale.getDefault()
        val previousTimeZone = TimeZone.getDefault()
        Locale.setDefault(Locale.US)
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
        try {
            assertThat(formatApiKeyExpireAtRaw(1_735_689_600)).isEqualTo("2025-01-01 00:00")
        } finally {
            Locale.setDefault(previousLocale)
            TimeZone.setDefault(previousTimeZone)
        }
    }
}
