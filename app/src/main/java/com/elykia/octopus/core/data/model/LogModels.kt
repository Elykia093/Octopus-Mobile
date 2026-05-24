package com.elykia.octopus.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LogListResponse(
    val logs: List<LogItem> = emptyList(),
    val total: Long = 0L,
    @SerialName("has_more") val hasMore: Boolean? = null,
    @SerialName("next_cursor") val nextCursor: LogCursor? = null,
    @SerialName("search_mode") val searchMode: String? = null,
    val warning: String? = null,
)

@Serializable
data class LogCursor(
    val time: Long = 0L,
    val id: Long = 0L,
)

@Serializable
data class LogItem(
    val id: Long = 0L,
    val time: Long = 0L,
    @SerialName("request_model_name") val requestModelName: String = "",
    @SerialName("request_api_key_name") val requestApiKeyName: String = "",
    val channel: Int = 0,
    @SerialName("channel_name") val channelName: String = "",
    @SerialName("actual_model_name") val actualModelName: String = "",
    @SerialName("input_tokens") val inputTokens: Int = 0,
    @SerialName("transport_input_tokens") val transportInputTokens: Int? = null,
    @SerialName("bill_input_tokens") val billInputTokens: Int? = null,
    @SerialName("cache_read_tokens") val cacheReadTokens: Int? = null,
    @SerialName("cache_write_tokens") val cacheWriteTokens: Int? = null,
    @SerialName("output_tokens") val outputTokens: Int = 0,
    val ftut: Int = 0,
    @SerialName("use_time") val useTime: Int = 0,
    val cost: Double = 0.0,
    @SerialName("request_content") val requestContent: String = "",
    @SerialName("response_content") val responseContent: String = "",
    val error: String = "",
    val success: Boolean = false,
    val attempts: List<ChannelAttempt> = emptyList(),
    @SerialName("total_attempts") val totalAttempts: Int = 0,
    @SerialName("used_ws") val usedWs: Boolean = false,
    @SerialName("ws_mode") val wsMode: String? = null,
    @SerialName("ws_exec_mode") val wsExecMode: String? = null,
    @SerialName("ws_recovery") val wsRecovery: String? = null,
) {
    val hasError: Boolean get() = error.isNotEmpty() || !success
}

@Serializable
data class ChannelAttempt(
    @SerialName("channel_id") val channelId: Int = 0,
    @SerialName("channel_key_id") val channelKeyId: Int? = null,
    @SerialName("channel_name") val channelName: String = "",
    @SerialName("model_name") val modelName: String = "",
    @SerialName("attempt_num") val attemptNum: Int = 0,
    val status: String = "",
    val duration: Int = 0,
    val sticky: Boolean = false,
    val msg: String = "",
)
