package com.elykia.octopus.feature.channel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elykia.octopus.core.common.AppResult
import com.elykia.octopus.core.data.model.Channel
import com.elykia.octopus.core.data.repository.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChannelUiState(
    val loading: Boolean = true,
    val channels: List<Channel> = emptyList(),
    val error: String? = null,
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
                is AppResult.Success -> _uiState.value = ChannelUiState(loading = false, channels = result.data)
                is AppResult.Error -> _uiState.value = ChannelUiState(loading = false, error = result.message)
            }
        }
    }

    fun delete(id: Int) {
        viewModelScope.launch {
            repository.deleteChannel(id)
            refresh()
        }
    }
}
