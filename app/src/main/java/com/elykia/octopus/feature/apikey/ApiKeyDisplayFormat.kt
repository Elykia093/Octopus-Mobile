package com.elykia.octopus.feature.apikey

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal fun formatApiKeyExpireAtRaw(expireAt: Long?): String {
    if (expireAt == null || expireAt <= 0L) {
        return ""
    }
    return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(expireAt * 1000))
}
