package com.elykia.octopus.core.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SettingItem(
    val key: String,
    val value: String,
)

@Serializable
data class ServerConfig(
    val baseUrl: String = "",
    val language: String = "system",
    val themeMode: Int = 0,
)
