package com.elykia.octopus.feature.group

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.elykia.octopus.R
import com.elykia.octopus.core.data.model.Channel
import com.elykia.octopus.core.data.model.Group
import com.elykia.octopus.core.data.model.GroupItem
import com.elykia.octopus.core.data.model.GroupPreset
import com.elykia.octopus.core.designsystem.AppListCard
import com.elykia.octopus.core.designsystem.DialogScrollableColumn
import com.elykia.octopus.core.designsystem.InlineEmptyCard
import com.elykia.octopus.core.designsystem.OperationErrorCard
import com.elykia.octopus.core.designsystem.ToolbarChip
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun GroupPresetDialog(
    visible: Boolean,
    group: Group?,
    presets: List<GroupPreset>,
    loading: Boolean,
    submitting: Boolean,
    error: String?,
    channels: List<Channel>,
    modelCandidates: List<GroupModelCandidate>,
    onLoad: (Int) -> Unit,
    onCreateCurrent: (Int, String, (GroupPreset) -> Unit) -> Unit,
    onCreateBlank: (Int, String, (GroupPreset) -> Unit) -> Unit,
    onClone: (Int, Int, String, (GroupPreset) -> Unit) -> Unit,
    onActivate: (Int, Int, () -> Unit) -> Unit,
    onDelete: (Int, Int, () -> Unit) -> Unit,
    onUpdate: (GroupPreset, String, Int, String, Int, Int, Boolean, Int, List<GroupItem>, () -> Unit) -> Unit,
    onDismiss: () -> Unit,
    onClearError: () -> Unit,
) {
    if (!visible || group == null) return

    var nameDraft by remember(group.id, visible) { mutableStateOf("") }
    var editingPreset by remember(group.id, visible) { mutableStateOf<GroupPreset?>(null) }
    var deletingPreset by remember(group.id, visible) { mutableStateOf<GroupPreset?>(null) }
    val activePresetId = group.activePresetId

    LaunchedEffect(group.id, visible) {
        if (visible) onLoad(group.id)
    }

    OverlayDialog(
        show = visible,
        title = stringResource(R.string.group_presets),
        summary = group.name.ifBlank { stringResource(R.string.group_unnamed) },
        onDismissRequest = onDismiss,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            error?.takeIf { it.isNotBlank() }?.let { message ->
                OperationErrorCard(message = message, onDismiss = onClearError)
            }

            TextField(
                value = nameDraft,
                onValueChange = { nameDraft = it },
                label = stringResource(R.string.group_preset_name_hint),
                useLabelAsPlaceholder = true,
                singleLine = true,
                enabled = !submitting,
                modifier = Modifier.fillMaxWidth(),
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ToolbarChip(
                    text = stringResource(R.string.group_preset_save_current),
                    selected = false,
                    onClick = if (submitting || nameDraft.isBlank()) {
                        null
                    } else {
                        {
                            onCreateCurrent(group.id, nameDraft.trim()) {
                                nameDraft = ""
                            }
                        }
                    },
                )
                ToolbarChip(
                    text = stringResource(R.string.group_preset_create_blank),
                    selected = false,
                    onClick = if (submitting || nameDraft.isBlank()) {
                        null
                    } else {
                        {
                            onCreateBlank(group.id, nameDraft.trim()) { preset ->
                                nameDraft = ""
                                editingPreset = preset
                            }
                        }
                    },
                )
            }

            DialogScrollableColumn(fraction = 0.56f, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                when {
                    loading -> Text(
                        text = stringResource(R.string.common_loading),
                        style = MiuixTheme.textStyles.body2,
                    )
                    presets.isEmpty() -> InlineEmptyCard(
                        title = stringResource(R.string.group_presets),
                        summary = stringResource(R.string.group_preset_empty),
                    )
                    else -> presets.forEach { preset ->
                        val presetName = preset.name.ifBlank { stringResource(R.string.group_preset_default_name) }
                        val clonedName = stringResource(R.string.group_preset_clone_name, presetName)
                        PresetListItem(
                            preset = preset,
                            active = preset.id == activePresetId,
                            submitting = submitting,
                            onActivate = {
                                onActivate(group.id, preset.id) {}
                            },
                            onEdit = { editingPreset = preset },
                            onClone = {
                                onClone(group.id, preset.id, clonedName) { cloned ->
                                    editingPreset = cloned
                                }
                            },
                            onDelete = { deletingPreset = preset },
                        )
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
            }
        }
    }

    deletingPreset?.let { preset ->
        OverlayDialog(
            show = true,
            title = stringResource(R.string.group_preset_delete_title),
            summary = stringResource(R.string.group_preset_delete_summary, preset.name),
            onDismissRequest = { deletingPreset = null },
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(
                    text = stringResource(R.string.common_cancel),
                    enabled = !submitting,
                    onClick = { deletingPreset = null },
                )
                TextButton(
                    text = stringResource(R.string.common_confirm),
                    enabled = !submitting,
                    onClick = {
                        onDelete(group.id, preset.id) {
                            deletingPreset = null
                        }
                    },
                )
            }
        }
    }

    editingPreset?.let { preset ->
        GroupEditorDialog(
            visible = true,
            title = stringResource(R.string.group_preset_edit_title),
            initialGroup = preset.toEditableGroup(),
            channels = channels,
            modelCandidates = modelCandidates,
            submitting = submitting,
            operationError = error,
            onConfirm = { name, mode, matchRegex, timeout, keepTime, retryEnabled, maxRetries, items ->
                onUpdate(
                    preset,
                    name,
                    mode,
                    matchRegex,
                    timeout,
                    keepTime,
                    retryEnabled,
                    maxRetries,
                    items,
                ) {
                    editingPreset = null
                }
            },
            onDismiss = {
                if (!submitting) editingPreset = null
            },
        )
    }
}

@Composable
private fun PresetListItem(
    preset: GroupPreset,
    active: Boolean,
    submitting: Boolean,
    onActivate: () -> Unit,
    onEdit: () -> Unit,
    onClone: () -> Unit,
    onDelete: () -> Unit,
) {
    AppListCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = preset.name.ifBlank { stringResource(R.string.group_preset_default_name) },
                        style = MiuixTheme.textStyles.main,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = stringResource(R.string.group_preset_summary, groupModeName(preset.mode), preset.items.size),
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (active) {
                    ToolbarChip(text = stringResource(R.string.group_preset_active), selected = true)
                }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ToolbarChip(
                    text = stringResource(R.string.group_preset_activate),
                    selected = active,
                    onClick = if (submitting || active) null else onActivate,
                )
                ToolbarChip(
                    text = stringResource(R.string.action_edit),
                    selected = false,
                    onClick = if (submitting) null else onEdit,
                )
                ToolbarChip(
                    text = stringResource(R.string.common_copy),
                    selected = false,
                    onClick = if (submitting) null else onClone,
                )
                if (!active) {
                    ToolbarChip(
                        text = stringResource(R.string.common_delete),
                        selected = false,
                        onClick = if (submitting) null else onDelete,
                    )
                }
            }
        }
    }
}

private fun GroupPreset.toEditableGroup(): Group = Group(
    id = id,
    name = name,
    mode = mode,
    matchRegex = matchRegex,
    firstTokenTimeOut = firstTokenTimeOut,
    sessionKeepTime = sessionKeepTime,
    retryEnabled = retryEnabled,
    maxRetries = maxRetries,
    items = items.sortedBy { it.priority }.map { item ->
        GroupItem(
            channelId = item.channelId,
            modelName = item.modelName,
            priority = item.priority,
            weight = item.weight,
        )
    },
)
