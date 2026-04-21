package com.elykia.octopus.feature.apikey

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elykia.octopus.core.data.model.ApiKeyItem
import com.elykia.octopus.core.data.remote.ApiKeyApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ApiKeyUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val items: List<ApiKeyItem> = emptyList(),
    val currentPage: Int = 0,
    val hasMore: Boolean = true,
)

@HiltViewModel
class ApiKeyViewModel @Inject constructor(
    private val apiKeyApi: ApiKeyApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ApiKeyUiState())
    val uiState: StateFlow<ApiKeyUiState> = _uiState.asStateFlow()

    init {
        loadApiKeys(isRefresh = true)
    }

    fun loadApiKeys(isRefresh: Boolean = false) {
        val state = _uiState.value
        if (!isRefresh && !state.hasMore) return

        val nextPage = if (isRefresh) 0 else state.currentPage + 1

        viewModelScope.launch {
            if (isRefresh) {
                _uiState.update { it.copy(isRefreshing = true, error = null) }
            } else {
                _uiState.update { it.copy(isLoading = true, error = null) }
            }

            try {
                val response = apiKeyApi.getApiKeys(page = nextPage)
                if (response.success && response.data != null) {
                    val pageItems = response.data
                    _uiState.update {
                        it.copy(
                            items = if (isRefresh) pageItems else it.items + pageItems,
                            currentPage = nextPage,
                            hasMore = pageItems.isNotEmpty(),
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
