package com.elykia.octopus.core.data.remote

import com.elykia.octopus.core.data.model.ApiEnvelope
import com.elykia.octopus.core.data.model.EmptyRequest
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
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.PUT

interface GroupApiService {
    @GET("/api/v1/group/list")
    suspend fun groups(): ApiEnvelope<List<Group>>

    @POST("/api/v1/group/create")
    suspend fun createGroup(@Body request: Group): ApiEnvelope<Group>

    @POST("/api/v1/group/update")
    suspend fun updateGroup(@Body request: GroupUpdateRequest): ApiEnvelope<Group>

    @DELETE("/api/v1/group/delete/{id}")
    suspend fun deleteGroup(@Path("id") id: Int): ApiEnvelope<String?>

    @POST("/api/v1/group/pin/{groupId}")
    suspend fun pinGroup(
        @Path("groupId") groupId: Int,
        @Body request: GroupPinRequest,
    ): ApiEnvelope<String?>

    @GET("/api/v1/group/preset/list/{groupId}")
    suspend fun groupPresets(@Path("groupId") groupId: Int): ApiEnvelope<List<GroupPreset>?>

    @POST("/api/v1/group/preset/create/{groupId}")
    suspend fun createGroupPreset(
        @Path("groupId") groupId: Int,
        @Body request: GroupPresetNameRequest,
    ): ApiEnvelope<GroupPreset>

    @POST("/api/v1/group/preset/create-blank/{groupId}")
    suspend fun createBlankGroupPreset(
        @Path("groupId") groupId: Int,
        @Body request: GroupPresetNameRequest,
    ): ApiEnvelope<GroupPreset>

    @POST("/api/v1/group/preset/clone/{presetId}")
    suspend fun cloneGroupPreset(
        @Path("presetId") presetId: Int,
        @Body request: GroupPresetNameRequest,
    ): ApiEnvelope<GroupPreset>

    @POST("/api/v1/group/preset/activate/{presetId}")
    suspend fun activateGroupPreset(
        @Path("presetId") presetId: Int,
        @Body request: EmptyRequest = EmptyRequest(),
    ): ApiEnvelope<String?>

    @PUT("/api/v1/group/preset/update/{presetId}")
    suspend fun updateGroupPreset(
        @Path("presetId") presetId: Int,
        @Body request: GroupPresetUpdateRequest,
    ): ApiEnvelope<GroupPreset>

    @DELETE("/api/v1/group/preset/delete/{presetId}")
    suspend fun deleteGroupPreset(@Path("presetId") presetId: Int): ApiEnvelope<String?>

    @GET("/api/v1/group/auto-group/config")
    suspend fun groupAutoGroupConfig(): ApiEnvelope<GroupAutoGroupConfig>

    @PUT("/api/v1/group/auto-group/config")
    suspend fun updateGroupAutoGroupConfig(
        @Body request: GroupAutoGroupConfigUpdateRequest,
    ): ApiEnvelope<GroupAutoGroupConfig>

    @POST("/api/v1/group/auto-group/run")
    suspend fun runGroupAutoGroup(
        @Body request: GroupAutoGroupRunRequest = GroupAutoGroupRunRequest(),
    ): ApiEnvelope<String?>

    @GET("/api/v1/group/health/list")
    suspend fun groupHealthList(): ApiEnvelope<List<GroupHealthGroupView>?>

    @GET("/api/v1/group/health/{groupId}")
    suspend fun groupHealth(@Path("groupId") groupId: Int): ApiEnvelope<GroupHealthGroupView>

    @POST("/api/v1/group/health/{groupId}/run")
    suspend fun runGroupHealth(
        @Path("groupId") groupId: Int,
        @Body request: RunGroupHealthRequest = RunGroupHealthRequest(),
    ): ApiEnvelope<RunGroupHealthAccepted>

    @POST("/api/v1/group/health/run-all")
    suspend fun runAllGroupHealth(
        @Body request: RunGroupHealthRequest = RunGroupHealthRequest(),
    ): ApiEnvelope<RunGroupHealthAccepted>
}
