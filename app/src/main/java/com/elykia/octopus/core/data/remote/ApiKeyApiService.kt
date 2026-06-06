package com.elykia.octopus.core.data.remote

import com.elykia.octopus.core.data.model.ApiEnvelope
import com.elykia.octopus.core.data.model.ApiKeyItem
import com.elykia.octopus.core.data.model.ApiKeyMutationRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiKeyApiService {
    @GET("/api/v1/apikey/list")
    suspend fun apiKeys(): ApiEnvelope<List<ApiKeyItem>>

    @POST("/api/v1/apikey/create")
    suspend fun createApiKey(@Body request: ApiKeyMutationRequest): ApiEnvelope<ApiKeyItem>

    @POST("/api/v1/apikey/update")
    suspend fun updateApiKey(@Body request: ApiKeyMutationRequest): ApiEnvelope<ApiKeyItem>

    @DELETE("/api/v1/apikey/delete/{id}")
    suspend fun deleteApiKey(@Path("id") id: Int): ApiEnvelope<String?>
}
