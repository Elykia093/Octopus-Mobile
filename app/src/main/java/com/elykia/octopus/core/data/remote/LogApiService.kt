package com.elykia.octopus.core.data.remote

import com.elykia.octopus.core.data.model.ApiResponse
import com.elykia.octopus.core.data.model.LogPageResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface LogApiService {
    @GET("api/log/")
    suspend fun getLogs(
        @Query("p") page: Int,
        @Query("page_size") pageSize: Int = 20,
    ): ApiResponse<LogPageResponse>
}
