package com.elykia.octopus.feature.apikey

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ApiKeyBatchMessagesTest {
    @Test
    fun progressMessagesUseLocalizedNeutralEnglishCopy() {
        assertThat(apiKeyBatchProgressMessage(ApiKeyBatchProgressAction.Delete, 1, 3))
            .isEqualTo("Deleting 1/3...")
        assertThat(apiKeyBatchProgressMessage(ApiKeyBatchProgressAction.Enable, 2, 3))
            .isEqualTo("Enabling 2/3...")
        assertThat(apiKeyBatchProgressMessage(ApiKeyBatchProgressAction.Disable, 3, 3))
            .isEqualTo("Disabling 3/3...")
    }

    @Test
    fun resultMessageMatchesSharedBatchCopy() {
        assertThat(apiKeyBatchResultMessage(successCount = 2, failCount = 1))
            .isEqualTo("2 succeeded, 1 failed")
    }
}
