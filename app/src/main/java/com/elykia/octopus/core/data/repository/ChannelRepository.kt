package com.elykia.octopus.core.data.repository

import com.elykia.octopus.core.common.AppResult
import com.elykia.octopus.core.common.DispatchersProvider
import com.elykia.octopus.core.data.model.Channel
import com.elykia.octopus.core.data.model.ChannelEnableRequest
import com.elykia.octopus.core.data.model.ChannelFetchModelRequest
import com.elykia.octopus.core.data.model.ChannelUpdateRequest
import com.elykia.octopus.core.data.remote.ChannelApiService
import com.elykia.octopus.core.data.remote.NetworkExecutor
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChannelRepository @Inject constructor(
    private val apiService: ChannelApiService,
    private val executor: NetworkExecutor,
    private val dispatchers: DispatchersProvider,
) {
    suspend fun channels(): AppResult<List<Channel>> = withContext(dispatchers.io) {
        when (val result = executor.execute { apiService.channels() }) {
            is AppResult.Success -> AppResult.Success(result.data.map { it.withHiddenKeys() })
            is AppResult.Error -> result
        }
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

    suspend fun deleteChannel(id: Int): AppResult<String?> = withContext(dispatchers.io) {
        executor.executeNullable { apiService.deleteChannel(id) }
    }

    suspend fun setChannelEnabled(id: Int, enabled: Boolean): AppResult<String?> = withContext(dispatchers.io) {
        executor.executeNullable { apiService.enableChannel(ChannelEnableRequest(id = id, enabled = enabled)) }
    }
}

private fun Channel.withHiddenKeys(): Channel = copy(
    keys = keys.map { it.copy(channelKey = "") },
)
