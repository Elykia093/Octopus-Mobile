package com.elykia.octopus.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LogItem(
    val id: Long = 0L,
    val type: Int = 0,
    @SerialName("created_at") val createdAt: Long = 0L,
    val content: String = "",
    @SerialName("model_name") val modelName: String = "",
    @SerialName("prompt_tokens") val promptTokens: Int = 0,
    @SerialName("completion_tokens") val completionTokens: Int = 0,
    @SerialName("elapsed_time") val elapsedTime: Long = 0L,
    @SerialName("channel") val channelId: Int = 0,
    @SerialName("token_name") val tokenName: String = "",
    val username: String = "",
)
