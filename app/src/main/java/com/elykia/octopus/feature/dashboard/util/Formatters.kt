package com.elykia.octopus.feature.dashboard.util

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatCurrency(amount: Double): String {
    val formatter = DecimalFormat("#,##0.00")
    return "$${formatter.format(amount)}"
}

fun formatNumber(number: Double): String {
    val formatter = DecimalFormat("#,###")
    return formatter.format(number)
}

fun formatDateLabel(timestamp: Long): String {
    val sdf = SimpleDateFormat("MM-dd", Locale.getDefault())
    return sdf.format(Date(timestamp * 1000))
}