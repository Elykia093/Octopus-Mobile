package com.elykia.octopus.feature.group

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.elykia.octopus.R
import com.elykia.octopus.core.data.model.Channel
import com.elykia.octopus.core.data.model.Group
import com.elykia.octopus.core.data.model.GroupItem
import com.elykia.octopus.core.designsystem.DialogScrollableColumn
import com.elykia.octopus.core.designsystem.InlineEmptyCard
import com.elykia.octopus.core.designsystem.OptionChipGroup
import com.elykia.octopus.core.designsystem.OptionChipItem
import com.elykia.octopus.core.designsystem.OperationErrorCard
import com.elykia.octopus.core.designsystem.SearchField
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * Group 编辑器对话框
 */
@Composable
internal fun GroupEditorDialog(
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

/**
 * Channel 选择器对话框
 */
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
                        com.elykia.octopus.core.designsystem.SelectableListCard(
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
