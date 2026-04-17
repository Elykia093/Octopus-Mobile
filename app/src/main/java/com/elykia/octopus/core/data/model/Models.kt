package com.elykia.octopus.core.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ServerConfig(
    val baseUrl: String = "",
    val language: String = "system",
    val themeMode: Int = 0, // 0 = System, 1 = Light, 2 = Dark
)

@Serializable
data class AuthState(
    val token: String = "",
    val isApiKeyMode: Boolean = false,
)
