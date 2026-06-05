package com.elykia.octopus.feature.apikey

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.PersistableBundle
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.elykia.octopus.R
import com.elykia.octopus.core.data.model.ApiKeyItem
import com.elykia.octopus.core.designsystem.AppLazyPageScaffold
import com.elykia.octopus.core.designsystem.AppInfoChip
import com.elykia.octopus.core.designsystem.AppListCard
import com.elykia.octopus.core.designsystem.AppMetricRow
import com.elykia.octopus.core.designsystem.DangerConfirmDialog
import com.elykia.octopus.core.designsystem.DialogScrollableColumn
import com.elykia.octopus.core.designsystem.ErrorStateCard
import com.elykia.octopus.core.designsystem.InlineEmptyCard
import com.elykia.octopus.core.designsystem.LoadingStateCard
import com.elykia.octopus.core.designsystem.OctopusTokens
import com.elykia.octopus.core.designsystem.OperationErrorCard
import com.elykia.octopus.core.designsystem.PageActionButton
import com.elykia.octopus.core.designsystem.SearchField
import com.elykia.octopus.core.designsystem.SecureVisibleWindow
import com.elykia.octopus.core.designsystem.SoftIconTile
import com.elykia.octopus.core.designsystem.formatMoney
import com.elykia.octopus.core.designsystem.icons.AppMiuixIcons
import com.elykia.octopus.feature.setting.SettingViewModel
import com.elykia.octopus.feature.setting.shouldShowApiKeyPageError
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Checkbox
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme

private enum class BatchAction {
    DELETE, ENABLE, DISABLE
}

@Composable
fun ApiKeyScreen(
    contentPadding: PaddingValues,
    viewModel: SettingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var searchTerm by remember { mutableStateOf("") }
    var searchVisible by remember { mutableStateOf(false) }
    var deletingId by remember { mutableStateOf<Int?>(null) }
    var showBatchDeleteConfirm by remember { mutableStateOf(false) }
    var batchAction by remember { mutableStateOf<BatchAction?>(null) }
    var editingItem by remember { mutableStateOf<ApiKeyItem?>(null) }
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
                        uiState.error?.takeIf { it.isNotBlank() }?.let { error ->
                            item { OperationErrorCard(message = error) }
                        }
                        uiState.apiKeyListError?.takeIf { it.isNotBlank() }?.let { error ->
                            item { OperationErrorCard(message = error) }
                        }
                        uiState.apiKeyOperationError?.takeIf { it.isNotBlank() }?.let { error ->
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

    // 批量操作底部工具栏
    if (uiState.apiKeySelectionMode && uiState.selectedApiKeyIds.isNotEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(OctopusTokens.Card.copy(alpha = 0.95f))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    text = "启用",
                    enabled = !uiState.apiKeySubmitting,
                    onClick = { batchAction = BatchAction.ENABLE },
                )
                TextButton(
                    text = "禁用",
                    enabled = !uiState.apiKeySubmitting,
                    onClick = { batchAction = BatchAction.DISABLE },
                )
                TextButton(
                    text = "删除",
                    enabled = !uiState.apiKeySubmitting,
                    onClick = { showBatchDeleteConfirm = true },
                )
            }
        }
    }

    // 批量删除确认对话框
    DangerConfirmDialog(
        visible = showBatchDeleteConfirm,
        title = "批量删除 API Key",
        summary = "确定要删除选中的 ${uiState.selectedApiKeyIds.size} 个 API Key 吗？此操作不可撤销。",
        onConfirm = {
            viewModel.batchDeleteApiKeys()
            showBatchDeleteConfirm = false
        },
        onDismiss = { showBatchDeleteConfirm = false },
    )

    // 批量启用/禁用确认
    if (batchAction != null) {
        val isEnable = batchAction == BatchAction.ENABLE
        OverlayDialog(
            show = true,
            title = if (isEnable) "批量启用" else "批量禁用",
            summary = "确定要${if (isEnable) "启用" else "禁用"}选中的 ${uiState.selectedApiKeyIds.size} 个 API Key 吗？",
            onDismissRequest = { if (!uiState.apiKeySubmitting) batchAction = null },
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                uiState.batchApiKeyOperationProgress?.let { progress ->
                    Text(
                        text = progress,
                        style = MiuixTheme.textStyles.body2,
                        color = OctopusTokens.TextSecondary,
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(
                        text = "取消",
                        enabled = !uiState.apiKeySubmitting,
                        onClick = { batchAction = null },
                    )
                    TextButton(
                        text = if (uiState.apiKeySubmitting) "处理中..." else "确定",
                        enabled = !uiState.apiKeySubmitting,
                        onClick = {
                            viewModel.batchSetApiKeysEnabled(isEnable) {
                                batchAction = null
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ApiKeyRow(
    item: ApiKeyItem,
    submitting: Boolean,
    selectionMode: Boolean,
    isSelected: Boolean,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSelect: () -> Unit,
    onCopy: () -> Unit,
) {
    val supportedModels = item.supportedModels
        ?.split(',')
        ?.map { it.trim() }
        ?.filter { it.isNotBlank() }
        .orEmpty()

    AppListCard(padding = PaddingValues(horizontal = 18.dp, vertical = 18.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (selectionMode) {
                    Checkbox(
                        state = if (isSelected) ToggleableState.On else ToggleableState.Off,
                        onClick = { onSelect() },
                        enabled = !submitting,
                    )
                }
                SoftIconTile(
                    icon = AppMiuixIcons.ApiKey,
                    contentDescription = item.name,
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = item.name.ifBlank { stringResource(R.string.apikey_fallback_name, item.id) },
                        style = MiuixTheme.textStyles.title3,
                        fontWeight = FontWeight.Bold,
                        color = OctopusTokens.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = item.apiKey.maskApiKey(),
                        style = MiuixTheme.textStyles.body2,
                        color = OctopusTokens.TextSecondary,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                ApiKeyStatusPill(
                    enabled = item.enabled,
                    clickable = !submitting && !selectionMode,
                    onClick = { onToggle(!item.enabled) },
                )
                if (!selectionMode) {
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        IconButton(onClick = onEdit, enabled = !submitting) {
                            Icon(
                                imageVector = AppMiuixIcons.Create,
                                contentDescription = stringResource(R.string.action_edit),
                                tint = OctopusTokens.TextSecondary,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                        IconButton(onClick = onDelete, enabled = !submitting) {
                            Icon(
                                imageVector = AppMiuixIcons.Delete,
                                contentDescription = stringResource(R.string.common_delete),
                                tint = OctopusTokens.TextSecondary,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AppMetricRow(
                    icon = AppMiuixIcons.Time,
                    label = stringResource(R.string.apikey_expire_at_label),
                    value = item.expireAt
                        ?.takeIf { it > 0L }
                        ?.toString()
                        ?: stringResource(R.string.apikey_expire_never),
                )
                AppMetricRow(
                    icon = AppMiuixIcons.Cost,
                    label = stringResource(R.string.apikey_max_cost_label),
                    value = item.maxCost
                        ?.takeIf { it > 0.0 }
                        ?.let(::formatMoney)
                        ?: stringResource(R.string.apikey_cost_unlimited),
                )
            }

            Text(
                text = supportedModels.modelSummary(),
                style = MiuixTheme.textStyles.body2,
                color = OctopusTokens.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ApiKeyStatusPill(
    enabled: Boolean,
    clickable: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (enabled) OctopusTokens.Accent else OctopusTokens.Muted)
            .then(if (clickable) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 14.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(if (enabled) R.string.apikey_enabled_summary else R.string.apikey_disabled_summary),
            color = if (enabled) OctopusTokens.OnAccent else OctopusTokens.TextSecondary,
            style = MiuixTheme.textStyles.body2,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

internal fun String.maskApiKey(): String = when {
    isBlank() -> ""
    length <= 4 -> "****"
    length <= 12 -> take(2) + "..." + takeLast(2)
    else -> take(8) + "..." + takeLast(4)
}

internal enum class ApiKeyEditorValidationIssue {
    InvalidExpireAt,
    InvalidMaxCost,
}

internal data class ApiKeyEditorValues(
    val expireAt: Long,
    val maxCost: Double,
)

internal fun parseApiKeyEditorValues(
    expireAt: String,
    maxCost: String,
): Result<ApiKeyEditorValues> {
    val parsedExpireAt = parseOptionalNonNegativeLong(expireAt)
        ?: return Result.failure(ApiKeyEditorValidationException(ApiKeyEditorValidationIssue.InvalidExpireAt))
    val parsedMaxCost = parseOptionalNonNegativeFiniteDouble(maxCost)
        ?: return Result.failure(ApiKeyEditorValidationException(ApiKeyEditorValidationIssue.InvalidMaxCost))

    return Result.success(
        ApiKeyEditorValues(
            expireAt = parsedExpireAt,
            maxCost = parsedMaxCost,
        ),
    )
}

internal fun apiKeyEditorValidationIssue(
    expireAt: String,
    maxCost: String,
): ApiKeyEditorValidationIssue? =
    (parseApiKeyEditorValues(expireAt = expireAt, maxCost = maxCost).exceptionOrNull() as? ApiKeyEditorValidationException)
        ?.issue

internal fun canSubmitApiKeyEditor(
    name: String,
    expireAt: String,
    maxCost: String,
    submitting: Boolean,
): Boolean =
    !submitting &&
        name.isNotBlank() &&
        apiKeyEditorValidationIssue(expireAt = expireAt, maxCost = maxCost) == null

private fun parseOptionalNonNegativeLong(value: String): Long? {
    val trimmed = value.trim()
    if (trimmed.isBlank()) return 0L
    return trimmed.toLongOrNull()?.takeIf { it >= 0L }
}

private fun parseOptionalNonNegativeFiniteDouble(value: String): Double? {
    val trimmed = value.trim()
    if (trimmed.isBlank()) return 0.0
    val number = trimmed.toDoubleOrNull() ?: return null
    return number.takeIf { it >= 0.0 && !it.isNaN() && !it.isInfinite() }
}

internal class ApiKeyEditorValidationException(
    val issue: ApiKeyEditorValidationIssue,
) : IllegalArgumentException(issue.name)

@Composable
private fun List<String>.modelSummary(): String {
    return if (isEmpty()) {
        stringResource(R.string.apikey_models_all)
    } else {
        val head = take(3).joinToString(", ")
        if (size > 3) {
            "$head · ${stringResource(R.string.apikey_models_more, size - 3)}"
        } else {
            head
        }
    }
}

@Composable
private fun CreatedApiKeyDialog(
    item: ApiKeyItem,
    onCopy: () -> Unit,
    onDismiss: () -> Unit,
) {
    SecureVisibleWindow()

    OverlayDialog(
        show = true,
        title = stringResource(R.string.apikey_created_title),
        summary = stringResource(R.string.apikey_created_summary),
        onDismissRequest = onDismiss,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = stringResource(R.string.apikey_created_value_label),
                style = MiuixTheme.textStyles.main,
                fontWeight = FontWeight.SemiBold,
            )
            AppListCard {
                Text(
                    text = item.apiKey,
                    style = MiuixTheme.textStyles.body2,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppInfoChip(
                    text = item.expireAt?.takeIf { it > 0 }?.toString() ?: stringResource(R.string.common_never_expires),
                    icon = AppMiuixIcons.Time,
                )
                AppInfoChip(
                    text = item.maxCost
                        ?.takeIf { it > 0.0 }
                        ?.let(::formatMoney)
                        ?: stringResource(R.string.apikey_cost_unlimited),
                    icon = AppMiuixIcons.Cost,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(text = stringResource(R.string.common_copy), onClick = onCopy)
                TextButton(text = stringResource(R.string.common_confirm), onClick = onDismiss)
            }
        }
    }
}

@Composable
private fun ApiKeyEditorDialog(
    visible: Boolean,
    title: String,
    initialItem: ApiKeyItem?,
    submitting: Boolean,
    operationError: String?,
    onConfirm: (String, Long, Double, String, Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    if (!visible) return

    var name by remember(initialItem?.id, initialItem?.name, visible) {
        mutableStateOf(initialItem?.name.orEmpty())
    }
    var expireAt by remember(initialItem?.id, initialItem?.expireAt, visible) {
        mutableStateOf(initialItem?.expireAt?.toString().orEmpty())
    }
    var maxCost by remember(initialItem?.id, initialItem?.maxCost, visible) {
        mutableStateOf(initialItem?.maxCost?.toString().orEmpty())
    }
    var supportedModels by remember(initialItem?.id, initialItem?.supportedModels, visible) {
        mutableStateOf(initialItem?.supportedModels.orEmpty())
    }
    var enabled by remember(initialItem?.id, initialItem?.enabled, visible) {
        mutableStateOf(initialItem?.enabled ?: true)
    }
    var validationIssue by remember(
        initialItem?.id,
        initialItem?.expireAt,
        initialItem?.maxCost,
        visible,
    ) {
        mutableStateOf<ApiKeyEditorValidationIssue?>(null)
    }
    val editorScrollState = rememberScrollState()
    val currentValidationIssue = apiKeyEditorValidationIssue(expireAt = expireAt, maxCost = maxCost)
        ?: validationIssue

    OverlayDialog(
        show = visible,
        title = title,
        summary = stringResource(R.string.setting_apikey_summary),
        onDismissRequest = {
            if (!submitting) onDismiss()
        },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DialogScrollableColumn(
                fraction = 0.58f,
                scrollState = editorScrollState,
            ) {
                operationError?.takeIf { it.isNotBlank() }?.let { error ->
                    OperationErrorCard(message = error)
                }
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = stringResource(R.string.apikey_name_hint),
                    useLabelAsPlaceholder = true,
                    singleLine = true,
                    enabled = !submitting,
                    modifier = Modifier.fillMaxWidth(),
                )
                TextField(
                    value = maxCost,
                    onValueChange = {
                        maxCost = it
                        validationIssue = null
                    },
                    label = stringResource(R.string.apikey_max_cost_hint),
                    useLabelAsPlaceholder = true,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    enabled = !submitting,
                    modifier = Modifier.fillMaxWidth(),
                )
                TextField(
                    value = expireAt,
                    onValueChange = {
                        expireAt = it
                        validationIssue = null
                    },
                    label = stringResource(R.string.apikey_expire_at_hint),
                    useLabelAsPlaceholder = true,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled = !submitting,
                    modifier = Modifier.fillMaxWidth(),
                )
                currentValidationIssue?.let { issue ->
                    OperationErrorCard(message = apiKeyEditorValidationMessage(issue))
                }
                TextField(
                    value = supportedModels,
                    onValueChange = { supportedModels = it },
                    label = stringResource(R.string.apikey_supported_models_hint),
                    useLabelAsPlaceholder = true,
                    singleLine = false,
                    maxLines = 3,
                    enabled = !submitting,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(text = stringResource(R.string.apikey_enabled_label), style = MiuixTheme.textStyles.main)
                    Switch(checked = enabled, onCheckedChange = { if (!submitting) enabled = it })
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(text = stringResource(R.string.common_cancel), enabled = !submitting, onClick = onDismiss)
                TextButton(
                    text = if (submitting) {
                        stringResource(R.string.common_saving)
                    } else {
                        stringResource(R.string.common_confirm)
                    },
                    enabled = canSubmitApiKeyEditor(
                        name = name,
                        expireAt = expireAt,
                        maxCost = maxCost,
                        submitting = submitting,
                    ),
                    colors = ButtonDefaults.textButtonColorsPrimary(),
                    onClick = {
                        val parsedValues = parseApiKeyEditorValues(
                            expireAt = expireAt,
                            maxCost = maxCost,
                        ).getOrElse { exception ->
                            validationIssue = (exception as? ApiKeyEditorValidationException)?.issue
                                ?: ApiKeyEditorValidationIssue.InvalidMaxCost
                            return@TextButton
                        }
                        onConfirm(
                            name.trim(),
                            parsedValues.expireAt,
                            parsedValues.maxCost,
                            supportedModels.trim(),
                            enabled,
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun apiKeyEditorValidationMessage(issue: ApiKeyEditorValidationIssue): String = when (issue) {
    ApiKeyEditorValidationIssue.InvalidExpireAt -> stringResource(R.string.apikey_invalid_expire_at)
    ApiKeyEditorValidationIssue.InvalidMaxCost -> stringResource(R.string.apikey_invalid_max_cost)
}

private fun copyApiKey(context: Context, value: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
    val clip = ClipData.newPlainText("api_key", value).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            description.extras = PersistableBundle().apply {
                putBoolean("android.content.extra.IS_SENSITIVE", true)
            }
        }
    }
    clipboard.setPrimaryClip(clip)
    android.os.Handler(context.mainLooper).postDelayed({
        if (clipboard.primaryClip?.getItemAt(0)?.text?.toString() == value) {
            clipboard.setPrimaryClip(ClipData.newPlainText("", ""))
        }
    }, 60_000L)
}
