package com.elykia.octopus.core.data.repository

import com.elykia.octopus.core.common.AppResult
import com.elykia.octopus.core.common.DispatchersProvider
import com.elykia.octopus.core.data.model.LatestInfo
import com.elykia.octopus.core.data.remote.NetworkExecutor
import com.elykia.octopus.core.data.remote.UpdateApiService
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateRepository @Inject constructor(
    private val apiService: UpdateApiService,
    private val executor: NetworkExecutor,
    private val dispatchers: DispatchersProvider,
) {
    suspend fun latestInfo(): AppResult<LatestInfo> = withContext(dispatchers.io) {
        executor.execute { apiService.latestUpdate() }
    }

    suspend fun currentVersion(): AppResult<String> = withContext(dispatchers.io) {
        executor.execute { apiService.currentVersion() }
    }

    suspend fun triggerUpdate(): AppResult<String> = withContext(dispatchers.io) {
        executor.execute { apiService.triggerUpdate() }
    }
}
