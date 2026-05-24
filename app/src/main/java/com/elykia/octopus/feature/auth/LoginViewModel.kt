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
    val expireMinutes: String = "86400",
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
    fun updateExpireMinutes(minutes: String) = _uiState.update { it.copy(expireMinutes = minutes, error = null) }

    fun submit() {
        val state = _uiState.value
        
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            
            val result = if (state.mode == LoginMode.ADMIN) {
                if (state.username.isBlank() || state.password.isBlank()) {
                    _uiState.update { it.copy(isSubmitting = false, error = "用户名和密码不能为空") }
                    return@launch
                }
                val minutes = state.expireMinutes.trim().toIntOrNull()
                if (minutes == null || minutes <= 0) {
                    _uiState.update { it.copy(isSubmitting = false, error = "有效期分钟数必须为正整数") }
                    return@launch
                }
                authRepository.loginAsAdmin(
                    buildAdminLoginRequest(state, minutes)
                )
            } else {
                if (state.apiKey.isBlank()) {
                    _uiState.update { it.copy(isSubmitting = false, error = "API Key 不能为空") }
                    return@launch
                }
                authRepository.loginWithApiKey(state.apiKey.trim())
            }

            if (result.isFailure) {
                _uiState.update { it.copy(
                    isSubmitting = false, 
                    error = result.exceptionOrNull()?.message ?: "未知错误"
                ) }
            } else {
                // 登录态更新后会触发导航跳转
                _uiState.update { it.copy(isSubmitting = false) }
            }
        }
    }
}

internal fun buildAdminLoginRequest(state: LoginUiState, expireMinutes: Int): LoginRequest {
    return LoginRequest(
        username = state.username.trim(),
        password = state.password,
        expire = expireMinutes,
    )
}
