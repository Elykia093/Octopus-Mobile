package com.elykia.octopus.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiKeyItem(
    val id: Long = 0L,
    val name: String = "",
    val status: Int = 1, // 1 = enabled, 2 = disabled
    @SerialName("expire_at") val expireAt: Long = 0,
    @SerialName("created_at") val createdAt: Long = 0,
    val models: String = "",
    @SerialName("max_quota") val maxQuota: Long = 0,
    @SerialName("used_quota") val usedQuota: Long = 0,
)

@Serializable
data class ApiKeyPageResponse(
    val list: List<ApiKeyItem> = emptyList(),
    val total: Long = 0L
)
