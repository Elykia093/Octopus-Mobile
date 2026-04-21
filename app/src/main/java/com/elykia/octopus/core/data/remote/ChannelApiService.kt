package com.elykia.octopus.core.data.remote

import com.elykia.octopus.core.data.model.ApiResponse
import com.elykia.octopus.core.data.model.Channel
import retrofit2.http.GET
import retrofit2.http.Query

interface ChannelApiService {
    @GET("api/channel/")
    suspend fun getChannels(
        @Query("p") page: Int,
    ): ApiResponse<List<Channel>>
}
