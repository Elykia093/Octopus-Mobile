package com.elykia.octopus.core.data.remote

import com.elykia.octopus.core.data.model.ApiResponse
import com.elykia.octopus.core.data.model.LogItem
import retrofit2.http.GET
import retrofit2.http.Query

interface LogApiService {
    @GET("log/list")
    suspend fun getLogs(
        @Query("page") page: Int,
        @Query("page_size") pageSize: Int = 20,
    ): ApiResponse<List<LogItem>>
}
