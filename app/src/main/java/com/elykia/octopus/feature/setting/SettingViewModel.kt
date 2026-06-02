package com.elykia.octopus.feature.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elykia.octopus.core.common.AppResult
import com.elykia.octopus.core.data.model.ApiKeyItem
import com.elykia.octopus.core.data.model.ApiKeyMutationRequest
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

data class SettingSection(
    val key: String,
    val title: String,
    val summary: String,
    val items: List<SettingItem>,
)

data class SettingUiState(
    val loading: Boolean = true,
    val settings: List<SettingItem> = emptyList(),
    val sections: List<SettingSection> = emptyList(),
    val apiKeys: List<ApiKeyItem> = emptyList(),
    val latestInfo: LatestInfo? = null,
    val currentVersion: String? = null,
    val username: String = "",
    val language: String = "system",
    val themeMode: Int = 0,
    val modelLastUpdateTime: String? = null,
    val createdApiKey: ApiKeyItem? = null,
    val error: String? = null,
    val apiKeySubmitting: Boolean = false,
    val apiKeyOperationError: String? = null,
)

internal fun SettingUiState.apiKeyOperationStarted(): SettingUiState = copy(
    apiKeySubmitting = true,
    apiKeyOperationError = null,
)

internal fun SettingUiState.apiKeyOperationSucceeded(): SettingUiState = copy(
    apiKeySubmitting = false,
    apiKeyOperationError = null,
)

internal fun SettingUiState.apiKeyOperationFailed(message: String): SettingUiState = copy(
    apiKeySubmitting = false,
    apiKeyOperationError = message,
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
            val previous = _uiState.value
            val settingsDeferred = async { appRepository.settings() }
            val apiKeysDeferred = async { dashboardRepository.apiKeys() }
            val latestDeferred = async { dashboardRepository.latestInfo() }
            val versionDeferred = async { dashboardRepository.currentVersion() }
            val modelTimeDeferred = async { dashboardRepository.modelLastUpdateTime() }
            val authDeferred = async { appRepository.currentAuth() }
            val configDeferred = async { appRepository.currentServerConfig() }

            val settings = settingsDeferred.await()
            if (settings is AppResult.Success) {
                val config = configDeferred.await()
                val auth = authDeferred.await()
                _uiState.value = SettingUiState(
                    loading = false,
                    settings = settings.data,
                    sections = settings.data.toSections(),
                    apiKeys = (apiKeysDeferred.await() as? AppResult.Success)?.data.orEmpty(),
                    latestInfo = (latestDeferred.await() as? AppResult.Success)?.data,
                    currentVersion = (versionDeferred.await() as? AppResult.Success)?.data,
                    modelLastUpdateTime = (modelTimeDeferred.await() as? AppResult.Success)?.data,
                    username = auth.username,
                    language = config.language,
                    themeMode = config.themeMode,
                    createdApiKey = _uiState.value.createdApiKey,
                    apiKeySubmitting = previous.apiKeySubmitting,
                    apiKeyOperationError = previous.apiKeyOperationError,
                )
            } else {
                _uiState.value = SettingUiState(
                    loading = false,
                    error = (settings as AppResult.Error).message,
                    createdApiKey = previous.createdApiKey,
                    apiKeySubmitting = previous.apiKeySubmitting,
                    apiKeyOperationError = previous.apiKeyOperationError,
                )
            }
        }
    }

    fun refreshLatestInfo() {
        viewModelScope.launch {
            val latest = dashboardRepository.latestInfo()
            val current = dashboardRepository.currentVersion()
            _uiState.value = _uiState.value.copy(
                latestInfo = (latest as? AppResult.Success)?.data ?: _uiState.value.latestInfo,
                currentVersion = (current as? AppResult.Success)?.data ?: _uiState.value.currentVersion,
            )
        }
    }

    fun updateSetting(key: String, value: String) {
        viewModelScope.launch {
            appRepository.saveSetting(SettingItem(key, value))
            refresh()
        }
    }

    fun updateAppearance(language: String, themeMode: Int) {
        viewModelScope.launch {
            appRepository.saveAppearance(language, themeMode)
            refresh()
        }
    }

    fun triggerUpdate() {
        viewModelScope.launch {
            dashboardRepository.triggerUpdate()
            refresh()
        }
    }

    fun refreshModelPrice() {
        viewModelScope.launch {
            dashboardRepository.refreshModelPrice()
            refresh()
        }
    }

    fun syncChannelModels() {
        viewModelScope.launch {
            dashboardRepository.syncChannelModels()
            refresh()
        }
    }

    fun setApiKeyEnabled(item: ApiKeyItem, enabled: Boolean) {
        viewModelScope.launch {
            if (_uiState.value.apiKeySubmitting) return@launch
            _uiState.value = _uiState.value.apiKeyOperationStarted()
            when (val result = dashboardRepository.updateApiKey(item.copy(enabled = enabled))) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.apiKeyOperationSucceeded()
                    refresh()
                }
                is AppResult.Error -> _uiState.value = _uiState.value.apiKeyOperationFailed(result.message)
            }
        }
    }

    fun createApiKey(
        name: String,
        expireAt: Long,
        maxCost: Double,
        supportedModels: String,
        enabled: Boolean,
        onSuccess: () -> Unit = {},
    ) {
        viewModelScope.launch {
            if (_uiState.value.apiKeySubmitting) return@launch
            _uiState.value = _uiState.value.apiKeyOperationStarted()
            when (
                val result = dashboardRepository.createApiKey(
                ApiKeyMutationRequest(
                    name = name,
                    enabled = enabled,
                    expireAt = expireAt,
                    maxCost = maxCost,
                    supportedModels = supportedModels,
                )
            )) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        apiKeySubmitting = false,
                        apiKeyOperationError = null,
                        createdApiKey = result.data,
                    )
                    onSuccess()
                    refresh()
                }
                is AppResult.Error -> _uiState.value = _uiState.value.apiKeyOperationFailed(result.message)
            }
        }
    }

    fun clearApiKeyOperationError() {
        _uiState.value = _uiState.value.copy(apiKeyOperationError = null)
    }

    fun dismissCreatedApiKey() {
        _uiState.value = _uiState.value.copy(createdApiKey = null)
    }

    fun updateApiKey(
        item: ApiKeyItem,
        onSuccess: () -> Unit = {},
    ) {
        viewModelScope.launch {
            if (_uiState.value.apiKeySubmitting) return@launch
            _uiState.value = _uiState.value.apiKeyOperationStarted()
            when (val result = dashboardRepository.updateApiKey(item)) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.apiKeyOperationSucceeded()
                    onSuccess()
                    refresh()
                }
                is AppResult.Error -> _uiState.value = _uiState.value.apiKeyOperationFailed(result.message)
            }
        }
    }

    fun deleteApiKey(id: Int) {
        viewModelScope.launch {
            if (_uiState.value.apiKeySubmitting) return@launch
            _uiState.value = _uiState.value.apiKeyOperationStarted()
            when (val result = dashboardRepository.deleteApiKey(id)) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.apiKeyOperationSucceeded()
                    refresh()
                }
                is AppResult.Error -> _uiState.value = _uiState.value.apiKeyOperationFailed(result.message)
            }
        }
    }
}

private fun List<SettingItem>.toSections(): List<SettingSection> {
    val mapping = linkedMapOf(
        "system" to SettingSection(
            key = "system",
            title = "系统设置",
            summary = "服务端基础配置项。",
            items = filter { it.key in setOf("proxy_url", "stats_save_interval", "cors_allow_origins") },
        ),
        "log" to SettingSection(
            key = "log",
            title = "日志设置",
            summary = "历史日志保留与开关。",
            items = filter { it.key in setOf("relay_log_keep_period", "relay_log_keep_enabled") },
        ),
        "price" to SettingSection(
            key = "price",
            title = "模型价格",
            summary = "价格更新周期相关配置。",
            items = filter { it.key in setOf("model_info_update_interval") },
        ),
        "sync" to SettingSection(
            key = "sync",
            title = "渠道同步",
            summary = "渠道模型同步相关配置。",
            items = filter { it.key in setOf("sync_llm_interval") },
        ),
        "circuit" to SettingSection(
            key = "circuit",
            title = "熔断设置",
            summary = "连续失败熔断控制参数。",
            items = filter {
                it.key in setOf(
                    "circuit_breaker_threshold",
                    "circuit_breaker_cooldown",
                    "circuit_breaker_max_cooldown",
                )
            },
        ),
    )
    return mapping.values.filter { it.items.isNotEmpty() }
}
