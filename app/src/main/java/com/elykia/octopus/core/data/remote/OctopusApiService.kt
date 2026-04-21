package com.elykia.octopus.core.data.remote

import com.elykia.octopus.core.data.model.ApiResponse
import com.elykia.octopus.core.data.model.LoginRequest
import com.elykia.octopus.core.data.model.UserProfile
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface OctopusApiService {

    @POST("api/user/login")
    suspend fun loginUser(@Body request: LoginRequest): ApiResponse<UserProfile>

    @GET("api/user/token")
    suspend fun generateAccessToken(): ApiResponse<String>

    @GET("api/user/self")
    suspend fun getSelf(): ApiResponse<UserProfile>
}
