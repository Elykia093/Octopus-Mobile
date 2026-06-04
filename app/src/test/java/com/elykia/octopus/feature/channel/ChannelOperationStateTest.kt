package com.elykia.octopus.feature.channel

import com.elykia.octopus.core.data.model.BaseUrl
import com.elykia.octopus.core.data.model.Channel
import com.elykia.octopus.core.data.model.ChannelKey
import com.elykia.octopus.core.data.model.CustomHeader
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ChannelOperationStateTest {
    @Test
    fun operationFailureKeepsVisibleErrorAndStopsSubmitting() {
        val state = ChannelUiState(submitting = true)
            .channelOperationFailed("channel failed")

        assertThat(state.submitting).isFalse()
        assertThat(state.operationError).isEqualTo("channel failed")
    }

    @Test
    fun operationStartAndSuccessClearPreviousError() {
        val started = ChannelUiState(operationError = "old error")
            .channelOperationStarted()

        assertThat(started.submitting).isTrue()
        assertThat(started.operationError).isNull()

        val succeeded = started.channelOperationSucceeded()

        assertThat(succeeded.submitting).isFalse()
        assertThat(succeeded.operationError).isNull()
    }

    @Test
    fun basicMobileEditorOnlyAllowsSingleUrlSingleKeyAndNoAdvancedFields() {
        assertThat(Channel(name = "simple", type = 0).canUseBasicMobileEditor()).isTrue()
        assertThat(
            Channel(
                name = "single",
                type = 0,
                baseUrls = listOf(BaseUrl("https://api.example.com")),
                keys = listOf(ChannelKey(channelKey = "")),
            ).canUseBasicMobileEditor(),
        ).isTrue()

        assertThat(
            Channel(
                name = "multi url",
                type = 0,
                baseUrls = listOf(BaseUrl("https://a.example.com"), BaseUrl("https://b.example.com")),
            ).canUseBasicMobileEditor(),
        ).isFalse()
        assertThat(
            Channel(
                name = "multi key",
                type = 0,
                keys = listOf(ChannelKey(channelKey = ""), ChannelKey(channelKey = "")),
            ).canUseBasicMobileEditor(),
        ).isFalse()
        assertThat(
            Channel(
                name = "advanced",
                type = 0,
                customHeader = listOf(CustomHeader(headerKey = "X-Test", headerValue = "1")),
            ).canUseBasicMobileEditor(),
        ).isFalse()
    }
}
