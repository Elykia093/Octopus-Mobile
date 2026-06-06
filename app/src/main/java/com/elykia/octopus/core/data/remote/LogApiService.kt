package com.elykia.octopus.core.data.remote

import com.elykia.octopus.core.data.model.ApiEnvelope
import com.elykia.octopus.core.data.model.RelayLog
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Query

interface LogApiService {
    @GET("/api/v1/log/list")
    suspend fun logs(
        @Query("page") page: Int,
        @Query("page_size") pageSize: Int,
    ): ApiEnvelope<List<RelayLog>?>

    @DELETE("/api/v1/log/clear")
    suspend fun clearLogs(): ApiEnvelope<String?>
}
