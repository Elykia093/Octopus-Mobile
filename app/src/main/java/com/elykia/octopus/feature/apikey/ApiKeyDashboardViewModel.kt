package com.elykia.octopus.feature.apikey

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elykia.octopus.core.common.AppResult
import com.elykia.octopus.core.data.model.ApiKeyDashboard
import com.elykia.octopus.core.data.repository.ApiKeyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ApiKeyDashboardUiState(
    val loading: Boolean = true,
    val dashboard: ApiKeyDashboard? = null,
    val error: String? = null,
)

@HiltViewModel
class ApiKeyDashboardViewModel @Inject constructor(
    private val repository: ApiKeyRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ApiKeyDashboardUiState())
    val uiState: StateFlow<ApiKeyDashboardUiState> = _uiState

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                loading = _uiState.value.dashboard == null,
                error = null,
            )
            when (val result = repository.dashboardStats()) {
                is AppResult.Success -> {
                    _uiState.value = ApiKeyDashboardUiState(
                        loading = false,
                        dashboard = result.data,
                    )
                }
                is AppResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        loading = false,
                        error = result.message,
                    )
                }
            }
        }
    }
}
