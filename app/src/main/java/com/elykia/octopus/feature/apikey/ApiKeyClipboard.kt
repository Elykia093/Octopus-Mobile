package com.elykia.octopus.feature.apikey

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.PersistableBundle

internal fun copyApiKey(context: Context, value: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
    val clip = ClipData.newPlainText("api_key", value).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            description.extras = PersistableBundle().apply {
                putBoolean("android.content.extra.IS_SENSITIVE", true)
            }
        }
    }
    clipboard.setPrimaryClip(clip)
    android.os.Handler(context.mainLooper).postDelayed({
        if (clipboard.primaryClip?.getItemAt(0)?.text?.toString() == value) {
            clipboard.setPrimaryClip(ClipData.newPlainText("", ""))
        }
    }, 60_000L)
}
