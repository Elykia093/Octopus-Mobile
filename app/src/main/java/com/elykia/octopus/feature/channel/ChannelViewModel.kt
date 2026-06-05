package com.elykia.octopus.feature.channel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elykia.octopus.core.common.AppResult
import com.elykia.octopus.core.data.model.BaseUrl
import com.elykia.octopus.core.data.model.Channel
import com.elykia.octopus.core.data.model.ChannelKey
import com.elykia.octopus.core.data.model.ChannelKeyAddRequest
import com.elykia.octopus.core.data.model.ChannelFetchModelRequest
import com.elykia.octopus.core.data.model.ChannelUpdateRequest
import com.elykia.octopus.core.data.repository.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
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
    private val repository: DashboardRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ChannelUiState())
    val uiState: StateFlow<ChannelUiState> = _uiState

    init {
        refresh()
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

    fun fetchModels(
        type: Int,
        baseUrl: String,
        apiKey: String,
        proxy: Boolean,
        onSuccess: () -> Unit = {},
    ) {
        viewModelScope.launch {
            if (_uiState.value.submitting) return@launch
            val normalizedBaseUrl = baseUrl.trim()
            if (!hasValidChannelBaseUrl(normalizedBaseUrl)) {
                _uiState.value = _uiState.value.channelOperationFailed("请输入有效的 HTTPS 渠道地址。")
                return@launch
            }
            _uiState.value = _uiState.value.channelOperationStarted()
            val result = repository.fetchChannelModels(
                ChannelFetchModelRequest(
                    type = type,
                    baseUrls = normalizedBaseUrl.takeIf { it.isNotEmpty() }?.let { listOf(BaseUrl(url = it)) } ?: emptyList(),
                    keys = apiKey.trim().takeIf { it.isNotEmpty() }?.let {
                        listOf(ChannelKeyAddRequest(channelKey = it))
                    } ?: emptyList(),
                    proxy = proxy,
                )
            )
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
                    batchOperationProgress = "删除中 ${index + 1}/${selectedIds.size}..."
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
                    "批量删除完成：成功 $successCount 个，失败 $failCount 个"
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
                    batchOperationProgress = "${if (enabled) "启用" else "禁用"}中 ${index + 1}/${selectedIds.size}..."
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
                    "批量操作完成：成功 $successCount 个，失败 $failCount 个"
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
        name: String,
        type: Int,
        enabled: Boolean,
        baseUrl: String,
        apiKey: String,
        model: String,
        customModel: String,
        proxy: Boolean,
        autoSync: Boolean,
        onSuccess: () -> Unit = {},
    ) {
        viewModelScope.launch {
            if (_uiState.value.submitting) return@launch
            val normalizedBaseUrl = baseUrl.trim()
            if (!hasValidChannelBaseUrl(normalizedBaseUrl)) {
                _uiState.value = _uiState.value.channelOperationFailed("请输入有效的 HTTPS 渠道地址。")
                return@launch
            }
            _uiState.value = _uiState.value.channelOperationStarted()
            when (val result = repository.createChannel(
                Channel(
                    name = name.trim(),
                    type = type,
                    enabled = enabled,
                    baseUrls = normalizedBaseUrl.takeIf { it.isNotEmpty() }?.let { listOf(BaseUrl(url = it)) } ?: emptyList(),
                    keys = apiKey.trim().takeIf { it.isNotEmpty() }?.let {
                        listOf(ChannelKey(channelKey = it, channelId = 0))
                    } ?: emptyList(),
                    model = model.trim(),
                    customModel = customModel.trim(),
                    proxy = proxy,
                    autoSync = autoSync,
                )
            )) {
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
        name: String,
        type: Int,
        enabled: Boolean,
        baseUrl: String,
        apiKey: String,
        model: String,
        customModel: String,
        proxy: Boolean,
        autoSync: Boolean,
        onSuccess: () -> Unit = {},
    ) {
        viewModelScope.launch {
            if (_uiState.value.submitting) return@launch
            if (!channel.canUseBasicMobileEditor()) {
                _uiState.value = _uiState.value.channelOperationFailed("当前移动端仅支持基础渠道编辑，请在 Web 端维护多 Key、多地址或高级配置。")
                return@launch
            }
            val normalizedBaseUrl = baseUrl.trim()
            if (!hasValidChannelBaseUrl(normalizedBaseUrl)) {
                _uiState.value = _uiState.value.channelOperationFailed("请输入有效的 HTTPS 渠道地址。")
                return@launch
            }
            _uiState.value = _uiState.value.channelOperationStarted()
            val existingKey = channel.keys.firstOrNull()
            val trimmedKey = apiKey.trim()
            val keysToAdd = if (existingKey == null && trimmedKey.isNotEmpty()) {
                listOf(ChannelKeyAddRequest(channelKey = trimmedKey, enabled = true))
            } else {
                emptyList()
            }
            val keysToUpdate = if (existingKey != null && trimmedKey.isNotEmpty()) {
                listOf(
                    com.elykia.octopus.core.data.model.ChannelKeyUpdateRequest(
                        id = existingKey.id,
                        enabled = true,
                        channelKey = trimmedKey,
                        remark = existingKey.remark,
                    )
                )
            } else {
                emptyList()
            }

            when (val result = repository.updateChannel(
                ChannelUpdateRequest(
                    id = channel.id,
                    name = name.trim(),
                    type = type,
                    enabled = enabled,
                    baseUrls = normalizedBaseUrl.takeIf { it.isNotEmpty() }?.let { listOf(BaseUrl(url = it)) } ?: emptyList(),
                    model = model.trim(),
                    customModel = customModel.trim(),
                    proxy = proxy,
                    autoSync = autoSync,
                    keysToAdd = keysToAdd,
                    keysToUpdate = keysToUpdate,
                    keysToDelete = emptyList(),
                )
            )) {
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
    proxy = proxy,
    channelProxy = channelProxy,
    matchRegex = matchRegex,
    customHeader = customHeader,
)

internal fun hasValidChannelBaseUrl(baseUrl: String): Boolean =
    baseUrl.isBlank() || baseUrl.trim().toHttpUrlOrNull()?.let { url ->
        url.scheme == "https" &&
            url.encodedUsername.isBlank() &&
            url.encodedPassword.isBlank() &&
            url.encodedQuery == null &&
            url.encodedFragment == null
    } == true

internal fun canSubmitChannelEditor(
    name: String,
    baseUrl: String,
    submitting: Boolean,
    basicEditSupported: Boolean,
): Boolean =
    !submitting &&
        basicEditSupported &&
        name.isNotBlank() &&
        hasValidChannelBaseUrl(baseUrl)

internal fun Channel.canUseBasicMobileEditor(): Boolean =
    baseUrls.size <= 1 &&
        keys.size <= 1 &&
        customHeader.isEmpty() &&
        channelProxy.isNullOrBlank() &&
        matchRegex.isNullOrBlank() &&
        paramOverride.isNullOrBlank()
