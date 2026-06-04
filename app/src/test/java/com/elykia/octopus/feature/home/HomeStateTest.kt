package com.elykia.octopus.feature.home

import com.elykia.octopus.core.common.AppResult
import com.elykia.octopus.core.data.model.ApiKeyItem
import com.elykia.octopus.core.data.model.Channel
import com.elykia.octopus.core.data.model.StatsApiKeyEntry
import com.elykia.octopus.core.data.model.StatsDaily
import com.elykia.octopus.core.data.model.StatsHourly
import com.elykia.octopus.core.data.model.StatsTotal
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class HomeStateTest {
    @Test
    fun partialRefreshFailureKeepsPreviousDataAndExposesErrors() {
        val previousDaily = listOf(StatsDaily(date = "2026-06-01", requestSuccess = 10))
        val previousHourly = listOf(StatsHourly(hour = 8, date = "2026-06-01", requestSuccess = 3))
        val previousChannels = listOf(Channel(id = 1, name = "OpenAI", type = 1))
        val previousApiKeys = listOf(ApiKeyItem(id = 1, name = "Mobile", apiKey = ""))
        val previousApiKeyStats = listOf(StatsApiKeyEntry(apiKeyId = 1, requestSuccess = 5))
        val previous = HomeUiState(
            today = StatsDaily(date = "2026-06-01", requestSuccess = 1),
            total = StatsTotal(requestSuccess = 10),
            daily = previousDaily,
            hourly = previousHourly,
            channels = previousChannels,
            apiKeys = previousApiKeys,
            apiKeyStats = previousApiKeyStats,
        )

        val state = buildHomeRefreshState(
            previous = previous,
            todayResult = AppResult.Error("today failed"),
            totalResult = AppResult.Success(StatsTotal(requestSuccess = 20)),
            dailyResult = AppResult.Error("daily failed"),
            hourlyResult = AppResult.Error("hourly failed"),
            channelsResult = AppResult.Error("channels failed"),
            apiKeysResult = AppResult.Error("keys failed"),
            apiKeyStatsResult = AppResult.Error("stats failed"),
        )

        assertThat(state.loading).isFalse()
        assertThat(state.total?.requestSuccess).isEqualTo(20)
        assertThat(state.today).isEqualTo(previous.today)
        assertThat(state.daily).isEqualTo(previousDaily)
        assertThat(state.hourly).isEqualTo(previousHourly)
        assertThat(state.channels).isEqualTo(previousChannels)
        assertThat(state.apiKeys).isEqualTo(previousApiKeys)
        assertThat(state.apiKeyStats).isEqualTo(previousApiKeyStats)
        assertThat(state.partialErrors()).containsExactly(
            "today failed",
            "daily failed",
            "hourly failed",
            "channels failed",
            "keys failed",
            "stats failed",
        ).inOrder()
    }

    @Test
    fun totalFailureKeepsPageLevelError() {
        val state = buildHomeRefreshState(
            previous = HomeUiState(total = StatsTotal(requestSuccess = 10)),
            todayResult = AppResult.Success(StatsDaily(date = "2026-06-01")),
            totalResult = AppResult.Error("total failed"),
            dailyResult = AppResult.Success(emptyList()),
            hourlyResult = AppResult.Success(emptyList()),
            channelsResult = AppResult.Success(emptyList()),
            apiKeysResult = AppResult.Success(emptyList()),
            apiKeyStatsResult = AppResult.Success(emptyList()),
        )

        assertThat(state.loading).isFalse()
        assertThat(state.error).isEqualTo("total failed")
        assertThat(state.total).isNull()
        assertThat(state.partialErrors()).isEmpty()
    }
}
