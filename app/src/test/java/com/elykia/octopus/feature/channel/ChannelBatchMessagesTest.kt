package com.elykia.octopus.feature.channel

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ChannelBatchMessagesTest {
    @Test
    fun progressMessagesUseSharedEnglishCopy() {
        assertThat(channelBatchProgressMessage(ChannelBatchProgressAction.Delete, 1, 3))
            .isEqualTo("Deleting 1/3...")
        assertThat(channelBatchProgressMessage(ChannelBatchProgressAction.Enable, 2, 3))
            .isEqualTo("Enabling 2/3...")
        assertThat(channelBatchProgressMessage(ChannelBatchProgressAction.Disable, 3, 3))
            .isEqualTo("Disabling 3/3...")
    }

    @Test
    fun resultMessageMatchesBatchCopy() {
        assertThat(channelBatchResultMessage(successCount = 2, failCount = 1))
            .isEqualTo("2 succeeded, 1 failed")
    }
}
