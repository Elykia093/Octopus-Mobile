package com.elykia.octopus.core.data.remote

import com.elykia.octopus.core.data.model.ApiResponse
import com.elykia.octopus.core.data.model.LoginRequest
import com.elykia.octopus.core.data.model.UserProfile
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface OctopusApiService {

    @POST("user/login")
    suspend fun loginUser(@Body request: LoginRequest): ApiResponse<UserProfile>
}
