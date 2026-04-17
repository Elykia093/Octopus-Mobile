package com.elykia.octopus.core.designsystem

import java.text.DecimalFormat
import kotlin.math.absoluteValue

private val amountFormat = DecimalFormat("#,##0.##")
private val preciseFormat = DecimalFormat("#,##0.####")

/**
 * Format like upstream web utils.ts: 1000 -> 1K, 1000000 -> 1M, 1000000000 -> 1B
 */
fun formatCount(value: Long): String {
    val raw = value.toDouble().absoluteValue
    val signed = if (value < 0) -1 else 1
    val formatted = when {
        raw >= 1_000_000_000 -> amountFormat.format(value / 1_000_000_000.0) + "B"
        raw >= 1_000_000 -> amountFormat.format(value / 1_000_000.0) + "M"
        raw >= 1_000 -> amountFormat.format(value / 1_000.0) + "K"
        else -> value.toString()
    }
    return if (signed < 0 && !formatted.startsWith("-")) "-$formatted" else formatted
}

/**
 * Format like upstream web utils.ts: 1000 -> $1K
 */
fun formatMoney(value: Double): String {
    val raw = value.absoluteValue
    val sign = if (value < 0) "-" else ""
    return when {
        raw >= 1_000_000_000 -> sign + amountFormat.format(raw / 1_000_000_000.0) + "B$"
        raw >= 1_000_000 -> sign + amountFormat.format(raw / 1_000_000.0) + "M$"
        raw >= 1_000 -> sign + amountFormat.format(raw / 1_000.0) + "K$"
        else -> sign + "$" + preciseFormat.format(raw)
    }
}

/**
 * Format like upstream web utils.ts: duration -> d/h/m/s/ms
 */
fun formatDurationMs(value: Long): String = when {
    value >= 86_400_000 -> amountFormat.format(value / 86_400_000.0) + "d"
    value >= 3_600_000 -> amountFormat.format(value / 3_600_000.0) + "h"
    value >= 60_000 -> amountFormat.format(value / 60_000.0) + "m"
    value >= 1_000 -> amountFormat.format(value / 1_000.0) + "s"
    else -> "$value ms"
}

fun formatPercent(value: Double): String = "${amountFormat.format(value)}%"
