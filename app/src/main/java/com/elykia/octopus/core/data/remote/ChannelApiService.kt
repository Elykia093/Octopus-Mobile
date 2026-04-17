package com.elykia.octopus.core.data.remote

import com.elykia.octopus.core.data.model.ApiResponse
import com.elykia.octopus.core.data.model.Channel
import com.elykia.octopus.core.data.model.ChannelPageResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ChannelApiService {
    @GET("api/v1/channel/")
    suspend fun getChannels(
        @Query("p") page: Int,
        @Query("page_size") pageSize: Int = 20,
    ): ApiResponse<ChannelPageResponse>

    // Temporary method signatures for future CRUD operations
    // @POST("api/v1/channel/")
    // suspend fun addChannel(@Body channel: Channel): ApiResponse<Unit>
}
