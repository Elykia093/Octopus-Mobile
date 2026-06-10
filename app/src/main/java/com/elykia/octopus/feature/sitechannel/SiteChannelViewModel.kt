package com.elykia.octopus.feature.sitechannel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elykia.octopus.core.common.AppResult
import com.elykia.octopus.core.data.model.SiteChannelAccount
import com.elykia.octopus.core.data.model.SiteChannelCard
import com.elykia.octopus.core.data.model.SiteChannelKeyCreateRequest
import com.elykia.octopus.core.data.model.SiteChannelModel
import com.elykia.octopus.core.data.model.SiteGroupProjectionUpdateRequest
import com.elykia.octopus.core.data.model.SiteManualModelAddEntry
import com.elykia.octopus.core.data.model.SiteManualModelAddRequest
import com.elykia.octopus.core.data.model.SiteManualModelDeleteRequest
import com.elykia.octopus.core.data.model.SiteModelDisableUpdateRequest
import com.elykia.octopus.core.data.model.SiteModelRouteUpdateRequest
import com.elykia.octopus.core.data.model.SiteProjectedChannelSettingsUpdateRequest
import com.elykia.octopus.core.data.model.SiteSourceKeyUpdateRequest
import com.elykia.octopus.core.data.repository.SiteChannelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SiteChannelUiState(
    val loading: Boolean = true,
    val submitting: Boolean = false,
    val cards: List<SiteChannelCard> = emptyList(),
    val error: String? = null,
    val operationError: String? = null,
    val operationMessage: String? = null,
)

internal fun SiteChannelUiState.shouldShowPageError(): Boolean =
    error != null && cards.isEmpty()

@HiltViewModel
class SiteChannelViewModel @Inject constructor(
    private val repository: SiteChannelRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SiteChannelUiState())
    val uiState: StateFlow<SiteChannelUiState> = _uiState

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null, operationError = null)
            when (val result = repository.siteChannels(includeHistory = true)) {
                is AppResult.Success -> _uiState.value = _uiState.value.copy(
                    loading = false,
                    cards = result.data.sortedWith(compareBy<SiteChannelCard> { it.siteName.lowercase() }.thenBy { it.siteId }),
                    error = null,
                )
                is AppResult.Error -> _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = result.message,
                )
            }
        }
    }

    fun clearOperationFeedback() {
        _uiState.value = _uiState.value.copy(operationError = null, operationMessage = null)
    }

    fun createKey(
        siteId: Int,
        accountId: Int,
        groupKey: String,
        name: String,
        onSuccess: () -> Unit = {},
    ) {
        launchAccountMutation(
            siteId = siteId,
            successMessage = "投影 Key 已创建。",
            invalidMessage = "分组无效，不能创建投影 Key。",
            isValid = { groupKey.isNotBlank() },
            block = {
                repository.createKey(
                    siteId = siteId,
                    accountId = accountId,
                    request = SiteChannelKeyCreateRequest(groupKey = groupKey, name = name.trim().ifBlank { null }),
                )
            },
            onSuccess = onSuccess,
        )
    }

    fun updateSourceKeys(
        siteId: Int,
        accountId: Int,
        request: SiteSourceKeyUpdateRequest,
        onSuccess: () -> Unit = {},
    ) {
        launchAccountMutation(
            siteId = siteId,
            successMessage = "站点 Key 已更新。",
            invalidMessage = "没有需要保存的 Key 变更。",
            isValid = {
                !request.keysToAdd.isNullOrEmpty() ||
                    !request.keysToUpdate.isNullOrEmpty() ||
                    !request.keysToDelete.isNullOrEmpty()
            },
            block = { repository.updateSourceKeys(siteId = siteId, accountId = accountId, request = request) },
            onSuccess = onSuccess,
        )
    }

    fun updateModelRoute(
        siteId: Int,
        accountId: Int,
        groupKey: String,
        model: SiteChannelModel,
        routeType: String,
        onSuccess: () -> Unit = {},
    ) {
        launchAccountMutation(
            siteId = siteId,
            successMessage = "模型端点格式已更新。",
            invalidMessage = "模型或端点格式无效。",
            isValid = { groupKey.isNotBlank() && model.modelName.isNotBlank() && routeType.isNotBlank() },
            block = {
                repository.updateModelRoutes(
                    siteId = siteId,
                    accountId = accountId,
                    request = listOf(
                        SiteModelRouteUpdateRequest(
                            groupKey = groupKey,
                            modelName = model.modelName,
                            routeType = routeType,
                        ),
                    ),
                )
            },
            onSuccess = onSuccess,
        )
    }

    fun resetModelRoutes(siteId: Int, accountId: Int) {
        launchAccountMutation(
            siteId = siteId,
            successMessage = "模型端点格式已重置。",
            block = { repository.resetModelRoutes(siteId = siteId, accountId = accountId) },
        )
    }

    fun setModelDisabled(
        siteId: Int,
        accountId: Int,
        groupKey: String,
        model: SiteChannelModel,
        disabled: Boolean,
    ) {
        launchAccountMutation(
            siteId = siteId,
            successMessage = if (disabled) "模型已禁用。" else "模型已启用。",
            invalidMessage = "模型无效，不能更新状态。",
            isValid = { groupKey.isNotBlank() && model.modelName.isNotBlank() },
            block = {
                repository.updateModelDisabled(
                    siteId = siteId,
                    accountId = accountId,
                    request = listOf(
                        SiteModelDisableUpdateRequest(
                            groupKey = groupKey,
                            modelName = model.modelName,
                            disabled = disabled,
                        ),
                    ),
                )
            },
        )
    }

    fun addManualModels(
        siteId: Int,
        accountId: Int,
        groupKey: String,
        modelNames: List<String>,
        routeType: String,
        onSuccess: () -> Unit = {},
    ) {
        val names = modelNames.map { it.trim() }.filter { it.isNotBlank() }.distinct()
        launchAccountMutation(
            siteId = siteId,
            successMessage = "自定义模型已添加。",
            invalidMessage = "请输入至少一个模型名称。",
            isValid = { groupKey.isNotBlank() && names.isNotEmpty() && routeType.isNotBlank() },
            block = {
                repository.addManualModels(
                    siteId = siteId,
                    accountId = accountId,
                    request = SiteManualModelAddRequest(
                        groupKey = groupKey,
                        models = names.map { name -> SiteManualModelAddEntry(modelName = name, routeType = routeType) },
                    ),
                )
            },
            onSuccess = onSuccess,
        )
    }

    fun deleteManualModel(
        siteId: Int,
        accountId: Int,
        groupKey: String,
        model: SiteChannelModel,
    ) {
        launchAccountMutation(
            siteId = siteId,
            successMessage = "自定义模型已删除。",
            invalidMessage = "只能删除自定义模型。",
            isValid = { groupKey.isNotBlank() && model.modelName.isNotBlank() && model.source == "manual" },
            block = {
                repository.deleteManualModel(
                    siteId = siteId,
                    accountId = accountId,
                    request = SiteManualModelDeleteRequest(groupKey = groupKey, modelName = model.modelName),
                )
            },
        )
    }

    fun updateProjectedChannelSettings(
        siteId: Int,
        accountId: Int,
        request: List<SiteProjectedChannelSettingsUpdateRequest>,
        onSuccess: () -> Unit = {},
    ) {
        launchAccountMutation(
            siteId = siteId,
            successMessage = "投影渠道设置已更新。",
            invalidMessage = "没有需要保存的投影渠道设置。",
            isValid = { request.isNotEmpty() },
            block = { repository.updateProjectedChannelSettings(siteId = siteId, accountId = accountId, request = request) },
            onSuccess = onSuccess,
        )
    }

    fun setGroupProjection(
        siteId: Int,
        accountId: Int,
        groupKey: String,
        projectionDisabled: Boolean,
    ) {
        launchAccountMutation(
            siteId = siteId,
            successMessage = if (projectionDisabled) "分组投影已停用。" else "分组投影已启用。",
            invalidMessage = "分组无效，不能更新投影状态。",
            isValid = { groupKey.isNotBlank() },
            block = {
                repository.updateGroupProjection(
                    siteId = siteId,
                    accountId = accountId,
                    request = SiteGroupProjectionUpdateRequest(groupKey = groupKey, projectionDisabled = projectionDisabled),
                )
            },
        )
    }

    private fun launchAccountMutation(
        siteId: Int,
        successMessage: String,
        invalidMessage: String = "输入无效。",
        isValid: () -> Boolean = { true },
        block: suspend () -> AppResult<SiteChannelAccount>,
        onSuccess: () -> Unit = {},
    ) {
        viewModelScope.launch {
            if (_uiState.value.submitting) return@launch
            if (!isValid()) {
                _uiState.value = _uiState.value.copy(operationError = invalidMessage, operationMessage = null)
                return@launch
            }
            _uiState.value = _uiState.value.copy(submitting = true, operationError = null, operationMessage = null)
            when (val result = block()) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        submitting = false,
                        cards = _uiState.value.cards.replaceAccount(siteId, result.data),
                        operationError = null,
                        operationMessage = successMessage,
                    )
                    onSuccess()
                }
                is AppResult.Error -> {
                    _uiState.value = _uiState.value.copy(submitting = false, operationError = result.message)
                }
            }
        }
    }
}

private fun List<SiteChannelCard>.replaceAccount(siteId: Int, account: SiteChannelAccount): List<SiteChannelCard> = map { card ->
    if (card.siteId != siteId) {
        card
    } else {
        card.copy(
            accounts = card.accounts.map { current ->
                if (current.accountId == account.accountId) account else current
            },
        )
    }
}
