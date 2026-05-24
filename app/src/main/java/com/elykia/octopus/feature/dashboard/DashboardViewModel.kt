package com.elykia.octopus.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elykia.octopus.core.data.model.ApiResponse
import com.elykia.octopus.core.data.model.StatsDaily
import com.elykia.octopus.core.data.model.StatsMetrics
import com.elykia.octopus.core.data.model.StatsHourly
import com.elykia.octopus.core.data.remote.ApiKeyApiService
import com.elykia.octopus.core.data.remote.DashboardApiService
import com.elykia.octopus.core.data.remote.toUserMessage
import com.elykia.octopus.core.data.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
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
    private val apiKeyApi: ApiKeyApiService,
    private val appRepository: AppRepository,
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
                val authState = appRepository.authState.first()
                if (authState.isApiKeyMode) {
                    val response = apiKeyApi.getApiKeyStats()
                    if (response.isSuccessful) {
                        val stats = response.data?.stats ?: StatsMetrics()
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                todayStats = stats,
                                totalStats = stats,
                                dailyStats = emptyList(),
                                hourlyStats = emptyList(),
                                error = null,
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                error = response.message.ifBlank { "加载 API Key 统计失败" },
                            )
                        }
                    }
                } else {
                    // 并行请求所有统计数据
                    val responses = coroutineScope {
                        val todayDeferred = async { dashboardApi.getTodayStats() }
                        val totalDeferred = async { dashboardApi.getTotalStats() }
                        val dailyDeferred = async { dashboardApi.getDailyStats() }
                        val hourlyDeferred = async { dashboardApi.getHourlyStats() }

                        DashboardResponses(
                            today = todayDeferred.await(),
                            total = totalDeferred.await(),
                            daily = dailyDeferred.await(),
                            hourly = hourlyDeferred.await(),
                        )
                    }

                    if (responses.isSuccessful) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                todayStats = responses.today.data ?: StatsMetrics(),
                                totalStats = responses.total.data ?: StatsMetrics(),
                                dailyStats = responses.daily.data ?: emptyList(),
                                hourlyStats = responses.hourly.data ?: emptyList(),
                                error = null,
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                error = responses.errorMessage.ifBlank { "加载仪表盘失败" },
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = e.toUserMessage("加载仪表盘失败"),
                    )
                }
            }
        }
    }
}

private data class DashboardResponses(
    val today: ApiResponse<StatsMetrics>,
    val total: ApiResponse<StatsMetrics>,
    val daily: ApiResponse<List<StatsDaily>>,
    val hourly: ApiResponse<List<StatsHourly>>,
) {
    val isSuccessful: Boolean
        get() = today.isSuccessful && total.isSuccessful && daily.isSuccessful && hourly.isSuccessful

    val errorMessage: String
        get() = listOf(today, total, daily, hourly)
            .firstOrNull { !it.isSuccessful }
            ?.message
            .orEmpty()
}
