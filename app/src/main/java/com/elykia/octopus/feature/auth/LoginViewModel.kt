package com.elykia.octopus.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elykia.octopus.core.data.model.LoginRequest
import com.elykia.octopus.core.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class LoginMode {
    ADMIN, API_KEY
}

data class LoginUiState(
    val mode: LoginMode = LoginMode.ADMIN,
    val username: String = "",
    val password: String = "",
    val apiKey: String = "",
    val expireDays: String = "",
    val isSubmitting: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun updateMode(mode: LoginMode) = _uiState.update { it.copy(mode = mode, error = null) }
    fun updateUsername(name: String) = _uiState.update { it.copy(username = name, error = null) }
    fun updatePassword(pwd: String) = _uiState.update { it.copy(password = pwd, error = null) }
    fun updateApiKey(key: String) = _uiState.update { it.copy(apiKey = key, error = null) }
    fun updateExpireDays(days: String) = _uiState.update { it.copy(expireDays = days, error = null) }

    fun submit() {
        val state = _uiState.value
        
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            
            val result = if (state.mode == LoginMode.ADMIN) {
                if (state.username.isBlank() || state.password.isBlank()) {
                    _uiState.update { it.copy(isSubmitting = false, error = "Username and password cannot be empty") }
                    return@launch
                }
                val days = state.expireDays.toIntOrNull()
                authRepository.loginAsAdmin(
                    LoginRequest(
                        username = state.username.trim(), 
                        password = state.password.trim(), 
                        expire = days
                    )
                )
            } else {
                if (state.apiKey.isBlank()) {
                    _uiState.update { it.copy(isSubmitting = false, error = "API Key cannot be empty") }
                    return@launch
                }
                authRepository.loginWithApiKey(state.apiKey.trim())
            }

            if (result.isFailure) {
                _uiState.update { it.copy(
                    isSubmitting = false, 
                    error = result.exceptionOrNull()?.message ?: "Unknown error"
                ) }
            } else {
                // Success: State will be updated in repository which triggers NavGraph redirection
                _uiState.update { it.copy(isSubmitting = false) }
            }
        }
    }
}
