package com.elykia.octopus.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiKeyItem(
    val id: Long = 0L,
    val name: String = "",
    val status: Int = 1,
    @SerialName("created_time") val createdTime: Long = 0L,
    @SerialName("accessed_time") val accessedTime: Long = 0L,
    @SerialName("expired_time") val expiredTime: Long = -1L,
    @SerialName("remain_quota") val remainQuota: Long = 0L,
    @SerialName("used_quota") val usedQuota: Long = 0L,
    @SerialName("unlimited_quota") val unlimitedQuota: Boolean = false,
    val models: String? = null,
)
