package com.elykia.octopus.core.data.remote

import com.elykia.octopus.core.data.model.ApiResponse
import com.elykia.octopus.core.data.model.ApiKeyItem
import com.elykia.octopus.core.data.model.ApiKeyPageResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiKeyApiService {
    @GET("api/v1/apikey/")
    suspend fun getApiKeys(
        @Query("p") page: Int,
        @Query("page_size") pageSize: Int = 20,
    ): ApiResponse<ApiKeyPageResponse>
}
