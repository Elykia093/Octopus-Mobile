package com.elykia.octopus.feature.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elykia.octopus.core.common.AppResult
import com.elykia.octopus.core.data.model.ApiKeyItem
import com.elykia.octopus.core.data.model.ApiKeyMutationRequest
import com.elykia.octopus.core.data.model.LatestInfo
import com.elykia.octopus.core.data.model.SettingItem
import com.elykia.octopus.core.data.repository.ApiKeyRepository
import com.elykia.octopus.core.data.repository.AppRepository
import com.elykia.octopus.core.data.repository.ChannelRepository
import com.elykia.octopus.core.data.repository.DataTransferRepository
import com.elykia.octopus.core.data.repository.ModelRepository
import com.elykia.octopus.core.data.repository.UpdateRepository
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
    val apiKeyListError: String? = null,
    val versionInfoError: String? = null,
    val modelLastUpdateError: String? = null,
    val apiKeySubmitting: Boolean = false,
    val apiKeyOperationError: String? = null,
    val dataTransferSubmitting: Boolean = false,
    val dataTransferMessage: String? = null,
    val dataTransferError: String? = null,
    val actionSubmitting: Boolean = false,
    val actionMessage: String? = null,
    val actionError: String? = null,
    val apiKeySelectionMode: Boolean = false,
    val selectedApiKeyIds: Set<Int> = emptySet(),
    val batchApiKeyOperationProgress: String? = null,
)

internal fun SettingUiState.shouldShowSettingsPageError(): Boolean =
    error != null && sections.isEmpty()

internal fun SettingUiState.shouldShowApiKeyPageError(): Boolean =
    apiKeyListError != null && apiKeys.isEmpty()

internal fun SettingUiState.apiKeyOperationStarted(): SettingUiState = copy(
    apiKeySubmitting = true,
    apiKeyOperationError = null,
    createdApiKey = null,
)

internal fun SettingUiState.apiKeyOperationSucceeded(): SettingUiState = copy(
    apiKeySubmitting = false,
    apiKeyOperationError = null,
)

internal fun SettingUiState.apiKeyOperationFailed(message: String): SettingUiState = copy(
    apiKeySubmitting = false,
    apiKeyOperationError = message,
)

internal fun SettingUiState.dataTransferStarted(): SettingUiState = copy(
    dataTransferSubmitting = true,
    dataTransferMessage = null,
    dataTransferError = null,
)

internal fun SettingUiState.dataTransferSucceeded(message: String): SettingUiState = copy(
    dataTransferSubmitting = false,
    dataTransferMessage = message,
    dataTransferError = null,
)

internal fun SettingUiState.dataTransferFailed(message: String): SettingUiState = copy(
    dataTransferSubmitting = false,
    dataTransferMessage = null,
    dataTransferError = message,
)

internal fun SettingUiState.actionStarted(): SettingUiState = copy(
    actionSubmitting = true,
    actionMessage = null,
    actionError = null,
)

internal fun SettingUiState.actionSucceeded(message: String): SettingUiState = copy(
    actionSubmitting = false,
    actionMessage = message,
    actionError = null,
)

internal fun SettingUiState.actionFailed(message: String): SettingUiState = copy(
    actionSubmitting = false,
    actionMessage = null,
    actionError = message,
)

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val appRepository: AppRepository,
    private val apiKeyRepository: ApiKeyRepository,
    private val updateRepository: UpdateRepository,
    private val modelRepository: ModelRepository,
    private val channelRepository: ChannelRepository,
    private val dataTransferRepository: DataTransferRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingUiState())
    val uiState: StateFlow<SettingUiState> = _uiState

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val previous = _uiState.value
            _uiState.value = previous.copy(loading = true, error = null)
            val settingsDeferred = async { appRepository.settings() }
            val apiKeysDeferred = async { apiKeyRepository.apiKeys() }
            val latestDeferred = async { updateRepository.latestInfo() }
            val versionDeferred = async { updateRepository.currentVersion() }
            val modelTimeDeferred = async { modelRepository.modelLastUpdateTime() }
            val authDeferred = async { appRepository.currentAuth() }
            val configDeferred = async { appRepository.currentServerConfig() }

            val settings = settingsDeferred.await()
            val config = configDeferred.await()
            val auth = authDeferred.await()
            val apiKeys = apiKeysDeferred.await()
            val latest = latestDeferred.await()
            val version = versionDeferred.await()
            val modelTime = modelTimeDeferred.await()
            _uiState.value = buildSettingRefreshState(
                previous = previous,
                settingsResult = settings,
                apiKeysResult = apiKeys,
                latestResult = latest,
                versionResult = version,
                modelTimeResult = modelTime,
                username = auth.username,
                language = config.language,
                themeMode = config.themeMode,
            )
        }
    }

    fun refreshLatestInfo() {
        viewModelScope.launch {
            if (_uiState.value.actionSubmitting) return@launch
            _uiState.value = _uiState.value.actionStarted()
            val latest = updateRepository.latestInfo()
            val current = updateRepository.currentVersion()
            val latestData = (latest as? AppResult.Success)?.data
            val currentData = (current as? AppResult.Success)?.data
            if (latestData != null || currentData != null) {
                _uiState.value = _uiState.value.actionSucceeded("版本信息已刷新。").copy(
                    latestInfo = latestData ?: _uiState.value.latestInfo,
                    currentVersion = currentData ?: _uiState.value.currentVersion,
                )
            } else {
                _uiState.value = _uiState.value.actionFailed(
                    (latest as? AppResult.Error)?.message
                        ?: (current as? AppResult.Error)?.message
                        ?: "刷新版本信息失败。",
                )
            }
        }
    }

    fun updateSetting(key: String, value: String) {
        viewModelScope.launch {
            if (_uiState.value.actionSubmitting) return@launch
            _uiState.value = _uiState.value.actionStarted()
            when (val result = appRepository.saveSetting(SettingItem(key, value))) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.actionSucceeded("设置已保存。")
                    refresh()
                }
                is AppResult.Error -> _uiState.value = _uiState.value.actionFailed(result.message)
            }
        }
    }

    fun updateAppearance(language: String, themeMode: Int) {
        viewModelScope.launch {
            if (_uiState.value.actionSubmitting) return@launch
            _uiState.value = _uiState.value.actionStarted()
            when (val result = appRepository.saveAppearance(language, themeMode)) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.actionSucceeded("外观设置已保存。")
                    refresh()
                }
                is AppResult.Error -> _uiState.value = _uiState.value.actionFailed(result.message)
            }
        }
    }

    fun triggerUpdate() {
        viewModelScope.launch {
            if (_uiState.value.actionSubmitting) return@launch
            _uiState.value = _uiState.value.actionStarted()
            when (val result = updateRepository.triggerUpdate()) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.actionSucceeded(result.data.ifBlank { "更新任务已触发。" })
                    refresh()
                }
                is AppResult.Error -> _uiState.value = _uiState.value.actionFailed(result.message)
            }
        }
    }

    fun refreshModelPrice() {
        viewModelScope.launch {
            if (_uiState.value.actionSubmitting) return@launch
            _uiState.value = _uiState.value.actionStarted()
            when (val result = modelRepository.refreshModelPrice()) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.actionSucceeded(result.data?.takeIf { it.isNotBlank() } ?: "模型价格已刷新。")
                    refresh()
                }
                is AppResult.Error -> _uiState.value = _uiState.value.actionFailed(result.message)
            }
        }
    }

    fun syncChannelModels() {
        viewModelScope.launch {
            if (_uiState.value.actionSubmitting) return@launch
            _uiState.value = _uiState.value.actionStarted()
            when (val result = channelRepository.syncChannelModels()) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.actionSucceeded(result.data?.takeIf { it.isNotBlank() } ?: "渠道模型已同步。")
                    refresh()
                }
                is AppResult.Error -> _uiState.value = _uiState.value.actionFailed(result.message)
            }
        }
    }

    fun exportData(onReady: (ByteArray) -> Unit) {
        viewModelScope.launch {
            if (_uiState.value.dataTransferSubmitting) return@launch
            _uiState.value = _uiState.value.dataTransferStarted()
            when (val result = dataTransferRepository.exportData(includeLogs = false, includeStats = false)) {
                is AppResult.Success -> runCatching { onReady(result.data) }
                    .onFailure { exception ->
                        _uiState.value = _uiState.value.dataTransferFailed(exception.message ?: "启动导出失败。")
                    }
                is AppResult.Error -> _uiState.value = _uiState.value.dataTransferFailed(result.message)
            }
        }
    }

    fun importData(fileName: String, content: ByteArray) {
        viewModelScope.launch {
            if (_uiState.value.dataTransferSubmitting) return@launch
            _uiState.value = _uiState.value.dataTransferStarted()
            when (val result = dataTransferRepository.importData(fileName, content)) {
                is AppResult.Success -> {
                    val rows = result.data.rowsAffected.values.sum()
                    _uiState.value = _uiState.value.dataTransferSucceeded("导入完成，更新 $rows 行。")
                    refresh()
                }
                is AppResult.Error -> _uiState.value = _uiState.value.dataTransferFailed(result.message)
            }
        }
    }

    fun markDataTransferSucceeded(message: String) {
        _uiState.value = _uiState.value.dataTransferSucceeded(message)
    }

    fun markDataTransferFailed(message: String) {
        _uiState.value = _uiState.value.dataTransferFailed(message)
    }

    fun clearDataTransferStatus() {
        _uiState.value = _uiState.value.copy(dataTransferMessage = null, dataTransferError = null)
    }

    fun clearActionStatus() {
        _uiState.value = _uiState.value.copy(actionMessage = null, actionError = null)
    }

    fun setApiKeyEnabled(item: ApiKeyItem, enabled: Boolean) {
        viewModelScope.launch {
            if (_uiState.value.apiKeySubmitting) return@launch
            _uiState.value = _uiState.value.apiKeyOperationStarted()
            when (val result = apiKeyRepository.updateApiKey(item.copy(enabled = enabled))) {
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
                val result = apiKeyRepository.createApiKey(
                ApiKeyMutationRequest(
                    name = name,
                    enabled = enabled,
                    expireAt = expireAt,
                    maxCost = maxCost,
                    supportedModels = supportedModels,
                )
            )) {
                is AppResult.Success -> {
                    val visibleCreatedKey = result.data
                    val hiddenListItem = visibleCreatedKey.copy(apiKey = "")
                    _uiState.value = _uiState.value.copy(
                        apiKeys = _uiState.value.apiKeys
                            .filterNot { it.id == visibleCreatedKey.id } + hiddenListItem,
                        apiKeySubmitting = false,
                        apiKeyOperationError = null,
                        createdApiKey = visibleCreatedKey,
                    )
                    onSuccess()
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
            when (val result = apiKeyRepository.updateApiKey(item)) {
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
            when (val result = apiKeyRepository.deleteApiKey(id)) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.apiKeyOperationSucceeded()
                    refresh()
                }
                is AppResult.Error -> _uiState.value = _uiState.value.apiKeyOperationFailed(result.message)
            }
        }
    }

    fun enterApiKeySelectionMode() {
        _uiState.value = _uiState.value.copy(apiKeySelectionMode = true, selectedApiKeyIds = emptySet())
    }

    fun exitApiKeySelectionMode() {
        _uiState.value = _uiState.value.copy(apiKeySelectionMode = false, selectedApiKeyIds = emptySet())
    }

    fun toggleApiKeySelection(id: Int) {
        val currentSelected = _uiState.value.selectedApiKeyIds
        _uiState.value = _uiState.value.copy(
            selectedApiKeyIds = if (id in currentSelected) {
                currentSelected - id
            } else {
                currentSelected + id
            }
        )
    }

    fun selectAllApiKeys() {
        _uiState.value = _uiState.value.copy(
            selectedApiKeyIds = _uiState.value.apiKeys.map { it.id }.toSet()
        )
    }

    fun batchDeleteApiKeys(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            if (_uiState.value.apiKeySubmitting) return@launch
            val selectedIds = _uiState.value.selectedApiKeyIds
            if (selectedIds.isEmpty()) return@launch

            _uiState.value = _uiState.value.apiKeyOperationStarted()
            var successCount = 0
            var failCount = 0

            selectedIds.forEachIndexed { index, id ->
                _uiState.value = _uiState.value.copy(
                    batchApiKeyOperationProgress = "删除中 ${index + 1}/${selectedIds.size}..."
                )
                when (apiKeyRepository.deleteApiKey(id)) {
                    is AppResult.Success -> successCount++
                    is AppResult.Error -> failCount++
                }
            }

            _uiState.value = _uiState.value.copy(
                apiKeySubmitting = false,
                batchApiKeyOperationProgress = null,
                apiKeySelectionMode = false,
                selectedApiKeyIds = emptySet(),
                apiKeyOperationError = if (failCount > 0) {
                    "批量删除完成：成功 $successCount 个，失败 $failCount 个"
                } else null
            )
            refresh()
            onComplete()
        }
    }

    fun batchSetApiKeysEnabled(enabled: Boolean, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            if (_uiState.value.apiKeySubmitting) return@launch
            val selectedIds = _uiState.value.selectedApiKeyIds
            if (selectedIds.isEmpty()) return@launch

            _uiState.value = _uiState.value.apiKeyOperationStarted()
            var successCount = 0
            var failCount = 0

            val apiKeysMap = _uiState.value.apiKeys.associateBy { it.id }
            selectedIds.forEachIndexed { index, id ->
                _uiState.value = _uiState.value.copy(
                    batchApiKeyOperationProgress = "${if (enabled) "启用" else "禁用"}中 ${index + 1}/${selectedIds.size}..."
                )
                val apiKey = apiKeysMap[id]
                if (apiKey != null) {
                    when (apiKeyRepository.updateApiKey(apiKey.copy(enabled = enabled))) {
                        is AppResult.Success -> successCount++
                        is AppResult.Error -> failCount++
                    }
                }
            }

            _uiState.value = _uiState.value.copy(
                apiKeySubmitting = false,
                batchApiKeyOperationProgress = null,
                apiKeySelectionMode = false,
                selectedApiKeyIds = emptySet(),
                apiKeyOperationError = if (failCount > 0) {
                    "批量操作完成：成功 $successCount 个，失败 $failCount 个"
                } else null
            )
            refresh()
            onComplete()
        }
    }
}

internal fun buildSettingRefreshState(
    previous: SettingUiState,
    settingsResult: AppResult<List<SettingItem>>,
    apiKeysResult: AppResult<List<ApiKeyItem>>,
    latestResult: AppResult<LatestInfo>,
    versionResult: AppResult<String>,
    modelTimeResult: AppResult<String>,
    username: String,
    language: String,
    themeMode: Int,
): SettingUiState {
    val apiKeyListError = apiKeysResult.errorMessageOrNull()
    val versionInfoError = latestResult.errorMessageOrNull() ?: versionResult.errorMessageOrNull()
    val modelLastUpdateError = modelTimeResult.errorMessageOrNull()

    return when (settingsResult) {
        is AppResult.Success -> previous.copy(
            loading = false,
            settings = settingsResult.data,
            sections = settingsResult.data.toSections(),
            apiKeys = apiKeysResult.dataOrPrevious(previous.apiKeys),
            latestInfo = latestResult.dataOrPreviousNullable(previous.latestInfo),
            currentVersion = versionResult.dataOrPreviousNullable(previous.currentVersion),
            modelLastUpdateTime = modelTimeResult.dataOrPreviousNullable(previous.modelLastUpdateTime),
            username = username,
            language = language,
            themeMode = themeMode,
            error = null,
            apiKeyListError = apiKeyListError,
            versionInfoError = versionInfoError,
            modelLastUpdateError = modelLastUpdateError,
        )
        is AppResult.Error -> previous.copy(
            loading = false,
            apiKeys = apiKeysResult.dataOrPrevious(previous.apiKeys),
            latestInfo = latestResult.dataOrPreviousNullable(previous.latestInfo),
            currentVersion = versionResult.dataOrPreviousNullable(previous.currentVersion),
            modelLastUpdateTime = modelTimeResult.dataOrPreviousNullable(previous.modelLastUpdateTime),
            username = username,
            language = language,
            themeMode = themeMode,
            error = settingsResult.message,
            apiKeyListError = apiKeyListError,
            versionInfoError = versionInfoError,
            modelLastUpdateError = modelLastUpdateError,
        )
    }
}

internal fun <T> AppResult<T>.dataOrPrevious(previous: T): T = when (this) {
    is AppResult.Success -> data
    is AppResult.Error -> previous
}

internal fun <T> AppResult<T>.dataOrPreviousNullable(previous: T?): T? = when (this) {
    is AppResult.Success -> data
    is AppResult.Error -> previous
}

internal fun AppResult<*>.errorMessageOrNull(): String? = when (this) {
    is AppResult.Success -> null
    is AppResult.Error -> message
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
