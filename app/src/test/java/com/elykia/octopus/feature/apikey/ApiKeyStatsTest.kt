package com.elykia.octopus.feature.apikey

import com.elykia.octopus.core.data.model.StatsApiKeyEntry
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.Locale

class ApiKeyStatsTest {
    @Test
    fun successRateFormatsZeroWhenNoRequests() {
        assertThat(formatApiKeyStatsSuccessRate(StatsApiKeyEntry())).isEqualTo("0%")
    }

    @Test
    fun successRateFormatsOneDecimal() {
        val previousLocale = Locale.getDefault()
        Locale.setDefault(Locale.US)
        try {
            val stats = StatsApiKeyEntry(requestSuccess = 2, requestFailed = 1)

            assertThat(formatApiKeyStatsSuccessRate(stats)).isEqualTo("66.7%")
        } finally {
            Locale.setDefault(previousLocale)
        }
    }
}
