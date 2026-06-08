package com.elykia.octopus.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elykia.octopus.core.common.AppResult
import com.elykia.octopus.core.data.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class LoginMode { User, ApiKey }

data class LoginUiState(
    val mode: LoginMode = LoginMode.User,
    val serverUrl: String = "",
    val showServerField: Boolean = false,
    val username: String = DEFAULT_LOGIN_USERNAME,
    val password: String = "",
    val apiKey: String = "",
    val passwordVisible: Boolean = false,
    val expireDays: String = "7",
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val appRepository: AppRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun initFromConfig(hasServer: Boolean, currentUrl: String) {
        _uiState.value = _uiState.value.copy(
            showServerField = !hasServer,
            serverUrl = currentUrl,
        )
    }

    fun updateServerUrl(value: String) {
        _uiState.value = _uiState.value.copy(serverUrl = value, error = null)
    }

    fun updateMode(value: LoginMode) {
        _uiState.value = _uiState.value.copy(mode = value, error = null)
    }

    fun updateUsername(value: String) {
        _uiState.value = _uiState.value.copy(username = value, error = null)
    }

    fun updatePassword(value: String) {
        _uiState.value = _uiState.value.copy(password = value, error = null)
    }

    fun updateApiKey(value: String) {
        _uiState.value = _uiState.value.copy(apiKey = value, error = null)
    }

    fun updatePasswordVisible(value: Boolean) {
        _uiState.value = _uiState.value.copy(passwordVisible = value)
    }

    fun updateExpireDays(value: String) {
        _uiState.value = _uiState.value.copy(expireDays = value, error = null)
    }

    fun submit(onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (_uiState.value.isLoading) return@launch
            val state = _uiState.value
            val inputError = loginInputError(state)
            if (inputError != null) {
                _uiState.value = state.copy(error = inputError)
                return@launch
            }
            _uiState.value = state.copy(isLoading = true, error = null)
            // 如果需要先保存服务器地址
            if (state.showServerField) {
                when (val saveResult = appRepository.saveServerUrl(state.serverUrl)) {
                    is AppResult.Error -> {
                        _uiState.value = _uiState.value.copy(isLoading = false, error = saveResult.message)
                        return@launch
                    }
                    is AppResult.Success -> { /* 继续 */ }
                }
            }

            val result = when (state.mode) {
                LoginMode.User -> {
                    val days = parseLoginExpireDays(state.expireDays)
                    if (days == null) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "请输入 1 到 3650 之间的令牌天数。",
                        )
                        return@launch
                    }
                    appRepository.login(state.username.trim(), state.password, days)
                }
                LoginMode.ApiKey -> appRepository.loginApiKey(state.apiKey.trim())
            }

            when (result) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onSuccess()
                }
                is AppResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                }
            }
        }
    }
}

internal fun canSubmitLogin(state: LoginUiState): Boolean =
    !state.isLoading && loginInputError(state) == null

internal fun loginInlineError(state: LoginUiState): String? {
    val error = loginInputError(state) ?: return null
    val userHasStartedInput = state.username != DEFAULT_LOGIN_USERNAME ||
        state.password.isNotBlank() ||
        state.apiKey.isNotEmpty() ||
        (state.showServerField && state.serverUrl.isNotBlank()) ||
        (state.expireDays.isNotBlank() && parseLoginExpireDays(state.expireDays) == null)
    return error.takeIf { userHasStartedInput }
}

internal fun loginInputError(state: LoginUiState): String? = when {
    state.showServerField && state.serverUrl.isBlank() -> "请输入服务器地址。"
    state.mode == LoginMode.ApiKey && state.apiKey.isBlank() -> "请输入 API 密钥。"
    state.mode == LoginMode.ApiKey -> null
    state.username.isBlank() -> "请输入用户名。"
    state.password.isBlank() -> "请输入密码。"
    parseLoginExpireDays(state.expireDays) == null -> "请输入 1 到 3650 之间的令牌天数。"
    else -> null
}

internal fun parseLoginExpireDays(value: String): Int? =
    value.trim().toIntOrNull()?.takeIf { it in 1..MAX_LOGIN_EXPIRE_DAYS }

private const val DEFAULT_LOGIN_USERNAME = "admin"
private const val MAX_LOGIN_EXPIRE_DAYS = 3650
