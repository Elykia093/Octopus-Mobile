package com.elykia.octopus.feature.channel

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.elykia.octopus.core.data.model.Channel
import com.elykia.octopus.core.designsystem.AppListCard
import com.elykia.octopus.core.designsystem.AppMetricRow
import com.elykia.octopus.core.designsystem.OctopusTones
import com.elykia.octopus.core.designsystem.OctopusTones.channelType
import com.elykia.octopus.core.designsystem.OctopusTokens
import com.elykia.octopus.core.designsystem.SoftIconTile
import com.elykia.octopus.core.designsystem.formatCount
import com.elykia.octopus.core.designsystem.formatMoney
import com.elykia.octopus.core.designsystem.icons.AppMiuixIcons
import top.yukonga.miuix.kmp.basic.Checkbox
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * Channel 列表项组件
 */
@Composable
fun ChannelRow(
    channel: Channel,
    submitting: Boolean,
    selectionMode: Boolean,
    isSelected: Boolean,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSelect: () -> Unit,
) {
    val stats = channel.stats
    val requestCount = (stats?.requestSuccess ?: 0L) + (stats?.requestFailed ?: 0L)
    val totalCost = (stats?.inputCost ?: 0.0) + (stats?.outputCost ?: 0.0)

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
                    icon = AppMiuixIcons.Channel,
                    contentDescription = channel.name,
                    tint = OctopusTones.channelType(channel.type),
                )
                Text(
                    text = channel.name.ifBlank { stringResource(R.string.channel_fallback_name, channel.id) },
                    style = MiuixTheme.textStyles.title3,
                    fontWeight = FontWeight.Bold,
                    color = OctopusTokens.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                StatusPill(
                    enabled = channel.enabled,
                    clickable = !submitting && !selectionMode,
                    onClick = { onToggle(!channel.enabled) },
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

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AppMetricRow(
                    icon = AppMiuixIcons.Request,
                    label = stringResource(R.string.channel_request_count),
                    value = formatCount(requestCount),
                )
                AppMetricRow(
                    icon = AppMiuixIcons.Cost,
                    label = stringResource(R.string.channel_total_cost),
                    value = formatMoney(totalCost),
                )
            }

            Text(
                text = channel.compactSummary(),
                style = MiuixTheme.textStyles.body2,
                color = OctopusTokens.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun StatusPill(
    enabled: Boolean,
    clickable: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (enabled) OctopusTokens.Accent else OctopusTokens.Muted)
            .then(if (clickable) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 14.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(if (enabled) R.string.common_enabled else R.string.common_disabled),
            color = if (enabled) OctopusTokens.OnAccent else OctopusTokens.TextSecondary,
            style = MiuixTheme.textStyles.body2,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun Channel.compactSummary(): String {
    val enabledKeys = keys.count { it.enabled }
    val modelText = model.ifBlank { customModel }.ifBlank { stringResource(R.string.common_unknown) }
    return stringResource(R.string.channel_compact_summary, channelTypeName(type), enabledKeys, keys.size, modelText)
}

@Composable
fun channelTypeName(type: Int): String = when (type) {
    0 -> stringResource(R.string.channel_type_openai_chat)
    1 -> stringResource(R.string.channel_type_openai_response)
    2 -> stringResource(R.string.channel_type_anthropic)
    3 -> stringResource(R.string.channel_type_gemini)
    4 -> stringResource(R.string.channel_type_volcengine)
    5 -> stringResource(R.string.channel_type_embedding)
    else -> stringResource(R.string.channel_type_unknown, type)
}
