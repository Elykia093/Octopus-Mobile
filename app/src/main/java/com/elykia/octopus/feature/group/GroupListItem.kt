package com.elykia.octopus.feature.group

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.elykia.octopus.R
import com.elykia.octopus.core.data.model.Group
import com.elykia.octopus.core.data.model.GroupItem
import com.elykia.octopus.core.designsystem.AppListCard
import com.elykia.octopus.core.designsystem.OctopusTones
import com.elykia.octopus.core.designsystem.OctopusTones.groupMode
import com.elykia.octopus.core.designsystem.OctopusTokens
import com.elykia.octopus.core.designsystem.SoftIconTile
import com.elykia.octopus.core.designsystem.icons.AppMiuixIcons
import top.yukonga.miuix.kmp.basic.Checkbox
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

private val GroupInnerRadius = 14.dp
private val GroupItemRadius = 12.dp
private val GroupBadgeRadius = 9.dp

/**
 * Group 列表项组件
 */
@Composable
fun GroupRow(
    group: Group,
    expanded: Boolean,
    submitting: Boolean,
    selectionMode: Boolean,
    isSelected: Boolean,
    onToggleExpanded: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSelect: () -> Unit,
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
                if (selectionMode) {
                    Checkbox(
                        state = if (isSelected) ToggleableState.On else ToggleableState.Off,
                        onClick = { onSelect() },
                        enabled = !submitting,
                    )
                }
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
            color = if (selected) OctopusTokens.OnAccent else OctopusTokens.TextPrimary,
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
                .background(OctopusTones.Orange),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = modelName.firstOrNull()?.uppercaseChar()?.toString() ?: "#",
                color = OctopusTokens.OnAccent,
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
fun groupModeName(mode: Int): String = when (mode) {
    1 -> stringResource(R.string.group_mode_round_robin)
    2 -> stringResource(R.string.group_mode_random)
    3 -> stringResource(R.string.group_mode_failover)
    4 -> stringResource(R.string.group_mode_weighted)
    else -> stringResource(R.string.group_mode_unknown, mode)
}
