package com.elykia.octopus.core.designsystem

import java.text.DecimalFormat
import kotlin.math.absoluteValue

private val amountFormat = DecimalFormat("#,##0.##")
private val preciseFormat = DecimalFormat("#,##0.####")

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

fun formatMoney(value: Double): String {
    val raw = value.absoluteValue
    val sign = if (value < 0) "-" else ""
    val formatted = when {
        raw >= 1_000_000_000 -> sign + amountFormat.format(raw / 1_000_000_000.0) + "B$"
        raw >= 1_000_000 -> sign + amountFormat.format(raw / 1_000_000.0) + "M$"
        raw >= 1_000 -> sign + amountFormat.format(raw / 1_000.0) + "K$"
        else -> sign + "$" + preciseFormat.format(raw)
    }
    return formatted
}

fun formatDurationMs(value: Long): String = when {
    value >= 86_400_000 -> amountFormat.format(value / 86_400_000.0) + "d"
    value >= 3_600_000 -> amountFormat.format(value / 3_600_000.0) + "h"
    value >= 60_000 -> amountFormat.format(value / 60_000.0) + "m"
    value >= 1_000 -> amountFormat.format(value / 1_000.0) + "s"
    else -> "$value ms"
}
