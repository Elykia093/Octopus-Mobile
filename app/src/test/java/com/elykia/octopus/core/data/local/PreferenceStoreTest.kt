package com.elykia.octopus.core.data.local

import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.mutablePreferencesOf
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.io.IOException

class PreferenceStoreTest {
    @Test
    fun preferencesToServerConfigUsesStoredValues() {
        val preferences = mutablePreferencesOf(
            PreferenceKeys.serverUrl to "https://example.com",
            PreferenceKeys.language to "zh-CN",
            PreferenceKeys.themeMode to 2,
        )

        val config = preferencesToServerConfig(preferences)

        assertThat(config.baseUrl).isEqualTo("https://example.com")
        assertThat(config.language).isEqualTo("zh-CN")
        assertThat(config.themeMode).isEqualTo(2)
    }

    @Test
    fun preferencesToServerConfigUsesDefaultsForMissingValues() {
        val config = preferencesToServerConfig(emptyPreferences())

        assertThat(config.baseUrl).isEmpty()
        assertThat(config.language).isEqualTo("system")
        assertThat(config.themeMode).isEqualTo(0)
    }

    @Test
    fun recoverPreferenceReadErrorsEmitsEmptyPreferencesForIoException() = runBlocking {
        val recovered = flow<androidx.datastore.preferences.core.Preferences> {
            throw IOException("corrupt preferences")
        }.recoverPreferenceReadErrors().single()

        assertThat(preferencesToServerConfig(recovered).baseUrl).isEmpty()
    }

    @Test
    fun recoverPreferenceReadErrorsRethrowsNonIoException() = runBlocking {
        val result = runCatching {
            flow<androidx.datastore.preferences.core.Preferences> {
                throw IllegalStateException("bad state")
            }.recoverPreferenceReadErrors().single()
        }

        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalStateException::class.java)
    }
}
