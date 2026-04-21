package com.elykia.octopus.core.data.remote

import com.elykia.octopus.core.data.model.ApiResponse
import com.elykia.octopus.core.data.model.LogItem
import retrofit2.http.GET
import retrofit2.http.Query

interface LogApiService {
    @GET("api/log/")
    suspend fun getAdminLogs(
        @Query("p") page: Int,
    ): ApiResponse<List<LogItem>>

    @GET("api/log/self/")
    suspend fun getUserLogs(
        @Query("p") page: Int,
    ): ApiResponse<List<LogItem>>
}
