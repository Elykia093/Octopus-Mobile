package com.elykia.octopus.feature.setting

import com.elykia.octopus.core.data.model.ApiKeyItem
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ApiKeyOperationStateTest {
    @Test
    fun apiKeyFailureKeepsVisibleErrorAndDoesNotClearCreatedKey() {
        val created = ApiKeyItem(id = 1, name = "New", apiKey = "sk-created")
        val state = SettingUiState(
            apiKeySubmitting = true,
            createdApiKey = created,
        ).apiKeyOperationFailed("create failed")

        assertThat(state.apiKeySubmitting).isFalse()
        assertThat(state.apiKeyOperationError).isEqualTo("create failed")
        assertThat(state.createdApiKey).isEqualTo(created)
    }

    @Test
    fun apiKeyStartClearsPreviousErrorAndVisibleCreatedKey() {
        val started = SettingUiState(
            apiKeyOperationError = "old error",
            createdApiKey = ApiKeyItem(id = 1, name = "New", apiKey = "sk-created"),
        )
            .apiKeyOperationStarted()

        assertThat(started.apiKeySubmitting).isTrue()
        assertThat(started.apiKeyOperationError).isNull()
        assertThat(started.createdApiKey).isNull()

        val succeeded = started.apiKeyOperationSucceeded()

        assertThat(succeeded.apiKeySubmitting).isFalse()
        assertThat(succeeded.apiKeyOperationError).isNull()
    }
}
