package com.elykia.octopus.core.data.repository

import com.elykia.octopus.core.common.AppResult
import com.elykia.octopus.core.common.DispatchersProvider
import com.elykia.octopus.core.data.model.StatsApiKeyEntry
import com.elykia.octopus.core.data.model.StatsDaily
import com.elykia.octopus.core.data.model.StatsHourly
import com.elykia.octopus.core.data.model.StatsTotal
import com.elykia.octopus.core.data.remote.NetworkExecutor
import com.elykia.octopus.core.data.remote.StatsApiService
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DashboardRepository @Inject constructor(
    private val apiService: StatsApiService,
    private val executor: NetworkExecutor,
    private val dispatchers: DispatchersProvider,
) {
    suspend fun totalStats(): AppResult<StatsTotal> = withContext(dispatchers.io) {
        executor.execute { apiService.statsTotal() }
    }

    suspend fun todayStats(): AppResult<StatsDaily> = withContext(dispatchers.io) {
        executor.execute { apiService.statsToday() }
    }

    suspend fun dailyStats(): AppResult<List<StatsDaily>> = withContext(dispatchers.io) {
        executor.execute { apiService.statsDaily() }
    }

    suspend fun hourlyStats(): AppResult<List<StatsHourly>> = withContext(dispatchers.io) {
        executor.execute { apiService.statsHourly() }
    }

    suspend fun apiKeyStats(): AppResult<List<StatsApiKeyEntry>> = withContext(dispatchers.io) {
        executor.execute { apiService.statsApiKey() }
    }
}
