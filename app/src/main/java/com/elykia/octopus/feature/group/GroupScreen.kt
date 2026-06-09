package com.elykia.octopus.feature.group

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.elykia.octopus.R
import com.elykia.octopus.core.data.model.GroupHealthGroupView
import com.elykia.octopus.core.data.model.GroupHealthProbeMode
import com.elykia.octopus.core.designsystem.AppInfoChip
import com.elykia.octopus.core.designsystem.AppLazyPageScaffold
import com.elykia.octopus.core.designsystem.AppListCard
import com.elykia.octopus.core.designsystem.DangerConfirmDialog
import com.elykia.octopus.core.designsystem.ErrorStateCard
import com.elykia.octopus.core.designsystem.InlineEmptyCard
import com.elykia.octopus.core.designsystem.LoadingStateCard
import com.elykia.octopus.core.designsystem.OctopusTokens
import com.elykia.octopus.core.designsystem.OperationErrorCard
import com.elykia.octopus.core.designsystem.PageActionButton
import com.elykia.octopus.core.designsystem.SearchField
import com.elykia.octopus.core.designsystem.ToolbarChip
import com.elykia.octopus.core.designsystem.icons.AppMiuixIcons
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton

@Composable
fun GroupScreen(
    contentPadding: PaddingValues,
    viewModel: GroupViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var deletingId by remember { mutableStateOf<Int?>(null) }
    var showBatchDeleteConfirm by remember { mutableStateOf(false) }
    var editingGroupId by remember { mutableStateOf<Int?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }

    var searchTerm by remember { mutableStateOf("") }
    var searchVisible by remember { mutableStateOf(false) }
    val expanded = remember { mutableStateMapOf<Int, Boolean>() }

    val groups = uiState.groups
        .filter { group ->
            searchTerm.isBlank() ||
                group.name.contains(searchTerm, ignoreCase = true) ||
                group.matchRegex.contains(searchTerm, ignoreCase = true) ||
                group.items.any { item ->
                    item.modelName.contains(searchTerm, ignoreCase = true) ||
                        item.channelId.toString().contains(searchTerm)
                }
        }
        .sortedByDescending { it.items.size }

    val modelCandidates = remember(uiState.channels, uiState.modelChannels) {
        buildGroupModelCandidates(uiState.channels, uiState.modelChannels)
    }

    val editingGroup = editingGroupId?.let { id ->
        uiState.groups.firstOrNull { it.id == id }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AppLazyPageScaffold(
            title = if (uiState.selectionMode) {
                stringResource(R.string.batch_selected_count, uiState.selectedIds.size)
            } else {
                stringResource(R.string.group_title)
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
                        enabled = !uiState.loading && !uiState.shouldShowPageError() && uiState.groups.isNotEmpty(),
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
                    LoadingStateCard(title = stringResource(R.string.group_title))
                }
                uiState.shouldShowPageError() -> item {
                    ErrorStateCard(
                        message = uiState.error ?: stringResource(R.string.error_title),
                        onRetry = viewModel::refresh,
                    )
                }
                else -> {
                    if (uiState.groups.isNotEmpty() && searchVisible) {
                        item {
                            SearchField(
                                value = searchTerm,
                                onValueChange = { searchTerm = it },
                                hint = stringResource(R.string.group_search_hint),
                            )
                        }
                    }
                    if (!showCreateDialog && editingGroup == null) {
                        uiState.error?.takeIf { it.isNotBlank() }?.let { error ->
                            item { OperationErrorCard(message = error) }
                        }
                        uiState.channelListError?.takeIf { it.isNotBlank() }?.let { error ->
                            item { OperationErrorCard(message = error) }
                        }
                        uiState.modelChannelError?.takeIf { it.isNotBlank() }?.let { error ->
                            item { OperationErrorCard(message = error) }
                        }
                        uiState.operationError?.takeIf { it.isNotBlank() }?.let { error ->
                            item { OperationErrorCard(message = error) }
                        }
                        uiState.groupHealthError?.takeIf { it.isNotBlank() }?.let { error ->
                            item { OperationErrorCard(message = error, onDismiss = viewModel::clearOperationError) }
                        }
                        uiState.groupHealthMessage?.takeIf { it.isNotBlank() }?.let { message ->
                            item { AppInfoChip(text = message, icon = AppMiuixIcons.Success, tint = OctopusTokens.Accent) }
                        }
                    }
                    when {
                        uiState.groups.isEmpty() -> item {
                            InlineEmptyCard(
                                title = stringResource(R.string.group_title),
                                summary = stringResource(R.string.group_empty),
                            )
                        }
                        groups.isEmpty() -> item {
                            InlineEmptyCard(
                                title = stringResource(R.string.empty_title),
                                summary = stringResource(R.string.group_search_empty),
                            )
                        }
                        else -> {
                            if (uiState.groupHealthEnabled) {
                                item {
                                    GroupHealthSummaryCard(
                                        views = uiState.groupHealth,
                                        submitting = uiState.groupHealthSubmitting,
                                        onRunStandard = { viewModel.runAllGroupHealth() },
                                        onRunFull = { viewModel.runAllGroupHealth(GroupHealthProbeMode.Full) },
                                    )
                                }
                            }
                            items(groups, key = { it.id }) { group ->
                                GroupRow(
                                    group = group,
                                    showHealth = uiState.groupHealthEnabled,
                                    health = uiState.groupHealth.firstOrNull { it.groupId == group.id },
                                    expanded = expanded[group.id] == true,
                                    submitting = uiState.submitting || uiState.groupHealthSubmitting,
                                    selectionMode = uiState.selectionMode,
                                    isSelected = group.id in uiState.selectedIds,
                                    onToggleExpanded = { expanded[group.id] = !(expanded[group.id] == true) },
                                    onEdit = {
                                        viewModel.clearOperationError()
                                        editingGroupId = group.id
                                    },
                                    onDelete = { deletingId = group.id },
                                    onSelect = { viewModel.toggleSelection(group.id) },
                                    onRunHealth = { mode -> viewModel.runGroupHealth(group.id, mode) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    DangerConfirmDialog(
        visible = deletingId != null,
        title = stringResource(R.string.group_delete_title),
        summary = stringResource(R.string.group_delete_summary),
        onConfirm = {
            deletingId?.let(viewModel::delete)
            deletingId = null
        },
        onDismiss = { deletingId = null },
    )

    GroupEditorDialog(
        visible = showCreateDialog,
        title = stringResource(R.string.group_create_title),
        initialGroup = null,
        channels = uiState.channels,
        modelCandidates = modelCandidates,
        submitting = uiState.submitting,
        operationError = uiState.operationError,
        onConfirm = { name, mode, matchRegex, timeout, keepTime, retryEnabled, maxRetries, items ->
            viewModel.createGroup(name, mode, matchRegex, timeout, keepTime, retryEnabled, maxRetries, items) {
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

    GroupEditorDialog(
        visible = editingGroup != null,
        title = stringResource(R.string.group_edit_title),
        initialGroup = editingGroup,
        channels = uiState.channels,
        modelCandidates = modelCandidates,
        submitting = uiState.submitting,
        operationError = uiState.operationError,
        onConfirm = { name, mode, matchRegex, timeout, keepTime, retryEnabled, maxRetries, items ->
            editingGroup?.let { current ->
                viewModel.updateGroup(current, name, mode, matchRegex, timeout, keepTime, retryEnabled, maxRetries, items) {
                    editingGroupId = null
                    viewModel.clearOperationError()
                }
            }
        },
        onDismiss = {
            if (!uiState.submitting) {
                editingGroupId = null
                viewModel.clearOperationError()
            }
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
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
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
        title = stringResource(R.string.batch_delete_groups_title),
        summary = stringResource(R.string.batch_delete_groups_summary, uiState.selectedIds.size),
        onConfirm = {
            viewModel.batchDelete()
            showBatchDeleteConfirm = false
        },
        onDismiss = { showBatchDeleteConfirm = false },
    )
}

@Composable
private fun GroupHealthSummaryCard(
    views: List<GroupHealthGroupView>,
    submitting: Boolean,
    onRunStandard: () -> Unit,
    onRunFull: () -> Unit,
) {
    val snapshots = views.mapNotNull { it.latest }
    val runningCount = snapshots.count { it.status == "running" }
    val successCount = snapshots.count { it.status == "success" }
    val failedCount = snapshots.count { it.status == "failed" || it.status == "partial" }
    val summary = if (snapshots.isEmpty()) {
        stringResource(R.string.group_health_empty)
    } else {
        stringResource(R.string.group_health_summary, successCount, failedCount, runningCount)
    }

    AppListCard(padding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.group_health_title),
                    style = top.yukonga.miuix.kmp.theme.MiuixTheme.textStyles.main,
                    fontWeight = FontWeight.SemiBold,
                    color = OctopusTokens.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = androidx.compose.ui.Modifier.weight(1f),
                )
                AppInfoChip(text = summary, icon = AppMiuixIcons.Success, tint = OctopusTokens.Accent)
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ToolbarChip(
                    text = stringResource(if (submitting) R.string.common_loading else R.string.group_health_run_all),
                    selected = false,
                    onClick = if (submitting) null else onRunStandard,
                )
                ToolbarChip(
                    text = stringResource(R.string.group_health_run_all_full),
                    selected = false,
                    onClick = if (submitting) null else onRunFull,
                )
            }
        }
    }
}
