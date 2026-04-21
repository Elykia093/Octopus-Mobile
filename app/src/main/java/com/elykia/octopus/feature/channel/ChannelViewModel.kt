package com.elykia.octopus.feature.channel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elykia.octopus.core.data.model.Channel
import com.elykia.octopus.core.data.remote.ChannelApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChannelUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val items: List<Channel> = emptyList(),
)

@HiltViewModel
class ChannelViewModel @Inject constructor(
    private val channelApi: ChannelApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChannelUiState())
    val uiState: StateFlow<ChannelUiState> = _uiState.asStateFlow()

    init {
        loadChannels(isRefresh = true)
    }

    fun loadChannels(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _uiState.update { it.copy(isRefreshing = true, error = null) }
            } else {
                _uiState.update { it.copy(isLoading = true, error = null) }
            }

            try {
                val response = channelApi.getChannels()
                if (response.success && response.data != null) {
                    _uiState.update {
                        it.copy(
                            items = response.data,
                            isLoading = false,
                            isRefreshing = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(error = response.message, isLoading = false, isRefreshing = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Network error", isLoading = false, isRefreshing = false) }
            }
        }
    }
}
