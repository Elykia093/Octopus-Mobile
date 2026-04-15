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

data class LoginUiState(
    val serverUrl: String = "",
    val showServerField: Boolean = false,
    val username: String = "admin",
    val password: String = "",
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

    fun updateUsername(value: String) {
        _uiState.value = _uiState.value.copy(username = value, error = null)
    }

    fun updatePassword(value: String) {
        _uiState.value = _uiState.value.copy(password = value, error = null)
    }

    fun updatePasswordVisible(value: Boolean) {
        _uiState.value = _uiState.value.copy(passwordVisible = value)
    }

    fun updateExpireDays(value: String) {
        _uiState.value = _uiState.value.copy(expireDays = value, error = null)
    }

    fun submit(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val state = _uiState.value

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

            // 登录
            val days = state.expireDays.toIntOrNull() ?: 7
            when (val result = appRepository.login(state.username, state.password, days)) {
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
