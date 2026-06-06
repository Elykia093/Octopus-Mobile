package com.elykia.octopus.core.data.remote

import com.elykia.octopus.core.data.model.ApiEnvelope
import com.elykia.octopus.core.data.model.StatsApiKeyEntry
import com.elykia.octopus.core.data.model.StatsDaily
import com.elykia.octopus.core.data.model.StatsHourly
import com.elykia.octopus.core.data.model.StatsTotal
import retrofit2.http.GET

interface StatsApiService {
    @GET("/api/v1/stats/total")
    suspend fun statsTotal(): ApiEnvelope<StatsTotal>

    @GET("/api/v1/stats/today")
    suspend fun statsToday(): ApiEnvelope<StatsDaily>

    @GET("/api/v1/stats/daily")
    suspend fun statsDaily(): ApiEnvelope<List<StatsDaily>>

    @GET("/api/v1/stats/hourly")
    suspend fun statsHourly(): ApiEnvelope<List<StatsHourly>>

    @GET("/api/v1/stats/apikey")
    suspend fun statsApiKey(): ApiEnvelope<List<StatsApiKeyEntry>>
}
