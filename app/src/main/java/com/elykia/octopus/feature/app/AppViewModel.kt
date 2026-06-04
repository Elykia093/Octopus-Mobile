package com.elykia.octopus.feature.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elykia.octopus.core.common.AppResult
import com.elykia.octopus.core.data.local.PreferenceStore
import com.elykia.octopus.core.data.local.SessionManager
import com.elykia.octopus.core.data.repository.AppRepository
import com.elykia.octopus.core.data.repository.SESSION_CLEAR_FAILED_MESSAGE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val appRepository: AppRepository,
    preferenceStore: PreferenceStore,
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val bootstrapState = MutableStateFlow<LaunchState>(LaunchState.Loading)
    private val securityMessageState = MutableStateFlow<String?>(null)

    val launchState: StateFlow<LaunchState> = bootstrapState
    val securityMessage: StateFlow<String?> = securityMessageState
    val themeMode: StateFlow<Int> = preferenceStore.serverConfig
        .combine(sessionManager.unauthorized) { config, unauthorized ->
            if (unauthorized) {
                bootstrapState.value = LaunchState.NeedLogin(config)
                sessionManager.consumeUnauthorized()
            }
            config.themeMode
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    init {
        bootstrap()
        viewModelScope.launch {
            sessionManager.securityWarning.collect { message ->
                if (!message.isNullOrBlank()) {
                    securityMessageState.value = message
                    sessionManager.consumeSecurityWarning()
                }
            }
        }
    }

    fun bootstrap() {
        viewModelScope.launch {
            val config = appRepository.currentServerConfig()
            if (config.baseUrl.isBlank()) {
                bootstrapState.value = LaunchState.NeedServer(config)
                return@launch
            }

            when (val result = appRepository.validateSession()) {
                is AppResult.Success -> {
                    if (result.data) {
                        val auth = appRepository.currentAuth()
                        bootstrapState.value = LaunchState.Ready(config, auth)
                    } else {
                        bootstrapState.value = LaunchState.NeedLogin(config)
                    }
                }

                is AppResult.Error -> {
                    if (result.message == SESSION_CLEAR_FAILED_MESSAGE) {
                        securityMessageState.value = result.message
                    }
                    bootstrapState.value = LaunchState.NeedLogin(config)
                }
            }
        }
    }

    fun onLoggedIn() {
        bootstrap()
    }

    fun onServerConfigured() {
        bootstrap()
    }

    fun logout() {
        viewModelScope.launch {
            when (val result = appRepository.logout()) {
                is AppResult.Success -> {
                    securityMessageState.value = null
                    bootstrap()
                }
                is AppResult.Error -> {
                    securityMessageState.value = result.message
                }
            }
        }
    }

    fun clearSecurityMessage() {
        securityMessageState.value = null
    }
}
