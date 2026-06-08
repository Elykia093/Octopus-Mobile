package com.elykia.octopus.core.data.repository

import com.elykia.octopus.core.common.AppResult
import com.elykia.octopus.core.common.DispatchersProvider
import com.elykia.octopus.core.data.model.LlmChannel
import com.elykia.octopus.core.data.model.LlmInfo
import com.elykia.octopus.core.data.remote.ModelApiService
import com.elykia.octopus.core.data.remote.NetworkExecutor
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModelRepository @Inject constructor(
    private val apiService: ModelApiService,
    private val executor: NetworkExecutor,
    private val dispatchers: DispatchersProvider,
) {
    suspend fun models(): AppResult<List<LlmInfo>> = withContext(dispatchers.io) {
        executor.execute { apiService.models() }
    }

    suspend fun modelChannels(): AppResult<List<LlmChannel>> = withContext(dispatchers.io) {
        executor.execute { apiService.modelChannels() }
    }

    suspend fun createModel(model: LlmInfo): AppResult<LlmInfo> = withContext(dispatchers.io) {
        executor.execute { apiService.createModel(model) }
    }

    suspend fun updateModel(model: LlmInfo): AppResult<LlmInfo> = withContext(dispatchers.io) {
        executor.execute { apiService.updateModel(model) }
    }

    suspend fun deleteModel(name: String): AppResult<String?> = withContext(dispatchers.io) {
        executor.executeNullable { apiService.deleteModel(mapOf("name" to name)) }
    }

    suspend fun refreshModelPrice(): AppResult<String?> = withContext(dispatchers.io) {
        executor.executeNullable { apiService.updateModelPrice() }
    }

    suspend fun modelLastUpdateTime(): AppResult<String> = withContext(dispatchers.io) {
        executor.execute { apiService.modelLastUpdateTime() }
    }
}
