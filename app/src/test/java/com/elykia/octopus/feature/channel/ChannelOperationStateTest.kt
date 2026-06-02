package com.elykia.octopus.feature.channel

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
}
