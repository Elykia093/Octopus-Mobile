package com.elykia.octopus.core.data.repository

import com.elykia.octopus.core.common.AppResult
import com.elykia.octopus.core.common.DispatchersProvider
import com.elykia.octopus.core.data.model.Group
import com.elykia.octopus.core.data.model.GroupAutoGroupConfig
import com.elykia.octopus.core.data.model.GroupAutoGroupConfigUpdateRequest
import com.elykia.octopus.core.data.model.GroupAutoGroupRunRequest
import com.elykia.octopus.core.data.model.GroupHealthGroupView
import com.elykia.octopus.core.data.model.GroupPinRequest
import com.elykia.octopus.core.data.model.GroupPreset
import com.elykia.octopus.core.data.model.GroupPresetNameRequest
import com.elykia.octopus.core.data.model.GroupPresetUpdateRequest
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

    suspend fun pinGroup(groupId: Int, pinned: Boolean): AppResult<String?> = withContext(dispatchers.io) {
        executor.executeNullable { apiService.pinGroup(groupId, GroupPinRequest(pinned)) }
    }

    suspend fun groupPresets(groupId: Int): AppResult<List<GroupPreset>> = withContext(dispatchers.io) {
        when (val result = executor.executeNullable { apiService.groupPresets(groupId) }) {
            is AppResult.Success -> AppResult.Success(result.data.orEmpty())
            is AppResult.Error -> result
        }
    }

    suspend fun createGroupPreset(groupId: Int, name: String): AppResult<GroupPreset> = withContext(dispatchers.io) {
        executor.execute { apiService.createGroupPreset(groupId, GroupPresetNameRequest(name)) }
    }

    suspend fun createBlankGroupPreset(groupId: Int, name: String): AppResult<GroupPreset> = withContext(dispatchers.io) {
        executor.execute { apiService.createBlankGroupPreset(groupId, GroupPresetNameRequest(name)) }
    }

    suspend fun cloneGroupPreset(presetId: Int, name: String): AppResult<GroupPreset> = withContext(dispatchers.io) {
        executor.execute { apiService.cloneGroupPreset(presetId, GroupPresetNameRequest(name)) }
    }

    suspend fun activateGroupPreset(presetId: Int): AppResult<String?> = withContext(dispatchers.io) {
        executor.executeNullable { apiService.activateGroupPreset(presetId) }
    }

    suspend fun updateGroupPreset(presetId: Int, request: GroupPresetUpdateRequest): AppResult<GroupPreset> = withContext(dispatchers.io) {
        executor.execute { apiService.updateGroupPreset(presetId, request) }
    }

    suspend fun deleteGroupPreset(presetId: Int): AppResult<String?> = withContext(dispatchers.io) {
        executor.executeNullable { apiService.deleteGroupPreset(presetId) }
    }

    suspend fun groupAutoGroupConfig(): AppResult<GroupAutoGroupConfig> = withContext(dispatchers.io) {
        executor.execute { apiService.groupAutoGroupConfig() }
    }

    suspend fun updateGroupAutoGroupConfig(request: GroupAutoGroupConfigUpdateRequest): AppResult<GroupAutoGroupConfig> = withContext(dispatchers.io) {
        executor.execute { apiService.updateGroupAutoGroupConfig(request) }
    }

    suspend fun runGroupAutoGroup(channelIds: List<Int> = emptyList()): AppResult<String?> = withContext(dispatchers.io) {
        executor.executeNullable { apiService.runGroupAutoGroup(GroupAutoGroupRunRequest(channelIds)) }
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
