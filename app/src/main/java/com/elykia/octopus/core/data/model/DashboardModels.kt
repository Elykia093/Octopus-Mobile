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
) {
    // Note: backend returns StatsMetrics fields flat in the same object
    @SerialName("input_token") var inputToken: Long = 0L
    @SerialName("output_token") var outputToken: Long = 0L
    @SerialName("input_cost") var inputCost: Double = 0.0
    @SerialName("output_cost") var outputCost: Double = 0.0
    @SerialName("wait_time") var waitTime: Long = 0L
    @SerialName("request_success") var requestSuccess: Long = 0L
    @SerialName("request_failed") var requestFailed: Long = 0L

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

@Serializable
data class TrendEntry(
    val title: String = "",
    @SerialName("request_count") val requestCount: Long = 0L,
    @SerialName("cost_value") val costValue: Double = 0.0,
    @SerialName("token_value") val tokenValue: Long = 0L,
)

// Legacy types kept for compatibility
@Serializable
data class DashboardEntry(
    @SerialName("day") val day: String = "",
    @SerialName("model_name") val modelName: String = "",
    @SerialName("request_count") val requestCount: Int = 0,
    val quota: Long = 0L,
    @SerialName("prompt_tokens") val promptTokens: Int = 0,
    @SerialName("completion_tokens") val completionTokens: Int = 0,
)
