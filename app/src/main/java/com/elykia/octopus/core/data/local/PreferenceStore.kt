package com.elykia.octopus.core.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import com.elykia.octopus.core.data.model.ServerConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "octopus_mobile")

@Singleton
class PreferenceStore @Inject constructor(
    @param:ApplicationContext
    private val context: Context,
) {
    private val store get() = context.dataStore

    val serverConfig: Flow<ServerConfig> = store.data
        .recoverPreferenceReadErrors()
        .map(::preferencesToServerConfig)

    suspend fun saveServerConfig(config: ServerConfig) {
        store.edit { preferences ->
            preferences[PreferenceKeys.serverUrl] = config.baseUrl
            preferences[PreferenceKeys.language] = config.language
            preferences[PreferenceKeys.themeMode] = config.themeMode
        }
    }
}

internal fun preferencesToServerConfig(preferences: Preferences): ServerConfig = ServerConfig(
    baseUrl = preferences[PreferenceKeys.serverUrl].orEmpty(),
    language = preferences[PreferenceKeys.language] ?: "system",
    themeMode = preferences[PreferenceKeys.themeMode] ?: 0,
)

internal fun Flow<Preferences>.recoverPreferenceReadErrors(): Flow<Preferences> = catch { exception ->
    if (exception is IOException) {
        emit(emptyPreferences())
    } else {
        throw exception
    }
}
