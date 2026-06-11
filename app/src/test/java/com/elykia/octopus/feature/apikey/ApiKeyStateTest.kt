package com.elykia.octopus.feature.apikey

import com.elykia.octopus.core.common.AppResult
import com.elykia.octopus.core.data.model.ApiKeyItem
import com.elykia.octopus.core.data.model.Group
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ApiKeyStateTest {
    @Test
    fun apiKeyPageErrorOnlyFollowsListFailureWithoutCachedKeys() {
        assertThat(ApiKeyUiState(apiKeyListError = "keys failed").shouldShowApiKeyPageError()).isTrue()

        assertThat(
            ApiKeyUiState(
                apiKeys = listOf(ApiKeyItem(id = 1, name = "Cached", apiKey = "")),
                apiKeyListError = "keys failed",
            ).shouldShowApiKeyPageError()
        ).isFalse()
    }

    @Test
    fun apiKeyRefreshSuccessClearsRecoveredListError() {
        val state = buildApiKeyRefreshState(
            previous = ApiKeyUiState(apiKeyListError = "old error"),
            apiKeysResult = AppResult.Success(listOf(ApiKeyItem(id = 1, name = "Main", apiKey = ""))),
        )

        assertThat(state.loading).isFalse()
        assertThat(state.apiKeys).containsExactly(ApiKeyItem(id = 1, name = "Main", apiKey = ""))
        assertThat(state.apiKeyListError).isNull()
    }

    @Test
    fun apiKeyRefreshFailureKeepsCachedKeysAndExposesError() {
        val previousKeys = listOf(ApiKeyItem(id = 1, name = "Cached", apiKey = ""))
        val state = buildApiKeyRefreshState(
            previous = ApiKeyUiState(apiKeys = previousKeys),
            apiKeysResult = AppResult.Error("keys failed"),
        )

        assertThat(state.loading).isFalse()
        assertThat(state.apiKeys).isEqualTo(previousKeys)
        assertThat(state.apiKeyListError).isEqualTo("keys failed")
    }

    @Test
    fun apiKeyRefreshSuccessUpdatesSupportedModelCandidatesFromGroups() {
        val state = buildApiKeyRefreshState(
            previous = ApiKeyUiState(supportedModelCandidates = listOf("old")),
            apiKeysResult = AppResult.Success(emptyList()),
            groupsResult = AppResult.Success(
                listOf(
                    Group(id = 1, name = "vip", mode = 1),
                    Group(id = 2, name = "default", mode = 1),
                )
            ),
        )

        assertThat(state.supportedModelCandidates).containsExactly("default", "vip").inOrder()
    }

    @Test
    fun apiKeyRefreshFailureKeepsCachedSupportedModelCandidatesWhenGroupsFail() {
        val state = buildApiKeyRefreshState(
            previous = ApiKeyUiState(supportedModelCandidates = listOf("cached")),
            apiKeysResult = AppResult.Success(emptyList()),
            groupsResult = AppResult.Error("groups failed"),
        )

        assertThat(state.supportedModelCandidates).containsExactly("cached")
    }

    @Test
    fun apiKeyFailureKeepsVisibleErrorAndDoesNotClearCreatedKey() {
        val created = ApiKeyItem(id = 1, name = "New", apiKey = "sk-created")
        val state = ApiKeyUiState(
            apiKeySubmitting = true,
            createdApiKey = created,
        ).apiKeyOperationFailed("create failed")

        assertThat(state.apiKeySubmitting).isFalse()
        assertThat(state.apiKeyOperationError).isEqualTo("create failed")
        assertThat(state.createdApiKey).isEqualTo(created)
    }

    @Test
    fun apiKeyStartClearsPreviousErrorAndVisibleCreatedKey() {
        val started = ApiKeyUiState(
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
