package com.elykia.octopus.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.elykia.octopus.R
import com.elykia.octopus.core.designsystem.AppInfoChip
import com.elykia.octopus.core.designsystem.AppListCard
import com.elykia.octopus.core.designsystem.OctopusTones
import com.elykia.octopus.core.designsystem.OctopusTokens
import com.elykia.octopus.core.designsystem.ToolbarChip
import com.elykia.octopus.core.designsystem.formatCount
import com.elykia.octopus.core.designsystem.formatDurationMs
import com.elykia.octopus.core.designsystem.formatMoney
import com.elykia.octopus.core.designsystem.icons.AppMiuixIcons
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * Dashboard 概览区块
 */
@Composable
fun DashboardOverviewSection(
    snapshot: StatsSnapshot,
    showToday: Boolean,
    onScopeChange: (Boolean) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        DashboardHeroCard(
            snapshot = snapshot,
            showToday = showToday,
            onScopeChange = onScopeChange,
        )
        DashboardBreakdownCard(snapshot = snapshot)
    }
}

@Composable
private fun DashboardHeroCard(
    snapshot: StatsSnapshot,
    showToday: Boolean,
    onScopeChange: (Boolean) -> Unit,
) {
    AppListCard(
        padding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(OctopusTokens.PrimarySoft.copy(alpha = 0.82f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = AppMiuixIcons.Total,
                            contentDescription = stringResource(R.string.home_all_title),
                            tint = OctopusTokens.Accent,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.home_all_title),
                            style = MiuixTheme.textStyles.title3,
                            fontWeight = FontWeight.SemiBold,
                            color = OctopusTokens.TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = stringResource(if (showToday) R.string.home_scope_today else R.string.home_scope_total),
                            style = MiuixTheme.textStyles.body2,
                            color = OctopusTokens.TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ToolbarChip(
                        text = stringResource(R.string.home_scope_today),
                        selected = showToday,
                        onClick = { onScopeChange(true) },
                    )
                    ToolbarChip(
                        text = stringResource(R.string.home_scope_total),
                        selected = !showToday,
                        onClick = { onScopeChange(false) },
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = stringResource(R.string.home_total_cost),
                        style = MiuixTheme.textStyles.body2,
                        color = OctopusTokens.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = formatMoney(snapshot.costValue),
                        style = MiuixTheme.textStyles.title1,
                        fontWeight = FontWeight.SemiBold,
                        color = OctopusTokens.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                AppInfoChip(
                    text = formatDurationMs(snapshot.waitValue),
                    icon = AppMiuixIcons.Time,
                    tint = OctopusTones.Orange,
                )
            }

            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (maxWidth < 280.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        DashboardMetricTile(
                            icon = AppMiuixIcons.Request,
                            label = stringResource(R.string.home_total_requests),
                            value = formatCount(snapshot.requestCount),
                            accent = OctopusTones.Request,
                        )
                        DashboardMetricTile(
                            icon = AppMiuixIcons.Token,
                            label = stringResource(R.string.home_total_tokens),
                            value = formatCount(snapshot.tokenValue),
                            accent = OctopusTones.Token,
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        DashboardMetricTile(
                            icon = AppMiuixIcons.Request,
                            label = stringResource(R.string.home_total_requests),
                            value = formatCount(snapshot.requestCount),
                            accent = OctopusTones.Request,
                            modifier = Modifier.weight(1f),
                        )
                        DashboardMetricTile(
                            icon = AppMiuixIcons.Token,
                            label = stringResource(R.string.home_total_tokens),
                            value = formatCount(snapshot.tokenValue),
                            accent = OctopusTones.Token,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardBreakdownCard(
    snapshot: StatsSnapshot,
) {
    AppListCard(padding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = stringResource(R.string.home_total_title),
                style = MiuixTheme.textStyles.title3,
                fontWeight = FontWeight.SemiBold,
                color = OctopusTokens.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            DashboardBreakdownLine(
                icon = AppMiuixIcons.ArrowDown,
                title = stringResource(R.string.home_input_title),
                tokenLabel = stringResource(R.string.home_input_tokens),
                tokenValue = formatCount(snapshot.inputToken),
                costValue = formatMoney(snapshot.inputCost),
                accent = OctopusTokens.Accent,
            )
            DashboardBreakdownLine(
                icon = AppMiuixIcons.ArrowUp,
                title = stringResource(R.string.home_output_title),
                tokenLabel = stringResource(R.string.home_output_tokens),
                tokenValue = formatCount(snapshot.outputToken),
                costValue = formatMoney(snapshot.outputCost),
                accent = OctopusTones.Orange,
            )
        }
    }
}

@Composable
private fun DashboardMetricTile(
    icon: ImageVector,
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(OctopusTokens.Muted.copy(alpha = 0.68f))
            .border(1.dp, OctopusTokens.Border.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(horizontal = 11.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(accent.copy(alpha = 0.11f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = accent,
                modifier = Modifier.size(20.dp),
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = label,
                style = MiuixTheme.textStyles.body2,
                color = OctopusTokens.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = value,
                style = MiuixTheme.textStyles.main,
                fontWeight = FontWeight.SemiBold,
                color = OctopusTokens.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun DashboardBreakdownLine(
    icon: ImageVector,
    title: String,
    tokenLabel: String,
    tokenValue: String,
    costValue: String,
    accent: Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(OctopusTokens.Muted.copy(alpha = 0.66f))
            .border(1.dp, OctopusTokens.Border.copy(alpha = 0.48f), RoundedCornerShape(16.dp))
            .padding(horizontal = 11.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(accent.copy(alpha = 0.11f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = accent,
                modifier = Modifier.size(20.dp),
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = title,
                style = MiuixTheme.textStyles.main,
                fontWeight = FontWeight.SemiBold,
                color = OctopusTokens.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "$tokenLabel · $tokenValue",
                style = MiuixTheme.textStyles.body2,
                color = OctopusTokens.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = costValue,
            style = MiuixTheme.textStyles.main,
            fontWeight = FontWeight.SemiBold,
            color = accent,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
