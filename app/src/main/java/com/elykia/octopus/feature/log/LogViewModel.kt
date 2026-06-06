package com.elykia.octopus.feature.log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elykia.octopus.core.common.AppResult
import com.elykia.octopus.core.data.model.RelayLog
import com.elykia.octopus.core.data.repository.LogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LogUiState(
    val loading: Boolean = true,
    val loadingMore: Boolean = false,
    val clearing: Boolean = false,
    val logs: List<RelayLog> = emptyList(),
    val page: Int = 1,
    val hasMore: Boolean = true,
    val error: String? = null,
    val pagingError: String? = null,
    val clearError: String? = null,
)

internal fun LogUiState.shouldShowPageError(): Boolean =
    error != null && logs.isEmpty()

internal fun LogUiState.clearLogsStarted(): LogUiState = copy(
    clearing = true,
    clearError = null,
)

internal fun LogUiState.clearLogsSucceeded(): LogUiState = copy(
    clearing = false,
    loading = false,
    loadingMore = false,
    logs = emptyList(),
    page = 1,
    hasMore = true,
    clearError = null,
    pagingError = null,
)

internal fun LogUiState.clearLogsFailed(message: String): LogUiState = copy(
    clearing = false,
    clearError = message,
)

@HiltViewModel
class LogViewModel @Inject constructor(
    private val repository: LogRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LogUiState())
    val uiState: StateFlow<LogUiState> = _uiState
    private var loadGeneration = 0

    init {
        refresh()
    }

    fun refresh() {
        load(refresh = true)
    }

    fun loadMore() {
        if (_uiState.value.loading || _uiState.value.loadingMore || !_uiState.value.hasMore) return
        load(refresh = false)
    }

    private fun load(refresh: Boolean) {
        viewModelScope.launch {
            val requestGeneration = if (refresh) {
                ++loadGeneration
            } else {
                loadGeneration
            }
            val nextPage = if (refresh) 1 else _uiState.value.page
            _uiState.value = if (refresh) {
                _uiState.value.copy(loading = true, loadingMore = false, error = null, pagingError = null, clearError = null)
            } else {
                _uiState.value.copy(loadingMore = true, pagingError = null)
            }
            when (val result = repository.logs(page = nextPage, pageSize = 20)) {
                is AppResult.Success -> {
                    if (requestGeneration != loadGeneration) return@launch
                    val mergedLogs = if (refresh) result.data else _uiState.value.logs + result.data
                    _uiState.value = _uiState.value.copy(
                        loading = false,
                        loadingMore = false,
                        logs = mergedLogs,
                        page = nextPage + 1,
                        hasMore = result.data.size >= 20,
                        error = null,
                        pagingError = null,
                    )
                }
                is AppResult.Error -> {
                    if (requestGeneration != loadGeneration) return@launch
                    _uiState.value = if (refresh) {
                        _uiState.value.copy(loading = false, loadingMore = false, error = result.message)
                    } else {
                        _uiState.value.copy(loadingMore = false, pagingError = result.message)
                    }
                }
            }
        }
    }

    fun clear() {
        viewModelScope.launch {
            if (_uiState.value.clearing) return@launch
            ++loadGeneration
            _uiState.value = _uiState.value.clearLogsStarted()
            when (val result = repository.clearLogs()) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.clearLogsSucceeded()
                    refresh()
                }
                is AppResult.Error -> _uiState.value = _uiState.value.clearLogsFailed(result.message)
            }
        }
    }
}
