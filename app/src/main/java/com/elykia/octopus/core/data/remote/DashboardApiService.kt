package com.elykia.octopus.core.data.remote

import com.elykia.octopus.core.data.model.ApiResponse
import com.elykia.octopus.core.data.model.StatsMetrics
import com.elykia.octopus.core.data.model.StatsDaily
import com.elykia.octopus.core.data.model.StatsHourly
import retrofit2.http.GET

interface DashboardApiService {
    @GET("stats/today")
    suspend fun getTodayStats(): ApiResponse<StatsMetrics>

    @GET("stats/total")
    suspend fun getTotalStats(): ApiResponse<StatsMetrics>

    @GET("stats/daily")
    suspend fun getDailyStats(): ApiResponse<List<StatsDaily>>

    @GET("stats/hourly")
    suspend fun getHourlyStats(): ApiResponse<List<StatsHourly>>
}
