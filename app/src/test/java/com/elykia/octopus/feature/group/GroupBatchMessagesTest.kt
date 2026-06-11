package com.elykia.octopus.feature.group

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class GroupBatchMessagesTest {
    @Test
    fun progressMessageUsesSharedEnglishCopy() {
        assertThat(groupBatchProgressMessage(current = 1, total = 3))
            .isEqualTo("Deleting 1/3...")
    }

    @Test
    fun resultMessageMatchesBatchCopy() {
        assertThat(groupBatchResultMessage(successCount = 2, failCount = 1))
            .isEqualTo("2 succeeded, 1 failed")
    }
}
