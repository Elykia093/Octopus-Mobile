package com.elykia.octopus.feature.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elykia.octopus.core.common.AppResult
import com.elykia.octopus.core.data.model.Channel
import com.elykia.octopus.core.data.model.Group
import com.elykia.octopus.core.data.model.GroupAutoGroupConfig
import com.elykia.octopus.core.data.model.GroupAutoGroupConfigUpdateRequest
import com.elykia.octopus.core.data.model.GroupAutoGroupSourceUpdateRequest
import com.elykia.octopus.core.data.model.GroupHealthGroupView
import com.elykia.octopus.core.data.model.GroupHealthProbeMode
import com.elykia.octopus.core.data.model.GroupItem
import com.elykia.octopus.core.data.model.GroupPreset
import com.elykia.octopus.core.data.model.GroupPresetItem
import com.elykia.octopus.core.data.model.GroupPresetUpdateRequest
import com.elykia.octopus.core.data.model.LlmChannel
import com.elykia.octopus.core.data.model.SettingItem
import com.elykia.octopus.core.data.repository.AppRepository
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
    val groupHealthEnabled: Boolean = false,
    val groupHealth: List<GroupHealthGroupView> = emptyList(),
    val groupHealthLoading: Boolean = false,
    val groupHealthError: String? = null,
    val groupHealthSubmitting: Boolean = false,
    val groupHealthMessage: String? = null,
    val presetsByGroupId: Map<Int, List<GroupPreset>> = emptyMap(),
    val presetLoadingIds: Set<Int> = emptySet(),
    val presetError: String? = null,
    val autoGroupConfig: GroupAutoGroupConfig? = null,
    val autoGroupLoading: Boolean = false,
    val autoGroupError: String? = null,
    val autoGroupSubmitting: Boolean = false,
    val autoGroupMessage: String? = null,
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
    private val appRepository: AppRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(GroupUiState())
    val uiState: StateFlow<GroupUiState> = _uiState

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val previous = _uiState.value
            _uiState.value = previous.copy(loading = true, error = null, groupHealthLoading = true, groupHealthError = null)
            val groupsDeferred = async { groupRepository.groups() }
            val channelsDeferred = async { channelRepository.channels() }
            val modelChannelsDeferred = async { modelRepository.modelChannels() }
            val settingsDeferred = async { appRepository.settings() }
            val settingsResult = settingsDeferred.await()
            val groupHealthEnabled = settingsResult.groupHealthEnabled()
            val groupHealthResult = if (groupHealthEnabled) {
                groupRepository.groupHealthList()
            } else {
                AppResult.Success(emptyList())
            }
            _uiState.value = buildGroupRefreshState(
                previous = previous,
                groupsResult = groupsDeferred.await(),
                channelsResult = channelsDeferred.await(),
                modelChannelsResult = modelChannelsDeferred.await(),
                groupHealthEnabled = groupHealthEnabled,
                groupHealthSettingError = settingsResult.errorMessageOrNull(),
                groupHealthResult = groupHealthResult,
            )
        }
    }

    fun clearOperationError() {
        _uiState.value = _uiState.value.copy(
            operationError = null,
            groupHealthError = null,
            groupHealthMessage = null,
            presetError = null,
            autoGroupError = null,
            autoGroupMessage = null,
        )
    }

    fun runGroupHealth(groupId: Int, probeMode: String? = null) {
        viewModelScope.launch {
            if (_uiState.value.groupHealthSubmitting || !_uiState.value.groupHealthEnabled || groupId <= 0) return@launch
            _uiState.value = _uiState.value.copy(
                groupHealthSubmitting = true,
                groupHealthError = null,
                groupHealthMessage = null,
            )
            when (val result = groupRepository.runGroupHealth(groupId, probeMode)) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        groupHealthSubmitting = false,
                        groupHealthMessage = groupHealthAcceptedMessage(result.data.probeMode ?: probeMode),
                    )
                    refresh()
                }
                is AppResult.Error -> _uiState.value = _uiState.value.copy(
                    groupHealthSubmitting = false,
                    groupHealthError = result.message,
                )
            }
        }
    }

    fun runAllGroupHealth(probeMode: String? = null) {
        viewModelScope.launch {
            if (_uiState.value.groupHealthSubmitting || !_uiState.value.groupHealthEnabled) return@launch
            _uiState.value = _uiState.value.copy(
                groupHealthSubmitting = true,
                groupHealthError = null,
                groupHealthMessage = null,
            )
            when (val result = groupRepository.runAllGroupHealth(probeMode)) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        groupHealthSubmitting = false,
                        groupHealthMessage = groupHealthAcceptedMessage(result.data.probeMode ?: probeMode, allGroups = true),
                    )
                    refresh()
                }
                is AppResult.Error -> _uiState.value = _uiState.value.copy(
                    groupHealthSubmitting = false,
                    groupHealthError = result.message,
                )
            }
        }
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

    fun togglePin(group: Group) {
        viewModelScope.launch {
            if (_uiState.value.submitting || group.id <= 0) return@launch
            _uiState.value = _uiState.value.copy(submitting = true, operationError = null)
            when (val result = groupRepository.pinGroup(group.id, !group.pinned)) {
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

    fun loadGroupPresets(groupId: Int, force: Boolean = true) {
        viewModelScope.launch {
            val current = _uiState.value
            if (groupId <= 0 || groupId in current.presetLoadingIds) return@launch
            if (!force && current.presetsByGroupId.containsKey(groupId)) return@launch

            _uiState.value = current.copy(
                presetLoadingIds = current.presetLoadingIds + groupId,
                presetError = null,
            )
            when (val result = groupRepository.groupPresets(groupId)) {
                is AppResult.Success -> {
                    val latest = _uiState.value
                    _uiState.value = latest.copy(
                        presetsByGroupId = latest.presetsByGroupId + (groupId to result.data),
                        presetLoadingIds = latest.presetLoadingIds - groupId,
                    )
                }
                is AppResult.Error -> {
                    val latest = _uiState.value
                    _uiState.value = latest.copy(
                        presetLoadingIds = latest.presetLoadingIds - groupId,
                        presetError = result.message,
                    )
                }
            }
        }
    }

    fun createCurrentPreset(
        groupId: Int,
        name: String,
        onSuccess: (GroupPreset) -> Unit = {},
    ) {
        viewModelScope.launch {
            val presetName = name.trim()
            if (_uiState.value.submitting || groupId <= 0 || presetName.isBlank()) return@launch
            _uiState.value = _uiState.value.copy(submitting = true, presetError = null, operationError = null)
            when (val result = groupRepository.createGroupPreset(groupId, presetName)) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(submitting = false)
                    onSuccess(result.data)
                    loadGroupPresets(groupId, force = true)
                    refresh()
                }
                is AppResult.Error -> _uiState.value = _uiState.value.copy(
                    submitting = false,
                    presetError = result.message,
                )
            }
        }
    }

    fun createBlankPreset(
        groupId: Int,
        name: String,
        onSuccess: (GroupPreset) -> Unit = {},
    ) {
        viewModelScope.launch {
            val presetName = name.trim()
            if (_uiState.value.submitting || groupId <= 0 || presetName.isBlank()) return@launch
            _uiState.value = _uiState.value.copy(submitting = true, presetError = null, operationError = null)
            when (val result = groupRepository.createBlankGroupPreset(groupId, presetName)) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(submitting = false)
                    onSuccess(result.data)
                    loadGroupPresets(groupId, force = true)
                }
                is AppResult.Error -> _uiState.value = _uiState.value.copy(
                    submitting = false,
                    presetError = result.message,
                )
            }
        }
    }

    fun clonePreset(
        groupId: Int,
        presetId: Int,
        name: String,
        onSuccess: (GroupPreset) -> Unit = {},
    ) {
        viewModelScope.launch {
            val presetName = name.trim()
            if (_uiState.value.submitting || groupId <= 0 || presetId <= 0 || presetName.isBlank()) return@launch
            _uiState.value = _uiState.value.copy(submitting = true, presetError = null, operationError = null)
            when (val result = groupRepository.cloneGroupPreset(presetId, presetName)) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(submitting = false)
                    onSuccess(result.data)
                    loadGroupPresets(groupId, force = true)
                }
                is AppResult.Error -> _uiState.value = _uiState.value.copy(
                    submitting = false,
                    presetError = result.message,
                )
            }
        }
    }

    fun activatePreset(groupId: Int, presetId: Int, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            if (_uiState.value.submitting || groupId <= 0 || presetId <= 0) return@launch
            _uiState.value = _uiState.value.copy(submitting = true, presetError = null, operationError = null)
            when (val result = groupRepository.activateGroupPreset(presetId)) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(submitting = false)
                    onSuccess()
                    loadGroupPresets(groupId, force = true)
                    refresh()
                }
                is AppResult.Error -> _uiState.value = _uiState.value.copy(
                    submitting = false,
                    presetError = result.message,
                )
            }
        }
    }

    fun updatePreset(
        preset: GroupPreset,
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
            if (_uiState.value.submitting || preset.id <= 0 || preset.groupId <= 0) return@launch
            _uiState.value = _uiState.value.copy(submitting = true, presetError = null, operationError = null)
            val request = GroupPresetUpdateRequest(
                name = name.trim(),
                mode = mode,
                matchRegex = matchRegex.trim(),
                firstTokenTimeOut = firstTokenTimeOut,
                sessionKeepTime = sessionKeepTime,
                retryEnabled = retryEnabled,
                maxRetries = maxRetries.takeIf { it > 0 } ?: 3,
                items = items.map { item ->
                    GroupPresetItem(
                        channelId = item.channelId,
                        modelName = item.modelName,
                        priority = item.priority,
                        weight = item.weight,
                    )
                },
            )
            when (val result = groupRepository.updateGroupPreset(preset.id, request)) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(submitting = false)
                    onSuccess()
                    loadGroupPresets(preset.groupId, force = true)
                    refresh()
                }
                is AppResult.Error -> _uiState.value = _uiState.value.copy(
                    submitting = false,
                    presetError = result.message,
                )
            }
        }
    }

    fun deletePreset(groupId: Int, presetId: Int, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            if (_uiState.value.submitting || groupId <= 0 || presetId <= 0) return@launch
            _uiState.value = _uiState.value.copy(submitting = true, presetError = null, operationError = null)
            when (val result = groupRepository.deleteGroupPreset(presetId)) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(submitting = false)
                    onSuccess()
                    loadGroupPresets(groupId, force = true)
                }
                is AppResult.Error -> _uiState.value = _uiState.value.copy(
                    submitting = false,
                    presetError = result.message,
                )
            }
        }
    }

    fun loadAutoGroupConfig(force: Boolean = true) {
        viewModelScope.launch {
            val current = _uiState.value
            if (current.autoGroupLoading) return@launch
            if (!force && current.autoGroupConfig != null) return@launch

            _uiState.value = current.copy(autoGroupLoading = true, autoGroupError = null)
            when (val result = groupRepository.groupAutoGroupConfig()) {
                is AppResult.Success -> _uiState.value = _uiState.value.copy(
                    autoGroupLoading = false,
                    autoGroupConfig = result.data,
                )
                is AppResult.Error -> _uiState.value = _uiState.value.copy(
                    autoGroupLoading = false,
                    autoGroupError = result.message,
                )
            }
        }
    }

    fun saveAutoGroupConfig(
        projectedGlobalAutoGroup: Int,
        sourceModes: Map<Int, Int>,
        runNow: Boolean,
        onSuccess: () -> Unit = {},
    ) {
        viewModelScope.launch {
            val config = _uiState.value.autoGroupConfig ?: return@launch
            if (_uiState.value.autoGroupSubmitting) return@launch

            val dirtyItems = config.sources.mapNotNull { source ->
                val next = sourceModes[source.channelId] ?: source.autoGroup
                if (next == source.autoGroup) {
                    null
                } else {
                    GroupAutoGroupSourceUpdateRequest(
                        channelId = source.channelId,
                        autoGroup = next,
                    )
                }
            }
            val globalMode = projectedGlobalAutoGroup.takeIf { it != config.projectedGlobalAutoGroup }
            if (globalMode == null && dirtyItems.isEmpty() && !runNow) {
                onSuccess()
                return@launch
            }

            _uiState.value = _uiState.value.copy(autoGroupSubmitting = true, autoGroupError = null, autoGroupMessage = null)
            val request = GroupAutoGroupConfigUpdateRequest(
                projectedGlobalAutoGroup = globalMode,
                items = dirtyItems,
                runNow = runNow,
            )
            when (val result = groupRepository.updateGroupAutoGroupConfig(request)) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        autoGroupSubmitting = false,
                        autoGroupConfig = result.data,
                        autoGroupMessage = if (runNow) "自动分组配置已保存并开始执行。" else "自动分组配置已保存。",
                    )
                    onSuccess()
                    refresh()
                }
                is AppResult.Error -> _uiState.value = _uiState.value.copy(
                    autoGroupSubmitting = false,
                    autoGroupError = result.message,
                )
            }
        }
    }

    fun runAutoGroup(onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            if (_uiState.value.autoGroupSubmitting) return@launch
            _uiState.value = _uiState.value.copy(autoGroupSubmitting = true, autoGroupError = null, autoGroupMessage = null)
            when (val result = groupRepository.runGroupAutoGroup()) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        autoGroupSubmitting = false,
                        autoGroupMessage = "自动分组已开始执行。",
                    )
                    onSuccess()
                    loadAutoGroupConfig(force = true)
                    refresh()
                }
                is AppResult.Error -> _uiState.value = _uiState.value.copy(
                    autoGroupSubmitting = false,
                    autoGroupError = result.message,
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
    groupHealthEnabled: Boolean,
    groupHealthSettingError: String?,
    groupHealthResult: AppResult<List<GroupHealthGroupView>>,
): GroupUiState = when (groupsResult) {
    is AppResult.Success -> previous.copy(
        loading = false,
        groups = groupsResult.data,
        channels = channelsResult.dataOrPrevious(previous.channels),
        modelChannels = modelChannelsResult.dataOrPrevious(previous.modelChannels),
        groupHealthEnabled = groupHealthEnabled,
        groupHealth = groupHealthResult.dataOrPrevious(previous.groupHealth),
        groupHealthLoading = false,
        groupHealthError = groupHealthSettingError ?: groupHealthResult.errorMessageOrNull(),
        error = null,
        channelListError = channelsResult.errorMessageOrNull(),
        modelChannelError = modelChannelsResult.errorMessageOrNull(),
    )
    is AppResult.Error -> previous.copy(
        loading = false,
        channels = channelsResult.dataOrPrevious(previous.channels),
        modelChannels = modelChannelsResult.dataOrPrevious(previous.modelChannels),
        groupHealthEnabled = groupHealthEnabled,
        groupHealth = groupHealthResult.dataOrPrevious(previous.groupHealth),
        groupHealthLoading = false,
        groupHealthError = groupHealthSettingError ?: groupHealthResult.errorMessageOrNull(),
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

private fun groupHealthAcceptedMessage(probeMode: String?, allGroups: Boolean = false): String {
    val scope = if (allGroups) "全部分组" else "分组"
    val mode = if (probeMode == GroupHealthProbeMode.Full) "完整探测" else "标准探测"
    return "$scope $mode 已开始。"
}

private fun AppResult<List<SettingItem>>.groupHealthEnabled(): Boolean = when (this) {
    is AppResult.Success -> data.firstOrNull { it.key == "group_health_enabled" }?.value == "true"
    is AppResult.Error -> false
}
