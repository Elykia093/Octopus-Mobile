package com.elykia.octopus.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elykia.octopus.core.common.AppResult
import com.elykia.octopus.core.data.model.ApiKeyItem
import com.elykia.octopus.core.data.model.Channel
import com.elykia.octopus.core.data.model.StatsApiKeyEntry
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
    val today: StatsDaily? = null,
    val total: StatsTotal? = null,
    val daily: List<StatsDaily> = emptyList(),
    val hourly: List<StatsHourly> = emptyList(),
    val channels: List<Channel> = emptyList(),
    val apiKeys: List<ApiKeyItem> = emptyList(),
    val apiKeyStats: List<StatsApiKeyEntry> = emptyList(),
    val error: String? = null,
    val todayError: String? = null,
    val dailyError: String? = null,
    val hourlyError: String? = null,
    val channelListError: String? = null,
    val apiKeyListError: String? = null,
    val apiKeyStatsError: String? = null,
)

internal fun HomeUiState.shouldShowPageError(): Boolean =
    error != null && total == null

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
            val previous = _uiState.value
            _uiState.value = previous.copy(loading = true, error = null)
            val todayDeferred = async { repository.todayStats() }
            val totalDeferred = async { repository.totalStats() }
            val dailyDeferred = async { repository.dailyStats() }
            val hourlyDeferred = async { repository.hourlyStats() }
            val channelsDeferred = async { repository.channels() }
            val apiKeysDeferred = async { repository.apiKeys() }
            val apiKeyStatsDeferred = async { repository.apiKeyStats() }

            val todayResult = todayDeferred.await()
            val totalResult = totalDeferred.await()
            val dailyResult = dailyDeferred.await()
            val hourlyResult = hourlyDeferred.await()
            val channelsResult = channelsDeferred.await()
            val apiKeysResult = apiKeysDeferred.await()
            val apiKeyStatsResult = apiKeyStatsDeferred.await()

            _uiState.value = buildHomeRefreshState(
                previous = previous,
                todayResult = todayResult,
                totalResult = totalResult,
                dailyResult = dailyResult,
                hourlyResult = hourlyResult,
                channelsResult = channelsResult,
                apiKeysResult = apiKeysResult,
                apiKeyStatsResult = apiKeyStatsResult,
            )
        }
    }
}

internal fun buildHomeRefreshState(
    previous: HomeUiState,
    todayResult: AppResult<StatsDaily>,
    totalResult: AppResult<StatsTotal>,
    dailyResult: AppResult<List<StatsDaily>>,
    hourlyResult: AppResult<List<StatsHourly>>,
    channelsResult: AppResult<List<Channel>>,
    apiKeysResult: AppResult<List<ApiKeyItem>>,
    apiKeyStatsResult: AppResult<List<StatsApiKeyEntry>>,
): HomeUiState {
    return when (totalResult) {
        is AppResult.Success -> previous.copy(
            loading = false,
            today = todayResult.dataOrPreviousNullable(previous.today),
            total = totalResult.data,
            daily = dailyResult.dataOrPrevious(previous.daily),
            hourly = hourlyResult.dataOrPrevious(previous.hourly),
            channels = channelsResult.dataOrPrevious(previous.channels),
            apiKeys = apiKeysResult.dataOrPrevious(previous.apiKeys),
            apiKeyStats = apiKeyStatsResult.dataOrPrevious(previous.apiKeyStats),
            todayError = todayResult.errorMessageOrNull(),
            dailyError = dailyResult.errorMessageOrNull(),
            hourlyError = hourlyResult.errorMessageOrNull(),
            channelListError = channelsResult.errorMessageOrNull(),
            apiKeyListError = apiKeysResult.errorMessageOrNull(),
            apiKeyStatsError = apiKeyStatsResult.errorMessageOrNull(),
            error = null,
        )
        is AppResult.Error -> previous.copy(
            loading = false,
            today = todayResult.dataOrPreviousNullable(previous.today),
            daily = dailyResult.dataOrPrevious(previous.daily),
            hourly = hourlyResult.dataOrPrevious(previous.hourly),
            channels = channelsResult.dataOrPrevious(previous.channels),
            apiKeys = apiKeysResult.dataOrPrevious(previous.apiKeys),
            apiKeyStats = apiKeyStatsResult.dataOrPrevious(previous.apiKeyStats),
            error = totalResult.message,
            todayError = todayResult.errorMessageOrNull(),
            dailyError = dailyResult.errorMessageOrNull(),
            hourlyError = hourlyResult.errorMessageOrNull(),
            channelListError = channelsResult.errorMessageOrNull(),
            apiKeyListError = apiKeysResult.errorMessageOrNull(),
            apiKeyStatsError = apiKeyStatsResult.errorMessageOrNull(),
        )
    }
}

internal fun HomeUiState.partialErrors(): List<String> = listOfNotNull(
    todayError,
    dailyError,
    hourlyError,
    channelListError,
    apiKeyListError,
    apiKeyStatsError,
).distinct()

private fun <T> AppResult<T>.dataOrPrevious(previous: T): T = when (this) {
    is AppResult.Success -> data
    is AppResult.Error -> previous
}

private fun <T> AppResult<T>.dataOrPreviousNullable(previous: T?): T? = when (this) {
    is AppResult.Success -> data
    is AppResult.Error -> previous
}

private fun AppResult<*>.errorMessageOrNull(): String? = when (this) {
    is AppResult.Success -> null
    is AppResult.Error -> message
}
