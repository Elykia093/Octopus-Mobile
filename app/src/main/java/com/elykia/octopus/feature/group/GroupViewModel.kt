package com.elykia.octopus.feature.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elykia.octopus.core.common.AppResult
import com.elykia.octopus.core.data.model.Channel
import com.elykia.octopus.core.data.model.Group
import com.elykia.octopus.core.data.model.GroupItem
import com.elykia.octopus.core.data.model.LlmChannel
import com.elykia.octopus.core.data.repository.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GroupUiState(
    val loading: Boolean = true,
    val groups: List<Group> = emptyList(),
    val channels: List<Channel> = emptyList(),
    val modelChannels: List<LlmChannel> = emptyList(),
    val error: String? = null,
    val channelListError: String? = null,
    val modelChannelError: String? = null,
    val submitting: Boolean = false,
    val operationError: String? = null,
)

@HiltViewModel
class GroupViewModel @Inject constructor(
    private val repository: DashboardRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(GroupUiState())
    val uiState: StateFlow<GroupUiState> = _uiState

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val previous = _uiState.value
            _uiState.value = previous.copy(loading = true, error = null)
            val groupsDeferred = async { repository.groups() }
            val channelsDeferred = async { repository.channels() }
            val modelChannelsDeferred = async { repository.modelChannels() }
            _uiState.value = buildGroupRefreshState(
                previous = previous,
                groupsResult = groupsDeferred.await(),
                channelsResult = channelsDeferred.await(),
                modelChannelsResult = modelChannelsDeferred.await(),
            )
        }
    }

    fun clearOperationError() {
        _uiState.value = _uiState.value.copy(operationError = null)
    }

    fun delete(id: Int) {
        viewModelScope.launch {
            if (_uiState.value.submitting) return@launch
            _uiState.value = _uiState.value.copy(submitting = true, operationError = null)
            when (val result = repository.deleteGroup(id)) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(submitting = false)
                    refresh()
                }
                is AppResult.Error -> _uiState.value = _uiState.value.copy(
                    submitting = false,
                    operationError = result.message,
                )
            }
        }
    }

    fun createGroup(
        name: String,
        mode: Int,
        matchRegex: String,
        firstTokenTimeOut: Int,
        sessionKeepTime: Int,
        retryEnabled: Boolean,
        maxRetries: Int,
        items: List<GroupItem>,
        onSuccess: () -> Unit = {},
    ) {
        viewModelScope.launch {
            if (_uiState.value.submitting) return@launch
            _uiState.value = _uiState.value.copy(submitting = true, operationError = null)
            when (val result = repository.createGroup(
                Group(
                    name = name.trim(),
                    mode = mode,
                    matchRegex = matchRegex.trim(),
                    firstTokenTimeOut = firstTokenTimeOut,
                    sessionKeepTime = sessionKeepTime,
                    retryEnabled = retryEnabled,
                    maxRetries = maxRetries.takeIf { it > 0 } ?: 3,
                    items = items,
                )
            )) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(submitting = false)
                    onSuccess()
                    refresh()
                }
                is AppResult.Error -> _uiState.value = _uiState.value.copy(
                    submitting = false,
                    operationError = result.message,
                )
            }
        }
    }

    fun updateGroup(
        group: Group,
        name: String,
        mode: Int,
        matchRegex: String,
        firstTokenTimeOut: Int,
        sessionKeepTime: Int,
        retryEnabled: Boolean,
        maxRetries: Int,
        items: List<GroupItem>,
        onSuccess: () -> Unit = {},
    ) {
        viewModelScope.launch {
            if (_uiState.value.submitting) return@launch
            _uiState.value = _uiState.value.copy(submitting = true, operationError = null)
            when (val result = repository.updateGroup(
                buildGroupUpdateRequest(
                    group = group,
                    name = name,
                    mode = mode,
                    matchRegex = matchRegex,
                    firstTokenTimeOut = firstTokenTimeOut,
                    sessionKeepTime = sessionKeepTime,
                    retryEnabled = retryEnabled,
                    maxRetries = maxRetries,
                    items = items,
                )
            )) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(submitting = false)
                    onSuccess()
                    refresh()
                }
                is AppResult.Error -> _uiState.value = _uiState.value.copy(
                    submitting = false,
                    operationError = result.message,
                )
            }
        }
    }
}

internal fun buildGroupRefreshState(
    previous: GroupUiState,
    groupsResult: AppResult<List<Group>>,
    channelsResult: AppResult<List<Channel>>,
    modelChannelsResult: AppResult<List<LlmChannel>>,
): GroupUiState = when (groupsResult) {
    is AppResult.Success -> previous.copy(
        loading = false,
        groups = groupsResult.data,
        channels = channelsResult.dataOrPrevious(previous.channels),
        modelChannels = modelChannelsResult.dataOrPrevious(previous.modelChannels),
        error = null,
        channelListError = channelsResult.errorMessageOrNull(),
        modelChannelError = modelChannelsResult.errorMessageOrNull(),
    )
    is AppResult.Error -> previous.copy(
        loading = false,
        error = groupsResult.message,
    )
}

private fun <T> AppResult<T>.dataOrPrevious(previous: T): T = when (this) {
    is AppResult.Success -> data
    is AppResult.Error -> previous
}

private fun AppResult<*>.errorMessageOrNull(): String? = when (this) {
    is AppResult.Success -> null
    is AppResult.Error -> message
}
