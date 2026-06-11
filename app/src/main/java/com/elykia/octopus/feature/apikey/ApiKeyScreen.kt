package com.elykia.octopus.feature.apikey

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.elykia.octopus.R
import com.elykia.octopus.core.data.model.ApiKeyItem
import com.elykia.octopus.core.designsystem.AppLazyPageScaffold
import com.elykia.octopus.core.designsystem.DangerConfirmDialog
import com.elykia.octopus.core.designsystem.ErrorStateCard
import com.elykia.octopus.core.designsystem.InlineEmptyCard
import com.elykia.octopus.core.designsystem.LoadingStateCard
import com.elykia.octopus.core.designsystem.OperationErrorCard
import com.elykia.octopus.core.designsystem.PageActionButton
import com.elykia.octopus.core.designsystem.SearchField
import com.elykia.octopus.core.designsystem.icons.AppMiuixIcons

@Composable
fun ApiKeyScreen(
    contentPadding: PaddingValues,
    viewModel: ApiKeyViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var searchTerm by remember { mutableStateOf("") }
    var searchVisible by remember { mutableStateOf(false) }
    var deletingId by remember { mutableStateOf<Int?>(null) }
    var showBatchDeleteConfirm by remember { mutableStateOf(false) }
    var batchAction by remember { mutableStateOf<ApiKeyBatchAction?>(null) }
    var editingItem by remember { mutableStateOf<ApiKeyItem?>(null) }
    var viewingStatsItem by remember { mutableStateOf<ApiKeyItem?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }

    val keys = uiState.apiKeys
        .filter { item ->
            searchTerm.isBlank() ||
                item.name.contains(searchTerm, ignoreCase = true) ||
                item.supportedModels.orEmpty().contains(searchTerm, ignoreCase = true)
        }
        .sortedByDescending { it.enabled }

    Box(modifier = Modifier.fillMaxSize()) {
        AppLazyPageScaffold(
            title = if (uiState.apiKeySelectionMode) {
                "已选 ${uiState.selectedApiKeyIds.size} 项"
            } else {
                stringResource(R.string.apikey_title)
            },
            actions = {
                if (uiState.apiKeySelectionMode) {
                    PageActionButton(
                        icon = AppMiuixIcons.Close,
                        contentDescription = "退出选择",
                        enabled = !uiState.apiKeySubmitting,
                        onClick = { viewModel.exitApiKeySelectionMode() },
                    )
                    PageActionButton(
                        icon = AppMiuixIcons.Check,
                        contentDescription = "全选",
                        enabled = !uiState.apiKeySubmitting,
                        onClick = { viewModel.selectAllApiKeys() },
                    )
                } else {
                    PageActionButton(
                        icon = AppMiuixIcons.More,
                        contentDescription = "批量操作",
                        enabled = !uiState.loading && !uiState.shouldShowApiKeyPageError() && uiState.apiKeys.isNotEmpty(),
                        onClick = { viewModel.enterApiKeySelectionMode() },
                    )
                    PageActionButton(
                        icon = if (searchVisible) AppMiuixIcons.Close else AppMiuixIcons.Search,
                        contentDescription = stringResource(R.string.action_open_search),
                        enabled = !uiState.loading && !uiState.shouldShowApiKeyPageError(),
                        onClick = {
                            searchVisible = !searchVisible
                            if (!searchVisible) searchTerm = ""
                        },
                    )
                    PageActionButton(
                        icon = AppMiuixIcons.Add,
                        contentDescription = stringResource(R.string.action_create),
                        enabled = !uiState.loading && !uiState.shouldShowApiKeyPageError() && !uiState.apiKeySubmitting,
                        onClick = {
                            viewModel.clearApiKeyOperationError()
                            showCreateDialog = true
                        },
                    )
                }
            },
            contentPadding = contentPadding,
        ) {
            when {
                uiState.loading -> item {
                    LoadingStateCard(title = stringResource(R.string.apikey_title))
                }
                uiState.shouldShowApiKeyPageError() -> item {
                    ErrorStateCard(
                        message = uiState.apiKeyListError ?: stringResource(R.string.error_title),
                        onRetry = viewModel::refresh,
                    )
                }
                else -> {
                    if (uiState.apiKeys.isNotEmpty() && searchVisible) {
                        item {
                            SearchField(
                                value = searchTerm,
                                onValueChange = { searchTerm = it },
                                hint = stringResource(R.string.setting_apikey_title),
                            )
                        }
                    }
                    if (!showCreateDialog && editingItem == null) {
                        uiState.apiKeyListError?.takeIf { it.isNotBlank() }?.let { error ->
                            item { OperationErrorCard(message = error) }
                        }
                        uiState.apiKeyOperationError?.takeIf { it.isNotBlank() }?.let { error ->
                            item { OperationErrorCard(message = error) }
                        }
                        uiState.apiKeyStatsError?.takeIf { it.isNotBlank() }?.let { error ->
                            item { OperationErrorCard(message = error) }
                        }
                    }
                    when {
                        uiState.apiKeys.isEmpty() -> item {
                            InlineEmptyCard(
                                title = stringResource(R.string.apikey_title),
                                summary = stringResource(R.string.apikey_empty),
                            )
                        }
                        keys.isEmpty() -> item {
                            InlineEmptyCard(
                                title = stringResource(R.string.empty_title),
                                summary = stringResource(R.string.apikey_search_empty),
                            )
                        }
                        else -> items(keys, key = { it.id }) { item ->
                            ApiKeyRow(
                                item = item,
                                submitting = uiState.apiKeySubmitting,
                                selectionMode = uiState.apiKeySelectionMode,
                                isSelected = item.id in uiState.selectedApiKeyIds,
                                onToggle = { viewModel.setApiKeyEnabled(item, it) },
                                onViewStats = { viewingStatsItem = item },
                                onEdit = {
                                    viewModel.clearApiKeyOperationError()
                                    editingItem = item
                                },
                                onDelete = { deletingId = item.id },
                                onSelect = { viewModel.toggleApiKeySelection(item.id) },
                                onCopy = { copyApiKey(context, item.apiKey) },
                            )
                        }
                    }
                }
            }
        }
    }

    DangerConfirmDialog(
        visible = deletingId != null,
        title = stringResource(R.string.common_delete),
        summary = stringResource(R.string.setting_apikey_summary),
        onConfirm = {
            deletingId?.let(viewModel::deleteApiKey)
            deletingId = null
        },
        onDismiss = { deletingId = null },
    )

    ApiKeyEditorDialog(
        visible = showCreateDialog,
        title = stringResource(R.string.apikey_create_title),
        initialItem = null,
        submitting = uiState.apiKeySubmitting,
        operationError = uiState.apiKeyOperationError,
        supportedModelCandidates = uiState.supportedModelCandidates,
        onConfirm = { name, expireAt, maxCost, supportedModels, enabled ->
            viewModel.createApiKey(name, expireAt, maxCost, supportedModels, enabled) {
                showCreateDialog = false
                viewModel.clearApiKeyOperationError()
            }
        },
        onDismiss = {
            if (!uiState.apiKeySubmitting) {
                showCreateDialog = false
                viewModel.clearApiKeyOperationError()
            }
        },
    )

    ApiKeyEditorDialog(
        visible = editingItem != null,
        title = stringResource(R.string.apikey_edit_title),
        initialItem = editingItem,
        submitting = uiState.apiKeySubmitting,
        operationError = uiState.apiKeyOperationError,
        supportedModelCandidates = uiState.supportedModelCandidates,
        onConfirm = { name, expireAt, maxCost, supportedModels, enabled ->
            editingItem?.let { current ->
                viewModel.updateApiKey(
                    current.copy(
                        name = name,
                        expireAt = expireAt,
                        maxCost = maxCost,
                        supportedModels = supportedModels,
                        enabled = enabled,
                    )
                ) {
                    editingItem = null
                    viewModel.clearApiKeyOperationError()
                }
            }
        },
        onDismiss = {
            if (!uiState.apiKeySubmitting) {
                editingItem = null
                viewModel.clearApiKeyOperationError()
            }
        },
    )

    viewingStatsItem?.let { item ->
        ApiKeyStatsDialog(
            visible = true,
            item = item,
            stats = uiState.apiKeyStats.firstOrNull { it.apiKeyId == item.id },
            onDismiss = { viewingStatsItem = null },
        )
    }

    uiState.createdApiKey?.let { created ->
        CreatedApiKeyDialog(
            item = created,
            onCopy = {
                copyApiKey(context, created.apiKey)
                viewModel.dismissCreatedApiKey()
            },
            onDismiss = viewModel::dismissCreatedApiKey,
        )
    }

    if (uiState.apiKeySelectionMode && uiState.selectedApiKeyIds.isNotEmpty()) {
        ApiKeyBatchActionBar(
            contentPadding = contentPadding,
            submitting = uiState.apiKeySubmitting,
            onEnable = { batchAction = ApiKeyBatchAction.ENABLE },
            onDisable = { batchAction = ApiKeyBatchAction.DISABLE },
            onDelete = { showBatchDeleteConfirm = true },
        )
    }

    ApiKeyBatchDeleteConfirmDialog(
        visible = showBatchDeleteConfirm,
        selectedCount = uiState.selectedApiKeyIds.size,
        onConfirm = {
            viewModel.batchDeleteApiKeys()
            showBatchDeleteConfirm = false
        },
        onDismiss = { showBatchDeleteConfirm = false },
    )

    ApiKeyBatchEnabledDialog(
        action = batchAction,
        selectedCount = uiState.selectedApiKeyIds.size,
        submitting = uiState.apiKeySubmitting,
        progress = uiState.batchApiKeyOperationProgress,
        onConfirm = { enabled ->
            viewModel.batchSetApiKeysEnabled(enabled) {
                batchAction = null
            }
        },
        onDismiss = { batchAction = null },
    )
}
