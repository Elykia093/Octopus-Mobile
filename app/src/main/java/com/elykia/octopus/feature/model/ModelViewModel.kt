package com.elykia.octopus.feature.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elykia.octopus.core.common.AppResult
import com.elykia.octopus.core.data.model.LlmChannel
import com.elykia.octopus.core.data.model.LlmInfo
import com.elykia.octopus.core.data.repository.ModelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ModelUiState(
    val loading: Boolean = true,
    val models: List<LlmInfo> = emptyList(),
    val modelChannels: List<LlmChannel> = emptyList(),
    val modelLastUpdateTime: String? = null,
    val modelListError: String? = null,
    val modelChannelError: String? = null,
    val modelLastUpdateError: String? = null,
    val submitting: Boolean = false,
    val operationError: String? = null,
)

internal enum class ModelFilter { All, Priced, Free }

internal enum class ModelSort { NameAsc, NameDesc }

internal fun ModelUiState.shouldShowModelPageError(): Boolean =
    modelListError != null && models.isEmpty()

internal fun ModelUiState.modelOperationStarted(): ModelUiState = copy(
    submitting = true,
    operationError = null,
)

internal fun ModelUiState.modelOperationSucceeded(): ModelUiState = copy(
    submitting = false,
    operationError = null,
)

internal fun ModelUiState.modelOperationFailed(message: String): ModelUiState = copy(
    submitting = false,
    operationError = message,
)

@HiltViewModel
class ModelViewModel @Inject constructor(
    private val repository: ModelRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ModelUiState())
    val uiState: StateFlow<ModelUiState> = _uiState

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val previous = _uiState.value
            _uiState.value = previous.copy(loading = true, modelListError = null)
            val modelsDeferred = async { repository.models() }
            val channelsDeferred = async { repository.modelChannels() }
            val lastUpdateDeferred = async { repository.modelLastUpdateTime() }
            _uiState.value = buildModelRefreshState(
                previous = previous,
                modelsResult = modelsDeferred.await(),
                modelChannelsResult = channelsDeferred.await(),
                lastUpdateResult = lastUpdateDeferred.await(),
            )
        }
    }

    fun clearOperationError() {
        _uiState.value = _uiState.value.copy(operationError = null)
    }

    fun refreshModelPrice() {
        viewModelScope.launch {
            if (_uiState.value.submitting) return@launch
            _uiState.value = _uiState.value.modelOperationStarted()
            when (val result = repository.refreshModelPrice()) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.modelOperationSucceeded()
                    refresh()
                }
                is AppResult.Error -> _uiState.value = _uiState.value.modelOperationFailed(result.message)
            }
        }
    }

    fun createModel(
        model: LlmInfo,
        onSuccess: () -> Unit = {},
    ) {
        viewModelScope.launch {
            if (_uiState.value.submitting) return@launch
            _uiState.value = _uiState.value.modelOperationStarted()
            when (val result = repository.createModel(model.normalized())) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.modelOperationSucceeded()
                    onSuccess()
                    refresh()
                }
                is AppResult.Error -> _uiState.value = _uiState.value.modelOperationFailed(result.message)
            }
        }
    }

    fun updateModel(
        model: LlmInfo,
        onSuccess: () -> Unit = {},
    ) {
        viewModelScope.launch {
            if (_uiState.value.submitting) return@launch
            _uiState.value = _uiState.value.modelOperationStarted()
            when (val result = repository.updateModel(model.normalized())) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.modelOperationSucceeded()
                    onSuccess()
                    refresh()
                }
                is AppResult.Error -> _uiState.value = _uiState.value.modelOperationFailed(result.message)
            }
        }
    }

    fun deleteModel(name: String) {
        viewModelScope.launch {
            if (_uiState.value.submitting) return@launch
            _uiState.value = _uiState.value.modelOperationStarted()
            when (val result = repository.deleteModel(name)) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.modelOperationSucceeded()
                    refresh()
                }
                is AppResult.Error -> _uiState.value = _uiState.value.modelOperationFailed(result.message)
            }
        }
    }
}

internal fun buildModelRefreshState(
    previous: ModelUiState,
    modelsResult: AppResult<List<LlmInfo>>,
    modelChannelsResult: AppResult<List<LlmChannel>>,
    lastUpdateResult: AppResult<String>,
): ModelUiState = previous.copy(
    loading = false,
    models = modelsResult.dataOrPrevious(previous.models),
    modelChannels = modelChannelsResult.dataOrPrevious(previous.modelChannels),
    modelLastUpdateTime = lastUpdateResult.dataOrPreviousNullable(previous.modelLastUpdateTime),
    modelListError = modelsResult.errorMessageOrNull(),
    modelChannelError = modelChannelsResult.errorMessageOrNull(),
    modelLastUpdateError = lastUpdateResult.errorMessageOrNull(),
)

internal fun filterAndSortModels(
    models: List<LlmInfo>,
    query: String,
    filter: ModelFilter,
    sort: ModelSort,
): List<LlmInfo> {
    val normalizedQuery = query.trim()
    return models
        .asSequence()
        .filter { model ->
            normalizedQuery.isBlank() || model.name.contains(normalizedQuery, ignoreCase = true)
        }
        .filter { model ->
            when (filter) {
                ModelFilter.All -> true
                ModelFilter.Priced -> model.hasPricing()
                ModelFilter.Free -> !model.hasPricing()
            }
        }
        .sortedWith(
            when (sort) {
                ModelSort.NameAsc -> compareBy(String.CASE_INSENSITIVE_ORDER) { it.name }
                ModelSort.NameDesc -> compareByDescending(String.CASE_INSENSITIVE_ORDER) { it.name }
            },
        )
        .toList()
}

internal fun LlmInfo.hasPricing(): Boolean =
    input + output + cacheRead + cacheWrite > 0.0

private fun LlmInfo.normalized(): LlmInfo = copy(name = name.trim())

private fun <T> AppResult<T>.dataOrPrevious(previous: T): T = when (this) {
    is AppResult.Success -> data
    is AppResult.Error -> previous
}

private fun <T> AppResult<T>.dataOrPreviousNullable(previous: T?): T? = when (this) {
    is AppResult.Success -> data
    is AppResult.Error -> previous
}

private fun AppResult<*>.errorMessageOrNull(): String? = when (this) {
    is AppResult.Success -> null
    is AppResult.Error -> message
}
