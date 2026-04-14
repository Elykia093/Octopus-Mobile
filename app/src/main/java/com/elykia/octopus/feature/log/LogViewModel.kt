package com.elykia.octopus.feature.log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elykia.octopus.core.common.AppResult
import com.elykia.octopus.core.data.model.RelayLog
import com.elykia.octopus.core.data.repository.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LogUiState(
    val loading: Boolean = true,
    val logs: List<RelayLog> = emptyList(),
    val page: Int = 1,
    val hasMore: Boolean = true,
    val error: String? = null,
)

@HiltViewModel
class LogViewModel @Inject constructor(
    private val repository: DashboardRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LogUiState())
    val uiState: StateFlow<LogUiState> = _uiState

    init {
        refresh()
    }

    fun refresh() {
        load(refresh = true)
    }

    fun loadMore() {
        if (_uiState.value.loading || !_uiState.value.hasMore) return
        load(refresh = false)
    }

    private fun load(refresh: Boolean) {
        viewModelScope.launch {
            val nextPage = if (refresh) 1 else _uiState.value.page
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            when (val result = repository.logs(page = nextPage, pageSize = 20)) {
                is AppResult.Success -> {
                    val mergedLogs = if (refresh) result.data else _uiState.value.logs + result.data
                    _uiState.value = _uiState.value.copy(
                        loading = false,
                        logs = mergedLogs,
                        page = nextPage + 1,
                        hasMore = result.data.size >= 20,
                        error = null,
                    )
                }
                is AppResult.Error -> _uiState.value = _uiState.value.copy(loading = false, error = result.message)
            }
        }
    }

    fun clear() {
        viewModelScope.launch {
            repository.clearLogs()
            refresh()
        }
    }
}
