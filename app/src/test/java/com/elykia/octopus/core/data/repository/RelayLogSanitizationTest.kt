package com.elykia.octopus.core.data.repository

import com.elykia.octopus.core.data.model.ChannelAttempt
import com.elykia.octopus.core.data.model.RelayLog
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RelayLogSanitizationTest {
    @Test
    fun relayLogContentIsClearedBeforeEnteringUiState() {
        val log = RelayLog(
            id = 1,
            time = 123,
            requestModelName = "gpt-test",
            requestApiKeyName = "test-key",
            channelId = 2,
            channelName = "test-channel",
            actualModelName = "gpt-test",
            inputTokens = 3,
            outputTokens = 4,
            ftut = 5,
            useTime = 6,
            cost = 0.01,
            requestContent = """{"Authorization":"Bearer sk-secret123","prompt":"hello"}""",
            responseContent = """{"answer":"world","token":"raw-token"}""",
            error = "upstream failed token=raw-token channelKey=camel-secret",
            attempts = listOf(
                ChannelAttempt(
                    channelId = 2,
                    channelKeyId = 9,
                    channelName = "test-channel",
                    modelName = "gpt-test",
                    attemptNum = 1,
                    status = "failed",
                    duration = 42,
                    msg = "Authorization: Bearer sk-secret123 channel_key=ck-live-secret",
                )
            ),
        )

        val sanitized = log.withHiddenContent()

        assertThat(sanitized.requestContent).isEmpty()
        assertThat(sanitized.responseContent).isEmpty()
        assertThat(sanitized.error).isEqualTo("upstream failed token=**** channelKey=****")
        assertThat(sanitized.attempts.first().msg).isEqualTo("Authorization: Bearer **** channel_key=****")
    }
}
