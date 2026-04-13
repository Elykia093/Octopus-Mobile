package com.elykia.octopus.feature.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elykia.octopus.core.common.AppResult
import com.elykia.octopus.core.data.model.ApiKeyDashboard
import com.elykia.octopus.core.data.model.ApiKeyItem
import com.elykia.octopus.core.data.model.LatestInfo
import com.elykia.octopus.core.data.model.SettingItem
import com.elykia.octopus.core.data.repository.AppRepository
import com.elykia.octopus.core.data.repository.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingUiState(
    val loading: Boolean = true,
    val settings: List<SettingItem> = emptyList(),
    val apiKeys: List<ApiKeyItem> = emptyList(),
    val apiKeyDashboard: ApiKeyDashboard? = null,
    val latestInfo: LatestInfo? = null,
    val currentVersion: String? = null,
    val error: String? = null,
)

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val appRepository: AppRepository,
    private val dashboardRepository: DashboardRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingUiState())
    val uiState: StateFlow<SettingUiState> = _uiState

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            val settingsDeferred = async { appRepository.settings() }
            val apiKeysDeferred = async { dashboardRepository.apiKeys() }
            val dashboardDeferred = async { dashboardRepository.apiKeyDashboard() }
            val latestDeferred = async { dashboardRepository.latestInfo() }
            val versionDeferred = async { dashboardRepository.currentVersion() }

            val settings = settingsDeferred.await()
            if (settings is AppResult.Success) {
                _uiState.value = SettingUiState(
                    loading = false,
                    settings = settings.data,
                    apiKeys = (apiKeysDeferred.await() as? AppResult.Success)?.data.orEmpty(),
                    apiKeyDashboard = (dashboardDeferred.await() as? AppResult.Success)?.data,
                    latestInfo = (latestDeferred.await() as? AppResult.Success)?.data,
                    currentVersion = (versionDeferred.await() as? AppResult.Success)?.data,
                )
            } else {
                _uiState.value = SettingUiState(loading = false, error = (settings as AppResult.Error).message)
            }
        }
    }

    fun updateSetting(key: String, value: String) {
        viewModelScope.launch {
            appRepository.saveSetting(SettingItem(key, value))
            refresh()
        }
    }

    fun triggerUpdate() {
        viewModelScope.launch {
            dashboardRepository.triggerUpdate()
            refresh()
        }
    }
}
