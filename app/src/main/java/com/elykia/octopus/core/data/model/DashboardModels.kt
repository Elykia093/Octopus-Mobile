package com.elykia.octopus.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StatsDaily(
    @SerialName("request_count") val requestCount: Long = 0L,
    @SerialName("cost_value") val costValue: Double = 0.0,
    @SerialName("token_value") val tokenValue: Long = 0L,
    @SerialName("wait_value") val waitValue: Long = 0L,
    @SerialName("success_count") val successCount: Long = 0L,
    @SerialName("input_cost") val inputCost: Double = 0.0,
    @SerialName("input_token") val inputToken: Long = 0L,
)

@Serializable
data class DashboardEntry(
    @SerialName("day") val day: String = "",
    @SerialName("model_name") val modelName: String = "",
    @SerialName("request_count") val requestCount: Int = 0,
    val quota: Long = 0L,
    @SerialName("prompt_tokens") val promptTokens: Int = 0,
    @SerialName("completion_tokens") val completionTokens: Int = 0,
)

@Serializable
data class StatsTotal(
    @SerialName("request_count") val requestCount: Long = 0L,
    @SerialName("cost_value") val costValue: Double = 0.0,
    @SerialName("token_value") val tokenValue: Long = 0L,
    @SerialName("wait_value") val waitValue: Long = 0L,
    @SerialName("success_count") val successCount: Long = 0L,
    @SerialName("input_cost") val inputCost: Double = 0.0,
    @SerialName("input_token") val inputToken: Long = 0L,
)

@Serializable
data class TrendEntry(
    val title: String = "",
    @SerialName("request_count") val requestCount: Long = 0L,
    @SerialName("cost_value") val costValue: Double = 0.0,
    @SerialName("token_value") val tokenValue: Long = 0L,
)

@Serializable
data class DashboardData(
    val daily: StatsDaily = StatsDaily(),
    val total: StatsTotal = StatsTotal(),
    @SerialName("trend_hourly") val trendHourly: List<TrendEntry> = emptyList(),
    @SerialName("trend_daily") val trendDaily: List<TrendEntry> = emptyList()
)

@Serializable
data class RankItem(
    val title: String = "",
    val value: Long = 0L,
    @SerialName("sub_value") val subValue: Double = 0.0,
)

@Serializable
data class DashboardRankings(
    val topUsers: List<RankItem> = emptyList(),
    val topChannels: List<RankItem> = emptyList()
)
