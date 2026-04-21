package com.elykia.octopus.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Channel(
    val id: Long = 0L,
    val name: String = "",
    val type: Int = 1,
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
    val stats: StatsChannel? = null,
    @SerialName("match_regex") val matchRegex: String? = null,
)

@Serializable
data class BaseUrl(
    val url: String = "",
    val delay: Int = 0,
)

@Serializable
data class CustomHeader(
    @SerialName("header_key") val headerKey: String = "",
    @SerialName("header_value") val headerValue: String = "",
)

@Serializable
data class ChannelKey(
    val id: Long = 0L,
    @SerialName("channel_id") val channelId: Long = 0L,
    val enabled: Boolean = true,
    @SerialName("channel_key") val channelKey: String = "",
    @SerialName("status_code") val statusCode: Int = 0,
    @SerialName("last_use_time_stamp") val lastUseTimeStamp: Long = 0L,
    @SerialName("total_cost") val totalCost: Double = 0.0,
    val remark: String = "",
)

@Serializable
data class StatsChannel(
    @SerialName("channel_id") val channelId: Long = 0L,
    @SerialName("input_token") val inputToken: Long = 0L,
    @SerialName("output_token") val outputToken: Long = 0L,
    @SerialName("input_cost") val inputCost: Double = 0.0,
    @SerialName("output_cost") val outputCost: Double = 0.0,
    @SerialName("wait_time") val waitTime: Long = 0L,
    @SerialName("request_success") val requestSuccess: Long = 0L,
    @SerialName("request_failed") val requestFailed: Long = 0L,
)
