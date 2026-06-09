package com.elykia.octopus.core.data.remote

import com.elykia.octopus.core.data.model.ApiEnvelope
import com.elykia.octopus.core.data.model.LogPageResponse
import com.elykia.octopus.core.data.model.LogStreamToken
import com.elykia.octopus.core.data.model.RelayLog
import okhttp3.ResponseBody
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

interface LogApiService {
    @GET("/api/v1/log/list")
    suspend fun logs(
        @Query("page") page: Int,
        @Query("page_size") pageSize: Int,
        @Query("include_content") includeContent: Boolean = false,
        @Query("with_total") withTotal: Boolean = false,
        @Query("pagination") pagination: String = "page",
        @Query("start_time") startTime: Long? = null,
        @Query("end_time") endTime: Long? = null,
        @Query("channel_ids") channelIds: String? = null,
        @Query("status") status: String? = null,
        @Query("keyword") keyword: String? = null,
        @Query("keyword_scope") keywordScope: String? = null,
        @Query("keyword_mode") keywordMode: String? = null,
    ): ApiEnvelope<LogPageResponse?>

    @DELETE("/api/v1/log/clear")
    suspend fun clearLogs(): ApiEnvelope<String?>

    @GET("/api/v1/log/{id}")
    suspend fun logDetail(@Path("id") id: Long): ApiEnvelope<RelayLog>

    @GET("/api/v1/log/stream-token")
    suspend fun streamToken(): ApiEnvelope<LogStreamToken>

    @Streaming
    @GET("/api/v1/log/stream")
    suspend fun streamLogs(@Query("token") token: String): ResponseBody
}
