package com.elykia.octopus.feature.app

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.elykia.octopus.R
import com.elykia.octopus.core.data.model.Channel
import com.elykia.octopus.core.data.model.Group
import com.elykia.octopus.core.data.model.GroupItem
import com.elykia.octopus.core.designsystem.AppInfoChip
import com.elykia.octopus.core.designsystem.AppListCard
import com.elykia.octopus.core.designsystem.AppPageScaffold
import com.elykia.octopus.core.designsystem.AppTypePill
import com.elykia.octopus.core.designsystem.DangerConfirmDialog
import com.elykia.octopus.core.designsystem.EmptyPane
import com.elykia.octopus.core.designsystem.ErrorPane
import com.elykia.octopus.core.designsystem.FloatingCreateButton
import com.elykia.octopus.core.designsystem.InlineEmptyCard
import com.elykia.octopus.core.designsystem.LoadingPane
import com.elykia.octopus.core.designsystem.OptionChipGroup
import com.elykia.octopus.core.designsystem.OptionChipItem
import com.elykia.octopus.core.designsystem.PageActionButton
import com.elykia.octopus.core.designsystem.SearchField
import com.elykia.octopus.core.designsystem.SelectableListCard
import com.elykia.octopus.core.designsystem.icons.AppMiuixIcons
import com.elykia.octopus.feature.group.GroupViewModel
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun GroupScreen(
    contentPadding: PaddingValues,
    viewModel: GroupViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var deletingId by remember { mutableStateOf<Int?>(null) }
    var searchTerm by remember { mutableStateOf("") }
    var editingGroup by remember { mutableStateOf<Group?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    val expanded = remember { mutableStateMapOf<Int, Boolean>() }

    when {
        uiState.loading -> LoadingPane(title = stringResource(R.string.group_title))
        uiState.error != null -> ErrorPane(message = uiState.error ?: stringResource(R.string.error_title), onRetry = viewModel::refresh)
        uiState.groups.isEmpty() -> EmptyPane(title = stringResource(R.string.group_title), summary = stringResource(R.string.group_empty))
        else -> {
            val groups = uiState.groups
                .filter { searchTerm.isBlank() || it.name.contains(searchTerm, ignoreCase = true) }
                .sortedByDescending { it.items.size }

            Box(modifier = Modifier.fillMaxSize()) {
                AppPageScaffold(
                    title = stringResource(R.string.group_title),
                    actions = {
                        PageActionButton(
                            icon = AppMiuixIcons.Refresh,
                            contentDescription = stringResource(R.string.common_refresh),
                            onClick = viewModel::refresh,
                        )
                    },
                    contentPadding = contentPadding,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SearchField(
                            value = searchTerm,
                            onValueChange = { searchTerm = it },
                            hint = stringResource(R.string.group_search_hint),
                        )
                        groups.forEach { group ->
                            GroupRow(
                                group = group,
                                expanded = expanded[group.id] == true,
                                onToggleExpanded = { expanded[group.id] = !(expanded[group.id] == true) },
                                onEdit = { editingGroup = group },
                                onDelete = { deletingId = group.id },
                            )
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
                onConfirm = { name, mode, matchRegex, timeout, keepTime, items ->
                    viewModel.createGroup(name, mode, matchRegex, timeout, keepTime, items)
                    showCreateDialog = false
                },
                onDismiss = { showCreateDialog = false },
            )

            GroupEditorDialog(
                visible = editingGroup != null,
                title = stringResource(R.string.group_edit_title),
                initialGroup = editingGroup,
                channels = uiState.channels,
                onConfirm = { name, mode, matchRegex, timeout, keepTime, items ->
                    editingGroup?.let { current ->
                        viewModel.updateGroup(current, name, mode, matchRegex, timeout, keepTime, items)
                    }
                    editingGroup = null
                },
                onDismiss = { editingGroup = null },
            )
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun GroupRow(
    group: Group,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val items = if (expanded) group.items else group.items.take(3)

    AppListCard(onClick = onToggleExpanded) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = group.name,
                    style = MiuixTheme.textStyles.main,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                AppTypePill(text = groupModeName(group.mode), color = groupModeColor(group.mode))
                Icon(
                    imageVector = AppMiuixIcons.Create,
                    contentDescription = stringResource(R.string.action_edit),
                    tint = MiuixTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(18.dp)
                        .padding(1.dp)
                        .clickable(onClick = onEdit),
                )
                Icon(
                    imageVector = AppMiuixIcons.Delete,
                    contentDescription = stringResource(R.string.common_delete),
                    tint = MiuixTheme.colorScheme.error,
                    modifier = Modifier
                        .size(18.dp)
                        .padding(start = 2.dp)
                        .clickable(onClick = onDelete),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (group.firstTokenTimeOut > 0) {
                    AppInfoChip(text = stringResource(R.string.group_timeout_summary, group.firstTokenTimeOut), icon = AppMiuixIcons.Time)
                }
                if (group.sessionKeepTime > 0) {
                    AppInfoChip(text = stringResource(R.string.group_keep_summary, group.sessionKeepTime), icon = AppMiuixIcons.Time)
                }
                AppInfoChip(text = stringResource(R.string.group_channel_count, group.items.size), icon = AppMiuixIcons.Group)
            }
            if (group.items.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items.forEach { item ->
                        AppInfoChip(
                            text = item.modelName.ifBlank { stringResource(R.string.group_item_channel_fallback, item.channelId) },
                            icon = AppMiuixIcons.Channel,
                        )
                    }
                    if (group.items.size > 3) {
                        Text(
                            text = if (expanded) stringResource(R.string.group_collapse) else stringResource(R.string.group_expand_more, group.items.size - 3),
                            color = MiuixTheme.colorScheme.primary,
                            style = MiuixTheme.textStyles.body2,
                            modifier = Modifier.align(Alignment.CenterVertically),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun groupModeName(mode: Int): String = when (mode) {
    1 -> stringResource(R.string.group_mode_round_robin)
    2 -> stringResource(R.string.group_mode_random)
    3 -> stringResource(R.string.group_mode_failover)
    4 -> stringResource(R.string.group_mode_weighted)
    else -> stringResource(R.string.group_mode_unknown, mode)
}

private fun groupModeColor(mode: Int): Color = when (mode) {
    1 -> Color(0xFF007AFF)
    2 -> Color(0xFFAF52DE)
    3 -> Color(0xFFFF9500)
    4 -> Color(0xFF34C759)
    else -> Color(0xFF8E8E93)
}

@Composable
private fun GroupEditorDialog(
    visible: Boolean,
    title: String,
    initialGroup: Group?,
    channels: List<Channel>,
    onConfirm: (String, Int, String, Int, Int, List<GroupItem>) -> Unit,
    onDismiss: () -> Unit,
) {
    if (!visible) return

    var name by remember(initialGroup?.id, visible) { mutableStateOf(initialGroup?.name.orEmpty()) }
    var mode by remember(initialGroup?.id, visible) { mutableStateOf(initialGroup?.mode ?: 1) }
    var matchRegex by remember(initialGroup?.id, visible) { mutableStateOf(initialGroup?.matchRegex.orEmpty()) }
    var firstTokenTimeOut by remember(initialGroup?.id, visible) {
        mutableStateOf(initialGroup?.firstTokenTimeOut?.toString().orEmpty())
    }
    var sessionKeepTime by remember(initialGroup?.id, visible) {
        mutableStateOf(initialGroup?.sessionKeepTime?.toString().orEmpty())
    }
    var showChannelPicker by remember(initialGroup?.id, visible) { mutableStateOf(false) }
    val items = remember(initialGroup?.id, visible) {
        mutableStateListOf<GroupItem>().apply { addAll(initialGroup?.items.orEmpty()) }
    }

    OverlayDialog(
        show = visible,
        title = title,
        summary = stringResource(R.string.group_panel_summary),
        onDismissRequest = onDismiss,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            TextField(
                value = name,
                onValueChange = { name = it },
                label = stringResource(R.string.group_name_hint),
                useLabelAsPlaceholder = true,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            TextField(
                value = matchRegex,
                onValueChange = { matchRegex = it },
                label = stringResource(R.string.group_match_regex_hint),
                useLabelAsPlaceholder = true,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = groupModeName(mode),
                style = MiuixTheme.textStyles.main,
                fontWeight = FontWeight.SemiBold,
            )
            OptionChipGroup(
                options = listOf(
                    OptionChipItem(1, groupModeName(1)),
                    OptionChipItem(2, groupModeName(2)),
                    OptionChipItem(3, groupModeName(3)),
                    OptionChipItem(4, groupModeName(4)),
                ),
                selectedValue = mode,
                onSelect = { mode = it },
                columns = 2,
            )
            TextField(
                value = firstTokenTimeOut,
                onValueChange = { firstTokenTimeOut = it },
                label = stringResource(R.string.group_timeout_label),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
            TextField(
                value = sessionKeepTime,
                onValueChange = { sessionKeepTime = it },
                label = stringResource(R.string.group_keep_time_label),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = stringResource(R.string.group_items_label), style = MiuixTheme.textStyles.main, fontWeight = FontWeight.SemiBold)
                TextButton(
                    text = stringResource(R.string.group_add_item),
                    onClick = { showChannelPicker = true },
                )
            }
            if (items.isEmpty()) {
                InlineEmptyCard(
                    title = stringResource(R.string.group_channel_picker_title),
                    summary = stringResource(R.string.group_channel_picker_summary),
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items.forEachIndexed { index, item ->
                        GroupItemEditorRow(
                            item = item,
                            channel = channels.firstOrNull { it.id == item.channelId },
                            onChange = { items[index] = it },
                            onDelete = { items.removeAt(index) },
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(text = stringResource(R.string.common_cancel), onClick = onDismiss)
                TextButton(
                    text = stringResource(R.string.common_confirm),
                    enabled = name.isNotBlank(),
                    onClick = {
                        onConfirm(
                            name.trim(),
                            mode,
                            matchRegex.trim(),
                            firstTokenTimeOut.toIntOrNull() ?: 0,
                            sessionKeepTime.toIntOrNull() ?: 0,
                            items.toList(),
                        )
                    },
                )
            }
        }
    }

    ChannelPickerDialog(
        visible = showChannelPicker,
        channels = channels,
        selectedChannelIds = items.map { it.channelId }.toSet(),
        onSelect = { channel ->
            items.add(
                GroupItem(
                    channelId = channel.id,
                    modelName = channel.model.ifBlank { channel.customModel.ifBlank { channel.name } },
                    priority = 100,
                    weight = 1,
                )
            )
            showChannelPicker = false
        },
        onDismiss = { showChannelPicker = false },
    )
}

@Composable
private fun GroupItemEditorRow(
    item: GroupItem,
    channel: Channel?,
    onChange: (GroupItem) -> Unit,
    onDelete: () -> Unit,
) {
    AppListCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            AppInfoChip(
                text = stringResource(
                    R.string.group_item_channel_summary,
                    item.channelId,
                    channel?.name ?: stringResource(R.string.group_item_channel_fallback, item.channelId),
                ),
                icon = AppMiuixIcons.Channel,
            )
            TextField(
                value = item.modelName,
                onValueChange = { onChange(item.copy(modelName = it)) },
                label = stringResource(R.string.group_item_model_name_hint),
                useLabelAsPlaceholder = true,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(
                    value = item.priority.toString(),
                    onValueChange = { onChange(item.copy(priority = it.toIntOrNull() ?: 0)) },
                    label = stringResource(R.string.group_item_priority_hint),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                )
                TextField(
                    value = item.weight.toString(),
                    onValueChange = { onChange(item.copy(weight = it.toIntOrNull() ?: 0)) },
                    label = stringResource(R.string.group_item_weight_hint),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                )
            }
            TextButton(text = stringResource(R.string.common_delete), onClick = onDelete)
        }
    }
}

@Composable
private fun ChannelPickerDialog(
    visible: Boolean,
    channels: List<Channel>,
    selectedChannelIds: Set<Int>,
    onSelect: (Channel) -> Unit,
    onDismiss: () -> Unit,
) {
    if (!visible) return

    val availableChannels = channels.filterNot { it.id in selectedChannelIds }

    OverlayDialog(
        show = visible,
        title = stringResource(R.string.group_channel_picker_title),
        summary = stringResource(R.string.group_channel_picker_summary),
        onDismissRequest = onDismiss,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (availableChannels.isEmpty()) {
                InlineEmptyCard(
                    title = stringResource(R.string.group_channel_picker_title),
                    summary = stringResource(R.string.group_channel_picker_empty),
                )
            } else {
                availableChannels.forEach { channel ->
                    SelectableListCard(
                        title = channel.name,
                        summary = stringResource(
                            R.string.group_item_channel_summary,
                            channel.id,
                            channel.model.ifBlank { channel.customModel.ifBlank { channel.name } },
                        ),
                        selected = false,
                        onClick = { onSelect(channel) },
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(text = stringResource(R.string.common_cancel), onClick = onDismiss)
            }
        }
    }
}
