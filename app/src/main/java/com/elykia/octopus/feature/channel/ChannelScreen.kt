package com.elykia.octopus.feature.channel

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.elykia.octopus.R
import com.elykia.octopus.core.data.model.Channel
import com.elykia.octopus.core.designsystem.AppLazyPageScaffold
import com.elykia.octopus.core.designsystem.AppListCard
import com.elykia.octopus.core.designsystem.AppMetricRow
import com.elykia.octopus.core.designsystem.DangerConfirmDialog
import com.elykia.octopus.core.designsystem.DialogScrollableColumn
import com.elykia.octopus.core.designsystem.ErrorStateCard
import com.elykia.octopus.core.designsystem.InlineEmptyCard
import com.elykia.octopus.core.designsystem.LoadingStateCard
import com.elykia.octopus.core.designsystem.OctopusTones
import com.elykia.octopus.core.designsystem.OctopusTokens
import com.elykia.octopus.core.designsystem.OperationErrorCard
import com.elykia.octopus.core.designsystem.PageActionButton
import com.elykia.octopus.core.designsystem.SearchField
import com.elykia.octopus.core.designsystem.SecureVisibleWindow
import com.elykia.octopus.core.designsystem.SoftIconTile
import com.elykia.octopus.core.designsystem.ToolbarChip
import com.elykia.octopus.core.designsystem.formatCount
import com.elykia.octopus.core.designsystem.formatMoney
import com.elykia.octopus.core.designsystem.icons.AppMiuixIcons
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
fun ChannelScreen(
    contentPadding: PaddingValues,
    viewModel: ChannelViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var deletingId by remember { mutableStateOf<Int?>(null) }
    var showBatchDeleteConfirm by remember { mutableStateOf(false) }
    var batchAction by remember { mutableStateOf<BatchAction?>(null) }
    var searchTerm by remember { mutableStateOf("") }
    var searchVisible by remember { mutableStateOf(false) }
    var editingChannelId by remember { mutableStateOf<Int?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedChannelForFetch by remember { mutableStateOf<Channel?>(null) }

    val channels = uiState.channels
        .filter { channel ->
            searchTerm.isBlank() ||
                channel.name.contains(searchTerm, ignoreCase = true) ||
                channel.model.contains(searchTerm, ignoreCase = true) ||
                channel.customModel.contains(searchTerm, ignoreCase = true) ||
                channel.baseUrls.any { it.url.contains(searchTerm, ignoreCase = true) }
        }
        .sortedByDescending { it.enabled }
    val editingChannel = resolveEditingChannel(uiState.channels, editingChannelId)

    Box(modifier = Modifier.fillMaxSize()) {
        AppLazyPageScaffold(
            title = if (uiState.selectionMode) {
                "已选 ${uiState.selectedIds.size} 项"
            } else {
                stringResource(R.string.channel_title)
            },
            actions = {
                if (uiState.selectionMode) {
                    PageActionButton(
                        icon = AppMiuixIcons.Close,
                        contentDescription = "退出选择",
                        enabled = !uiState.submitting,
                        onClick = { viewModel.exitSelectionMode() },
                    )
                    PageActionButton(
                        icon = AppMiuixIcons.Check,
                        contentDescription = "全选",
                        enabled = !uiState.submitting,
                        onClick = { viewModel.selectAll() },
                    )
                } else {
                    PageActionButton(
                        icon = AppMiuixIcons.More,
                        contentDescription = "批量操作",
                        enabled = !uiState.loading && !uiState.shouldShowPageError() && uiState.channels.isNotEmpty(),
                        onClick = { viewModel.enterSelectionMode() },
                    )
                    PageActionButton(
                        icon = if (searchVisible) AppMiuixIcons.Close else AppMiuixIcons.Search,
                        contentDescription = stringResource(R.string.action_open_search),
                        enabled = !uiState.loading && !uiState.shouldShowPageError(),
                        onClick = {
                            searchVisible = !searchVisible
                            if (!searchVisible) searchTerm = ""
                        },
                    )
                    PageActionButton(
                        icon = AppMiuixIcons.Add,
                        contentDescription = stringResource(R.string.action_create),
                        enabled = !uiState.loading && !uiState.shouldShowPageError() && !uiState.submitting,
                        onClick = {
                            viewModel.clearOperationError()
                            showCreateDialog = true
                        },
                    )
                }
            },
            contentPadding = contentPadding,
        ) {
            when {
                uiState.loading -> item {
                    LoadingStateCard(title = stringResource(R.string.channel_title))
                }
                uiState.shouldShowPageError() -> item {
                    ErrorStateCard(
                        message = uiState.error ?: stringResource(R.string.error_title),
                        onRetry = viewModel::refresh,
                    )
                }
                else -> {
                    if (uiState.channels.isNotEmpty() && searchVisible) {
                        item {
                            SearchField(
                                value = searchTerm,
                                onValueChange = { searchTerm = it },
                                hint = stringResource(R.string.channel_search_hint),
                            )
                        }
                    }
                    if (!showCreateDialog && editingChannel == null) {
                        uiState.error?.takeIf { it.isNotBlank() }?.let { error ->
                            item { OperationErrorCard(message = error) }
                        }
                        uiState.operationError?.takeIf { it.isNotBlank() }?.let { error ->
                            item { OperationErrorCard(message = error) }
                        }
                    }
                    when {
                        uiState.channels.isEmpty() -> item {
                            InlineEmptyCard(
                                title = stringResource(R.string.channel_title),
                                summary = stringResource(R.string.channel_empty),
                            )
                        }
                        channels.isEmpty() -> item {
                            InlineEmptyCard(
                                title = stringResource(R.string.empty_title),
                                summary = stringResource(R.string.channel_search_empty),
                            )
                        }
                        else -> items(channels, key = { it.id }) { channel ->
                            ChannelRow(
                                channel = channel,
                                submitting = uiState.submitting,
                                selectionMode = uiState.selectionMode,
                                isSelected = channel.id in uiState.selectedIds,
                                onToggle = { viewModel.setEnabled(channel.id, it) },
                                onEdit = {
                                    viewModel.clearOperationError()
                                    editingChannelId = channel.id
                                },
                                onDelete = { deletingId = channel.id },
                                onSelect = { viewModel.toggleSelection(channel.id) },
                            )
                        }
                    }
                }
            }
        }
    }

    DangerConfirmDialog(
        visible = deletingId != null,
        title = stringResource(R.string.channel_delete_title),
        summary = stringResource(R.string.channel_delete_summary),
        onConfirm = {
            deletingId?.let(viewModel::delete)
            deletingId = null
        },
        onDismiss = { deletingId = null },
    )

    ChannelEditorDialog(
        visible = showCreateDialog,
        title = stringResource(R.string.channel_create_title),
        initialChannel = null,
        submitting = uiState.submitting,
        operationError = uiState.operationError,
        onFetchModels = { type, baseUrl, apiKey, proxy ->
            viewModel.fetchModels(type, baseUrl, apiKey, proxy) {
                selectedChannelForFetch = Channel(name = "", type = type)
            }
        },
        onConfirm = { name, type, enabled, baseUrl, apiKey, model, customModel, proxy, autoSync ->
            viewModel.createChannel(name, type, enabled, baseUrl, apiKey, model, customModel, proxy, autoSync) {
                showCreateDialog = false
                viewModel.clearOperationError()
            }
        },
        onDismiss = {
            if (!uiState.submitting) {
                showCreateDialog = false
                viewModel.clearOperationError()
            }
        },
    )

    ChannelEditorDialog(
        visible = editingChannel != null,
        title = stringResource(R.string.channel_edit_title),
        initialChannel = editingChannel,
        submitting = uiState.submitting,
        operationError = uiState.operationError,
        onFetchModels = { type, baseUrl, apiKey, proxy ->
            val current = editingChannel
            viewModel.fetchModels(type, baseUrl, apiKey, proxy) {
                selectedChannelForFetch = current
            }
        },
        onConfirm = { name, type, enabled, baseUrl, apiKey, model, customModel, proxy, autoSync ->
            editingChannel?.let { current ->
                viewModel.updateChannel(current, name, type, enabled, baseUrl, apiKey, model, customModel, proxy, autoSync) {
                    editingChannelId = null
                    viewModel.clearOperationError()
                }
            }
        },
        onDismiss = {
            if (!uiState.submitting) {
                editingChannelId = null
                viewModel.clearOperationError()
            }
        },
    )

    ChannelFetchResultDialog(
        visible = selectedChannelForFetch != null,
        models = uiState.fetchedModels,
        onDismiss = {
            selectedChannelForFetch = null
            viewModel.clearFetchedModels()
        },
    )

    // 批量操作底部工具栏
    if (uiState.selectionMode && uiState.selectedIds.isNotEmpty()) {
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
                    enabled = !uiState.submitting,
                    onClick = { batchAction = BatchAction.ENABLE },
                )
                TextButton(
                    text = "禁用",
                    enabled = !uiState.submitting,
                    onClick = { batchAction = BatchAction.DISABLE },
                )
                TextButton(
                    text = "删除",
                    enabled = !uiState.submitting,
                    onClick = { showBatchDeleteConfirm = true },
                )
            }
        }
    }

    // 批量删除确认对话框
    DangerConfirmDialog(
        visible = showBatchDeleteConfirm,
        title = "批量删除渠道",
        summary = "确定要删除选中的 ${uiState.selectedIds.size} 个渠道吗？此操作不可撤销。",
        onConfirm = {
            viewModel.batchDelete()
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
            summary = "确定要${if (isEnable) "启用" else "禁用"}选中的 ${uiState.selectedIds.size} 个渠道吗？",
            onDismissRequest = { if (!uiState.submitting) batchAction = null },
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                uiState.batchOperationProgress?.let { progress ->
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
                        enabled = !uiState.submitting,
                        onClick = { batchAction = null },
                    )
                    TextButton(
                        text = if (uiState.submitting) "处理中..." else "确定",
                        enabled = !uiState.submitting,
                        onClick = {
                            viewModel.batchSetEnabled(isEnable) {
                                batchAction = null
                            }
                        },
                    )
                }
            }
        }
    }
}

internal fun resolveEditingChannel(
    channels: List<Channel>,
    editingChannelId: Int?,
): Channel? = editingChannelId?.let { id -> channels.firstOrNull { it.id == id } }

@Composable
private fun ChannelRow(
    channel: Channel,
    submitting: Boolean,
    selectionMode: Boolean,
    isSelected: Boolean,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSelect: () -> Unit,
) {
    val stats = channel.stats
    val requestCount = (stats?.requestSuccess ?: 0L) + (stats?.requestFailed ?: 0L)
    val totalCost = (stats?.inputCost ?: 0.0) + (stats?.outputCost ?: 0.0)

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
                    icon = AppMiuixIcons.Channel,
                    contentDescription = channel.name,
                    tint = OctopusTones.channelType(channel.type),
                )
                Text(
                    text = channel.name.ifBlank { stringResource(R.string.channel_fallback_name, channel.id) },
                    style = MiuixTheme.textStyles.title3,
                    fontWeight = FontWeight.Bold,
                    color = OctopusTokens.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                StatusPill(
                    enabled = channel.enabled,
                    clickable = !submitting && !selectionMode,
                    onClick = { onToggle(!channel.enabled) },
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
                    icon = AppMiuixIcons.Request,
                    label = stringResource(R.string.channel_request_count),
                    value = formatCount(requestCount),
                )
                AppMetricRow(
                    icon = AppMiuixIcons.Cost,
                    label = stringResource(R.string.channel_total_cost),
                    value = formatMoney(totalCost),
                )
            }

            Text(
                text = channel.compactSummary(),
                style = MiuixTheme.textStyles.body2,
                color = OctopusTokens.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun StatusPill(
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
            text = stringResource(if (enabled) R.string.common_enabled else R.string.common_disabled),
            color = if (enabled) OctopusTokens.OnAccent else OctopusTokens.TextSecondary,
            style = MiuixTheme.textStyles.body2,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun Channel.compactSummary(): String {
    val enabledKeys = keys.count { it.enabled }
    val modelText = model.ifBlank { customModel }.ifBlank { stringResource(R.string.common_unknown) }
    return stringResource(R.string.channel_compact_summary, channelTypeName(type), enabledKeys, keys.size, modelText)
}

@Composable
private fun channelTypeName(type: Int): String = when (type) {
    0 -> stringResource(R.string.channel_type_openai_chat)
    1 -> stringResource(R.string.channel_type_openai_response)
    2 -> stringResource(R.string.channel_type_anthropic)
    3 -> stringResource(R.string.channel_type_gemini)
    4 -> stringResource(R.string.channel_type_volcengine)
    5 -> stringResource(R.string.channel_type_embedding)
    else -> stringResource(R.string.channel_type_unknown, type)
}

@Composable
private fun ChannelEditorDialog(
    visible: Boolean,
    title: String,
    initialChannel: Channel?,
    submitting: Boolean,
    operationError: String?,
    onFetchModels: (Int, String, String, Boolean) -> Unit,
    onConfirm: (String, Int, Boolean, String, String, String, String, Boolean, Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    if (!visible) return

    SecureVisibleWindow()

    var name by remember(initialChannel?.id, visible) { mutableStateOf(initialChannel?.name.orEmpty()) }
    var type by remember(initialChannel?.id, visible) { mutableStateOf(initialChannel?.type ?: 0) }
    var enabled by remember(initialChannel?.id, visible) { mutableStateOf(initialChannel?.enabled ?: true) }
    var baseUrl by remember(initialChannel?.id, visible) {
        mutableStateOf(initialChannel?.baseUrls?.firstOrNull()?.url.orEmpty())
    }
    var apiKey by remember(initialChannel?.id, visible) {
        mutableStateOf("")
    }
    var apiKeyVisible by remember(initialChannel?.id, visible) { mutableStateOf(false) }
    var model by remember(initialChannel?.id, visible) { mutableStateOf(initialChannel?.model.orEmpty()) }
    var customModel by remember(initialChannel?.id, visible) { mutableStateOf(initialChannel?.customModel.orEmpty()) }
    var proxy by remember(initialChannel?.id, visible) { mutableStateOf(initialChannel?.proxy ?: false) }
    var autoSync by remember(initialChannel?.id, visible) { mutableStateOf(initialChannel?.autoSync ?: false) }
    val fetchRequiresNewKey = initialChannel != null && initialChannel.keys.isNotEmpty() && apiKey.isBlank()
    val basicEditSupported = initialChannel?.canUseBasicMobileEditor() ?: true
    val editorScrollState = rememberScrollState()

    OverlayDialog(
        show = visible,
        title = title,
        summary = stringResource(R.string.channel_panel_summary),
        onDismissRequest = {
            if (!submitting) onDismiss()
        },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DialogScrollableColumn(
                fraction = 0.64f,
                scrollState = editorScrollState,
            ) {
                operationError?.takeIf { it.isNotBlank() }?.let { error ->
                    OperationErrorCard(message = error)
                }
                if (!basicEditSupported) {
                    OperationErrorCard(message = stringResource(R.string.channel_basic_editor_unsupported))
                }
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = stringResource(R.string.channel_name_hint),
                    useLabelAsPlaceholder = true,
                    singleLine = true,
                    enabled = !submitting && basicEditSupported,
                    modifier = Modifier.fillMaxWidth(),
                )
                TextField(
                    value = baseUrl,
                    onValueChange = { baseUrl = it },
                    label = stringResource(R.string.channel_base_url_hint),
                    useLabelAsPlaceholder = true,
                    singleLine = true,
                    enabled = !submitting && basicEditSupported,
                    modifier = Modifier.fillMaxWidth(),
                )
                TextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = if (initialChannel == null) {
                        stringResource(R.string.channel_api_key_hint)
                    } else {
                        stringResource(R.string.channel_api_key_replace_hint)
                    },
                    useLabelAsPlaceholder = true,
                    singleLine = true,
                    visualTransformation = if (apiKeyVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(
                            onClick = { apiKeyVisible = !apiKeyVisible },
                            enabled = !submitting && basicEditSupported,
                        ) {
                            Icon(
                                imageVector = if (apiKeyVisible) AppMiuixIcons.Info else AppMiuixIcons.ApiKey,
                                contentDescription = if (apiKeyVisible) {
                                    stringResource(R.string.login_action_hide_password)
                                } else {
                                    stringResource(R.string.login_action_show_password)
                                },
                            )
                        }
                    },
                    enabled = !submitting && basicEditSupported,
                    modifier = Modifier.fillMaxWidth(),
                )
                TextField(
                    value = model,
                    onValueChange = { model = it },
                    label = stringResource(R.string.channel_model_hint),
                    useLabelAsPlaceholder = true,
                    singleLine = true,
                    enabled = !submitting && basicEditSupported,
                    modifier = Modifier.fillMaxWidth(),
                )
                TextField(
                    value = customModel,
                    onValueChange = { customModel = it },
                    label = stringResource(R.string.channel_custom_model_hint),
                    useLabelAsPlaceholder = true,
                    singleLine = true,
                    enabled = !submitting && basicEditSupported,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = stringResource(R.string.channel_type_label),
                    style = MiuixTheme.textStyles.main,
                    fontWeight = FontWeight.SemiBold,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    (0..5).forEach { optionType ->
                        ChannelTypeOption(type = optionType, selectedType = type) {
                            if (!submitting && basicEditSupported) type = optionType
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = stringResource(R.string.channel_enabled_label), style = MiuixTheme.textStyles.main)
                    Switch(checked = enabled, onCheckedChange = { if (!submitting && basicEditSupported) enabled = it })
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = stringResource(R.string.channel_proxy_label), style = MiuixTheme.textStyles.main)
                    Switch(checked = proxy, onCheckedChange = { if (!submitting && basicEditSupported) proxy = it })
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = stringResource(R.string.channel_auto_sync_label), style = MiuixTheme.textStyles.main)
                    Switch(checked = autoSync, onCheckedChange = { if (!submitting && basicEditSupported) autoSync = it })
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
            ) {
                TextButton(
                    text = stringResource(
                        if (fetchRequiresNewKey) {
                            R.string.channel_fetch_model_needs_key
                        } else {
                            R.string.action_fetch_model
                        },
                    ),
                    enabled = !submitting && basicEditSupported && !fetchRequiresNewKey,
                    onClick = { onFetchModels(type, baseUrl, apiKey, proxy) },
                )
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
                    enabled = canSubmitChannelEditor(
                        name = name,
                        baseUrl = baseUrl,
                        submitting = submitting,
                        basicEditSupported = basicEditSupported,
                    ),
                    onClick = {
                        onConfirm(name.trim(), type, enabled, baseUrl.trim(), apiKey.trim(), model.trim(), customModel.trim(), proxy, autoSync)
                    },
                )
            }
        }
    }
}

@Composable
private fun ChannelTypeOption(
    type: Int,
    selectedType: Int,
    onSelect: () -> Unit,
) {
    ToolbarChip(
        text = channelTypeName(type),
        selected = selectedType == type,
        onClick = onSelect,
    )
}

@Composable
private fun ChannelFetchResultDialog(
    visible: Boolean,
    models: List<String>,
    onDismiss: () -> Unit,
) {
    if (!visible) return

    OverlayDialog(
        show = visible,
        title = stringResource(R.string.channel_fetch_model_title),
        summary = stringResource(R.string.channel_fetch_model_summary),
        onDismissRequest = onDismiss,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (models.isEmpty()) {
                InlineEmptyCard(
                    title = stringResource(R.string.channel_fetch_model_title),
                    summary = stringResource(R.string.channel_fetch_model_empty),
                )
            } else {
                DialogScrollableColumn(fraction = 0.48f) {
                    models.forEach { model ->
                        Text(
                            text = model,
                            style = MiuixTheme.textStyles.body2,
                            color = OctopusTokens.TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(OctopusTokens.Muted.copy(alpha = 0.64f))
                                .padding(horizontal = 12.dp, vertical = 9.dp),
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(text = stringResource(R.string.action_close), onClick = onDismiss)
            }
        }
    }
}
