package com.elykia.octopus.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LlmInfo(
    val name: String = "",
    val input: Double = 0.0,
    val output: Double = 0.0,
    @SerialName("cache_read") val cacheRead: Double = 0.0,
    @SerialName("cache_write") val cacheWrite: Double = 0.0,
)

@Serializable
data class LlmChannel(
    val name: String = "",
    val enabled: Boolean = false,
    @SerialName("channel_id") val channelId: Int = 0,
    @SerialName("channel_name") val channelName: String = "",
    @SerialName("site_id") val siteId: Int? = null,
    @SerialName("site_account_id") val siteAccountId: Int? = null,
    @SerialName("site_group_key") val siteGroupKey: String = "",
    @SerialName("site_group_name") val siteGroupName: String = "",
    @SerialName("site_name") val siteName: String = "",
    @SerialName("site_account_name") val siteAccountName: String = "",
    @SerialName("endpoint_type") val endpointType: String = "",
)
