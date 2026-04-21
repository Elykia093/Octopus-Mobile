package com.elykia.octopus.feature.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elykia.octopus.core.data.model.ServerConfig
import com.elykia.octopus.core.data.repository.AppRepository
import com.elykia.octopus.core.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingUiState(
    val config: ServerConfig = ServerConfig(),
    val isApiKeyMode: Boolean = false,
    val isLoggingOut: Boolean = false
)

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val appRepository: AppRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingUiState())
    val uiState: StateFlow<SettingUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(appRepository.serverConfig, appRepository.authState) { currentConfig, authState ->
                currentConfig to authState
            }.collect { (currentConfig, authState) ->
                _uiState.update {
                    it.copy(
                        config = currentConfig,
                        isApiKeyMode = authState.isApiKeyMode
                    )
                }
            }
        }
    }

    fun updateTheme(mode: Int) {
        viewModelScope.launch {
            appRepository.updateServerConfig(_uiState.value.config.copy(themeMode = mode))
        }
    }

    fun logout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoggingOut = true) }
            authRepository.logout()
            _uiState.update { it.copy(isLoggingOut = false) }
        }
    }

    fun resetServerConfig() {
        viewModelScope.launch {
            appRepository.clearConfigAndAuth()
        }
    }
}
