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
import javax.inject.Inject

data class ChannelUiState(
    val loading: Boolean = true,
    val channels: List<Channel> = emptyList(),
    val fetchedModels: List<String> = emptyList(),
    val error: String? = null,
    val submitting: Boolean = false,
    val operationError: String? = null,
)

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
            _uiState.value = _uiState.value.channelOperationStarted()
            val result = repository.fetchChannelModels(
                ChannelFetchModelRequest(
                    type = type,
                    baseUrls = baseUrl.trim().takeIf { it.isNotEmpty() }?.let { listOf(BaseUrl(url = it)) } ?: emptyList(),
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
            _uiState.value = _uiState.value.channelOperationStarted()
            when (val result = repository.createChannel(
                Channel(
                    name = name.trim(),
                    type = type,
                    enabled = enabled,
                    baseUrls = baseUrl.trim().takeIf { it.isNotEmpty() }?.let { listOf(BaseUrl(url = it)) } ?: emptyList(),
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
            val keysToDelete = if (existingKey != null && trimmedKey.isEmpty()) {
                listOf(existingKey.id)
            } else {
                emptyList()
            }

            when (val result = repository.updateChannel(
                ChannelUpdateRequest(
                    id = channel.id,
                    name = name.trim(),
                    type = type,
                    enabled = enabled,
                    baseUrls = baseUrl.trim().takeIf { it.isNotEmpty() }?.let { listOf(BaseUrl(url = it)) } ?: emptyList(),
                    model = model.trim(),
                    customModel = customModel.trim(),
                    proxy = proxy,
                    autoSync = autoSync,
                    keysToAdd = keysToAdd,
                    keysToUpdate = keysToUpdate,
                    keysToDelete = keysToDelete,
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
