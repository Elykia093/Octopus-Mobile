package com.elykia.octopus.core.data.repository

import com.elykia.octopus.core.common.AppResult
import com.elykia.octopus.core.common.DispatchersProvider
import com.elykia.octopus.core.data.model.ApiKeyItem
import com.elykia.octopus.core.data.model.Channel
import com.elykia.octopus.core.data.model.Group
import com.elykia.octopus.core.data.model.LatestInfo
import com.elykia.octopus.core.data.model.LlmChannel
import com.elykia.octopus.core.data.model.LlmInfo
import com.elykia.octopus.core.data.model.RelayLog
import com.elykia.octopus.core.data.model.StatsDaily
import com.elykia.octopus.core.data.model.StatsHourly
import com.elykia.octopus.core.data.model.StatsTotal
import com.elykia.octopus.core.data.remote.NetworkExecutor
import com.elykia.octopus.core.data.remote.OctopusApiService
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DashboardRepository @Inject constructor(
    private val apiService: OctopusApiService,
    private val executor: NetworkExecutor,
    private val dispatchers: DispatchersProvider,
) {
    suspend fun totalStats(): AppResult<StatsTotal> = withContext(dispatchers.io) {
        executor.execute { apiService.statsTotal() }
    }

    suspend fun dailyStats(): AppResult<List<StatsDaily>> = withContext(dispatchers.io) {
        executor.execute { apiService.statsDaily() }
    }

    suspend fun hourlyStats(): AppResult<List<StatsHourly>> = withContext(dispatchers.io) {
        executor.execute { apiService.statsHourly() }
    }

    suspend fun channels(): AppResult<List<Channel>> = withContext(dispatchers.io) {
        executor.execute { apiService.channels() }
    }

    suspend fun groups(): AppResult<List<Group>> = withContext(dispatchers.io) {
        executor.execute { apiService.groups() }
    }

    suspend fun models(): AppResult<List<LlmInfo>> = withContext(dispatchers.io) {
        executor.execute { apiService.models() }
    }

    suspend fun modelChannels(): AppResult<List<LlmChannel>> = withContext(dispatchers.io) {
        executor.execute { apiService.modelChannels() }
    }

    suspend fun logs(page: Int = 1, pageSize: Int = 20): AppResult<List<RelayLog>> = withContext(dispatchers.io) {
        when (val result = executor.execute { apiService.logs(page, pageSize) }) {
            is AppResult.Success -> AppResult.Success(result.data ?: emptyList())
            is AppResult.Error -> result
        }
    }

    suspend fun clearLogs(): AppResult<String?> = withContext(dispatchers.io) {
        executor.executeUnit { apiService.clearLogs() }
    }

    suspend fun apiKeys(): AppResult<List<ApiKeyItem>> = withContext(dispatchers.io) {
        executor.execute { apiService.apiKeys() }
    }

    suspend fun latestInfo(): AppResult<LatestInfo> = withContext(dispatchers.io) {
        executor.execute { apiService.latestUpdate() }
    }

    suspend fun currentVersion(): AppResult<String> = withContext(dispatchers.io) {
        executor.execute { apiService.currentVersion() }
    }

    suspend fun triggerUpdate(): AppResult<String> = withContext(dispatchers.io) {
        executor.execute { apiService.triggerUpdate() }
    }

    suspend fun refreshModelPrice(): AppResult<String?> = withContext(dispatchers.io) {
        executor.executeUnit { apiService.updateModelPrice() }
    }

    suspend fun modelLastUpdateTime(): AppResult<String> = withContext(dispatchers.io) {
        executor.execute { apiService.modelLastUpdateTime() }
    }

    suspend fun deleteChannel(id: Int): AppResult<String?> = withContext(dispatchers.io) {
        executor.executeUnit { apiService.deleteChannel(id) }
    }

    suspend fun deleteGroup(id: Int): AppResult<String?> = withContext(dispatchers.io) {
        executor.executeUnit { apiService.deleteGroup(id) }
    }
}
