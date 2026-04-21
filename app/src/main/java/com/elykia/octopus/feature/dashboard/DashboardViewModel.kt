package com.elykia.octopus.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elykia.octopus.core.data.model.DashboardEntry
import com.elykia.octopus.core.data.model.StatsDaily
import com.elykia.octopus.core.data.model.StatsTotal
import com.elykia.octopus.core.data.model.TrendEntry
import com.elykia.octopus.core.data.remote.DashboardApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val entries: List<DashboardEntry> = emptyList(),
    val dailyStats: StatsDaily = StatsDaily(),
    val totalStats: StatsTotal = StatsTotal(),
    val trendDaily: List<TrendEntry> = emptyList(),
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
                val response = dashboardApi.getUserDashboard()
                if (response.success && response.data != null) {
                    val entries = response.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            entries = entries,
                            dailyStats = entries.toDailyStats(),
                            totalStats = entries.toTotalStats(),
                            trendDaily = entries.toTrendDaily(),
                            error = null,
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            error = response.message.ifBlank { "加载仪表盘失败" },
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

private fun List<DashboardEntry>.toDailyStats(): StatsDaily {
    if (isEmpty()) return StatsDaily()
    val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
    val todayEntries = filter { it.day == today }
    return todayEntries.toStatsDaily()
}

private fun List<DashboardEntry>.toTotalStats(): StatsTotal {
    if (isEmpty()) return StatsTotal()
    return StatsTotal(
        requestCount = sumOf { it.requestCount.toLong() },
        costValue = sumOf { it.quota.toDouble() / 500000.0 },
        tokenValue = sumOf { (it.promptTokens + it.completionTokens).toLong() },
        inputCost = sumOf { it.promptTokens.toDouble() / 500000.0 },
        inputToken = sumOf { it.promptTokens.toLong() },
    )
}

private fun List<DashboardEntry>.toStatsDaily(): StatsDaily {
    return StatsDaily(
        requestCount = sumOf { it.requestCount.toLong() },
        costValue = sumOf { it.quota.toDouble() / 500000.0 },
        tokenValue = sumOf { (it.promptTokens + it.completionTokens).toLong() },
        inputCost = sumOf { it.promptTokens.toDouble() / 500000.0 },
        inputToken = sumOf { it.promptTokens.toLong() },
    )
}

private fun List<DashboardEntry>.toTrendDaily(): List<TrendEntry> {
    return groupBy { it.day }
        .toSortedMap()
        .map { (day, items) ->
            TrendEntry(
                title = day.substringAfter('-'),
                requestCount = items.sumOf { it.requestCount.toLong() },
                costValue = items.sumOf { it.quota.toDouble() / 500000.0 },
                tokenValue = items.sumOf { (it.promptTokens + it.completionTokens).toLong() },
            )
        }
}
