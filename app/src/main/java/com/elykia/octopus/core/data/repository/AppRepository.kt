package com.elykia.octopus.core.data.repository

import com.elykia.octopus.core.data.local.PreferenceStore
import com.elykia.octopus.core.data.model.AuthState
import com.elykia.octopus.core.data.model.ServerConfig
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepository @Inject constructor(
    private val preferenceStore: PreferenceStore,
) {
    val serverConfig: Flow<ServerConfig> = preferenceStore.serverConfig
    val authState: Flow<AuthState> = preferenceStore.authState

    suspend fun updateServerConfig(config: ServerConfig) {
        preferenceStore.updateServerConfig(config)
    }

    suspend fun clearConfigAndAuth() {
        preferenceStore.updateServerConfig(ServerConfig())
        preferenceStore.clearAuthState()
    }
}
