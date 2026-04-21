package com.elykia.octopus.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elykia.octopus.core.data.model.StatsDaily
import com.elykia.octopus.core.data.model.StatsMetrics
import com.elykia.octopus.core.data.model.StatsHourly
import com.elykia.octopus.core.data.remote.DashboardApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val todayStats: StatsMetrics = StatsMetrics(),
    val totalStats: StatsMetrics = StatsMetrics(),
    val dailyStats: List<StatsDaily> = emptyList(),
    val hourlyStats: List<StatsHourly> = emptyList(),
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val dashboardApi: DashboardApiService,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadData(isRefresh = false)
    }

    fun loadData(isRefresh: Boolean = true) {
        viewModelScope.launch {
            if (isRefresh) {
                _uiState.update { it.copy(isRefreshing = true, error = null) }
            } else {
                _uiState.update { it.copy(isLoading = true, error = null) }
            }

            try {
                // 并行请求所有统计数据
                val todayResponse = dashboardApi.getTodayStats()
                val totalResponse = dashboardApi.getTotalStats()
                val dailyResponse = dashboardApi.getDailyStats()
                val hourlyResponse = dashboardApi.getHourlyStats()

                if (todayResponse.success && totalResponse.success) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            todayStats = todayResponse.data ?: StatsMetrics(),
                            totalStats = totalResponse.data ?: StatsMetrics(),
                            dailyStats = dailyResponse.data ?: emptyList(),
                            hourlyStats = hourlyResponse.data ?: emptyList(),
                            error = null,
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            error = todayResponse.message.ifBlank { totalResponse.message.ifBlank { "加载仪表盘失败" } },
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = e.message ?: "加载仪表盘失败",
                    )
                }
            }
        }
    }
}
