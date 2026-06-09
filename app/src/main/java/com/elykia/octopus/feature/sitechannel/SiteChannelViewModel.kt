package com.elykia.octopus.feature.sitechannel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elykia.octopus.core.common.AppResult
import com.elykia.octopus.core.data.model.SiteChannelCard
import com.elykia.octopus.core.data.repository.SiteChannelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SiteChannelUiState(
    val loading: Boolean = true,
    val cards: List<SiteChannelCard> = emptyList(),
    val error: String? = null,
)

internal fun SiteChannelUiState.shouldShowPageError(): Boolean =
    error != null && cards.isEmpty()

@HiltViewModel
class SiteChannelViewModel @Inject constructor(
    private val repository: SiteChannelRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SiteChannelUiState())
    val uiState: StateFlow<SiteChannelUiState> = _uiState

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            when (val result = repository.siteChannels(includeHistory = false)) {
                is AppResult.Success -> _uiState.value = _uiState.value.copy(
                    loading = false,
                    cards = result.data.sortedWith(compareBy<SiteChannelCard> { it.siteName.lowercase() }.thenBy { it.siteId }),
                    error = null,
                )
                is AppResult.Error -> _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = result.message,
                )
            }
        }
    }
}
