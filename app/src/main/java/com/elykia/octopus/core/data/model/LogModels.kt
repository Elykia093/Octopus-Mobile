package com.elykia.octopus.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LogCursor(
    val time: Long = 0,
    val id: Long = 0,
)

@Serializable
data class LogPageResponse(
    val logs: List<RelayLog> = emptyList(),
    val total: Int = 0,
    @SerialName("has_more") val hasMore: Boolean = false,
    @SerialName("next_cursor") val nextCursor: LogCursor? = null,
    @SerialName("search_mode") val searchMode: String? = null,
    val warning: String? = null,
)

data class LogListFilter(
    val status: String = LogStatusFilter.All,
    val keyword: String = "",
    val keywordScope: String = LogKeywordScope.Default,
    val keywordMode: String = LogKeywordMode.Default,
)

object LogStatusFilter {
    const val All = "all"
    const val Success = "success"
    const val Error = "error"
}

object LogKeywordScope {
    const val Default = "default"
    const val Content = "content"
}

object LogKeywordMode {
    const val Default = "default"
    const val Prefix = "prefix"
    const val Exact = "exact"
    const val Contains = "contains"
}

@Serializable
data class LogStreamToken(
    val token: String = "",
)

@Serializable
data class ChannelAttempt(
    @SerialName("channel_id") val channelId: Int,
    @SerialName("channel_key_id") val channelKeyId: Int? = null,
    @SerialName("channel_name") val channelName: String,
    @SerialName("model_name") val modelName: String,
    @SerialName("attempt_num") val attemptNum: Int,
    val status: String,
    val duration: Int,
    val sticky: Boolean? = null,
    val msg: String? = null,
)

@Serializable
data class RelayLog(
    val id: Long,
    val time: Long,
    @SerialName("request_model_name") val requestModelName: String,
    @SerialName("request_api_key_name") val requestApiKeyName: String? = null,
    @SerialName("channel") val channelId: Int,
    @SerialName("channel_name") val channelName: String,
    @SerialName("actual_model_name") val actualModelName: String,
    @SerialName("input_tokens") val inputTokens: Int,
    @SerialName("output_tokens") val outputTokens: Int,
    val ftut: Int,
    @SerialName("use_time") val useTime: Int,
    val cost: Double,
    @SerialName("request_content") val requestContent: String,
    @SerialName("response_content") val responseContent: String,
    val error: String,
    val attempts: List<ChannelAttempt> = emptyList(),
    @SerialName("total_attempts") val totalAttempts: Int = 0,
)
