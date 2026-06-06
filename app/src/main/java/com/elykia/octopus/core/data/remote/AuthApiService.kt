package com.elykia.octopus.core.data.remote

import com.elykia.octopus.core.data.model.ApiEnvelope
import com.elykia.octopus.core.data.model.UserLoginRequest
import com.elykia.octopus.core.data.model.UserLoginResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApiService {
    @POST("/api/v1/user/login")
    suspend fun login(@Body request: UserLoginRequest): ApiEnvelope<UserLoginResponse>

    @GET("/api/v1/user/status")
    suspend fun status(): ApiEnvelope<String>
}
