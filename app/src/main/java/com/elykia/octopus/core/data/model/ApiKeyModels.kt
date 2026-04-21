package com.elykia.octopus.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiKeyItem(
    val id: Long = 0L,
    val name: String = "",
    @SerialName("api_key") val apiKey: String = "",
    val enabled: Boolean = true,
    @SerialName("expire_at") val expireAt: Long = 0L,
    @SerialName("max_cost") val maxCost: Double = 0.0,
    @SerialName("supported_models") val supportedModels: String = "",
)
