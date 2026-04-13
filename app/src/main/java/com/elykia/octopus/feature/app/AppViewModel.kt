package com.elykia.octopus.feature.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elykia.octopus.core.common.AppResult
import com.elykia.octopus.core.data.local.PreferenceStore
import com.elykia.octopus.core.data.local.SessionManager
import com.elykia.octopus.core.data.repository.AppRepository
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
    sessionManager: SessionManager,
) : ViewModel() {
    private val bootstrapState = MutableStateFlow<LaunchState>(LaunchState.Loading)

    val launchState: StateFlow<LaunchState> = bootstrapState
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
            appRepository.logout()
            bootstrap()
        }
    }
}
