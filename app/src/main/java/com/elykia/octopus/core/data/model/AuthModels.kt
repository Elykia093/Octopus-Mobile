package com.elykia.octopus.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserLoginRequest(
    val username: String,
    val password: String,
    val expire: Int,
)

@Serializable
data class UserLoginResponse(
    val token: String,
    @SerialName("expire_at") val expireAt: String,
)

@Serializable
data class AuthState(
    val token: String = "",
    val expireAt: String? = null,
    val username: String = "",
    val apiKeyMode: Boolean = false,
    val serverUrl: String = "",
)
