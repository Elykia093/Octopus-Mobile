package com.elykia.octopus.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiKeyItem(
    val id: Int,
    val name: String,
    @SerialName("api_key") val apiKey: String,
    val enabled: Boolean = true,
    @SerialName("expire_at") val expireAt: Long? = null,
    @SerialName("max_cost") val maxCost: Double? = null,
    @SerialName("supported_models") val supportedModels: String? = null,
)

@Serializable
data class ApiKeyMutationRequest(
    val id: Int = 0,
    val name: String,
    @SerialName("api_key") val apiKey: String? = null,
    val enabled: Boolean = true,
    @SerialName("expire_at") val expireAt: Long = 0,
    @SerialName("max_cost") val maxCost: Double = 0.0,
    @SerialName("supported_models") val supportedModels: String = "",
)

@Serializable
data class ApiKeyStats(
    @SerialName("api_key_id") val apiKeyId: Int,
    @SerialName("input_token") val inputToken: Long = 0,
    @SerialName("output_token") val outputToken: Long = 0,
    @SerialName("input_cost") val inputCost: Double = 0.0,
    @SerialName("output_cost") val outputCost: Double = 0.0,
    @SerialName("wait_time") val waitTime: Long = 0,
    @SerialName("request_success") val requestSuccess: Long = 0,
    @SerialName("request_failed") val requestFailed: Long = 0,
)

@Serializable
data class ApiKeyDashboard(
    val stats: ApiKeyStats,
    val info: ApiKeyItem,
)
