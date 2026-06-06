package com.elykia.octopus.feature.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elykia.octopus.core.common.AppResult
import com.elykia.octopus.core.data.model.Channel
import com.elykia.octopus.core.data.model.Group
import com.elykia.octopus.core.data.model.GroupItem
import com.elykia.octopus.core.data.model.LlmChannel
import com.elykia.octopus.core.data.repository.ChannelRepository
import com.elykia.octopus.core.data.repository.GroupRepository
import com.elykia.octopus.core.data.repository.ModelRepository
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
    val selectionMode: Boolean = false,
    val selectedIds: Set<Int> = emptySet(),
    val batchOperationProgress: String? = null,
)

internal fun GroupUiState.shouldShowPageError(): Boolean =
    error != null && groups.isEmpty()

@HiltViewModel
class GroupViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val channelRepository: ChannelRepository,
    private val modelRepository: ModelRepository,
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
            val groupsDeferred = async { groupRepository.groups() }
            val channelsDeferred = async { channelRepository.channels() }
            val modelChannelsDeferred = async { modelRepository.modelChannels() }
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
            selectedIds = _uiState.value.groups.map { it.id }.toSet()
        )
    }

    fun batchDelete(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            if (_uiState.value.submitting) return@launch
            val selectedIds = _uiState.value.selectedIds
            if (selectedIds.isEmpty()) return@launch

            _uiState.value = _uiState.value.copy(submitting = true, operationError = null)
            var successCount = 0
            var failCount = 0

            selectedIds.forEachIndexed { index, id ->
                _uiState.value = _uiState.value.copy(
                    batchOperationProgress = "删除中 ${index + 1}/${selectedIds.size}..."
                )
                when (groupRepository.deleteGroup(id)) {
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

    fun delete(id: Int) {
        viewModelScope.launch {
            if (_uiState.value.submitting) return@launch
            _uiState.value = _uiState.value.copy(submitting = true, operationError = null)
            when (val result = groupRepository.deleteGroup(id)) {
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
            when (val result = groupRepository.createGroup(
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
            when (val result = groupRepository.updateGroup(
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
        channels = channelsResult.dataOrPrevious(previous.channels),
        modelChannels = modelChannelsResult.dataOrPrevious(previous.modelChannels),
        error = groupsResult.message,
        channelListError = channelsResult.errorMessageOrNull(),
        modelChannelError = modelChannelsResult.errorMessageOrNull(),
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
