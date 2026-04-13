package com.elykia.octopus.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elykia.octopus.core.common.AppResult
import com.elykia.octopus.core.data.model.StatsDaily
import com.elykia.octopus.core.data.model.StatsHourly
import com.elykia.octopus.core.data.model.StatsTotal
import com.elykia.octopus.core.data.repository.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val loading: Boolean = true,
    val total: StatsTotal? = null,
    val daily: List<StatsDaily> = emptyList(),
    val hourly: List<StatsHourly> = emptyList(),
    val error: String? = null,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: DashboardRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            val totalDeferred = async { repository.totalStats() }
            val dailyDeferred = async { repository.dailyStats() }
            val hourlyDeferred = async { repository.hourlyStats() }

            val totalResult = totalDeferred.await()
            val dailyResult = dailyDeferred.await()
            val hourlyResult = hourlyDeferred.await()

            if (totalResult is AppResult.Success) {
                _uiState.value = HomeUiState(
                    loading = false,
                    total = totalResult.data,
                    daily = (dailyResult as? AppResult.Success)?.data.orEmpty(),
                    hourly = (hourlyResult as? AppResult.Success)?.data.orEmpty(),
                )
            } else {
                _uiState.value = HomeUiState(loading = false, error = (totalResult as AppResult.Error).message)
            }
        }
    }
}
