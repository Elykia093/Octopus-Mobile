package com.elykia.octopus.core.data.remote

import com.elykia.octopus.core.data.model.ApiEnvelope
import com.elykia.octopus.core.data.model.LogPageResponse
import com.elykia.octopus.core.data.model.RelayLog
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface LogApiService {
    @GET("/api/v1/log/list")
    suspend fun logs(
        @Query("page") page: Int,
        @Query("page_size") pageSize: Int,
        @Query("include_content") includeContent: Boolean = false,
        @Query("with_total") withTotal: Boolean = false,
        @Query("pagination") pagination: String = "page",
    ): ApiEnvelope<LogPageResponse?>

    @DELETE("/api/v1/log/clear")
    suspend fun clearLogs(): ApiEnvelope<String?>

    @GET("/api/v1/log/{id}")
    suspend fun logDetail(@Path("id") id: Long): ApiEnvelope<RelayLog>
}
