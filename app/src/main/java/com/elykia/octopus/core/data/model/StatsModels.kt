package com.elykia.octopus.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StatsTotal(
    val id: Int = 0,
    @SerialName("input_token") val inputToken: Long = 0,
    @SerialName("output_token") val outputToken: Long = 0,
    @SerialName("input_cost") val inputCost: Double = 0.0,
    @SerialName("output_cost") val outputCost: Double = 0.0,
    @SerialName("wait_time") val waitTime: Long = 0,
    @SerialName("request_success") val requestSuccess: Long = 0,
    @SerialName("request_failed") val requestFailed: Long = 0,
)

@Serializable
data class StatsDaily(
    val date: String,
    @SerialName("input_token") val inputToken: Long = 0,
    @SerialName("output_token") val outputToken: Long = 0,
    @SerialName("input_cost") val inputCost: Double = 0.0,
    @SerialName("output_cost") val outputCost: Double = 0.0,
    @SerialName("wait_time") val waitTime: Long = 0,
    @SerialName("request_success") val requestSuccess: Long = 0,
    @SerialName("request_failed") val requestFailed: Long = 0,
)

@Serializable
data class StatsApiKeyEntry(
    @SerialName("api_key_id") val apiKeyId: Int = 0,
    @SerialName("input_token") val inputToken: Long = 0,
    @SerialName("output_token") val outputToken: Long = 0,
    @SerialName("input_cost") val inputCost: Double = 0.0,
    @SerialName("output_cost") val outputCost: Double = 0.0,
    @SerialName("wait_time") val waitTime: Long = 0,
    @SerialName("request_success") val requestSuccess: Long = 0,
    @SerialName("request_failed") val requestFailed: Long = 0,
)

@Serializable
data class StatsHourly(
    val hour: Int,
    val date: String,
    @SerialName("input_token") val inputToken: Long = 0,
    @SerialName("output_token") val outputToken: Long = 0,
    @SerialName("input_cost") val inputCost: Double = 0.0,
    @SerialName("output_cost") val outputCost: Double = 0.0,
    @SerialName("wait_time") val waitTime: Long = 0,
    @SerialName("request_success") val requestSuccess: Long = 0,
    @SerialName("request_failed") val requestFailed: Long = 0,
)

@Serializable
data class StatsChannel(
    @SerialName("channel_id") val channelId: Int = 0,
    @SerialName("input_token") val inputToken: Long = 0,
    @SerialName("output_token") val outputToken: Long = 0,
    @SerialName("input_cost") val inputCost: Double = 0.0,
    @SerialName("output_cost") val outputCost: Double = 0.0,
    @SerialName("wait_time") val waitTime: Long = 0,
    @SerialName("request_success") val requestSuccess: Long = 0,
    @SerialName("request_failed") val requestFailed: Long = 0,
)
