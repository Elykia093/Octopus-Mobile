package com.elykia.octopus.feature.home

/**
 * 统计数据快照
 * 用于在 Dashboard 中展示当前统计视图（今日或总计）
 */
data class StatsSnapshot(
    val requestCount: Long = 0L,
    val costValue: Double = 0.0,
    val tokenValue: Long = 0L,
    val waitValue: Long = 0L,
    val inputCost: Double = 0.0,
    val inputToken: Long = 0L,
    val outputCost: Double = 0.0,
    val outputToken: Long = 0L,
)
