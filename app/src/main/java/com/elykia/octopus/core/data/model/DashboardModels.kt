package com.elykia.octopus.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StatsMetrics(
    @SerialName("input_token") val inputToken: Long = 0L,
    @SerialName("output_token") val outputToken: Long = 0L,
    @SerialName("input_cost") val inputCost: Double = 0.0,
    @SerialName("output_cost") val outputCost: Double = 0.0,
    @SerialName("wait_time") val waitTime: Long = 0L,
    @SerialName("request_success") val requestSuccess: Long = 0L,
    @SerialName("request_failed") val requestFailed: Long = 0L,
) {
    val totalCost: Double get() = inputCost + outputCost
    val totalTokens: Long get() = inputToken + outputToken
    val totalRequests: Long get() = requestSuccess + requestFailed
    val successRate: Double get() = if (totalRequests > 0) requestSuccess.toDouble() / totalRequests else 0.0
}

@Serializable
data class StatsDaily(
    val date: String = "",
    @SerialName("input_token") val inputToken: Long = 0L,
    @SerialName("output_token") val outputToken: Long = 0L,
    @SerialName("input_cost") val inputCost: Double = 0.0,
    @SerialName("output_cost") val outputCost: Double = 0.0,
    @SerialName("wait_time") val waitTime: Long = 0L,
    @SerialName("request_success") val requestSuccess: Long = 0L,
    @SerialName("request_failed") val requestFailed: Long = 0L,
) {
    val totalCost: Double get() = inputCost + outputCost
    val totalTokens: Long get() = inputToken + outputToken
    val totalRequests: Long get() = requestSuccess + requestFailed
}

@Serializable
data class StatsHourly(
    val hour: Int = 0,
    val date: String = "",
    @SerialName("input_token") val inputToken: Long = 0L,
    @SerialName("output_token") val outputToken: Long = 0L,
    @SerialName("input_cost") val inputCost: Double = 0.0,
    @SerialName("output_cost") val outputCost: Double = 0.0,
    @SerialName("wait_time") val waitTime: Long = 0L,
    @SerialName("request_success") val requestSuccess: Long = 0L,
    @SerialName("request_failed") val requestFailed: Long = 0L,
)
