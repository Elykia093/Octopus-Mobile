package com.elykia.octopus.feature.group

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.elykia.octopus.R
import com.elykia.octopus.core.data.model.GroupAutoGroupConfig
import com.elykia.octopus.core.data.model.GroupAutoGroupSource
import com.elykia.octopus.core.designsystem.AppListCard
import com.elykia.octopus.core.designsystem.DialogScrollableColumn
import com.elykia.octopus.core.designsystem.InlineEmptyCard
import com.elykia.octopus.core.designsystem.LoadingStateCard
import com.elykia.octopus.core.designsystem.OperationErrorCard
import com.elykia.octopus.core.designsystem.SearchField
import com.elykia.octopus.core.designsystem.ToolbarChip
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun GroupAutoGroupDialog(
    visible: Boolean,
    config: GroupAutoGroupConfig?,
    loading: Boolean,
    submitting: Boolean,
    error: String?,
    onLoad: () -> Unit,
    onSave: (Int, Map<Int, Int>, Boolean, () -> Unit) -> Unit,
    onRun: (() -> Unit) -> Unit,
    onDismiss: () -> Unit,
    onClearError: () -> Unit,
) {
    if (!visible) return

    var searchTerm by remember(visible) { mutableStateOf("") }
    var projectedGlobalMode by remember(visible) { mutableStateOf(0) }
    val sourceModes = remember(visible) { mutableStateMapOf<Int, Int>() }

    LaunchedEffect(visible) {
        if (visible) onLoad()
    }
    LaunchedEffect(config) {
        if (config == null) return@LaunchedEffect
        projectedGlobalMode = config.projectedGlobalAutoGroup
        sourceModes.clear()
        config.sources.forEach { source ->
            sourceModes[source.channelId] = source.autoGroup
        }
    }

    val query = searchTerm.trim()
    val sources = config?.sources.orEmpty()
        .filter { source -> query.isBlank() || source.matchesAutoGroupQuery(query) }
        .sortedWith(compareBy<GroupAutoGroupSource> { it.siteName.orEmpty().lowercase() }.thenBy { it.channelName.lowercase() })

    OverlayDialog(
        show = visible,
        title = stringResource(R.string.group_auto_group_title),
        summary = stringResource(R.string.group_auto_group_summary),
        onDismissRequest = onDismiss,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            error?.takeIf { it.isNotBlank() }?.let { message ->
                OperationErrorCard(message = message, onDismiss = onClearError)
            }

            if (loading && config == null) {
                LoadingStateCard(title = stringResource(R.string.group_auto_group_title))
            } else {
                Text(
                    text = stringResource(R.string.group_auto_group_global_mode),
                    style = MiuixTheme.textStyles.main,
                    fontWeight = FontWeight.SemiBold,
                )
                AutoGroupModeChips(
                    selectedMode = projectedGlobalMode,
                    enabled = !submitting,
                    onSelect = { projectedGlobalMode = it },
                )

                SearchField(
                    value = searchTerm,
                    onValueChange = { searchTerm = it },
                    hint = stringResource(R.string.group_auto_group_search_hint),
                )

                DialogScrollableColumn(fraction = 0.56f, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    when {
                        config == null -> InlineEmptyCard(
                            title = stringResource(R.string.group_auto_group_title),
                            summary = stringResource(R.string.group_auto_group_empty),
                        )
                        sources.isEmpty() -> InlineEmptyCard(
                            title = stringResource(R.string.empty_title),
                            summary = stringResource(R.string.group_auto_group_search_empty),
                        )
                        else -> sources.forEach { source ->
                            AutoGroupSourceCard(
                                source = source,
                                selectedMode = sourceModes[source.channelId] ?: source.autoGroup,
                                globalMode = projectedGlobalMode,
                                submitting = submitting,
                                onModeChange = { sourceModes[source.channelId] = it },
                            )
                        }
                    }
                }

                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ToolbarChip(
                        text = stringResource(R.string.group_auto_group_run),
                        selected = false,
                        onClick = if (submitting || config == null) null else { { onRun {} } },
                    )
                    ToolbarChip(
                        text = stringResource(R.string.common_save),
                        selected = false,
                        onClick = if (submitting || config == null) {
                            null
                        } else {
                            { onSave(projectedGlobalMode, sourceModes.toMap(), false) {} }
                        },
                    )
                    ToolbarChip(
                        text = stringResource(R.string.group_auto_group_save_and_run),
                        selected = false,
                        onClick = if (submitting || config == null) {
                            null
                        } else {
                            { onSave(projectedGlobalMode, sourceModes.toMap(), true) {} }
                        },
                    )
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
}

@Composable
private fun AutoGroupSourceCard(
    source: GroupAutoGroupSource,
    selectedMode: Int,
    globalMode: Int,
    submitting: Boolean,
    onModeChange: (Int) -> Unit,
) {
    val siteName = source.siteName?.takeIf { it.isNotBlank() } ?: stringResource(R.string.group_auto_group_manual_source)
    val summary = buildString {
        append(siteName)
        source.siteAccountName?.takeIf { it.isNotBlank() }?.let { append(" / ").append(it) }
        source.siteGroupName?.takeIf { it.isNotBlank() }?.let { append(" / ").append(it) }
        append(" · ")
        append(stringResource(R.string.group_auto_group_model_count, source.modelCount))
    }

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
                        text = source.channelName.ifBlank { stringResource(R.string.group_auto_group_channel_fallback, source.channelId) },
                        style = MiuixTheme.textStyles.main,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = summary,
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (!source.enabled) {
                    ToolbarChip(text = stringResource(R.string.common_disabled), selected = true)
                } else if (source.managed && globalMode != 0) {
                    ToolbarChip(text = autoGroupModeName(globalMode), selected = true)
                }
            }
            AutoGroupModeChips(
                selectedMode = selectedMode,
                enabled = !submitting,
                onSelect = onModeChange,
            )
        }
    }
}

@Composable
private fun AutoGroupModeChips(
    selectedMode: Int,
    enabled: Boolean,
    onSelect: (Int) -> Unit,
) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(0, 1, 2, 3).forEach { mode ->
            ToolbarChip(
                text = autoGroupModeName(mode),
                selected = selectedMode == mode,
                onClick = if (enabled) { { onSelect(mode) } } else null,
            )
        }
    }
}

@Composable
private fun autoGroupModeName(mode: Int): String = when (mode) {
    1 -> stringResource(R.string.channel_auto_group_fuzzy)
    2 -> stringResource(R.string.channel_auto_group_exact)
    3 -> stringResource(R.string.channel_auto_group_regex)
    0 -> stringResource(R.string.channel_auto_group_none)
    else -> stringResource(R.string.channel_auto_group_unknown, mode)
}

private fun GroupAutoGroupSource.matchesAutoGroupQuery(query: String): Boolean {
    val normalized = query.lowercase()
    return listOf(
        channelName,
        siteName.orEmpty(),
        siteAccountName.orEmpty(),
        siteGroupName.orEmpty(),
        siteGroupKey.orEmpty(),
        endpointType.orEmpty(),
    ).any { it.lowercase().contains(normalized) } ||
        models.any { it.lowercase().contains(normalized) }
}
