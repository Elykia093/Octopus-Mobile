package com.elykia.octopus.core.data.repository

import com.elykia.octopus.core.common.AppResult
import com.elykia.octopus.core.common.DispatchersProvider
import com.elykia.octopus.core.data.model.Group
import com.elykia.octopus.core.data.model.GroupHealthGroupView
import com.elykia.octopus.core.data.model.GroupUpdateRequest
import com.elykia.octopus.core.data.model.RunGroupHealthAccepted
import com.elykia.octopus.core.data.model.RunGroupHealthRequest
import com.elykia.octopus.core.data.remote.GroupApiService
import com.elykia.octopus.core.data.remote.NetworkExecutor
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupRepository @Inject constructor(
    private val apiService: GroupApiService,
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

    suspend fun groupHealthList(): AppResult<List<GroupHealthGroupView>> = withContext(dispatchers.io) {
        when (val result = executor.executeNullable { apiService.groupHealthList() }) {
            is AppResult.Success -> AppResult.Success(result.data.orEmpty())
            is AppResult.Error -> result
        }
    }

    suspend fun groupHealth(groupId: Int): AppResult<GroupHealthGroupView> = withContext(dispatchers.io) {
        executor.execute { apiService.groupHealth(groupId) }
    }

    suspend fun runGroupHealth(groupId: Int, probeMode: String? = null): AppResult<RunGroupHealthAccepted> = withContext(dispatchers.io) {
        executor.execute { apiService.runGroupHealth(groupId, RunGroupHealthRequest(probeMode = probeMode)) }
    }

    suspend fun runAllGroupHealth(probeMode: String? = null): AppResult<RunGroupHealthAccepted> = withContext(dispatchers.io) {
        executor.execute { apiService.runAllGroupHealth(RunGroupHealthRequest(probeMode = probeMode)) }
    }
}
