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
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            when (val result = repository.logs()) {
                is AppResult.Success -> _uiState.value = LogUiState(loading = false, logs = result.data)
                is AppResult.Error -> _uiState.value = LogUiState(loading = false, error = result.message)
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
