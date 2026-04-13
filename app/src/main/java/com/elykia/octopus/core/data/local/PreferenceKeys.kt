package com.elykia.octopus.core.data.local

import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object PreferenceKeys {
    val serverUrl = stringPreferencesKey("server_url")
    val language = stringPreferencesKey("language")
    val themeMode = intPreferencesKey("theme_mode")
}
