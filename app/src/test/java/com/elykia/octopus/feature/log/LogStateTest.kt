package com.elykia.octopus.feature.log

import com.elykia.octopus.core.data.model.LogKeywordMode
import com.elykia.octopus.core.data.model.LogKeywordScope
import com.elykia.octopus.core.data.model.LogListFilter
import com.elykia.octopus.core.data.model.LogStatusFilter
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class LogStateTest {
    @Test
    fun pageErrorOnlyShowsWhenRefreshFailsWithoutCachedLogs() {
        assertThat(
            LogUiState(error = "logs failed", logs = emptyList())
                .shouldShowPageError(),
        ).isTrue()

        assertThat(
            LogUiState(
                error = "logs failed",
                logs = listOf(sampleLog(id = 1)),
            ).shouldShowPageError(),
        ).isFalse()
    }

    @Test
    fun clearLogsStartClearsPreviousErrorAndMarksClearing() {
        val state = LogUiState(clearError = "old error")
            .clearLogsStarted()

        assertThat(state.clearing).isTrue()
        assertThat(state.clearError).isNull()
    }

    @Test
    fun clearLogsSuccessAndFailureStopClearing() {
        val succeeded = LogUiState(
            loading = true,
            loadingMore = true,
            clearing = true,
            logs = listOf(sampleLog(id = 1), sampleLog(id = 2)),
            page = 3,
            hasMore = false,
            pagingError = "old paging error",
            clearError = "old error",
        )
            .clearLogsSucceeded()

        assertThat(succeeded.clearing).isFalse()
        assertThat(succeeded.loading).isFalse()
        assertThat(succeeded.loadingMore).isFalse()
        assertThat(succeeded.logs).isEmpty()
        assertThat(succeeded.page).isEqualTo(1)
        assertThat(succeeded.hasMore).isTrue()
        assertThat(succeeded.pagingError).isNull()
        assertThat(succeeded.clearError).isNull()

        val failed = LogUiState(clearing = true)
            .clearLogsFailed("clear failed")

        assertThat(failed.clearing).isFalse()
        assertThat(failed.clearError).isEqualTo("clear failed")
    }

    @Test
    fun streamLogRespectsActiveStatusAndKeywordFilter() {
        val state = LogUiState(logs = listOf(sampleLog(id = 1, time = 100, channelId = 1, requestModelName = "gpt-4")))
        val filter = LogListFilter(
            startTime = 90,
            endTime = 110,
            channelIds = listOf(2),
            status = LogStatusFilter.Error,
            keyword = "claude",
            keywordMode = LogKeywordMode.Contains,
        )

        val ignoredSuccess = state.withStreamLog(sampleLog(id = 2, time = 100, channelId = 2, requestModelName = "claude"), filter)
        val ignoredChannel = state.withStreamLog(sampleLog(id = 3, time = 100, channelId = 1, requestModelName = "claude", error = "failed"), filter)
        val ignoredTime = state.withStreamLog(sampleLog(id = 4, time = 120, channelId = 2, requestModelName = "claude", error = "failed"), filter)
        val acceptedError = state.withStreamLog(sampleLog(id = 5, time = 100, channelId = 2, requestModelName = "claude", error = "failed"), filter)

        assertThat(ignoredSuccess.logs.map { it.id }).containsExactly(1L)
        assertThat(ignoredChannel.logs.map { it.id }).containsExactly(1L)
        assertThat(ignoredTime.logs.map { it.id }).containsExactly(1L)
        assertThat(acceptedError.logs.map { it.id }).containsExactly(5L, 1L).inOrder()
    }

    @Test
    fun streamLogKeywordContentScopeMatchesContentFields() {
        val state = LogUiState()
        val filter = LogListFilter(
            keyword = "anthropic",
            keywordScope = LogKeywordScope.Content,
            keywordMode = LogKeywordMode.Contains,
        )

        val ignoredMetadata = state.withStreamLog(sampleLog(id = 1, requestModelName = "anthropic"), filter)
        val acceptedContent = state.withStreamLog(sampleLog(id = 2, requestContent = "use anthropic route"), filter)

        assertThat(ignoredMetadata.logs).isEmpty()
        assertThat(acceptedContent.logs.map { it.id }).containsExactly(2L)
    }

    @Test
    fun logFilterActiveStateTracksServerFilterFields() {
        assertThat(LogListFilter().hasActiveFilters()).isFalse()
        assertThat(LogListFilter(startTime = 1L).hasActiveFilters()).isTrue()
        assertThat(LogListFilter(channelIds = listOf(2)).hasActiveFilters()).isTrue()
        assertThat(LogListFilter(status = LogStatusFilter.Success).hasActiveFilters()).isTrue()
        assertThat(LogListFilter(keyword = "gpt").hasActiveFilters()).isTrue()
        assertThat(LogListFilter(keywordMode = LogKeywordMode.Exact).hasActiveFilters()).isTrue()
    }
}

private fun sampleLog(
    id: Long,
    time: Long = 0L,
    channelId: Int = 1,
    requestModelName: String = "gpt-test",
    requestContent: String = "",
    error: String = "",
) = com.elykia.octopus.core.data.model.RelayLog(
    id = id,
    time = time,
    requestModelName = requestModelName,
    requestApiKeyName = "main",
    channelId = channelId,
    channelName = "OpenAI",
    actualModelName = "gpt-test",
    inputTokens = 1,
    outputTokens = 1,
    ftut = 1,
    useTime = 1,
    cost = 0.0,
    requestContent = requestContent,
    responseContent = "",
    error = error,
)
