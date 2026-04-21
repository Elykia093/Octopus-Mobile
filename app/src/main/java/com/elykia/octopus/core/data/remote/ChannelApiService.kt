package com.elykia.octopus.core.data.remote

import com.elykia.octopus.core.data.model.ApiResponse
import com.elykia.octopus.core.data.model.Channel
import retrofit2.http.GET

interface ChannelApiService {
    @GET("channel/list")
    suspend fun getChannels(): ApiResponse<List<Channel>>
}
