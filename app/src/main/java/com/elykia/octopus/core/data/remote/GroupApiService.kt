package com.elykia.octopus.core.data.remote

import com.elykia.octopus.core.data.model.ApiResponse
import com.elykia.octopus.core.data.model.Group
import com.elykia.octopus.core.data.model.GroupPinRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface GroupApiService {
    @GET("group/list")
    suspend fun getGroups(): ApiResponse<List<Group>>

    @POST("group/pin/{id}")
    suspend fun setPinned(
        @Path("id") id: Int,
        @Body request: GroupPinRequest,
    ): ApiResponse<String>
}
