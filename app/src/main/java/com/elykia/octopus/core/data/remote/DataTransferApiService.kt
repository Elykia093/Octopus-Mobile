package com.elykia.octopus.core.data.remote

import com.elykia.octopus.core.data.model.ApiEnvelope
import com.elykia.octopus.core.data.model.ImportResult
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface DataTransferApiService {
    @GET("/api/v1/setting/export")
    suspend fun exportData(
        @Query("include_logs") includeLogs: Boolean,
        @Query("include_stats") includeStats: Boolean,
    ): Response<ResponseBody>

    @Multipart
    @POST("/api/v1/setting/import")
    suspend fun importData(@Part file: MultipartBody.Part): ApiEnvelope<ImportResult>
}
