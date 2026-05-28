package com.elykia.octopus.feature.group

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.elykia.octopus.R
import com.elykia.octopus.core.data.model.Channel
import com.elykia.octopus.core.data.model.Group
import com.elykia.octopus.core.data.model.GroupItem
import com.elykia.octopus.core.designsystem.AppInfoChip
import com.elykia.octopus.core.designsystem.AppListCard
import com.elykia.octopus.core.designsystem.AppPageScaffold
import com.elykia.octopus.core.designsystem.DangerConfirmDialog
import com.elykia.octopus.core.designsystem.ErrorPane
import com.elykia.octopus.core.designsystem.InlineEmptyCard
import com.elykia.octopus.core.designsystem.LoadingPane
import com.elykia.octopus.core.designsystem.OctopusTones
import com.elykia.octopus.core.designsystem.OctopusTokens
import com.elykia.octopus.core.designsystem.OptionChipGroup
import com.elykia.octopus.core.designsystem.OptionChipItem
import com.elykia.octopus.core.designsystem.PageActionButton
import com.elykia.octopus.core.designsystem.SearchField
import com.elykia.octopus.core.designsystem.SelectableListCard
import com.elykia.octopus.core.designsystem.icons.AppMiuixIcons
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme

private val GroupInnerRadius = 14.dp
private val GroupItemRadius = 12.dp
private val GroupBadgeRadius = 9.dp

@Composable
fun GroupScreen(
    contentPadding: PaddingValues,
    viewModel: GroupViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var deletingId by remember { mutableStateOf<Int?>(null) }
    var searchTerm by remember { mutableStateOf("") }
    var searchVisible by remember { mutableStateOf(false) }
    var editingGroup by remember { mutableStateOf<Group?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    val expanded = remember { mutableStateMapOf<Int, Boolean>() }

    when {
        uiState.loading -> LoadingPane(title = stringResource(R.string.group_title))
        uiState.error != null -> ErrorPane(message = uiState.error ?: stringResource(R.string.error_title), onRetry = viewModel::refresh)
        else -> {
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

            Box(modifier = Modifier.fillMaxSize()) {
                AppPageScaffold(
                    title = stringResource(R.string.group_title),
                    actions = {
                        PageActionButton(
                            icon = if (searchVisible) AppMiuixIcons.Close else AppMiuixIcons.Search,
                            contentDescription = stringResource(R.string.action_open_search),
                            onClick = {
                                searchVisible = !searchVisible
                                if (!searchVisible) searchTerm = ""
                            },
                        )
                        PageActionButton(
                            icon = AppMiuixIcons.Refresh,
                            contentDescription = stringResource(R.string.common_refresh),
                            onClick = viewModel::refresh,
                        )
                        PageActionButton(
                            icon = AppMiuixIcons.Add,
                            contentDescription = stringResource(R.string.action_create),
                            onClick = { showCreateDialog = true },
                        )
                    },
                    contentPadding = contentPadding,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (uiState.groups.isNotEmpty() && searchVisible) {
                            SearchField(
                                value = searchTerm,
                                onValueChange = { searchTerm = it },
                                hint = stringResource(R.string.group_search_hint),
                            )
                        }
                        when {
                            uiState.groups.isEmpty() -> InlineEmptyCard(
                                title = stringResource(R.string.group_title),
                                summary = stringResource(R.string.group_empty),
                            )
                            groups.isEmpty() -> InlineEmptyCard(
                                title = stringResource(R.string.empty_title),
                                summary = stringResource(R.string.group_search_empty),
                            )
                            else -> groups.forEach { group ->
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
private fun GroupRow(
    group: Group,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val sortedItems = group.items.sortedBy { it.priority }
    val visibleItems = if (expanded) sortedItems else sortedItems.take(4)

    AppListCard(
        onClick = onToggleExpanded,
        padding = PaddingValues(horizontal = 18.dp, vertical = 18.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = group.name.ifBlank { stringResource(R.string.group_unnamed) },
                    style = MiuixTheme.textStyles.title3,
                    fontWeight = FontWeight.Bold,
                    color = OctopusTokens.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(1, 2, 3, 4).forEach { mode ->
                    ModeOptionPill(
                        text = groupModeName(mode),
                        selected = group.mode == mode,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 96.dp)
                    .clip(RoundedCornerShape(GroupInnerRadius))
                    .background(OctopusTokens.Card)
                    .border(
                        width = 1.dp,
                        color = OctopusTokens.Border,
                        shape = RoundedCornerShape(GroupInnerRadius),
                    )
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (group.items.isEmpty()) {
                    Text(
                        text = stringResource(R.string.group_items_empty),
                        style = MiuixTheme.textStyles.body2,
                        color = OctopusTokens.TextSecondary,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 10.dp),
                    )
                } else {
                    visibleItems.forEachIndexed { index, item ->
                        GroupItemRow(index = index, item = item, showWeight = group.mode == 4)
                    }
                    if (group.items.size > visibleItems.size) {
                        Text(
                            text = if (expanded) {
                                stringResource(R.string.group_collapse)
                            } else {
                                stringResource(R.string.group_expand_more, group.items.size - visibleItems.size)
                            },
                            color = OctopusTokens.Accent,
                            style = MiuixTheme.textStyles.body2,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(top = 2.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModeOptionPill(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) OctopusTokens.Accent else OctopusTokens.Muted)
            .padding(horizontal = 8.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MiuixTheme.textStyles.body2,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (selected) Color.White else OctopusTokens.TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun GroupItemRow(
    index: Int,
    item: GroupItem,
    showWeight: Boolean,
) {
    val modelName = item.modelName.ifBlank { stringResource(R.string.group_item_channel_fallback, item.channelId) }
    val itemMeta = if (showWeight) {
        "P${item.priority} / W${item.weight}"
    } else {
        "P${item.priority}"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(GroupItemRadius))
            .background(OctopusTokens.Muted)
            .border(1.dp, OctopusTokens.Border.copy(alpha = 0.7f), RoundedCornerShape(GroupItemRadius))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(GroupBadgeRadius))
                .background(OctopusTokens.PrimarySoft),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = (index + 1).toString(),
                color = OctopusTokens.Accent,
                style = MiuixTheme.textStyles.body1,
                fontWeight = FontWeight.Bold,
            )
        }
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(OctopusTones.Orange.copy(alpha = 0.88f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = modelName.firstOrNull()?.uppercaseChar()?.toString() ?: "#",
                color = Color.White,
                style = MiuixTheme.textStyles.body2,
                fontWeight = FontWeight.Bold,
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = modelName,
                style = MiuixTheme.textStyles.main,
                fontWeight = FontWeight.Medium,
                color = OctopusTokens.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = stringResource(R.string.group_item_channel_summary, item.channelId, itemMeta),
                style = MiuixTheme.textStyles.body2,
                color = OctopusTokens.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
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
