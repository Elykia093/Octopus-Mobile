package com.elykia.octopus.feature.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elykia.octopus.core.data.model.Group
import com.elykia.octopus.core.data.model.GroupPinRequest
import com.elykia.octopus.core.data.remote.GroupApiService
import com.elykia.octopus.core.data.remote.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

data class GroupUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val items: List<Group> = emptyList(),
    val pinningIds: Set<Int> = emptySet(),
)

@HiltViewModel
class GroupViewModel @Inject constructor(
    private val groupApi: GroupApiService,
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupUiState())
    val uiState: StateFlow<GroupUiState> = _uiState.asStateFlow()

    init {
        loadGroups()
    }

    fun loadGroups(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _uiState.update { it.copy(isRefreshing = true, error = null) }
            } else {
                _uiState.update { it.copy(isLoading = true, error = null) }
            }

            try {
                val response = groupApi.getGroups()
                if (response.isSuccessful) {
                    _uiState.update {
                        it.copy(
                            items = response.data.orEmpty().sortedForDisplay(),
                            isLoading = false,
                            isRefreshing = false,
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            error = response.message.ifBlank { "加载分组失败" },
                            isLoading = false,
                            isRefreshing = false,
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.toUserMessage(), isLoading = false, isRefreshing = false)
                }
            }
        }
    }

    fun togglePinned(group: Group) {
        if (group.id <= 0 || _uiState.value.pinningIds.contains(group.id)) return

        viewModelScope.launch {
            val nextPinned = !group.pinned
            _uiState.update { it.copy(pinningIds = it.pinningIds + group.id, error = null) }

            try {
                val response = groupApi.setPinned(group.id, GroupPinRequest(nextPinned))
                if (response.isSuccessful) {
                    _uiState.update { state ->
                        state.copy(
                            items = state.items.map {
                                if (it.id == group.id) it.copy(pinned = nextPinned) else it
                            }.sortedForDisplay(),
                            pinningIds = state.pinningIds - group.id,
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            error = response.message.ifBlank { "更新置顶失败" },
                            pinningIds = it.pinningIds - group.id,
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.toUserMessage(), pinningIds = it.pinningIds - group.id)
                }
            }
        }
    }
}

private fun List<Group>.sortedForDisplay(): List<Group> {
    return sortedWith(
        compareByDescending<Group> { it.pinned }
            .thenBy { it.name.lowercase(Locale.getDefault()) }
    )
}
