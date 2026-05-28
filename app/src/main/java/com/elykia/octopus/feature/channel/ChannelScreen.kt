package com.elykia.octopus.feature.channel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.elykia.octopus.R
import com.elykia.octopus.core.data.model.Channel
import com.elykia.octopus.core.designsystem.AppInfoChip
import com.elykia.octopus.core.designsystem.AppListCard
import com.elykia.octopus.core.designsystem.AppPageScaffold
import com.elykia.octopus.core.designsystem.AppTypePill
import com.elykia.octopus.core.designsystem.DangerConfirmDialog
import com.elykia.octopus.core.designsystem.ErrorPane
import com.elykia.octopus.core.designsystem.FloatingCreateButton
import com.elykia.octopus.core.designsystem.InlineEmptyCard
import com.elykia.octopus.core.designsystem.LoadingPane
import com.elykia.octopus.core.designsystem.OctopusTones
import com.elykia.octopus.core.designsystem.PageActionButton
import com.elykia.octopus.core.designsystem.SearchField
import com.elykia.octopus.core.designsystem.ToolbarChip
import com.elykia.octopus.core.designsystem.icons.AppMiuixIcons
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun ChannelScreen(
    contentPadding: PaddingValues,
    viewModel: ChannelViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var deletingId by remember { mutableStateOf<Int?>(null) }
    var searchTerm by remember { mutableStateOf("") }
    var editingChannel by remember { mutableStateOf<Channel?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedChannelForFetch by remember { mutableStateOf<Channel?>(null) }

    when {
        uiState.loading -> LoadingPane(title = stringResource(R.string.channel_title))
        uiState.error != null -> ErrorPane(message = uiState.error ?: stringResource(R.string.error_title), onRetry = viewModel::refresh)
        else -> {
            val channels = uiState.channels
                .filter { channel ->
                    searchTerm.isBlank() ||
                        channel.name.contains(searchTerm, ignoreCase = true) ||
                        channel.model.contains(searchTerm, ignoreCase = true) ||
                        channel.customModel.contains(searchTerm, ignoreCase = true) ||
                        channel.baseUrls.any { it.url.contains(searchTerm, ignoreCase = true) }
                }
                .sortedByDescending { it.enabled }

            Box(modifier = Modifier.fillMaxSize()) {
                AppPageScaffold(
                    title = stringResource(R.string.channel_title),
                    actions = {
                        PageActionButton(
                            icon = AppMiuixIcons.Sync,
                            contentDescription = stringResource(R.string.action_sync),
                            onClick = viewModel::syncModels,
                        )
                        PageActionButton(
                            icon = AppMiuixIcons.Refresh,
                            contentDescription = stringResource(R.string.common_refresh),
                            onClick = viewModel::refresh,
                        )
                    },
                    contentPadding = contentPadding,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (uiState.channels.isNotEmpty()) {
                            SearchField(
                                value = searchTerm,
                                onValueChange = { searchTerm = it },
                                hint = stringResource(R.string.channel_search_hint),
                            )
                        }
                        when {
                            uiState.channels.isEmpty() -> InlineEmptyCard(
                                title = stringResource(R.string.channel_title),
                                summary = stringResource(R.string.channel_empty),
                            )
                            channels.isEmpty() -> InlineEmptyCard(
                                title = stringResource(R.string.empty_title),
                                summary = stringResource(R.string.channel_search_empty),
                            )
                            else -> channels.forEach { channel ->
                                ChannelRow(
                                    channel = channel,
                                    onToggle = { viewModel.setEnabled(channel.id, it) },
                                    onEdit = { editingChannel = channel },
                                    onDelete = { deletingId = channel.id },
                                )
                            }
                        }
                    }
                }

                FloatingCreateButton(
                    text = stringResource(R.string.action_create),
                    icon = AppMiuixIcons.Add,
                    onClick = { showCreateDialog = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 20.dp, bottom = 94.dp),
                )
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
                onFetchModels = { type, baseUrl, apiKey, proxy ->
                    selectedChannelForFetch = Channel(name = "", type = type)
                    viewModel.fetchModels(type, baseUrl, apiKey, proxy)
                },
                onConfirm = { name, type, enabled, baseUrl, apiKey, model, customModel, proxy, autoSync ->
                    viewModel.createChannel(name, type, enabled, baseUrl, apiKey, model, customModel, proxy, autoSync)
                    showCreateDialog = false
                },
                onDismiss = { showCreateDialog = false },
            )

            ChannelEditorDialog(
                visible = editingChannel != null,
                title = stringResource(R.string.channel_edit_title),
                initialChannel = editingChannel,
                onFetchModels = { type, baseUrl, apiKey, proxy ->
                    selectedChannelForFetch = editingChannel
                    viewModel.fetchModels(type, baseUrl, apiKey, proxy)
                },
                onConfirm = { name, type, enabled, baseUrl, apiKey, model, customModel, proxy, autoSync ->
                    editingChannel?.let { current ->
                        viewModel.updateChannel(current, name, type, enabled, baseUrl, apiKey, model, customModel, proxy, autoSync)
                    }
                    editingChannel = null
                },
                onDismiss = { editingChannel = null },
            )

            ChannelFetchResultDialog(
                visible = selectedChannelForFetch != null,
                models = uiState.fetchedModels,
                onDismiss = {
                    selectedChannelForFetch = null
                    viewModel.clearFetchedModels()
                },
            )
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun ChannelRow(
    channel: Channel,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    AppListCard(padding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Switch(
                    checked = channel.enabled,
                    onCheckedChange = onToggle,
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = channel.name.ifBlank { stringResource(R.string.channel_fallback_name, channel.id) },
                            style = MiuixTheme.textStyles.title3,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        AppTypePill(text = channelTypeName(channel.type), color = channelTypeColor(channel.type))
                    }
                    Text(
                        text = channel.primaryModelLabel(),
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = AppMiuixIcons.Create,
                            contentDescription = stringResource(R.string.action_edit),
                            tint = MiuixTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = AppMiuixIcons.Delete,
                            contentDescription = stringResource(R.string.common_delete),
                            tint = MiuixTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                val enabledKeys = channel.keys.count { it.enabled }
                val totalKeys = channel.keys.size
                AppInfoChip(
                    text = stringResource(R.string.channel_key_count, enabledKeys, totalKeys),
                    icon = AppMiuixIcons.ApiKey,
                )
                channel.baseUrls.firstOrNull()?.url?.takeIf { it.isNotBlank() }?.let { url ->
                    AppInfoChip(
                        text = stringResource(R.string.channel_base_url_summary, url.compactUiLabel()),
                        icon = AppMiuixIcons.Request,
                    )
                }
                if (channel.proxy) {
                    AppInfoChip(text = stringResource(R.string.channel_proxy_summary), icon = AppMiuixIcons.Setting)
                }
                if (channel.autoSync) {
                    AppInfoChip(text = stringResource(R.string.channel_auto_sync_summary), icon = AppMiuixIcons.Sync)
                }
                channel.stats?.let { stats ->
                    AppInfoChip(
                        text = stringResource(R.string.channel_stat_success, stats.requestSuccess),
                        icon = AppMiuixIcons.Check,
                        tint = OctopusTones.Success,
                    )
                    AppInfoChip(
                        text = stringResource(R.string.channel_stat_failed, stats.requestFailed),
                        icon = AppMiuixIcons.Close,
                        tint = OctopusTones.Danger,
                    )
                }
            }
        }
    }
}

@Composable
private fun Channel.primaryModelLabel(): String {
    val label = model.ifBlank { customModel }.ifBlank {
        baseUrls.firstOrNull()?.url.orEmpty()
    }
    return if (label.isBlank()) {
        stringResource(R.string.common_unknown)
    } else {
        label
    }
}

private fun String.compactUiLabel(limit: Int = 28): String {
    val clean = trim()
    return if (clean.length <= limit) {
        clean
    } else {
        clean.take(limit - 3) + "..."
    }
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

private fun channelTypeColor(type: Int): Color = OctopusTones.channelType(type)

@Composable
private fun ChannelEditorDialog(
    visible: Boolean,
    title: String,
    initialChannel: Channel?,
    onFetchModels: (Int, String, String, Boolean) -> Unit,
    onConfirm: (String, Int, Boolean, String, String, String, String, Boolean, Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    if (!visible) return

    var name by remember(initialChannel?.id, visible) { mutableStateOf(initialChannel?.name.orEmpty()) }
    var type by remember(initialChannel?.id, visible) { mutableStateOf(initialChannel?.type ?: 0) }
    var enabled by remember(initialChannel?.id, visible) { mutableStateOf(initialChannel?.enabled ?: true) }
    var baseUrl by remember(initialChannel?.id, visible) {
        mutableStateOf(initialChannel?.baseUrls?.firstOrNull()?.url.orEmpty())
    }
    var apiKey by remember(initialChannel?.id, visible) {
        mutableStateOf(initialChannel?.keys?.firstOrNull()?.channelKey.orEmpty())
    }
    var model by remember(initialChannel?.id, visible) { mutableStateOf(initialChannel?.model.orEmpty()) }
    var customModel by remember(initialChannel?.id, visible) { mutableStateOf(initialChannel?.customModel.orEmpty()) }
    var proxy by remember(initialChannel?.id, visible) { mutableStateOf(initialChannel?.proxy ?: false) }
    var autoSync by remember(initialChannel?.id, visible) { mutableStateOf(initialChannel?.autoSync ?: false) }

    OverlayDialog(
        show = visible,
        title = title,
        summary = stringResource(R.string.channel_panel_summary),
        onDismissRequest = onDismiss,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            TextField(
                value = name,
                onValueChange = { name = it },
                label = stringResource(R.string.channel_name_hint),
                useLabelAsPlaceholder = true,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            TextField(
                value = baseUrl,
                onValueChange = { baseUrl = it },
                label = stringResource(R.string.channel_base_url_hint),
                useLabelAsPlaceholder = true,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            TextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                label = stringResource(R.string.channel_api_key_hint),
                useLabelAsPlaceholder = true,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            TextField(
                value = model,
                onValueChange = { model = it },
                label = stringResource(R.string.channel_model_hint),
                useLabelAsPlaceholder = true,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            TextField(
                value = customModel,
                onValueChange = { customModel = it },
                label = stringResource(R.string.channel_custom_model_hint),
                useLabelAsPlaceholder = true,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = stringResource(R.string.channel_type_label),
                style = MiuixTheme.textStyles.main,
                fontWeight = FontWeight.SemiBold,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ChannelTypeOption(type = 0, selectedType = type) { type = 0 }
                ChannelTypeOption(type = 1, selectedType = type) { type = 1 }
                ChannelTypeOption(type = 2, selectedType = type) { type = 2 }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ChannelTypeOption(type = 3, selectedType = type) { type = 3 }
                ChannelTypeOption(type = 4, selectedType = type) { type = 4 }
                ChannelTypeOption(type = 5, selectedType = type) { type = 5 }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = stringResource(R.string.channel_enabled_label), style = MiuixTheme.textStyles.main)
                Switch(checked = enabled, onCheckedChange = { enabled = it })
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = stringResource(R.string.channel_proxy_label), style = MiuixTheme.textStyles.main)
                Switch(checked = proxy, onCheckedChange = { proxy = it })
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = stringResource(R.string.channel_auto_sync_label), style = MiuixTheme.textStyles.main)
                Switch(checked = autoSync, onCheckedChange = { autoSync = it })
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(
                    text = stringResource(R.string.action_fetch_model),
                    onClick = { onFetchModels(type, baseUrl, apiKey, proxy) },
                )
                TextButton(text = stringResource(R.string.common_cancel), onClick = onDismiss)
                TextButton(
                    text = stringResource(R.string.common_confirm),
                    enabled = name.isNotBlank(),
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
        summary = if (models.isNotEmpty()) {
            models.joinToString(", ")
        } else {
            stringResource(R.string.channel_fetch_model_empty)
        },
        onDismissRequest = onDismiss,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(text = stringResource(R.string.action_close), onClick = onDismiss)
        }
    }
}
