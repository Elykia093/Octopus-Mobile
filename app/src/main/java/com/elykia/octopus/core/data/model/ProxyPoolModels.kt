package com.elykia.octopus.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProxyConfiguration(
    val id: Int = 0,
    val name: String = "",
    val url: String = "",
    val enabled: Boolean = true,
    val remark: String = "",
    @SerialName("reference_count") val referenceCount: Int = 0,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)

@Serializable
data class ProxyConfigurationCreateRequest(
    val name: String,
    val url: String,
    val enabled: Boolean = true,
    val remark: String = "",
)

@Serializable
data class ProxyConfigurationUpdateRequest(
    val id: Int,
    val name: String,
    val url: String,
    val enabled: Boolean,
    val remark: String = "",
)

@Serializable
data class ProxyConfigurationReference(
    val type: String,
    @SerialName("site_id") val siteId: Int? = null,
    @SerialName("site_name") val siteName: String? = null,
    @SerialName("site_archived") val siteArchived: Boolean = false,
    @SerialName("site_account_id") val siteAccountId: Int? = null,
    @SerialName("site_account_name") val siteAccountName: String? = null,
    @SerialName("channel_id") val channelId: Int? = null,
    @SerialName("channel_name") val channelName: String? = null,
    val managed: Boolean = false,
)

@Serializable
data class ProxyTestRequest(
    @SerialName("proxy_config_id") val proxyConfigId: Int? = null,
    @SerialName("proxy_url") val proxyUrl: String? = null,
    val url: String? = null,
)

@Serializable
data class ProxyTestResult(
    val success: Boolean = false,
    @SerialName("status_code") val statusCode: Int = 0,
    @SerialName("duration_ms") val durationMs: Long = 0,
    val message: String = "",
)

object ProxyConfigurationReferenceType {
    const val Site = "site"
    const val SiteAccount = "site_account"
    const val Channel = "channel"
    const val ManagedChannel = "managed_channel"
}
