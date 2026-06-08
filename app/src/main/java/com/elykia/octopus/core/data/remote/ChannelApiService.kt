package com.elykia.octopus.core.data.remote

import com.elykia.octopus.core.data.model.ApiEnvelope
import com.elykia.octopus.core.data.model.Channel
import com.elykia.octopus.core.data.model.ChannelEnableRequest
import com.elykia.octopus.core.data.model.ChannelFetchModelRequest
import com.elykia.octopus.core.data.model.ChannelUpdateRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ChannelApiService {
    @GET("/api/v1/channel/list")
    suspend fun channels(): ApiEnvelope<List<Channel>>

    @POST("/api/v1/channel/create")
    suspend fun createChannel(@Body request: Channel): ApiEnvelope<Channel>

    @POST("/api/v1/channel/update")
    suspend fun updateChannel(@Body request: ChannelUpdateRequest): ApiEnvelope<Channel>

    @POST("/api/v1/channel/fetch-model")
    suspend fun fetchChannelModels(@Body request: ChannelFetchModelRequest): ApiEnvelope<List<String>>

    @POST("/api/v1/channel/sync")
    suspend fun syncChannelModels(@Body body: Map<String, String> = emptyMap()): ApiEnvelope<String?>

    @GET("/api/v1/channel/last-sync-time")
    suspend fun lastSyncTime(): ApiEnvelope<String>

    @POST("/api/v1/channel/enable")
    suspend fun enableChannel(@Body request: ChannelEnableRequest): ApiEnvelope<String?>

    @DELETE("/api/v1/channel/delete/{id}")
    suspend fun deleteChannel(@Path("id") id: Int): ApiEnvelope<String?>
}
