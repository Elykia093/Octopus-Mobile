package com.elykia.octopus.core.data.repository

import androidx.core.net.toUri
import com.elykia.octopus.core.common.AppResult
import com.elykia.octopus.core.common.DispatchersProvider
import com.elykia.octopus.core.data.local.PreferenceStore
import com.elykia.octopus.core.data.local.SecureSessionStore
import com.elykia.octopus.core.data.local.SessionManager
import com.elykia.octopus.core.data.model.AuthState
import com.elykia.octopus.core.data.model.ServerConfig
import com.elykia.octopus.core.data.model.SettingItem
import com.elykia.octopus.core.data.model.UserLoginRequest
import com.elykia.octopus.core.data.remote.NetworkExecutor
import com.elykia.octopus.core.data.remote.OctopusApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepository @Inject constructor(
    private val apiService: OctopusApiService,
    private val preferenceStore: PreferenceStore,
    private val secureSessionStore: SecureSessionStore,
    private val sessionManager: SessionManager,
    private val networkExecutor: NetworkExecutor,
    private val dispatchers: DispatchersProvider,
) {
    val serverConfig: Flow<ServerConfig> = preferenceStore.serverConfig

    suspend fun currentServerConfig(): ServerConfig = withContext(dispatchers.io) {
        preferenceStore.serverConfig.first()
    }

    suspend fun currentAuth(): AuthState = withContext(dispatchers.io) {
        secureSessionStore.load()
    }

    suspend fun saveServerUrl(rawUrl: String): AppResult<ServerConfig> = withContext(dispatchers.io) {
        val normalized = rawUrl.trim().trimEnd('/')
        val valid = runCatching { normalized.toUri() }.isSuccess && normalized.startsWith("http")
        if (!valid) {
            return@withContext AppResult.Error("请输入有效的地址。")
        }
        val config = preferenceStore.serverConfig.first().copy(baseUrl = normalized)
        preferenceStore.saveServerConfig(config)
        AppResult.Success(config)
    }

    suspend fun saveAppearance(language: String, themeMode: Int) = withContext(dispatchers.io) {
        val config = preferenceStore.serverConfig.first().copy(language = language, themeMode = themeMode)
        preferenceStore.saveServerConfig(config)
        AppResult.Success(config)
    }

    suspend fun login(username: String, password: String, expireDays: Int): AppResult<AuthState> = withContext(dispatchers.io) {
        when (val result = networkExecutor.execute {
            apiService.login(
                UserLoginRequest(
                    username = username,
                    password = password,
                    expire = expireDays * 24 * 60 * 60,
                )
            )
        }) {
            is AppResult.Success -> {
                val auth = AuthState(
                    token = result.data.token,
                    expireAt = result.data.expireAt,
                    username = username,
                )
                secureSessionStore.save(auth)
                sessionManager.update(auth)
                AppResult.Success(auth)
            }

            is AppResult.Error -> result
        }
    }

    suspend fun validateSession(): AppResult<Boolean> = withContext(dispatchers.io) {
        val auth = secureSessionStore.load()
        if (auth.token.isBlank()) {
            return@withContext AppResult.Success(false)
        }
        sessionManager.update(auth)
        when (val result = networkExecutor.execute { apiService.status() }) {
            is AppResult.Success -> AppResult.Success(true)
            is AppResult.Error -> result
        }
    }

    suspend fun logout() = withContext(dispatchers.io) {
        secureSessionStore.clear()
        sessionManager.clear()
    }

    suspend fun settings(): AppResult<List<SettingItem>> = withContext(dispatchers.io) {
        networkExecutor.execute { apiService.settings() }
    }

    suspend fun saveSetting(setting: SettingItem): AppResult<SettingItem> = withContext(dispatchers.io) {
        networkExecutor.execute { apiService.setSetting(setting) }
    }
}
