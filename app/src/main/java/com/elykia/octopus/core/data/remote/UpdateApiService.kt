package com.elykia.octopus.core.data.remote

import com.elykia.octopus.core.data.model.ApiEnvelope
import com.elykia.octopus.core.data.model.LatestInfo
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface UpdateApiService {
    @GET("/api/v1/update")
    suspend fun latestUpdate(): ApiEnvelope<LatestInfo>

    @GET("/api/v1/update/now-version")
    suspend fun currentVersion(): ApiEnvelope<String>

    @POST("/api/v1/update")
    suspend fun triggerUpdate(@Body body: Map<String, String> = emptyMap()): ApiEnvelope<String>
}
