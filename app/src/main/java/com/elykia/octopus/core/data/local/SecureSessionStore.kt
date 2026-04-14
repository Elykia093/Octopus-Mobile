package com.elykia.octopus.core.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.elykia.octopus.core.data.model.AuthState
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecureSessionStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val appContext = context.applicationContext

    private val preferences: SharedPreferences by lazy {
        createPreferencesWithRecovery()
    }

    private fun createPreferencesWithRecovery(): SharedPreferences {
        return try {
            createEncryptedPreferences()
        } catch (_: Exception) {
            appContext.deleteSharedPreferences("octopus_secure_session")
            runCatching {
                appContext.deleteSharedPreferences("androidx.security.crypto.master_key")
            }
            runCatching {
                appContext.deleteFile("androidx.security.crypto.master_key")
            }
            runCatching {
                createEncryptedPreferences()
            }.getOrElse {
                appContext.getSharedPreferences("octopus_secure_session_fallback", Context.MODE_PRIVATE)
            }
        }
    }

    private fun createEncryptedPreferences(): SharedPreferences {
        val masterKey = MasterKey.Builder(appContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            appContext,
            "octopus_secure_session",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    fun load(): AuthState = AuthState(
        token = preferences.getString("token", "").orEmpty(),
        expireAt = preferences.getString("expire_at", null),
        username = preferences.getString("username", "").orEmpty(),
        apiKeyMode = preferences.getBoolean("api_key_mode", false),
    )

    fun save(authState: AuthState) {
        preferences.edit()
            .putString("token", authState.token)
            .putString("expire_at", authState.expireAt)
            .putString("username", authState.username)
            .putBoolean("api_key_mode", authState.apiKeyMode)
            .apply()
    }

    fun clear() {
        preferences.edit().clear().apply()
    }
}
