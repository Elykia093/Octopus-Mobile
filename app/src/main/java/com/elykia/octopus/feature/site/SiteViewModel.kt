package com.elykia.octopus.feature.site

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elykia.octopus.core.common.AppResult
import com.elykia.octopus.core.common.map
import com.elykia.octopus.core.data.model.AllApiHubImportResult
import com.elykia.octopus.core.data.model.MetApiImportResult
import com.elykia.octopus.core.data.model.Site
import com.elykia.octopus.core.data.model.SiteAccount
import com.elykia.octopus.core.data.repository.SiteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SiteUiState(
    val loading: Boolean = true,
    val submitting: Boolean = false,
    val archivedLoading: Boolean = false,
    val sites: List<Site> = emptyList(),
    val archivedSites: List<Site> = emptyList(),
    val error: String? = null,
    val operationError: String? = null,
    val operationMessage: String? = null,
)

internal fun SiteUiState.shouldShowPageError(): Boolean =
    error != null && sites.isEmpty()

@HiltViewModel
class SiteViewModel @Inject constructor(
    private val repository: SiteRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SiteUiState())
    val uiState: StateFlow<SiteUiState> = _uiState

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null, operationError = null)
            when (val result = repository.sites()) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        loading = false,
                        sites = result.data.sortedWith(compareByDescending<Site> { it.isPinned }.thenBy { it.sortOrder }.thenBy { it.name }),
                        error = null,
                    )
                }
                is AppResult.Error -> {
                    _uiState.value = _uiState.value.copy(loading = false, error = result.message)
                }
            }
        }
    }

    fun refreshArchived() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(archivedLoading = true, operationError = null)
            when (val result = repository.archivedSites()) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(archivedLoading = false, archivedSites = result.data)
                }
                is AppResult.Error -> {
                    _uiState.value = _uiState.value.copy(archivedLoading = false, operationError = result.message)
                }
            }
        }
    }

    fun clearOperationFeedback() {
        _uiState.value = _uiState.value.copy(operationError = null, operationMessage = null)
    }

    fun createSite(values: SiteEditorValues, onSuccess: () -> Unit = {}) {
        launchMutation(
            invalidMessage = "请输入有效的站点名称、地址和高级配置。",
            isValid = { canSubmitSite(values, submitting = false) },
            block = { repository.createSite(values.toCreateRequest()) },
            onSuccess = { onSuccess() },
        )
    }

    fun updateSite(site: Site, values: SiteEditorValues, onSuccess: () -> Unit = {}) {
        launchMutation(
            invalidMessage = "请输入有效的站点名称、地址和高级配置。",
            isValid = { canSubmitSite(values, submitting = false) },
            block = { repository.updateSite(values.toUpdateRequest(site)) },
            onSuccess = { onSuccess() },
        )
    }

    fun setSiteEnabled(site: Site, enabled: Boolean) {
        launchMutation(
            block = { repository.setSiteEnabled(site.id, enabled) },
        )
    }

    fun archiveSite(site: Site) {
        launchMutation(
            block = { repository.archiveSite(site.id) },
        )
    }

    fun restoreSite(site: Site) {
        launchMutation(
            block = { repository.restoreSite(site.id) },
            onSuccess = { refreshArchived() },
        )
    }

    fun deleteSite(site: Site) {
        launchMutation(
            block = { repository.deleteSite(site.id) },
        )
    }

    fun createAccount(site: Site, values: SiteAccountEditorValues, onSuccess: () -> Unit = {}) {
        launchMutation(
            invalidMessage = "请输入有效的账号名称和凭据。",
            isValid = { canSubmitSiteAccount(values, original = null, submitting = false, sitePlatform = site.platform) },
            block = { repository.createAccount(values.toCreateRequest(site.id)) },
            onSuccess = { onSuccess() },
        )
    }

    fun updateAccount(account: SiteAccount, values: SiteAccountEditorValues, onSuccess: () -> Unit = {}) {
        val sitePlatform = _uiState.value.sites.firstOrNull { it.id == account.siteId }?.platform
        launchMutation(
            invalidMessage = "请输入有效的账号名称和凭据。",
            isValid = { canSubmitSiteAccount(values, original = account, submitting = false, sitePlatform = sitePlatform) },
            block = { repository.updateAccount(values.toUpdateRequest(account)) },
            onSuccess = { onSuccess() },
        )
    }

    fun setAccountEnabled(account: SiteAccount, enabled: Boolean) {
        launchMutation(
            block = { repository.setAccountEnabled(account.id, enabled) },
        )
    }

    fun deleteAccount(account: SiteAccount) {
        launchMutation(
            block = { repository.deleteAccount(account.id) },
        )
    }

    fun syncAccount(account: SiteAccount) {
        launchMutation(
            block = { repository.syncAccount(account.id) },
            onSuccess = {
                _uiState.value = _uiState.value.copy(operationMessage = "同步已完成。")
            },
        )
    }

    fun checkinAccount(account: SiteAccount) {
        launchMutation(
            block = { repository.checkinAccount(account.id) },
            onSuccess = {
                _uiState.value = _uiState.value.copy(operationMessage = "签到已完成。")
            },
        )
    }

    fun syncAll() {
        launchMutation(
            block = { repository.syncAll() },
            onSuccess = {
                _uiState.value = _uiState.value.copy(operationMessage = "批量同步已开始。")
            },
        )
    }

    fun checkinAll() {
        launchMutation(
            block = { repository.checkinAll() },
            onSuccess = {
                _uiState.value = _uiState.value.copy(operationMessage = "批量签到已开始。")
            },
        )
    }

    fun importSites(source: SiteImportSource, payload: String, onSuccess: () -> Unit = {}) {
        launchMutation(
            invalidMessage = "请粘贴有效的导出 JSON。",
            isValid = { payload.isNotBlank() },
            block = {
                when (source) {
                    SiteImportSource.AllApiHub -> repository.importAllApiHub(payload).map { it.toOperationMessage() }
                    SiteImportSource.MetApi -> repository.importMetApi(payload).map { it.toOperationMessage() }
                }
            },
            onSuccess = { message ->
                _uiState.value = _uiState.value.copy(operationMessage = message)
                onSuccess()
            },
        )
    }

    private fun <T> launchMutation(
        invalidMessage: String? = null,
        isValid: () -> Boolean = { true },
        block: suspend () -> AppResult<T>,
        onSuccess: (T) -> Unit = {},
    ) {
        viewModelScope.launch {
            if (_uiState.value.submitting) return@launch
            if (!isValid()) {
                _uiState.value = _uiState.value.copy(operationError = invalidMessage ?: "输入无效。")
                return@launch
            }
            _uiState.value = _uiState.value.copy(submitting = true, operationError = null, operationMessage = null)
            when (val result = block()) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(submitting = false, operationError = null)
                    onSuccess(result.data)
                    refresh()
                }
                is AppResult.Error -> {
                    _uiState.value = _uiState.value.copy(submitting = false, operationError = result.message)
                }
            }
        }
    }
}

private fun AllApiHubImportResult.toOperationMessage(): String =
    "All API Hub 导入完成：创建站点 $createdSites，复用站点 $reusedSites，创建账号 $createdAccounts，更新账号 $updatedAccounts，跳过 $skippedAccounts，计划同步 $scheduledSyncAccounts。"

private fun MetApiImportResult.toOperationMessage(): String =
    "MetAPI 导入完成：创建站点 $createdSites，复用站点 $reusedSites，创建账号 $createdAccounts，更新账号 $updatedAccounts，跳过 $skippedAccounts，导入 token $importedTokens，分组 $importedGroups，模型 $importedModels。"
