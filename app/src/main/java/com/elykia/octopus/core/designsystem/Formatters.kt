package com.elykia.octopus.core.designsystem

import java.text.DecimalFormat
import kotlin.math.absoluteValue

private val amountFormat = DecimalFormat("#,##0.####")

fun formatCount(value: Long): String {
    val raw = value.toDouble().absoluteValue
    val signed = if (value < 0) -1 else 1
    val formatted = when {
        raw >= 1_000_000_000 -> amountFormat.format(value / 1_000_000_000.0) + "十亿"
        raw >= 1_000_000 -> amountFormat.format(value / 1_000_000.0) + "百万"
        raw >= 1_000 -> amountFormat.format(value / 1_000.0) + "千"
        else -> value.toString()
    }
    return if (signed < 0 && !formatted.startsWith("-")) "-$formatted" else formatted
}

fun formatMoney(value: Double): String = "¥" + amountFormat.format(value)

fun formatDurationMs(value: Long): String = when {
    value >= 60_000 -> amountFormat.format(value / 60_000.0) + " 分钟"
    value >= 1_000 -> amountFormat.format(value / 1_000.0) + " 秒"
    else -> "$value 毫秒"
}
