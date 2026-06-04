package com.elykia.octopus.feature.log

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class LogStateTest {
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
}

private fun sampleLog(id: Long) = com.elykia.octopus.core.data.model.RelayLog(
    id = id,
    time = 0L,
    requestModelName = "gpt-test",
    requestApiKeyName = "main",
    channelId = 1,
    channelName = "OpenAI",
    actualModelName = "gpt-test",
    inputTokens = 1,
    outputTokens = 1,
    ftut = 1,
    useTime = 1,
    cost = 0.0,
    requestContent = "",
    responseContent = "",
    error = "",
)
