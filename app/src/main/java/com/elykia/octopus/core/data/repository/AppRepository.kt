package com.elykia.octopus.core.data.repository

import com.elykia.octopus.core.common.AppResult
import com.elykia.octopus.core.common.DispatchersProvider
import com.elykia.octopus.core.data.local.PreferenceStore
import com.elykia.octopus.core.data.local.SecureSessionStore
import com.elykia.octopus.core.data.local.SessionManager
import com.elykia.octopus.core.data.model.AuthState
import com.elykia.octopus.core.data.model.ChangePasswordRequest
import com.elykia.octopus.core.data.model.ChangeUsernameRequest
import com.elykia.octopus.core.data.model.ServerConfig
import com.elykia.octopus.core.data.model.SettingItem
import com.elykia.octopus.core.data.model.UserLoginRequest
import com.elykia.octopus.core.data.remote.AuthApiService
import com.elykia.octopus.core.data.remote.NetworkExecutor
import com.elykia.octopus.core.data.remote.ServerUrlProvider
import com.elykia.octopus.core.data.remote.SettingApiService
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CancellationException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepository @Inject constructor(
    private val authApiService: AuthApiService,
    private val settingApiService: SettingApiService,
    private val preferenceStore: PreferenceStore,
    private val secureSessionStore: SecureSessionStore,
    private val sessionManager: SessionManager,
    private val networkExecutor: NetworkExecutor,
    private val serverUrlProvider: ServerUrlProvider,
    private val dispatchers: DispatchersProvider,
) {
    val serverConfig: Flow<ServerConfig> = preferenceStore.serverConfig

    suspend fun currentServerConfig(): ServerConfig = withContext(dispatchers.io) {
        preferenceStore.serverConfig.first().httpsOnly().also(serverUrlProvider::update)
    }

    suspend fun currentAuth(): AuthState = withContext(dispatchers.io) {
        secureSessionStore.load()
    }

    suspend fun saveServerUrl(rawUrl: String): AppResult<ServerConfig> = withContext(dispatchers.io) {
        val normalized = normalizeValidServerIdentity(rawUrl)
        if (normalized == null) {
            return@withContext AppResult.Error("请输入有效的 HTTPS 地址。")
        }
        runServerConfigMutation {
            val currentConfig = preferenceStore.serverConfig.first()
            val shouldClearAuth = normalizeServerIdentity(currentConfig.baseUrl) != normalized &&
                secureSessionStore.load().token.isNotBlank()
            val config = currentConfig.copy(baseUrl = normalized)
            if (shouldClearAuth) {
                if (!secureSessionStore.clear()) {
                    return@runServerConfigMutation AppResult.Error(SESSION_CLEAR_FAILED_MESSAGE)
                }
                sessionManager.clear()
            }
            preferenceStore.saveServerConfig(config)
            serverUrlProvider.update(config)
            AppResult.Success(config)
        }
    }

    suspend fun saveAppearance(language: String, themeMode: Int): AppResult<ServerConfig> = withContext(dispatchers.io) {
        runServerConfigMutation {
            val config = preferenceStore.serverConfig.first().copy(language = language, themeMode = themeMode)
            preferenceStore.saveServerConfig(config)
            serverUrlProvider.update(config)
            AppResult.Success(config)
        }
    }

    suspend fun login(username: String, password: String, expireDays: Int): AppResult<AuthState> = withContext(dispatchers.io) {
        when (val result = networkExecutor.execute {
            authApiService.login(
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
                    serverUrl = normalizeServerIdentity(preferenceStore.serverConfig.first().baseUrl),
                )
                when (
                    val saved = persistLoginAuth(
                        auth = auth,
                        saveAuth = secureSessionStore::save,
                        updateSession = sessionManager::update,
                    )
                ) {
                    is AppResult.Success -> saved
                    is AppResult.Error -> saved
                }
            }

            is AppResult.Error -> result
        }
    }

    suspend fun validateSession(): AppResult<Boolean> = withContext(dispatchers.io) {
        val auth = secureSessionStore.load()
        if (auth.token.isBlank()) {
            return@withContext AppResult.Success(false)
        }
        val config = preferenceStore.serverConfig.first().httpsOnly().also(serverUrlProvider::update)
        val serverUrl = normalizeServerIdentity(config.baseUrl)
        if (!auth.isBoundToServer(serverUrl)) {
            if (!secureSessionStore.clear()) {
                return@withContext AppResult.Error(SESSION_CLEAR_FAILED_MESSAGE)
            }
            sessionManager.clear()
            return@withContext AppResult.Success(false)
        }
        sessionManager.update(auth)
        when (val result = networkExecutor.execute { authApiService.status() }) {
            is AppResult.Success -> AppResult.Success(true)
            is AppResult.Error -> result
        }
    }

    suspend fun logout(): AppResult<Unit> = withContext(dispatchers.io) {
        if (!secureSessionStore.clear()) {
            return@withContext AppResult.Error(SESSION_CLEAR_FAILED_MESSAGE)
        }
        sessionManager.clear()
        AppResult.Success(Unit)
    }

    suspend fun changeUsername(newUsername: String): AppResult<String> = withContext(dispatchers.io) {
        networkExecutor.execute {
            authApiService.changeUsername(ChangeUsernameRequest(newUsername = newUsername))
        }
    }

    suspend fun changePassword(oldPassword: String, newPassword: String): AppResult<String> = withContext(dispatchers.io) {
        networkExecutor.execute {
            authApiService.changePassword(
                ChangePasswordRequest(
                    oldPassword = oldPassword,
                    newPassword = newPassword,
                )
            )
        }
    }

    suspend fun settings(): AppResult<List<SettingItem>> = withContext(dispatchers.io) {
        networkExecutor.execute { settingApiService.settings() }
    }

    suspend fun saveSetting(setting: SettingItem): AppResult<SettingItem> = withContext(dispatchers.io) {
        networkExecutor.execute { settingApiService.setSetting(setting) }
    }
}

internal fun ServerConfig.httpsOnly(): ServerConfig {
    return copy(baseUrl = normalizeValidServerIdentity(baseUrl).orEmpty())
}

internal fun normalizeServerIdentity(baseUrl: String): String = baseUrl.trim().trimEnd('/')

internal fun normalizeValidServerIdentity(baseUrl: String): String? {
    val normalized = normalizeServerIdentity(baseUrl)
    val parsed = normalized.toHttpUrlOrNull() ?: return null
    if (parsed.scheme != "https") return null
    if (parsed.encodedUsername.isNotBlank() || parsed.encodedPassword.isNotBlank()) return null
    if (parsed.encodedQuery != null || parsed.encodedFragment != null) return null
    return normalized
}

internal fun AuthState.isBoundToServer(serverUrl: String): Boolean =
    token.isNotBlank() && serverUrl.isNotBlank() && this.serverUrl == serverUrl

internal fun persistLoginAuth(
    auth: AuthState,
    saveAuth: (AuthState) -> Boolean,
    updateSession: (AuthState) -> Unit,
): AppResult<AuthState> {
    if (!saveAuth(auth)) {
        return AppResult.Error(SESSION_SAVE_FAILED_MESSAGE)
    }

    updateSession(auth)
    return AppResult.Success(auth)
}

internal const val SESSION_SAVE_FAILED_MESSAGE = "安全保存会话失败，请检查设备安全存储后重试。"
internal const val SESSION_CLEAR_FAILED_MESSAGE = "安全清除会话失败，请检查设备安全存储后重试。"

internal suspend fun runServerConfigMutation(
    block: suspend () -> AppResult<ServerConfig>,
): AppResult<ServerConfig> = try {
    block()
} catch (exception: CancellationException) {
    throw exception
} catch (exception: Exception) {
    AppResult.Error(SERVER_CONFIG_SAVE_FAILED_MESSAGE, exception)
}

internal const val SERVER_CONFIG_SAVE_FAILED_MESSAGE = "保存本地配置失败，请稍后重试。"
