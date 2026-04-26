package com.elykia.octopus.core.data.repository

import com.elykia.octopus.core.common.AppResult
import com.elykia.octopus.core.common.DispatchersProvider
import com.elykia.octopus.core.data.model.ApiKeyItem
import com.elykia.octopus.core.data.model.ApiKeyMutationRequest
import com.elykia.octopus.core.data.model.Channel
import com.elykia.octopus.core.data.model.Group
import com.elykia.octopus.core.data.model.ChannelUpdateRequest
import com.elykia.octopus.core.data.model.ChannelFetchModelRequest
import com.elykia.octopus.core.data.model.GroupUpdateRequest
import com.elykia.octopus.core.data.model.LatestInfo
import com.elykia.octopus.core.data.model.LlmChannel
import com.elykia.octopus.core.data.model.LlmInfo
import com.elykia.octopus.core.data.model.RelayLog
import com.elykia.octopus.core.data.model.ChannelEnableRequest
import com.elykia.octopus.core.data.model.StatsApiKeyEntry
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

    suspend fun channels(): AppResult<List<Channel>> = withContext(dispatchers.io) {
        executor.execute { apiService.channels() }
    }

    suspend fun createChannel(channel: Channel): AppResult<Channel> = withContext(dispatchers.io) {
        executor.execute { apiService.createChannel(channel) }
    }

    suspend fun updateChannel(request: ChannelUpdateRequest): AppResult<Channel> = withContext(dispatchers.io) {
        executor.execute { apiService.updateChannel(request) }
    }

    suspend fun fetchChannelModels(request: ChannelFetchModelRequest): AppResult<List<String>> = withContext(dispatchers.io) {
        executor.execute { apiService.fetchChannelModels(request) }
    }

    suspend fun syncChannelModels(): AppResult<String?> = withContext(dispatchers.io) {
        executor.executeNullable { apiService.syncChannelModels() }
    }

    suspend fun groups(): AppResult<List<Group>> = withContext(dispatchers.io) {
        executor.execute { apiService.groups() }
    }

    suspend fun createGroup(group: Group): AppResult<Group> = withContext(dispatchers.io) {
        executor.execute { apiService.createGroup(group) }
    }

    suspend fun updateGroup(request: GroupUpdateRequest): AppResult<Group> = withContext(dispatchers.io) {
        executor.execute { apiService.updateGroup(request) }
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
        executor.executeNullable { apiService.clearLogs() }
    }

    suspend fun apiKeys(): AppResult<List<ApiKeyItem>> = withContext(dispatchers.io) {
        executor.execute { apiService.apiKeys() }
    }

    suspend fun createApiKey(request: ApiKeyMutationRequest): AppResult<ApiKeyItem> = withContext(dispatchers.io) {
        executor.execute { apiService.createApiKey(request) }
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
        executor.executeNullable { apiService.updateModelPrice() }
    }

    suspend fun modelLastUpdateTime(): AppResult<String> = withContext(dispatchers.io) {
        executor.execute { apiService.modelLastUpdateTime() }
    }

    suspend fun deleteChannel(id: Int): AppResult<String?> = withContext(dispatchers.io) {
        executor.executeNullable { apiService.deleteChannel(id) }
    }

    suspend fun setChannelEnabled(id: Int, enabled: Boolean): AppResult<String?> = withContext(dispatchers.io) {
        executor.executeNullable { apiService.enableChannel(ChannelEnableRequest(id = id, enabled = enabled)) }
    }

    suspend fun deleteGroup(id: Int): AppResult<String?> = withContext(dispatchers.io) {
        executor.executeNullable { apiService.deleteGroup(id) }
    }

    suspend fun updateApiKey(item: ApiKeyItem): AppResult<ApiKeyItem> = withContext(dispatchers.io) {
        executor.execute {
            apiService.updateApiKey(
                ApiKeyMutationRequest(
                    id = item.id,
                    name = item.name,
                    apiKey = item.apiKey,
                    enabled = item.enabled,
                    expireAt = item.expireAt ?: 0,
                    maxCost = item.maxCost ?: 0.0,
                    supportedModels = item.supportedModels.orEmpty(),
                )
            )
        }
    }

    suspend fun deleteApiKey(id: Int): AppResult<String?> = withContext(dispatchers.io) {
        executor.executeNullable { apiService.deleteApiKey(id) }
    }
}
