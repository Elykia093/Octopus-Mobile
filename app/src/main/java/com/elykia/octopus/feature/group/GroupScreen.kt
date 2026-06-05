package com.elykia.octopus.feature.group

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.elykia.octopus.R
import com.elykia.octopus.core.data.model.Channel
import com.elykia.octopus.core.data.model.Group
import com.elykia.octopus.core.data.model.GroupItem
import com.elykia.octopus.core.designsystem.AppLazyPageScaffold
import com.elykia.octopus.core.designsystem.AppInfoChip
import com.elykia.octopus.core.designsystem.AppListCard
import com.elykia.octopus.core.designsystem.DangerConfirmDialog
import com.elykia.octopus.core.designsystem.DialogScrollableColumn
import com.elykia.octopus.core.designsystem.ErrorStateCard
import com.elykia.octopus.core.designsystem.InlineEmptyCard
import com.elykia.octopus.core.designsystem.LoadingStateCard
import com.elykia.octopus.core.designsystem.OctopusTones
import com.elykia.octopus.core.designsystem.OctopusTokens
import com.elykia.octopus.core.designsystem.OptionChipGroup
import com.elykia.octopus.core.designsystem.OptionChipItem
import com.elykia.octopus.core.designsystem.OperationErrorCard
import com.elykia.octopus.core.designsystem.PageActionButton
import com.elykia.octopus.core.designsystem.SearchField
import com.elykia.octopus.core.designsystem.SelectableListCard
import com.elykia.octopus.core.designsystem.SoftIconTile
import com.elykia.octopus.core.designsystem.icons.AppMiuixIcons
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Switch
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
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var deletingId by remember { mutableStateOf<Int?>(null) }
    var searchTerm by remember { mutableStateOf("") }
    var searchVisible by remember { mutableStateOf(false) }
    var editingGroupId by remember { mutableStateOf<Int?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
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
    val editingGroup = resolveEditingGroup(uiState.groups, editingGroupId)

    Box(modifier = Modifier.fillMaxSize()) {
        AppLazyPageScaffold(
            title = stringResource(R.string.group_title),
            actions = {
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
                        else -> items(groups, key = { it.id }) { group ->
                            GroupRow(
                                group = group,
                                expanded = expanded[group.id] == true,
                                submitting = uiState.submitting,
                                onToggleExpanded = { expanded[group.id] = !(expanded[group.id] == true) },
                                onEdit = {
                                    viewModel.clearOperationError()
                                    editingGroupId = group.id
                                },
                                onDelete = { deletingId = group.id },
                            )
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
}

internal fun resolveEditingGroup(
    groups: List<Group>,
    editingGroupId: Int?,
): Group? = editingGroupId?.let { id -> groups.firstOrNull { it.id == id } }

@Composable
private fun GroupRow(
    group: Group,
    expanded: Boolean,
    submitting: Boolean,
    onToggleExpanded: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val sortedItems = group.items.sortedBy { it.priority }
    val visibleItems = if (expanded) sortedItems else sortedItems.take(4)

    AppListCard(padding = PaddingValues(horizontal = 18.dp, vertical = 18.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                SoftIconTile(
                    icon = AppMiuixIcons.Group,
                    contentDescription = group.name,
                    tint = OctopusTones.groupMode(group.mode),
                )
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

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(1, 2, 3, 4).chunked(2).forEach { rowModes ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        rowModes.forEach { mode ->
                            ModeOptionPill(
                                text = groupModeName(mode),
                                selected = group.mode == mode,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 96.dp)
                    .clip(RoundedCornerShape(GroupInnerRadius))
                    .background(OctopusTokens.Muted.copy(alpha = 0.58f))
                    .border(
                        width = 1.dp,
                        color = OctopusTokens.Border.copy(alpha = 0.72f),
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
                    GroupExpandAction(
                        expanded = expanded,
                        hiddenCount = (group.items.size - visibleItems.size).coerceAtLeast(0),
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        onClick = onToggleExpanded,
                    )
                }
            }
        }
    }
}

@Composable
private fun GroupExpandAction(
    expanded: Boolean,
    hiddenCount: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    if (!expanded && hiddenCount <= 0) return
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (expanded) AppMiuixIcons.ArrowUp else AppMiuixIcons.ArrowDown,
            contentDescription = null,
            tint = OctopusTokens.Accent,
            modifier = Modifier.size(14.dp),
        )
        Text(
            text = if (expanded) {
                stringResource(R.string.group_collapse)
            } else {
                stringResource(R.string.group_expand_more, hiddenCount)
            },
            color = OctopusTokens.Accent,
            style = MiuixTheme.textStyles.body2,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
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
            .background(OctopusTokens.Card.copy(alpha = 0.82f))
            .border(1.dp, OctopusTokens.Border.copy(alpha = 0.62f), RoundedCornerShape(GroupItemRadius))
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
                .background(OctopusTones.Orange.copy(alpha = 0.72f)),
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

internal enum class GroupEditorValidationIssue {
    InvalidFirstTokenTimeout,
    InvalidSessionKeepTime,
    InvalidMaxRetries,
}

internal data class GroupEditorValues(
    val firstTokenTimeOut: Int,
    val sessionKeepTime: Int,
    val maxRetries: Int,
)

internal fun parseGroupEditorValues(
    firstTokenTimeOut: String,
    sessionKeepTime: String,
    retryEnabled: Boolean,
    maxRetries: String,
): Result<GroupEditorValues> {
    val parsedTimeout = parseOptionalNonNegativeInt(firstTokenTimeOut)
        ?: return Result.failure(GroupEditorValidationException(GroupEditorValidationIssue.InvalidFirstTokenTimeout))
    val parsedKeepTime = parseOptionalNonNegativeInt(sessionKeepTime)
        ?: return Result.failure(GroupEditorValidationException(GroupEditorValidationIssue.InvalidSessionKeepTime))
    val parsedMaxRetries = if (retryEnabled) {
        parseOptionalPositiveInt(maxRetries, defaultValue = 3)
            ?: return Result.failure(GroupEditorValidationException(GroupEditorValidationIssue.InvalidMaxRetries))
    } else {
        3
    }

    return Result.success(
        GroupEditorValues(
            firstTokenTimeOut = parsedTimeout,
            sessionKeepTime = parsedKeepTime,
            maxRetries = parsedMaxRetries,
        ),
    )
}

internal fun groupEditorValidationIssue(
    firstTokenTimeOut: String,
    sessionKeepTime: String,
    retryEnabled: Boolean,
    maxRetries: String,
): GroupEditorValidationIssue? =
    (parseGroupEditorValues(
        firstTokenTimeOut = firstTokenTimeOut,
        sessionKeepTime = sessionKeepTime,
        retryEnabled = retryEnabled,
        maxRetries = maxRetries,
    ).exceptionOrNull() as? GroupEditorValidationException)
        ?.issue

internal fun canSubmitGroupEditor(
    name: String,
    firstTokenTimeOut: String,
    sessionKeepTime: String,
    retryEnabled: Boolean,
    maxRetries: String,
    hasValidItems: Boolean,
    submitting: Boolean,
): Boolean =
    !submitting &&
        name.isNotBlank() &&
        hasValidItems &&
        groupEditorValidationIssue(
            firstTokenTimeOut = firstTokenTimeOut,
            sessionKeepTime = sessionKeepTime,
            retryEnabled = retryEnabled,
            maxRetries = maxRetries,
        ) == null

private fun parseOptionalNonNegativeInt(value: String): Int? {
    val trimmed = value.trim()
    if (trimmed.isBlank()) return 0
    return trimmed.toIntOrNull()?.takeIf { it >= 0 }
}

private fun parseOptionalPositiveInt(value: String, defaultValue: Int): Int? {
    val trimmed = value.trim()
    if (trimmed.isBlank()) return defaultValue
    return trimmed.toIntOrNull()?.takeIf { it > 0 }
}

internal class GroupEditorValidationException(
    val issue: GroupEditorValidationIssue,
) : IllegalArgumentException(issue.name)

internal fun parseGroupItemNonNegativeInt(value: String): Int? =
    value.trim().toIntOrNull()?.takeIf { it >= 0 }

internal data class GroupItemNumberInput(
    val displayValue: String,
    val value: Int,
)

internal fun parseGroupItemNumberInput(value: String): GroupItemNumberInput? {
    val trimmed = value.trim()
    if (trimmed.isBlank()) return GroupItemNumberInput(displayValue = "", value = 0)
    val parsed = trimmed.toIntOrNull()?.takeIf { it >= 0 } ?: return null
    return GroupItemNumberInput(displayValue = trimmed, value = parsed)
}

@Composable
private fun GroupEditorDialog(
    visible: Boolean,
    title: String,
    initialGroup: Group?,
    channels: List<Channel>,
    modelCandidates: List<GroupModelCandidate>,
    submitting: Boolean,
    operationError: String?,
    onConfirm: (String, Int, String, Int, Int, Boolean, Int, List<GroupItem>) -> Unit,
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
    var retryEnabled by remember(initialGroup?.id, visible) {
        mutableStateOf(initialGroup?.retryEnabled ?: false)
    }
    var maxRetries by remember(initialGroup?.id, visible) {
        mutableStateOf((initialGroup?.maxRetries?.takeIf { it > 0 } ?: 3).toString())
    }
    var validationIssue by remember(initialGroup?.id, visible) { mutableStateOf<GroupEditorValidationIssue?>(null) }
    var showChannelPicker by remember(initialGroup?.id, visible) { mutableStateOf(false) }
    val items = remember(initialGroup?.id, visible) {
        mutableStateListOf<GroupItem>().apply { addAll(initialGroup?.items.orEmpty().sortedBy { it.priority }) }
    }
    val selectedKeys = items.map { GroupModelCandidateKey(it.channelId, it.modelName) }.toSet()
    val matchingCandidates = findMatchingGroupModelCandidates(
        candidates = modelCandidates,
        name = name,
        matchRegex = matchRegex,
        selectedKeys = selectedKeys,
    )
    val hasValidItems = items.all { it.modelName.isNotBlank() }
    val editorScrollState = rememberScrollState()
    val currentValidationIssue = groupEditorValidationIssue(
        firstTokenTimeOut = firstTokenTimeOut,
        sessionKeepTime = sessionKeepTime,
        retryEnabled = retryEnabled,
        maxRetries = maxRetries,
    ) ?: validationIssue

    OverlayDialog(
        show = visible,
        title = title,
        summary = stringResource(R.string.group_panel_summary),
        onDismissRequest = {
            if (!submitting) onDismiss()
        },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DialogScrollableColumn(
                fraction = 0.66f,
                scrollState = editorScrollState,
            ) {
                operationError?.takeIf { it.isNotBlank() }?.let { error ->
                    OperationErrorCard(message = error)
                }
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = stringResource(R.string.group_name_hint),
                    useLabelAsPlaceholder = true,
                    singleLine = true,
                    enabled = !submitting,
                    modifier = Modifier.fillMaxWidth(),
                )
                TextField(
                    value = matchRegex,
                    onValueChange = { matchRegex = it },
                    label = stringResource(R.string.group_match_regex_hint),
                    useLabelAsPlaceholder = true,
                    singleLine = true,
                    enabled = !submitting,
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
                    onSelect = { if (!submitting) mode = it },
                    columns = 2,
                )
                TextField(
                    value = firstTokenTimeOut,
                    onValueChange = {
                        firstTokenTimeOut = it
                        validationIssue = null
                    },
                    label = stringResource(R.string.group_timeout_label),
                    singleLine = true,
                    enabled = !submitting,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                TextField(
                    value = sessionKeepTime,
                    onValueChange = {
                        sessionKeepTime = it
                        validationIssue = null
                    },
                    label = stringResource(R.string.group_keep_time_label),
                    singleLine = true,
                    enabled = !submitting,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.group_retry_enabled_label),
                        style = MiuixTheme.textStyles.main,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                    )
                    Switch(
                        checked = retryEnabled,
                        onCheckedChange = {
                            if (!submitting) {
                                retryEnabled = it
                                validationIssue = null
                            }
                        },
                    )
                }
                TextField(
                    value = maxRetries,
                    onValueChange = {
                        maxRetries = it
                        validationIssue = null
                    },
                    label = stringResource(R.string.group_max_retries_label),
                    singleLine = true,
                    enabled = !submitting && retryEnabled,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                currentValidationIssue?.let { issue ->
                    OperationErrorCard(message = groupEditorValidationMessage(issue))
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = stringResource(R.string.group_items_label),
                        style = MiuixTheme.textStyles.main,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(
                            text = stringResource(R.string.group_auto_add_items),
                            enabled = !submitting && matchingCandidates.isNotEmpty(),
                            onClick = {
                                matchingCandidates.forEach { candidate ->
                                    items.add(
                                        GroupItem(
                                            channelId = candidate.channelId,
                                            modelName = candidate.modelName,
                                            priority = nextGroupItemPriority(items),
                                            weight = 1,
                                        )
                                    )
                                }
                            },
                        )
                        TextButton(
                            text = stringResource(R.string.group_add_item),
                            enabled = !submitting,
                            onClick = { showChannelPicker = true },
                        )
                    }
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
                                enabled = !submitting,
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(
                    text = stringResource(R.string.common_cancel),
                    enabled = !submitting,
                    onClick = onDismiss,
                )
                TextButton(
                    text = if (submitting) {
                        stringResource(R.string.group_submitting)
                    } else {
                        stringResource(R.string.common_confirm)
                    },
                    enabled = canSubmitGroupEditor(
                        name = name,
                        firstTokenTimeOut = firstTokenTimeOut,
                        sessionKeepTime = sessionKeepTime,
                        retryEnabled = retryEnabled,
                        maxRetries = maxRetries,
                        hasValidItems = hasValidItems,
                        submitting = submitting,
                    ),
                    onClick = {
                        val parsedValues = parseGroupEditorValues(
                            firstTokenTimeOut = firstTokenTimeOut,
                            sessionKeepTime = sessionKeepTime,
                            retryEnabled = retryEnabled,
                            maxRetries = maxRetries,
                        ).getOrElse { exception ->
                            validationIssue = (exception as? GroupEditorValidationException)?.issue
                                ?: GroupEditorValidationIssue.InvalidFirstTokenTimeout
                            return@TextButton
                        }
                        onConfirm(
                            name.trim(),
                            mode,
                            matchRegex.trim(),
                            parsedValues.firstTokenTimeOut,
                            parsedValues.sessionKeepTime,
                            retryEnabled,
                            parsedValues.maxRetries,
                            items.toList(),
                        )
                    },
                )
            }
        }
    }

    ChannelPickerDialog(
        visible = showChannelPicker,
        candidates = modelCandidates,
        selectedKeys = selectedKeys,
        onSelect = { candidate ->
            items.add(
                GroupItem(
                    channelId = candidate.channelId,
                    modelName = candidate.modelName,
                    priority = nextGroupItemPriority(items),
                    weight = 1,
                )
            )
            showChannelPicker = false
        },
        onDismiss = { showChannelPicker = false },
    )
}

@Composable
private fun groupEditorValidationMessage(issue: GroupEditorValidationIssue): String = when (issue) {
    GroupEditorValidationIssue.InvalidFirstTokenTimeout -> stringResource(R.string.group_invalid_timeout)
    GroupEditorValidationIssue.InvalidSessionKeepTime -> stringResource(R.string.group_invalid_keep_time)
    GroupEditorValidationIssue.InvalidMaxRetries -> stringResource(R.string.group_invalid_max_retries)
}

@Composable
private fun GroupItemEditorRow(
    item: GroupItem,
    channel: Channel?,
    onChange: (GroupItem) -> Unit,
    onDelete: () -> Unit,
    enabled: Boolean,
) {
    var priorityText by remember(item.id, item.groupId, item.channelId, item.modelName) {
        mutableStateOf(item.priority.toString())
    }
    var weightText by remember(item.id, item.groupId, item.channelId, item.modelName) {
        mutableStateOf(item.weight.toString())
    }

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
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(
                    value = priorityText,
                    onValueChange = { value ->
                        parseGroupItemNumberInput(value)?.let { input ->
                            priorityText = input.displayValue
                            onChange(item.copy(priority = input.value))
                        }
                    },
                    label = stringResource(R.string.group_item_priority_hint),
                    singleLine = true,
                    enabled = enabled,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                )
                TextField(
                    value = weightText,
                    onValueChange = { value ->
                        parseGroupItemNumberInput(value)?.let { input ->
                            weightText = input.displayValue
                            onChange(item.copy(weight = input.value))
                        }
                    },
                    label = stringResource(R.string.group_item_weight_hint),
                    singleLine = true,
                    enabled = enabled,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                )
            }
            TextButton(text = stringResource(R.string.common_delete), enabled = enabled, onClick = onDelete)
        }
    }
}

@Composable
private fun ChannelPickerDialog(
    visible: Boolean,
    candidates: List<GroupModelCandidate>,
    selectedKeys: Set<GroupModelCandidateKey>,
    onSelect: (GroupModelCandidate) -> Unit,
    onDismiss: () -> Unit,
) {
    if (!visible) return

    val availableCandidates = candidates.filterNot { it.key in selectedKeys }
    val pickerScrollState = rememberScrollState()
    var searchTerm by remember { mutableStateOf("") }
    val visibleCandidates = remember(availableCandidates, searchTerm) {
        val keyword = searchTerm.trim()
        if (keyword.isBlank()) {
            availableCandidates
        } else {
            availableCandidates.filter { candidate ->
                candidate.modelName.contains(keyword, ignoreCase = true) ||
                    candidate.channelName.contains(keyword, ignoreCase = true) ||
                    candidate.channelId.toString().contains(keyword)
            }
        }
    }

    OverlayDialog(
        show = visible,
        title = stringResource(R.string.group_channel_picker_title),
        summary = stringResource(R.string.group_channel_picker_summary),
        onDismissRequest = onDismiss,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (availableCandidates.isNotEmpty()) {
                SearchField(
                    value = searchTerm,
                    onValueChange = { searchTerm = it },
                    hint = stringResource(R.string.group_model_picker_search_hint),
                )
            }
            DialogScrollableColumn(
                fraction = 0.58f,
                scrollState = pickerScrollState,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (availableCandidates.isEmpty()) {
                    InlineEmptyCard(
                        title = stringResource(R.string.group_channel_picker_title),
                        summary = stringResource(R.string.group_channel_picker_empty),
                    )
                } else if (visibleCandidates.isEmpty()) {
                    InlineEmptyCard(
                        title = stringResource(R.string.empty_title),
                        summary = stringResource(R.string.group_model_picker_search_empty),
                    )
                } else {
                    visibleCandidates.forEach { candidate ->
                        SelectableListCard(
                            title = candidate.modelName,
                            summary = stringResource(
                                R.string.group_item_channel_summary,
                                candidate.channelId,
                                candidate.channelName,
                            ),
                            selected = false,
                            onClick = { onSelect(candidate) },
                        )
                    }
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
