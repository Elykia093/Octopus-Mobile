package com.elykia.octopus.feature.connection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elykia.octopus.core.data.model.ServerConfig
import com.elykia.octopus.core.data.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import javax.inject.Inject

data class SetupUiState(
    val urlInput: String = "",
    val isSaving: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class SetupViewModel @Inject constructor(
    private val appRepository: AppRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SetupUiState())
    val uiState: StateFlow<SetupUiState> = _uiState.asStateFlow()

    fun updateUrlInput(url: String) {
        _uiState.update { it.copy(urlInput = url, error = null) }
    }

    fun saveConfiguration() {
        val currentUrl = _uiState.value.urlInput.trim()
        if (currentUrl.isBlank()) {
            _uiState.update { it.copy(error = "URL cannot be empty") }
            return
        }

        val finalUrl = if (!currentUrl.startsWith("http://") && !currentUrl.startsWith("https://")) {
            "https://$currentUrl"
        } else currentUrl

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            
            // Simple validation: must be a parsable HttpUrl
            val parsed = finalUrl.toHttpUrlOrNull()
            if (parsed == null) {
                _uiState.update { it.copy(isSaving = false, error = "Invalid URL format") }
                return@launch
            }

            // Save config - this triggers the AppState observation in OctopusApp and navigates forward
            appRepository.updateServerConfig(ServerConfig(baseUrl = finalUrl))
            _uiState.update { it.copy(isSaving = false) }
        }
    }
}
