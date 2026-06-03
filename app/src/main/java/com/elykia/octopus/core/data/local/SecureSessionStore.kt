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

    private val preferences: SharedPreferences? by lazy {
        createPreferencesWithRecovery()
    }

    private fun createPreferencesWithRecovery(): SharedPreferences? {
        return try {
            createEncryptedPreferences()
        } catch (exception: Exception) {
            if (!shouldRecoverEncryptedPreferences(exception)) {
                return null
            }
            appContext.deleteSharedPreferences("octopus_secure_session")
            runCatching {
                appContext.deleteSharedPreferences("androidx.security.crypto.master_key")
            }
            runCatching {
                appContext.deleteFile("androidx.security.crypto.master_key")
            }
            runCatching {
                createEncryptedPreferences()
            }.getOrNull()
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
        token = preferences?.getString("token", "").orEmpty(),
        expireAt = preferences?.getString("expire_at", null),
        username = preferences?.getString("username", "").orEmpty(),
        apiKeyMode = preferences?.getBoolean("api_key_mode", false) ?: false,
        serverUrl = preferences?.getString("server_url", "").orEmpty(),
    )

    fun save(authState: AuthState): Boolean {
        val editor = preferences?.edit() ?: return false
        return editor
            .putString("token", authState.token)
            .putString("expire_at", authState.expireAt)
            .putString("username", authState.username)
            .putBoolean("api_key_mode", authState.apiKeyMode)
            .putString("server_url", authState.serverUrl)
            .commit()
    }

    fun clear(): Boolean = preferences?.edit()?.clear()?.commit() ?: false
}

internal fun shouldRecoverEncryptedPreferences(exception: Throwable): Boolean =
    exception.walkCauses().any { cause ->
        val name = cause::class.java.simpleName
        val message = cause.message.orEmpty().lowercase()
        name == "AEADBadTagException" ||
            name == "InvalidProtocolBufferException" ||
            "could not decrypt" in message ||
            "bad decrypt" in message ||
            "mac check failed" in message ||
            "invalid protocol buffer" in message ||
            "keyset" in message
    }

private fun Throwable.walkCauses(): Sequence<Throwable> = sequence {
    var current: Throwable? = this@walkCauses
    while (current != null) {
        yield(current)
        current = current.cause
    }
}
