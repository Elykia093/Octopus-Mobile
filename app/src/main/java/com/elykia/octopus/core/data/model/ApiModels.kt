package com.elykia.octopus.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val message: String = "",
    val data: T? = null
)

@Serializable
data class LoginRequest(
    val username: String,
    val password: String? = null, // Only for admin login
    val expireDays: Int? = null, // Optional for admin token generation
)

@Serializable
data class LoginResponse(
    val token: String,
)
