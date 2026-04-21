package com.elykia.octopus.core.data.remote

import com.elykia.octopus.core.data.model.ApiResponse
import com.elykia.octopus.core.data.model.ApiKeyItem
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiKeyApiService {
    @GET("apikey/list")
    suspend fun getApiKeys(): ApiResponse<List<ApiKeyItem>>
}
