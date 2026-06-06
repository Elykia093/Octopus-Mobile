package com.elykia.octopus.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LlmInfo(
    val name: String,
    val input: Double = 0.0,
    val output: Double = 0.0,
    @SerialName("cache_read") val cacheRead: Double = 0.0,
    @SerialName("cache_write") val cacheWrite: Double = 0.0,
)

@Serializable
data class LlmChannel(
    val name: String,
    val enabled: Boolean,
    @SerialName("channel_id") val channelId: Int,
    @SerialName("channel_name") val channelName: String,
)
