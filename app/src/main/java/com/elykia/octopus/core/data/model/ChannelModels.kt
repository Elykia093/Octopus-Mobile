package com.elykia.octopus.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BaseUrl(
    val url: String,
    val delay: Int = 0,
)

@Serializable
data class ChannelKey(
    val id: Int = 0,
    @SerialName("channel_id") val channelId: Int = 0,
    val enabled: Boolean = true,
    @SerialName("channel_key") val channelKey: String,
    @SerialName("status_code") val statusCode: Int = 0,
    @SerialName("last_use_time_stamp") val lastUseTimeStamp: Long = 0,
    @SerialName("total_cost") val totalCost: Double = 0.0,
    val remark: String = "",
)

@Serializable
data class CustomHeader(
    @SerialName("header_key") val headerKey: String,
    @SerialName("header_value") val headerValue: String,
)

@Serializable
data class Channel(
    val id: Int = 0,
    val name: String,
    val type: Int,
    val enabled: Boolean = true,
    @SerialName("base_urls") val baseUrls: List<BaseUrl> = emptyList(),
    val keys: List<ChannelKey> = emptyList(),
    val model: String = "",
    @SerialName("custom_model") val customModel: String = "",
    val proxy: Boolean = false,
    @SerialName("auto_sync") val autoSync: Boolean = false,
    @SerialName("auto_group") val autoGroup: Int = 0,
    @SerialName("custom_header") val customHeader: List<CustomHeader> = emptyList(),
    @SerialName("param_override") val paramOverride: String? = null,
    @SerialName("channel_proxy") val channelProxy: String? = null,
    @SerialName("match_regex") val matchRegex: String? = null,
    val stats: StatsChannel? = null,
)

@Serializable
data class ChannelKeyAddRequest(
    val enabled: Boolean = true,
    @SerialName("channel_key") val channelKey: String,
    val remark: String = "",
)

@Serializable
data class ChannelKeyUpdateRequest(
    val id: Int,
    val enabled: Boolean? = null,
    @SerialName("channel_key") val channelKey: String? = null,
    val remark: String? = null,
)

@Serializable
data class ChannelUpdateRequest(
    val id: Int,
    val name: String? = null,
    val type: Int? = null,
    val enabled: Boolean? = null,
    @SerialName("base_urls") val baseUrls: List<BaseUrl>? = null,
    val model: String? = null,
    @SerialName("custom_model") val customModel: String? = null,
    val proxy: Boolean? = null,
    @SerialName("auto_sync") val autoSync: Boolean? = null,
    @SerialName("auto_group") val autoGroup: Int? = null,
    @SerialName("custom_header") val customHeader: List<CustomHeader>? = null,
    @SerialName("channel_proxy") val channelProxy: String? = null,
    @SerialName("param_override") val paramOverride: String? = null,
    @SerialName("match_regex") val matchRegex: String? = null,
    @SerialName("keys_to_add") val keysToAdd: List<ChannelKeyAddRequest> = emptyList(),
    @SerialName("keys_to_update") val keysToUpdate: List<ChannelKeyUpdateRequest> = emptyList(),
    @SerialName("keys_to_delete") val keysToDelete: List<Int> = emptyList(),
)

@Serializable
data class ChannelFetchModelRequest(
    val type: Int,
    @SerialName("base_urls") val baseUrls: List<BaseUrl> = emptyList(),
    val keys: List<ChannelKeyAddRequest> = emptyList(),
    val proxy: Boolean = false,
    @SerialName("channel_proxy") val channelProxy: String? = null,
    @SerialName("match_regex") val matchRegex: String? = null,
    @SerialName("custom_header") val customHeader: List<CustomHeader> = emptyList(),
)

@Serializable
data class ChannelEnableRequest(
    val id: Int,
    val enabled: Boolean,
)
