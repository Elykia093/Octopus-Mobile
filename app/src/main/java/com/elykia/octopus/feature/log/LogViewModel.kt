package com.elykia.octopus.feature.log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elykia.octopus.core.data.model.LogItem
import com.elykia.octopus.core.data.model.LogListResponse
import com.elykia.octopus.core.data.remote.LogApiService
import com.elykia.octopus.core.data.remote.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LogUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val items: List<LogItem> = emptyList(),
    val total: Long = 0L,
    val warning: String? = null,
    val searchMode: String? = null,
    val currentPage: Int = 0,
    val hasMore: Boolean = true,
)

@HiltViewModel
class LogViewModel @Inject constructor(
    private val logApi: LogApiService,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogUiState())
    val uiState: StateFlow<LogUiState> = _uiState.asStateFlow()

    init {
        loadLogs()
    }

    fun loadLogs(isRefresh: Boolean = false) {
        val state = _uiState.value
        if (!isRefresh && !state.hasMore) return

        val nextPage = if (isRefresh) 1 else state.currentPage + 1

        viewModelScope.launch {
            if (isRefresh) {
                _uiState.update { it.copy(isRefreshing = true, error = null) }
            } else {
                _uiState.update { it.copy(isLoading = true, error = null) }
            }

            try {
                val response = logApi.getLogs(page = nextPage, pageSize = 20)
                val payload = response.data ?: LogListResponse()
                if (response.isSuccessful) {
                    val pageItems = payload.logs
                    val hasMore = payload.hasMore ?: (pageItems.size >= 20)
                    _uiState.update {
                        it.copy(
                            items = if (isRefresh) pageItems else it.items + pageItems,
                            total = payload.total,
                            warning = payload.warning,
                            searchMode = payload.searchMode,
                            currentPage = nextPage,
                            hasMore = hasMore,
                            isLoading = false,
                            isRefreshing = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(error = response.message.ifBlank { "加载日志失败" }, isLoading = false, isRefreshing = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.toUserMessage(), isLoading = false, isRefreshing = false) }
            }
        }
    }
}
