package com.elykia.octopus.feature.apikey

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elykia.octopus.core.common.AppResult
import com.elykia.octopus.core.data.model.ApiKeyItem
import com.elykia.octopus.core.data.model.ApiKeyMutationRequest
import com.elykia.octopus.core.data.model.Group
import com.elykia.octopus.core.data.repository.ApiKeyRepository
import com.elykia.octopus.core.data.repository.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ApiKeyUiState(
    val loading: Boolean = true,
    val apiKeys: List<ApiKeyItem> = emptyList(),
    val supportedModelCandidates: List<String> = emptyList(),
    val createdApiKey: ApiKeyItem? = null,
    val apiKeyListError: String? = null,
    val apiKeySubmitting: Boolean = false,
    val apiKeyOperationError: String? = null,
    val apiKeySelectionMode: Boolean = false,
    val selectedApiKeyIds: Set<Int> = emptySet(),
    val batchApiKeyOperationProgress: String? = null,
)

internal fun ApiKeyUiState.shouldShowApiKeyPageError(): Boolean =
    apiKeyListError != null && apiKeys.isEmpty()

internal fun ApiKeyUiState.apiKeyOperationStarted(): ApiKeyUiState = copy(
    apiKeySubmitting = true,
    apiKeyOperationError = null,
    createdApiKey = null,
)

internal fun ApiKeyUiState.apiKeyOperationSucceeded(): ApiKeyUiState = copy(
    apiKeySubmitting = false,
    apiKeyOperationError = null,
)

internal fun ApiKeyUiState.apiKeyOperationFailed(message: String): ApiKeyUiState = copy(
    apiKeySubmitting = false,
    apiKeyOperationError = message,
)

@HiltViewModel
class ApiKeyViewModel @Inject constructor(
    private val repository: ApiKeyRepository,
    private val groupRepository: GroupRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ApiKeyUiState())
    val uiState: StateFlow<ApiKeyUiState> = _uiState

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val previous = _uiState.value
            _uiState.value = previous.copy(loading = true, apiKeyListError = null)
            _uiState.value = buildApiKeyRefreshState(
                previous = previous,
                apiKeysResult = repository.apiKeys(),
                groupsResult = groupRepository.groups(),
            )
        }
    }

    fun setApiKeyEnabled(item: ApiKeyItem, enabled: Boolean) {
        viewModelScope.launch {
            if (_uiState.value.apiKeySubmitting) return@launch
            _uiState.value = _uiState.value.apiKeyOperationStarted()
            when (val result = repository.updateApiKey(item.copy(enabled = enabled))) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.apiKeyOperationSucceeded()
                    refresh()
                }
                is AppResult.Error -> _uiState.value = _uiState.value.apiKeyOperationFailed(result.message)
            }
        }
    }

    fun createApiKey(
        name: String,
        expireAt: Long,
        maxCost: Double,
        supportedModels: String,
        enabled: Boolean,
        onSuccess: () -> Unit = {},
    ) {
        viewModelScope.launch {
            if (_uiState.value.apiKeySubmitting) return@launch
            _uiState.value = _uiState.value.apiKeyOperationStarted()
            when (
                val result = repository.createApiKey(
                    ApiKeyMutationRequest(
                        name = name,
                        enabled = enabled,
                        expireAt = expireAt,
                        maxCost = maxCost,
                        supportedModels = supportedModels,
                    )
                )
            ) {
                is AppResult.Success -> {
                    val visibleCreatedKey = result.data
                    val hiddenListItem = visibleCreatedKey.copy(apiKey = "")
                    _uiState.value = _uiState.value.copy(
                        apiKeys = _uiState.value.apiKeys
                            .filterNot { it.id == visibleCreatedKey.id } + hiddenListItem,
                        apiKeySubmitting = false,
                        apiKeyOperationError = null,
                        createdApiKey = visibleCreatedKey,
                    )
                    onSuccess()
                }
                is AppResult.Error -> _uiState.value = _uiState.value.apiKeyOperationFailed(result.message)
            }
        }
    }

    fun clearApiKeyOperationError() {
        _uiState.value = _uiState.value.copy(apiKeyOperationError = null)
    }

    fun dismissCreatedApiKey() {
        _uiState.value = _uiState.value.copy(createdApiKey = null)
    }

    fun updateApiKey(
        item: ApiKeyItem,
        onSuccess: () -> Unit = {},
    ) {
        viewModelScope.launch {
            if (_uiState.value.apiKeySubmitting) return@launch
            _uiState.value = _uiState.value.apiKeyOperationStarted()
            when (val result = repository.updateApiKey(item)) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.apiKeyOperationSucceeded()
                    onSuccess()
                    refresh()
                }
                is AppResult.Error -> _uiState.value = _uiState.value.apiKeyOperationFailed(result.message)
            }
        }
    }

    fun deleteApiKey(id: Int) {
        viewModelScope.launch {
            if (_uiState.value.apiKeySubmitting) return@launch
            _uiState.value = _uiState.value.apiKeyOperationStarted()
            when (val result = repository.deleteApiKey(id)) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.apiKeyOperationSucceeded()
                    refresh()
                }
                is AppResult.Error -> _uiState.value = _uiState.value.apiKeyOperationFailed(result.message)
            }
        }
    }

    fun enterApiKeySelectionMode() {
        _uiState.value = _uiState.value.copy(apiKeySelectionMode = true, selectedApiKeyIds = emptySet())
    }

    fun exitApiKeySelectionMode() {
        _uiState.value = _uiState.value.copy(apiKeySelectionMode = false, selectedApiKeyIds = emptySet())
    }

    fun toggleApiKeySelection(id: Int) {
        val currentSelected = _uiState.value.selectedApiKeyIds
        _uiState.value = _uiState.value.copy(
            selectedApiKeyIds = if (id in currentSelected) {
                currentSelected - id
            } else {
                currentSelected + id
            }
        )
    }

    fun selectAllApiKeys() {
        _uiState.value = _uiState.value.copy(
            selectedApiKeyIds = _uiState.value.apiKeys.map { it.id }.toSet()
        )
    }

    fun batchDeleteApiKeys(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            if (_uiState.value.apiKeySubmitting) return@launch
            val selectedIds = _uiState.value.selectedApiKeyIds
            if (selectedIds.isEmpty()) return@launch

            _uiState.value = _uiState.value.apiKeyOperationStarted()
            var successCount = 0
            var failCount = 0

            selectedIds.forEachIndexed { index, id ->
                _uiState.value = _uiState.value.copy(
                    batchApiKeyOperationProgress = "删除中 ${index + 1}/${selectedIds.size}..."
                )
                when (repository.deleteApiKey(id)) {
                    is AppResult.Success -> successCount++
                    is AppResult.Error -> failCount++
                }
            }

            _uiState.value = _uiState.value.copy(
                apiKeySubmitting = false,
                batchApiKeyOperationProgress = null,
                apiKeySelectionMode = false,
                selectedApiKeyIds = emptySet(),
                apiKeyOperationError = if (failCount > 0) {
                    "批量删除完成：成功 $successCount 个，失败 $failCount 个"
                } else null
            )
            refresh()
            onComplete()
        }
    }

    fun batchSetApiKeysEnabled(enabled: Boolean, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            if (_uiState.value.apiKeySubmitting) return@launch
            val selectedIds = _uiState.value.selectedApiKeyIds
            if (selectedIds.isEmpty()) return@launch

            _uiState.value = _uiState.value.apiKeyOperationStarted()
            var successCount = 0
            var failCount = 0

            val apiKeysMap = _uiState.value.apiKeys.associateBy { it.id }
            selectedIds.forEachIndexed { index, id ->
                _uiState.value = _uiState.value.copy(
                    batchApiKeyOperationProgress = "${if (enabled) "启用" else "禁用"}中 ${index + 1}/${selectedIds.size}..."
                )
                val apiKey = apiKeysMap[id]
                if (apiKey != null) {
                    when (repository.updateApiKey(apiKey.copy(enabled = enabled))) {
                        is AppResult.Success -> successCount++
                        is AppResult.Error -> failCount++
                    }
                }
            }

            _uiState.value = _uiState.value.copy(
                apiKeySubmitting = false,
                batchApiKeyOperationProgress = null,
                apiKeySelectionMode = false,
                selectedApiKeyIds = emptySet(),
                apiKeyOperationError = if (failCount > 0) {
                    "批量操作完成：成功 $successCount 个，失败 $failCount 个"
                } else null
            )
            refresh()
            onComplete()
        }
    }
}

internal fun buildApiKeyRefreshState(
    previous: ApiKeyUiState,
    apiKeysResult: AppResult<List<ApiKeyItem>>,
    groupsResult: AppResult<List<Group>> = AppResult.Success(emptyList()),
): ApiKeyUiState = previous.copy(
    loading = false,
    apiKeys = apiKeysResult.dataOrPrevious(previous.apiKeys),
    supportedModelCandidates = when (groupsResult) {
        is AppResult.Success -> apiKeyModelRestrictionCandidates(groupsResult.data)
        is AppResult.Error -> previous.supportedModelCandidates
    },
    apiKeyListError = apiKeysResult.errorMessageOrNull(),
)

private fun <T> AppResult<T>.dataOrPrevious(previous: T): T = when (this) {
    is AppResult.Success -> data
    is AppResult.Error -> previous
}

private fun AppResult<*>.errorMessageOrNull(): String? = when (this) {
    is AppResult.Success -> null
    is AppResult.Error -> message
}
