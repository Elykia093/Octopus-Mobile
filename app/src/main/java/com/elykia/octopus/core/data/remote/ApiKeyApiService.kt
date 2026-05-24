package com.elykia.octopus.core.data.remote

import com.elykia.octopus.core.data.model.ApiResponse
import com.elykia.octopus.core.data.model.ApiKeyItem
import com.elykia.octopus.core.data.model.ApiKeyStatsResponse
import retrofit2.http.GET
import retrofit2.http.Header

interface ApiKeyApiService {
    @GET("apikey/login")
    suspend fun loginApiKey(
        @Header("Authorization") authorization: String? = null,
    ): ApiResponse<Unit?>

    @GET("apikey/stats")
    suspend fun getApiKeyStats(): ApiResponse<ApiKeyStatsResponse>

    @GET("apikey/list")
    suspend fun getApiKeys(): ApiResponse<List<ApiKeyItem>>
}
