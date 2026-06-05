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
    fun pageErrorOnlyShowsWhenTotalFailsWithoutCachedTotal() {
        assertThat(HomeUiState(error = "total failed").shouldShowPageError()).isTrue()

        assertThat(
            HomeUiState(
                total = StatsTotal(requestSuccess = 10),
                error = "total failed",
            ).shouldShowPageError()
        ).isFalse()
    }

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
        val previousTotal = StatsTotal(requestSuccess = 10)
        val state = buildHomeRefreshState(
            previous = HomeUiState(total = previousTotal),
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
        assertThat(state.total).isEqualTo(previousTotal)
        assertThat(state.partialErrors()).isEmpty()
    }

    @Test
    fun totalFailureStillUpdatesRecoveredSubDataAndClearsPartialErrors() {
        val recoveredDaily = listOf(StatsDaily(date = "2026-06-02", requestSuccess = 20))
        val recoveredHourly = listOf(StatsHourly(hour = 9, date = "2026-06-02", requestSuccess = 4))
        val recoveredChannels = listOf(Channel(id = 2, name = "Recovered", type = 1))
        val recoveredApiKeys = listOf(ApiKeyItem(id = 2, name = "Recovered key", apiKey = ""))
        val recoveredApiKeyStats = listOf(StatsApiKeyEntry(apiKeyId = 2, requestSuccess = 7))
        val previous = HomeUiState(
            today = StatsDaily(date = "2026-06-01", requestSuccess = 1),
            total = StatsTotal(requestSuccess = 10),
            daily = listOf(StatsDaily(date = "2026-06-01", requestSuccess = 10)),
            hourly = listOf(StatsHourly(hour = 8, date = "2026-06-01", requestSuccess = 3)),
            channels = listOf(Channel(id = 1, name = "Stale", type = 1)),
            apiKeys = listOf(ApiKeyItem(id = 1, name = "Stale key", apiKey = "")),
            apiKeyStats = listOf(StatsApiKeyEntry(apiKeyId = 1, requestSuccess = 5)),
            todayError = "old today failed",
            dailyError = "old daily failed",
            hourlyError = "old hourly failed",
            channelListError = "old channels failed",
            apiKeyListError = "old keys failed",
            apiKeyStatsError = "old stats failed",
        )

        val state = buildHomeRefreshState(
            previous = previous,
            todayResult = AppResult.Success(StatsDaily(date = "2026-06-02", requestSuccess = 2)),
            totalResult = AppResult.Error("total failed"),
            dailyResult = AppResult.Success(recoveredDaily),
            hourlyResult = AppResult.Success(recoveredHourly),
            channelsResult = AppResult.Success(recoveredChannels),
            apiKeysResult = AppResult.Success(recoveredApiKeys),
            apiKeyStatsResult = AppResult.Success(recoveredApiKeyStats),
        )

        assertThat(state.loading).isFalse()
        assertThat(state.error).isEqualTo("total failed")
        assertThat(state.total).isEqualTo(previous.total)
        assertThat(state.today?.date).isEqualTo("2026-06-02")
        assertThat(state.daily).isEqualTo(recoveredDaily)
        assertThat(state.hourly).isEqualTo(recoveredHourly)
        assertThat(state.channels).isEqualTo(recoveredChannels)
        assertThat(state.apiKeys).isEqualTo(recoveredApiKeys)
        assertThat(state.apiKeyStats).isEqualTo(recoveredApiKeyStats)
        assertThat(state.partialErrors()).isEmpty()
    }
}
