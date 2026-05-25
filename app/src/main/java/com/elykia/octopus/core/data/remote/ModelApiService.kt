package com.elykia.octopus.core.data.remote

import com.elykia.octopus.core.data.model.ApiResponse
import com.elykia.octopus.core.data.model.LlmChannel
import com.elykia.octopus.core.data.model.LlmInfo
import kotlinx.serialization.json.JsonObject
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ModelApiService {
    @GET("model/list")
    suspend fun getModels(): ApiResponse<List<LlmInfo>>

    @GET("model/channel")
    suspend fun getModelChannels(): ApiResponse<List<LlmChannel>>

    @POST("model/update-price")
    suspend fun updatePrice(@Body body: JsonObject): ApiResponse<Unit?>

    @GET("model/last-update-time")
    suspend fun getLastUpdateTime(): ApiResponse<String>
}
