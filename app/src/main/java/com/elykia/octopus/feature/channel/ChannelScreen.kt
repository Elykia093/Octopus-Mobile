package com.elykia.octopus.feature.channel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.elykia.octopus.R
import com.elykia.octopus.core.data.model.Channel
import com.elykia.octopus.core.designsystem.AppLazyPageScaffold
import com.elykia.octopus.core.designsystem.DangerConfirmDialog
import com.elykia.octopus.core.designsystem.ErrorStateCard
import com.elykia.octopus.core.designsystem.InlineEmptyCard
import com.elykia.octopus.core.designsystem.LoadingStateCard
import com.elykia.octopus.core.designsystem.OctopusTokens
import com.elykia.octopus.core.designsystem.OperationErrorCard
import com.elykia.octopus.core.designsystem.PageActionButton
import com.elykia.octopus.core.designsystem.SearchField
import com.elykia.octopus.core.designsystem.icons.AppMiuixIcons
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme

private enum class BatchAction { ENABLE, DISABLE }

@Composable
fun ChannelScreen(
    contentPadding: PaddingValues,
    viewModel: ChannelViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var deletingId by remember { mutableStateOf<Int?>(null) }
    var showBatchDeleteConfirm by remember { mutableStateOf(false) }
    var batchAction by remember { mutableStateOf<BatchAction?>(null) }
    var editingChannelId by remember { mutableStateOf<Int?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedChannelForFetch by remember { mutableStateOf<Channel?>(null) }

    var searchTerm by remember { mutableStateOf("") }
    var searchVisible by remember { mutableStateOf(false) }

    val channels = uiState.channels
        .filter { channel ->
            searchTerm.isBlank() ||
                channel.name.contains(searchTerm, ignoreCase = true) ||
                channel.model.contains(searchTerm, ignoreCase = true) ||
                channel.customModel.contains(searchTerm, ignoreCase = true) ||
                channel.baseUrls.any { it.url.contains(searchTerm, ignoreCase = true) }
        }
        .sortedByDescending { it.enabled }

    val editingChannel = editingChannelId?.let { id ->
        uiState.channels.firstOrNull { it.id == id }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AppLazyPageScaffold(
            title = if (uiState.selectionMode) {
                stringResource(R.string.batch_selected_count, uiState.selectedIds.size)
            } else {
                stringResource(R.string.channel_title)
            },
            actions = {
                if (uiState.selectionMode) {
                    PageActionButton(
                        icon = AppMiuixIcons.Close,
                        contentDescription = stringResource(R.string.batch_exit_selection),
                        enabled = !uiState.submitting,
                        onClick = { viewModel.exitSelectionMode() },
                    )
                    PageActionButton(
                        icon = AppMiuixIcons.Check,
                        contentDescription = stringResource(R.string.batch_select_all),
                        enabled = !uiState.submitting,
                        onClick = { viewModel.selectAll() },
                    )
                } else {
                    PageActionButton(
                        icon = AppMiuixIcons.More,
                        contentDescription = stringResource(R.string.batch_operations),
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
        proxyConfigurations = uiState.proxyConfigurations,
        proxyConfigurationError = uiState.proxyConfigurationError,
        submitting = uiState.submitting,
        operationError = uiState.operationError,
        onFetchModels = { values ->
            viewModel.fetchModels(values) {
                selectedChannelForFetch = Channel(name = "", type = values.type)
            }
        },
        onConfirm = { values ->
            viewModel.createChannel(values) {
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
        proxyConfigurations = uiState.proxyConfigurations,
        proxyConfigurationError = uiState.proxyConfigurationError,
        submitting = uiState.submitting,
        operationError = uiState.operationError,
        onFetchModels = { values ->
            val current = editingChannel
            viewModel.fetchModels(values) {
                selectedChannelForFetch = current
            }
        },
        onConfirm = { values ->
            editingChannel?.let { current ->
                viewModel.updateChannel(current, values) {
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
                    text = stringResource(R.string.batch_enable),
                    enabled = !uiState.submitting,
                    onClick = { batchAction = BatchAction.ENABLE },
                )
                TextButton(
                    text = stringResource(R.string.batch_disable),
                    enabled = !uiState.submitting,
                    onClick = { batchAction = BatchAction.DISABLE },
                )
                TextButton(
                    text = stringResource(R.string.batch_delete),
                    enabled = !uiState.submitting,
                    onClick = { showBatchDeleteConfirm = true },
                )
            }
        }
    }

    DangerConfirmDialog(
        visible = showBatchDeleteConfirm,
        title = stringResource(R.string.batch_delete_channels_title),
        summary = stringResource(R.string.batch_delete_channels_summary, uiState.selectedIds.size),
        onConfirm = {
            viewModel.batchDelete()
            showBatchDeleteConfirm = false
        },
        onDismiss = { showBatchDeleteConfirm = false },
    )

    if (batchAction != null) {
        val isEnable = batchAction == BatchAction.ENABLE
        OverlayDialog(
            show = true,
            title = stringResource(if (isEnable) R.string.batch_enable_title else R.string.batch_disable_title),
            summary = stringResource(
                if (isEnable) R.string.batch_enable_summary else R.string.batch_disable_summary,
                uiState.selectedIds.size,
            ),
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
                        text = stringResource(R.string.common_cancel),
                        enabled = !uiState.submitting,
                        onClick = { batchAction = null },
                    )
                    TextButton(
                        text = if (uiState.submitting) {
                            stringResource(R.string.common_saving)
                        } else {
                            stringResource(R.string.common_confirm)
                        },
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
