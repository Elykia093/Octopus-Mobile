package com.elykia.octopus.core.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.elykia.octopus.core.data.model.AuthState
import com.elykia.octopus.core.data.model.ServerConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "octopus_prefs")

@Singleton
class PreferenceStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val configKey = stringPreferencesKey("server_config")
    private val authStateKey = stringPreferencesKey("auth_state")

    private val json = Json { ignoreUnknownKeys = true }

    val serverConfig: Flow<ServerConfig> = context.dataStore.data.map { prefs ->
        prefs[configKey]?.let {
            try { json.decodeFromString<ServerConfig>(it) } catch (e: Exception) { ServerConfig() }
        } ?: ServerConfig()
    }

    val authState: Flow<AuthState> = context.dataStore.data.map { prefs ->
        prefs[authStateKey]?.let {
            try { json.decodeFromString<AuthState>(it) } catch (e: Exception) { AuthState() }
        } ?: AuthState()
    }

    suspend fun updateServerConfig(config: ServerConfig) {
        context.dataStore.edit { prefs ->
            prefs[configKey] = json.encodeToString(config)
        }
    }

    suspend fun updateAuthState(state: AuthState) {
        context.dataStore.edit { prefs ->
            prefs[authStateKey] = json.encodeToString(state)
        }
    }

    suspend fun clearAuthState() {
        context.dataStore.edit { prefs ->
            prefs.remove(authStateKey)
        }
    }
}
