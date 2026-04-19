package com.elykia.octopus.core.data.remote

import com.elykia.octopus.core.data.model.ApiResponse
import com.elykia.octopus.core.data.model.DashboardData
import com.elykia.octopus.core.data.model.DashboardRankings
import retrofit2.http.GET

interface DashboardApiService {

    @GET("api/v1/stats/today")
    suspend fun getStatsToday(): ApiResponse<com.elykia.octopus.core.data.model.StatsDaily>

    @GET("api/v1/stats/total")
    suspend fun getStatsTotal(): ApiResponse<com.elykia.octopus.core.data.model.StatsTotal>

    @GET("api/v1/stats/daily")
    suspend fun getStatsDailyTrend(): ApiResponse<List<com.elykia.octopus.core.data.model.TrendEntry>>

    @GET("api/v1/stats/hourly")
    suspend fun getStatsHourlyTrend(): ApiResponse<List<com.elykia.octopus.core.data.model.TrendEntry>>

    @GET("api/v1/user/ranking")
    suspend fun getDashboardRankings(): ApiResponse<DashboardRankings>

    // For API key mode
    @GET("api/v1/stats/apikey")
    suspend fun getApiKeyDashboardStats(): ApiResponse<DashboardData>
}
