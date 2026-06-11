package com.elykia.octopus.feature.channel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elykia.octopus.core.common.AppResult
import com.elykia.octopus.core.data.model.Channel
import com.elykia.octopus.core.data.model.ChannelKey
import com.elykia.octopus.core.data.model.ChannelKeyAddRequest
import com.elykia.octopus.core.data.model.ChannelFetchModelRequest
import com.elykia.octopus.core.data.model.ProxyConfiguration
import com.elykia.octopus.core.data.model.ProxyMode
import com.elykia.octopus.core.data.repository.ChannelRepository
import com.elykia.octopus.core.data.repository.ProxyPoolRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChannelUiState(
    val loading: Boolean = true,
    val channels: List<Channel> = emptyList(),
    val fetchedModels: List<String> = emptyList(),
    val error: String? = null,
    val submitting: Boolean = false,
    val operationError: String? = null,
    val selectionMode: Boolean = false,
    val selectedIds: Set<Int> = emptySet(),
    val batchOperationProgress: String? = null,
    val proxyConfigurations: List<ProxyConfiguration> = emptyList(),
    val proxyConfigurationError: String? = null,
)

internal fun ChannelUiState.shouldShowPageError(): Boolean =
    error != null && channels.isEmpty()

internal fun ChannelUiState.channelOperationStarted(): ChannelUiState = copy(
    submitting = true,
    operationError = null,
)

internal fun ChannelUiState.channelOperationSucceeded(): ChannelUiState = copy(
    submitting = false,
    operationError = null,
)

internal fun ChannelUiState.channelOperationFailed(message: String): ChannelUiState = copy(
    submitting = false,
    operationError = message,
)

@HiltViewModel
class ChannelViewModel @Inject constructor(
    private val repository: ChannelRepository,
    private val proxyPoolRepository: ProxyPoolRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ChannelUiState())
    val uiState: StateFlow<ChannelUiState> = _uiState

    init {
        refresh()
        refreshProxyConfigurations()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            when (val result = repository.channels()) {
                is AppResult.Success -> _uiState.value = _uiState.value.copy(loading = false, channels = result.data, error = null)
                is AppResult.Error -> _uiState.value = _uiState.value.copy(loading = false, error = result.message)
            }
        }
    }

    fun syncModels() {
        viewModelScope.launch {
            if (_uiState.value.submitting) return@launch
            _uiState.value = _uiState.value.channelOperationStarted()
            when (val result = repository.syncChannelModels()) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.channelOperationSucceeded()
                    refresh()
                }
                is AppResult.Error -> _uiState.value = _uiState.value.channelOperationFailed(result.message)
            }
        }
    }

    fun refreshProxyConfigurations() {
        viewModelScope.launch {
            when (val result = proxyPoolRepository.proxyConfigurations()) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        proxyConfigurations = result.data.sortedWith(
                            compareByDescending<ProxyConfiguration> { it.enabled }
                                .thenBy { it.name.lowercase() }
                                .thenBy { it.id },
                        ),
                        proxyConfigurationError = null,
                    )
                }
                is AppResult.Error -> {
                    _uiState.value = _uiState.value.copy(proxyConfigurationError = result.message)
                }
            }
        }
    }

    fun fetchModels(
        channel: Channel,
        onSuccess: () -> Unit = {},
    ) {
        viewModelScope.launch {
            if (_uiState.value.submitting) return@launch
            _uiState.value = _uiState.value.channelOperationStarted()
            val result = repository.fetchChannelModels(channel.toFetchRequest())
            when (result) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        submitting = false,
                        fetchedModels = result.data,
                        operationError = null,
                    )
                    onSuccess()
                }
                is AppResult.Error -> _uiState.value = _uiState.value.channelOperationFailed(result.message)
            }
        }
    }

    fun fetchModels(values: ChannelEditorValues, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            if (_uiState.value.submitting) return@launch
            if (!hasValidChannelBaseUrls(values.baseUrls)) {
                _uiState.value = _uiState.value.channelOperationFailed("请输入有效的 HTTPS 渠道地址。")
                return@launch
            }
            if (!values.keys.any { it.channelKey.isNotBlank() }) {
                _uiState.value = _uiState.value.channelOperationFailed("请输入可用于抓取模型的渠道密钥。")
                return@launch
            }
            _uiState.value = _uiState.value.channelOperationStarted()
            val result = repository.fetchChannelModels(values.toFetchRequest())
            when (result) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        submitting = false,
                        fetchedModels = result.data,
                        operationError = null,
                    )
                    onSuccess()
                }
                is AppResult.Error -> _uiState.value = _uiState.value.channelOperationFailed(result.message)
            }
        }
    }

    fun clearOperationError() {
        _uiState.value = _uiState.value.copy(operationError = null)
    }

    fun clearFetchedModels() {
        _uiState.value = _uiState.value.copy(fetchedModels = emptyList())
    }

    fun enterSelectionMode() {
        _uiState.value = _uiState.value.copy(selectionMode = true, selectedIds = emptySet())
    }

    fun exitSelectionMode() {
        _uiState.value = _uiState.value.copy(selectionMode = false, selectedIds = emptySet())
    }

    fun toggleSelection(id: Int) {
        val currentSelected = _uiState.value.selectedIds
        _uiState.value = _uiState.value.copy(
            selectedIds = if (id in currentSelected) {
                currentSelected - id
            } else {
                currentSelected + id
            }
        )
    }

    fun selectAll() {
        _uiState.value = _uiState.value.copy(
            selectedIds = _uiState.value.channels.map { it.id }.toSet()
        )
    }

    fun batchDelete(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            if (_uiState.value.submitting) return@launch
            val selectedIds = _uiState.value.selectedIds
            if (selectedIds.isEmpty()) return@launch

            _uiState.value = _uiState.value.channelOperationStarted()
            var successCount = 0
            var failCount = 0

            selectedIds.forEachIndexed { index, id ->
                _uiState.value = _uiState.value.copy(
                    batchOperationProgress = channelBatchProgressMessage(
                        action = ChannelBatchProgressAction.Delete,
                        current = index + 1,
                        total = selectedIds.size,
                    )
                )
                when (repository.deleteChannel(id)) {
                    is AppResult.Success -> successCount++
                    is AppResult.Error -> failCount++
                }
            }

            _uiState.value = _uiState.value.copy(
                submitting = false,
                batchOperationProgress = null,
                selectionMode = false,
                selectedIds = emptySet(),
                operationError = if (failCount > 0) {
                    channelBatchResultMessage(successCount = successCount, failCount = failCount)
                } else null
            )
            refresh()
            onComplete()
        }
    }

    fun batchSetEnabled(enabled: Boolean, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            if (_uiState.value.submitting) return@launch
            val selectedIds = _uiState.value.selectedIds
            if (selectedIds.isEmpty()) return@launch

            _uiState.value = _uiState.value.channelOperationStarted()
            var successCount = 0
            var failCount = 0

            selectedIds.forEachIndexed { index, id ->
                _uiState.value = _uiState.value.copy(
                    batchOperationProgress = channelBatchProgressMessage(
                        action = if (enabled) {
                            ChannelBatchProgressAction.Enable
                        } else {
                            ChannelBatchProgressAction.Disable
                        },
                        current = index + 1,
                        total = selectedIds.size,
                    )
                )
                when (repository.setChannelEnabled(id, enabled)) {
                    is AppResult.Success -> successCount++
                    is AppResult.Error -> failCount++
                }
            }

            _uiState.value = _uiState.value.copy(
                submitting = false,
                batchOperationProgress = null,
                selectionMode = false,
                selectedIds = emptySet(),
                operationError = if (failCount > 0) {
                    channelBatchResultMessage(successCount = successCount, failCount = failCount)
                } else null
            )
            refresh()
            onComplete()
        }
    }

    fun delete(id: Int) {
        viewModelScope.launch {
            if (_uiState.value.submitting) return@launch
            _uiState.value = _uiState.value.channelOperationStarted()
            when (val result = repository.deleteChannel(id)) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.channelOperationSucceeded()
                    refresh()
                }
                is AppResult.Error -> _uiState.value = _uiState.value.channelOperationFailed(result.message)
            }
        }
    }

    fun setEnabled(id: Int, enabled: Boolean) {
        viewModelScope.launch {
            if (_uiState.value.submitting) return@launch
            _uiState.value = _uiState.value.channelOperationStarted()
            when (val result = repository.setChannelEnabled(id, enabled)) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.channelOperationSucceeded()
                    refresh()
                }
                is AppResult.Error -> _uiState.value = _uiState.value.channelOperationFailed(result.message)
            }
        }
    }

    fun createChannel(
        values: ChannelEditorValues,
        onSuccess: () -> Unit = {},
    ) {
        viewModelScope.launch {
            if (_uiState.value.submitting) return@launch
            if (!canSubmitChannelEditor(values, submitting = false)) {
                _uiState.value = _uiState.value.channelOperationFailed("请输入有效的渠道名称、HTTPS 地址和渠道密钥。")
                return@launch
            }
            _uiState.value = _uiState.value.channelOperationStarted()
            when (val result = repository.createChannel(values.toCreateChannel())) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.channelOperationSucceeded()
                    onSuccess()
                    refresh()
                }
                is AppResult.Error -> _uiState.value = _uiState.value.channelOperationFailed(result.message)
            }
        }
    }

    fun updateChannel(
        channel: Channel,
        values: ChannelEditorValues,
        onSuccess: () -> Unit = {},
    ) {
        viewModelScope.launch {
            if (_uiState.value.submitting) return@launch
            if (!canSubmitChannelEditor(values, submitting = false)) {
                _uiState.value = _uiState.value.channelOperationFailed("请输入有效的渠道名称、HTTPS 地址和至少一个渠道密钥。")
                return@launch
            }
            _uiState.value = _uiState.value.channelOperationStarted()
            when (val result = repository.updateChannel(buildChannelUpdateRequest(channel, values))) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.channelOperationSucceeded()
                    onSuccess()
                    refresh()
                }
                is AppResult.Error -> _uiState.value = _uiState.value.channelOperationFailed(result.message)
            }
        }
    }
}

private fun ChannelEditorValues.toCreateChannel(): Channel = Channel(
    name = name.trim(),
    type = type,
    enabled = enabled,
    baseUrls = normalizedBaseUrls(),
    keys = keys
        .filter { it.channelKey.trim().isNotBlank() }
        .map {
            ChannelKey(
                channelKey = it.channelKey.trim(),
                channelId = 0,
                enabled = it.enabled,
                remark = it.remark.trim(),
            )
        },
    model = model.trim(),
    customModel = customModel.trim(),
    proxy = proxyMode != ProxyMode.Direct,
    proxyMode = proxyMode,
    proxyConfigId = proxyConfigId.takeIf { proxyMode == ProxyMode.Pool },
    autoSync = autoSync,
    autoGroup = autoGroup,
    customHeader = normalizedHeaders(),
    channelProxy = trimmedChannelProxy(),
    paramOverride = trimmedParamOverride(),
    matchRegex = trimmedMatchRegex(),
)

private fun ChannelEditorValues.toFetchRequest(): ChannelFetchModelRequest = ChannelFetchModelRequest(
    type = type,
    baseUrls = normalizedBaseUrls(),
    keys = keys
        .filter { it.channelKey.trim().isNotBlank() }
        .map {
            ChannelKeyAddRequest(
                enabled = it.enabled,
                channelKey = it.channelKey.trim(),
                remark = it.remark.trim(),
            )
        },
    proxy = proxyMode != ProxyMode.Direct,
    proxyMode = proxyMode,
    proxyConfigId = proxyConfigId.takeIf { proxyMode == ProxyMode.Pool },
    channelProxy = trimmedChannelProxy().takeIf { it.isNotBlank() },
    matchRegex = trimmedMatchRegex().takeIf { it.isNotBlank() },
    customHeader = normalizedHeaders(),
)

private fun Channel.toFetchRequest(): ChannelFetchModelRequest = ChannelFetchModelRequest(
    type = type,
    baseUrls = baseUrls,
    keys = keys.filter { it.channelKey.isNotBlank() }.map {
        ChannelKeyAddRequest(
            enabled = it.enabled,
            channelKey = it.channelKey,
            remark = it.remark,
        )
    },
    proxy = proxyMode != ProxyMode.Direct,
    proxyMode = proxyMode,
    proxyConfigId = proxyConfigId.takeIf { proxyMode == ProxyMode.Pool },
    channelProxy = channelProxy,
    matchRegex = matchRegex,
    customHeader = customHeader,
)
