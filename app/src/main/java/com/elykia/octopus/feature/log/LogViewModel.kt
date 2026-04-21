package com.elykia.octopus.feature.log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elykia.octopus.core.data.model.LogItem
import com.elykia.octopus.core.data.remote.LogApiService
import com.elykia.octopus.core.data.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LogUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val items: List<LogItem> = emptyList(),
    val currentPage: Int = 0,
    val hasMore: Boolean = true,
)

@HiltViewModel
class LogViewModel @Inject constructor(
    private val logApi: LogApiService,
    private val appRepository: AppRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogUiState())
    val uiState: StateFlow<LogUiState> = _uiState.asStateFlow()

    init {
        loadLogs(isRefresh = true)
    }

    fun loadLogs(isRefresh: Boolean = false) {
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
                val authState = appRepository.authState.first()
                val response = if (authState.role >= 10 && !authState.isApiKeyMode) {
                    logApi.getAdminLogs(page = nextPage)
                } else {
                    logApi.getUserLogs(page = nextPage)
                }
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
