package com.elykia.octopus.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val code: Int? = null,
    val success: Boolean = false,
    val message: String = "",
    val data: T? = null
) {
    val isSuccessful: Boolean
        get() = success || code in 200..299
}

@Serializable
data class LoginRequest(
    val username: String,
    val password: String? = null, // Only for admin login
    val expire: Int? = null, // Optional for admin token generation
)

@Serializable
data class LoginResponse(
    val token: String = "",
)

@Serializable
data class UserProfile(
    val id: Long = 0L,
    val username: String = "",
    @SerialName("display_name") val displayName: String = "",
    val role: Int = 0,
    val status: Int = 0,
    val email: String = "",
    @SerialName("access_token") val accessToken: String = "",
    val quota: Long = 0L,
    @SerialName("used_quota") val usedQuota: Long = 0L,
    @SerialName("request_count") val requestCount: Int = 0,
)
