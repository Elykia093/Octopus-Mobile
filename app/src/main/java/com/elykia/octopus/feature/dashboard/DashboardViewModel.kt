package com.elykia.octopus.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elykia.octopus.core.data.model.DashboardData
import com.elykia.octopus.core.data.model.DashboardRankings
import com.elykia.octopus.core.data.remote.DashboardApiService
import com.elykia.octopus.core.data.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val stats: DashboardData? = null,
    val rankings: DashboardRankings? = null,
    val showToday: Boolean = true, // Toggle between Daily and Total scope
    val showHourlyTrend: Boolean = true // Toggle chart trend type
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val dashboardApi: DashboardApiService,
    private val appRepository: AppRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadData(isRefresh = false)
    }

    fun toggleScope() = _uiState.update { it.copy(showToday = !it.showToday) }
    fun toggleTrend() = _uiState.update { it.copy(showHourlyTrend = !it.showHourlyTrend) }

    fun loadData(isRefresh: Boolean = true) {
        viewModelScope.launch {
            if (isRefresh) {
                _uiState.update { it.copy(isRefreshing = true, error = null) }
            } else {
                _uiState.update { it.copy(isLoading = true, error = null) }
            }

            try {
                // Determine auth mode to fetch correct dashboard
                val isApiKeyMode = appRepository.authState.firstOrNull()?.isApiKeyMode ?: false

                val statsResponse = if (isApiKeyMode) {
                    dashboardApi.getApiKeyDashboardStats()
                } else {
                    dashboardApi.getDashboardStats()
                }
                
                // Only admin usually fetches rankings
                val rankingsResponse = if (!isApiKeyMode) {
                    dashboardApi.getDashboardRankings()
                } else {
                    null
                }

                if (statsResponse.success) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            stats = statsResponse.data,
                            rankings = rankingsResponse?.data,
                            error = null
                        )
                    }
                } else {
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            isRefreshing = false, 
                            error = statsResponse.message 
                        ) 
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        isRefreshing = false, 
                        error = e.message ?: "Failed to load dashboard"
                    ) 
                }
            }
        }
    }
}
