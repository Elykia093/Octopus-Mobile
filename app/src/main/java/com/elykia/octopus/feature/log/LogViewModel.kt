package com.elykia.octopus.feature.log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elykia.octopus.core.common.AppResult
import com.elykia.octopus.core.data.model.RelayLog
import com.elykia.octopus.core.data.repository.LogStreamEvent
import com.elykia.octopus.core.data.repository.LogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
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
    val detailLoading: Boolean = false,
    val detailLog: RelayLog? = null,
    val detailError: String? = null,
    val streamConnected: Boolean = false,
    val streamError: String? = null,
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
    private var streamJob: Job? = null

    init {
        refresh()
        startStream()
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
                    val pageData = result.data
                    val mergedLogs = if (refresh) pageData.logs else _uiState.value.logs + pageData.logs
                    _uiState.value = _uiState.value.copy(
                        loading = false,
                        loadingMore = false,
                        logs = mergedLogs,
                        page = nextPage + 1,
                        hasMore = pageData.hasMore,
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

    fun openDetail(log: RelayLog) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                detailLoading = true,
                detailLog = log,
                detailError = null,
            )
            when (val result = repository.logDetail(log.id)) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        detailLoading = false,
                        detailLog = result.data,
                        detailError = null,
                    )
                }
                is AppResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        detailLoading = false,
                        detailError = result.message,
                    )
                }
            }
        }
    }

    fun closeDetail() {
        _uiState.value = _uiState.value.copy(
            detailLoading = false,
            detailLog = null,
            detailError = null,
        )
    }

    private fun startStream() {
        streamJob?.cancel()
        streamJob = viewModelScope.launch {
            while (isActive) {
                var hadError = false
                repository.streamLogs().collect { event ->
                    when (event) {
                        LogStreamEvent.Connected -> {
                            _uiState.value = _uiState.value.copy(streamConnected = true, streamError = null)
                        }
                        is LogStreamEvent.Item -> {
                            _uiState.value = _uiState.value.withStreamLog(event.log)
                        }
                        is LogStreamEvent.Error -> {
                            hadError = true
                            _uiState.value = _uiState.value.copy(streamConnected = false, streamError = event.message)
                        }
                    }
                }
                _uiState.value = _uiState.value.copy(streamConnected = false)
                delay(if (hadError) 3_000L else 1_000L)
            }
        }
    }
}

internal fun LogUiState.withStreamLog(log: RelayLog): LogUiState {
    val merged = buildList {
        add(log)
        logs.forEach { existing ->
            if (existing.id != log.id) add(existing)
        }
    }
    return copy(
        logs = merged,
        streamConnected = true,
        streamError = null,
    )
}
