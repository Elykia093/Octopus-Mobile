package com.elykia.octopus.core.data.remote

import com.elykia.octopus.core.data.model.ApiResponse
import com.elykia.octopus.core.data.model.DashboardEntry
import retrofit2.http.GET

interface DashboardApiService {
    @GET("api/user/dashboard")
    suspend fun getUserDashboard(): ApiResponse<List<DashboardEntry>>
}
