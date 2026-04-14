package com.elykia.octopus.feature.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elykia.octopus.core.common.AppResult
import com.elykia.octopus.core.data.model.Channel
import com.elykia.octopus.core.data.model.Group
import com.elykia.octopus.core.data.model.GroupItem
import com.elykia.octopus.core.data.model.GroupItemAddRequest
import com.elykia.octopus.core.data.model.GroupItemUpdateRequest
import com.elykia.octopus.core.data.model.GroupUpdateRequest
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
    val error: String? = null,
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
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            val groupsDeferred = async { repository.groups() }
            val channelsDeferred = async { repository.channels() }
            when (val result = groupsDeferred.await()) {
                is AppResult.Success -> {
                    val channels = (channelsDeferred.await() as? AppResult.Success)?.data.orEmpty()
                    _uiState.value = GroupUiState(loading = false, groups = result.data, channels = channels)
                }
                is AppResult.Error -> _uiState.value = GroupUiState(loading = false, error = result.message)
            }
        }
    }

    fun delete(id: Int) {
        viewModelScope.launch {
            repository.deleteGroup(id)
            refresh()
        }
    }

    fun createGroup(
        name: String,
        mode: Int,
        matchRegex: String,
        firstTokenTimeOut: Int,
        sessionKeepTime: Int,
        items: List<GroupItem>,
    ) {
        viewModelScope.launch {
            repository.createGroup(
                Group(
                    name = name.trim(),
                    mode = mode,
                    matchRegex = matchRegex.trim(),
                    firstTokenTimeOut = firstTokenTimeOut,
                    sessionKeepTime = sessionKeepTime,
                    items = items,
                )
            )
            refresh()
        }
    }

    fun updateGroup(
        group: Group,
        name: String,
        mode: Int,
        matchRegex: String,
        firstTokenTimeOut: Int,
        sessionKeepTime: Int,
        items: List<GroupItem>,
    ) {
        viewModelScope.launch {
            val existingItemsById = group.items.associateBy { it.id }
            val newItems = items.filter { it.id == 0 }
            val changedItems = items.filter { it.id != 0 }
            val deleteItemIds = group.items.map { it.id }.filter { existingId ->
                items.none { current -> current.id == existingId }
            }.toMutableList()

            val itemsToAdd = mutableListOf<GroupItemAddRequest>()
            val itemsToUpdate = mutableListOf<GroupItemUpdateRequest>()

            changedItems.forEach { item ->
                val existing = existingItemsById[item.id] ?: return@forEach
                if (existing.channelId != item.channelId || existing.modelName != item.modelName) {
                    deleteItemIds += existing.id
                    itemsToAdd += GroupItemAddRequest(
                        channelId = item.channelId,
                        modelName = item.modelName,
                        priority = item.priority,
                        weight = item.weight,
                    )
                } else {
                    itemsToUpdate += GroupItemUpdateRequest(
                        id = item.id,
                        priority = item.priority,
                        weight = item.weight,
                    )
                }
            }

            repository.updateGroup(
                GroupUpdateRequest(
                    id = group.id,
                    name = name.trim(),
                    mode = mode,
                    matchRegex = matchRegex.trim(),
                    firstTokenTimeOut = firstTokenTimeOut,
                    sessionKeepTime = sessionKeepTime,
                    itemsToAdd = newItems.map {
                        GroupItemAddRequest(
                            channelId = it.channelId,
                            modelName = it.modelName,
                            priority = it.priority,
                            weight = it.weight,
                        )
                    } + itemsToAdd,
                    itemsToUpdate = itemsToUpdate,
                    itemsToDelete = deleteItemIds.distinct(),
                )
            )
            refresh()
        }
    }
}
