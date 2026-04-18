package com.elykia.octopus.core.data.remote

import com.elykia.octopus.core.data.model.ApiResponse
import com.elykia.octopus.core.data.model.LoginRequest
import com.elykia.octopus.core.data.model.LoginResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface OctopusApiService {

    // Auth endpoints
    @POST("api/v1/user/login")
    suspend fun loginUser(@Body request: LoginRequest): ApiResponse<LoginResponse>

    @GET("api/v1/apikey/login")
    suspend fun loginApiKey(): ApiResponse<LoginResponse> // Token is sent via AuthInterceptor

    // Example dashboard endpoint to verify auth
    @GET("api/v1/user/stats")
    suspend fun getUserStats(): ApiResponse<String> // Placeholder for actual Dashboard stats model
}
