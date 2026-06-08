package com.elykia.octopus.core.data.remote

import com.elykia.octopus.core.data.model.ApiEnvelope
import com.elykia.octopus.core.data.model.LlmChannel
import com.elykia.octopus.core.data.model.LlmInfo
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ModelApiService {
    @GET("/api/v1/model/list")
    suspend fun models(): ApiEnvelope<List<LlmInfo>>

    @GET("/api/v1/model/channel")
    suspend fun modelChannels(): ApiEnvelope<List<LlmChannel>>

    @POST("/api/v1/model/create")
    suspend fun createModel(@Body body: LlmInfo): ApiEnvelope<LlmInfo>

    @POST("/api/v1/model/update")
    suspend fun updateModel(@Body body: LlmInfo): ApiEnvelope<LlmInfo>

    @POST("/api/v1/model/delete")
    suspend fun deleteModel(@Body body: Map<String, String>): ApiEnvelope<String?>

    @POST("/api/v1/model/update-price")
    suspend fun updateModelPrice(@Body body: Map<String, String> = emptyMap()): ApiEnvelope<String?>

    @GET("/api/v1/model/last-update-time")
    suspend fun modelLastUpdateTime(): ApiEnvelope<String>
}
