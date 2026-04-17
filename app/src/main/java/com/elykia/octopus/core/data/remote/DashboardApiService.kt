package com.elykia.octopus.core.data.remote

import com.elykia.octopus.core.data.model.ApiResponse
import com.elykia.octopus.core.data.model.DashboardData
import com.elykia.octopus.core.data.model.DashboardRankings
import retrofit2.http.GET

interface DashboardApiService {

    @GET("api/v1/user/stats")
    suspend fun getDashboardStats(): ApiResponse<DashboardData>

    @GET("api/v1/user/ranking")
    suspend fun getDashboardRankings(): ApiResponse<DashboardRankings>

    // 假设未来如果需要 API Key 自己的 Dashboard
    @GET("api/v1/apikey/stats")
    suspend fun getApiKeyDashboardStats(): ApiResponse<DashboardData>
}
