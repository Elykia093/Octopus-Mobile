package com.elykia.octopus.core.data.remote

import com.elykia.octopus.core.data.model.ApiEnvelope
import com.elykia.octopus.core.data.model.Group
import com.elykia.octopus.core.data.model.GroupHealthGroupView
import com.elykia.octopus.core.data.model.GroupUpdateRequest
import com.elykia.octopus.core.data.model.RunGroupHealthAccepted
import com.elykia.octopus.core.data.model.RunGroupHealthRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface GroupApiService {
    @GET("/api/v1/group/list")
    suspend fun groups(): ApiEnvelope<List<Group>>

    @POST("/api/v1/group/create")
    suspend fun createGroup(@Body request: Group): ApiEnvelope<Group>

    @POST("/api/v1/group/update")
    suspend fun updateGroup(@Body request: GroupUpdateRequest): ApiEnvelope<Group>

    @DELETE("/api/v1/group/delete/{id}")
    suspend fun deleteGroup(@Path("id") id: Int): ApiEnvelope<String?>

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
