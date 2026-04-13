package com.elykia.octopus.core.data.local

import android.content.Context
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
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val preferences = EncryptedSharedPreferences.create(
        context,
        "octopus_secure_session",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

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
