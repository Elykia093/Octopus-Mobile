package com.elykia.octopus.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
    @SerialName("output_tokens") val outputTokens: Int = 0,
    val ftut: Int = 0,
    @SerialName("use_time") val useTime: Int = 0,
    val cost: Double = 0.0,
    val error: String = "",
) {
    val hasError: Boolean get() = error.isNotEmpty()
}
