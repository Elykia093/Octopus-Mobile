package com.elykia.octopus.core.data.repository

import com.elykia.octopus.core.common.AppResult
import com.elykia.octopus.core.common.DispatchersProvider
import com.elykia.octopus.core.data.model.ApiKeyItem
import com.elykia.octopus.core.data.model.ApiKeyMutationRequest
import com.elykia.octopus.core.data.remote.ApiKeyApiService
import com.elykia.octopus.core.data.remote.NetworkExecutor
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiKeyRepository @Inject constructor(
    private val apiService: ApiKeyApiService,
    private val executor: NetworkExecutor,
    private val dispatchers: DispatchersProvider,
) {
    suspend fun apiKeys(): AppResult<List<ApiKeyItem>> = withContext(dispatchers.io) {
        when (val result = executor.execute { apiService.apiKeys() }) {
            is AppResult.Success -> AppResult.Success(result.data.map { it.withHiddenApiKey() })
            is AppResult.Error -> result
        }
    }

    suspend fun createApiKey(request: ApiKeyMutationRequest): AppResult<ApiKeyItem> = withContext(dispatchers.io) {
        executor.execute { apiService.createApiKey(request) }
    }

    suspend fun updateApiKey(item: ApiKeyItem): AppResult<ApiKeyItem> = withContext(dispatchers.io) {
        executor.execute {
            apiService.updateApiKey(
                ApiKeyMutationRequest(
                    id = item.id,
                    name = item.name,
                    apiKey = item.apiKey.takeIf { it.isNotBlank() },
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

private fun ApiKeyItem.withHiddenApiKey(): ApiKeyItem = copy(apiKey = "")
