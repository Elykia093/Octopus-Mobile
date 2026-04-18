package com.elykia.octopus.core.data.remote

import com.elykia.octopus.core.data.model.ApiResponse
import com.elykia.octopus.core.data.model.DashboardData
import com.elykia.octopus.core.data.model.DashboardRankings
import retrofit2.http.GET

interface DashboardApiService {

    @GET("api/user/stats")
    suspend fun getDashboardStats(): ApiResponse<DashboardData>

    @GET("api/user/ranking")
    suspend fun getDashboardRankings(): ApiResponse<DashboardRankings>

    // For API key mode
    @GET("api/apikey/stats")
    suspend fun getApiKeyDashboardStats(): ApiResponse<DashboardData>
}
