package com.elykia.octopus.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Channel(
    val id: Long,
    val name: String,
    val type: Int,
    @SerialName("base_url") val baseUrl: String,
    val models: String,
    val status: Int = 1, // 1 = enabled, 2 = disabled
    val weight: Int = 1,
    @SerialName("used_quota") val usedQuota: Long = 0,
    @SerialName("response_time") val responseTime: Long = 0,
)

@Serializable
data class ChannelPageResponse(
    val list: List<Channel>,
    val total: Long
)
