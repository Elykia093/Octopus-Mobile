package com.elykia.octopus.core.data.repository

import com.elykia.octopus.core.common.AppResult
import com.elykia.octopus.core.common.DispatchersProvider
import com.elykia.octopus.core.data.model.Group
import com.elykia.octopus.core.data.model.GroupUpdateRequest
import com.elykia.octopus.core.data.remote.NetworkExecutor
import com.elykia.octopus.core.data.remote.OctopusApiService
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupRepository @Inject constructor(
    private val apiService: OctopusApiService,
    private val executor: NetworkExecutor,
    private val dispatchers: DispatchersProvider,
) {
    suspend fun groups(): AppResult<List<Group>> = withContext(dispatchers.io) {
        executor.execute { apiService.groups() }
    }

    suspend fun createGroup(group: Group): AppResult<Group> = withContext(dispatchers.io) {
        executor.execute { apiService.createGroup(group) }
    }

    suspend fun updateGroup(request: GroupUpdateRequest): AppResult<Group> = withContext(dispatchers.io) {
        executor.execute { apiService.updateGroup(request) }
    }

    suspend fun deleteGroup(id: Int): AppResult<String?> = withContext(dispatchers.io) {
        executor.executeNullable { apiService.deleteGroup(id) }
    }
}
