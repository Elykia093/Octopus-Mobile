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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.elykia.octopus.R
import com.elykia.octopus.core.data.model.ApiKeyItem
import com.elykia.octopus.core.data.model.Channel
import com.elykia.octopus.core.data.model.StatsApiKeyEntry
import com.elykia.octopus.core.data.model.StatsDaily
import com.elykia.octopus.core.data.model.StatsTotal
import com.elykia.octopus.core.designsystem.AppPageScaffold
import com.elykia.octopus.core.designsystem.EmptyStateCard
import com.elykia.octopus.core.designsystem.ErrorStateCard
import com.elykia.octopus.core.designsystem.AppListCard
import com.elykia.octopus.core.designsystem.LoadingStateCard
import com.elykia.octopus.core.designsystem.OctopusTones
import com.elykia.octopus.core.designsystem.OctopusTokens
import com.elykia.octopus.core.designsystem.OperationErrorCard
import com.elykia.octopus.core.designsystem.PageActionButton
import com.elykia.octopus.core.designsystem.ProgressToneBar
import com.elykia.octopus.core.designsystem.RankBadge
import com.elykia.octopus.core.designsystem.SectionCard
import com.elykia.octopus.core.designsystem.ToolbarChip
import com.elykia.octopus.core.designsystem.TrendLineChart
import com.elykia.octopus.core.designsystem.TrendEntry
import com.elykia.octopus.core.designsystem.formatCount
import com.elykia.octopus.core.designsystem.formatDurationMs
import com.elykia.octopus.core.designsystem.formatMoney
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import com.elykia.octopus.core.designsystem.icons.AppMiuixIcons

@Composable
fun HomeScreen(
    contentPadding: PaddingValues,
    onLogout: () -> Unit = {},
    securityMessage: String? = null,
    onClearSecurityMessage: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showToday by remember { mutableStateOf(true) }

    val total = uiState.total
    val snapshot = when (val s = if (showToday && uiState.today != null) uiState.today else total) {
        is StatsTotal -> StatsSnapshot(
            requestCount = s.requestSuccess + s.requestFailed,
            costValue = s.inputCost + s.outputCost,
            tokenValue = s.inputToken + s.outputToken,
            waitValue = s.waitTime,
            inputCost = s.inputCost,
            inputToken = s.inputToken,
            outputCost = s.outputCost,
            outputToken = s.outputToken,
        )
        is StatsDaily -> StatsSnapshot(
            requestCount = s.requestSuccess + s.requestFailed,
            costValue = s.inputCost + s.outputCost,
            tokenValue = s.inputToken + s.outputToken,
            waitValue = s.waitTime,
            inputCost = s.inputCost,
            inputToken = s.inputToken,
            outputCost = s.outputCost,
            outputToken = s.outputToken,
        )
        else -> StatsSnapshot()
    }

    AppPageScaffold(
        title = stringResource(R.string.home_title),
        actions = {
            PageActionButton(
                icon = AppMiuixIcons.Logout,
                contentDescription = stringResource(R.string.action_logout),
                onClick = onLogout,
            )
        },
        contentPadding = contentPadding,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            securityMessage?.takeIf { it.isNotBlank() }?.let { message ->
                OperationErrorCard(message = message, onDismiss = onClearSecurityMessage)
            }
            when {
                uiState.loading -> LoadingStateCard(title = stringResource(R.string.home_title))
                uiState.shouldShowPageError() -> ErrorStateCard(
                    message = uiState.error ?: stringResource(R.string.error_title),
                    onRetry = viewModel::refresh,
                )
                total == null -> EmptyStateCard(
                    title = stringResource(R.string.empty_title),
                    summary = stringResource(R.string.home_empty),
                )
                else -> {
                    uiState.error?.takeIf { it.isNotBlank() }?.let { error ->
                        OperationErrorCard(message = error)
                    }
                    uiState.partialErrors().forEach { error ->
                        OperationErrorCard(message = error)
                    }
                    DashboardOverviewSection(
                        snapshot = snapshot,
                        showToday = showToday,
                        onScopeChange = { showToday = it },
                    )
                    DashboardTrendSection(daily = uiState.daily)
                    DashboardRankingSection(
                        channels = uiState.channels,
                        apiKeyStats = uiState.apiKeyStats,
                        apiKeys = uiState.apiKeys,
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardOverviewSection(
    snapshot: StatsSnapshot,
    showToday: Boolean,
    onScopeChange: (Boolean) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
        padding = PaddingValues(horizontal = 20.dp, vertical = 22.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(OctopusTokens.PrimarySoft),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = AppMiuixIcons.Total,
                            contentDescription = stringResource(R.string.home_all_title),
                            tint = OctopusTokens.Accent,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.home_all_title),
                            style = MiuixTheme.textStyles.title3,
                            fontWeight = FontWeight.Bold,
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

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
                    fontWeight = FontWeight.Bold,
                    color = OctopusTokens.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (maxWidth < 280.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
            DashboardMetricTile(
                icon = AppMiuixIcons.Time,
                label = stringResource(R.string.home_total_wait_time),
                value = formatDurationMs(snapshot.waitValue),
                accent = OctopusTones.Orange,
            )
        }
    }
}

@Composable
private fun DashboardBreakdownCard(
    snapshot: StatsSnapshot,
) {
    AppListCard(padding = PaddingValues(horizontal = 18.dp, vertical = 18.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
            .clip(RoundedCornerShape(20.dp))
            .background(OctopusTokens.Muted.copy(alpha = 0.78f))
            .border(1.dp, OctopusTokens.Border.copy(alpha = 0.58f), RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(accent.copy(alpha = 0.12f)),
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
            .clip(RoundedCornerShape(20.dp))
            .background(OctopusTokens.Muted.copy(alpha = 0.74f))
            .border(1.dp, OctopusTokens.Border.copy(alpha = 0.56f), RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(accent.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = accent,
                modifier = Modifier.size(22.dp),
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

@Composable
private fun DashboardTrendSection(
    daily: List<StatsDaily>,
) {
    SectionCard(
        title = stringResource(R.string.home_chart_title),
        summary = stringResource(R.string.home_chart_summary),
    ) {
        if (daily.isEmpty()) {
            Text(
                text = stringResource(R.string.home_rank_empty),
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                style = MiuixTheme.textStyles.body2,
            )
        } else {
            val visible = daily.takeLast(7)
            TrendLineChart(
                entries = visible.map { item ->
                    val requests = item.requestSuccess + item.requestFailed
                    val cost = item.inputCost + item.outputCost
                    TrendEntry(
                        label = item.date.takeLast(5), // MM/dd
                        requests = requests,
                        cost = cost,
                    )
                },
            )
        }
    }
}

@Composable
private fun DashboardRankingSection(
    channels: List<Channel>,
    apiKeyStats: List<StatsApiKeyEntry>,
    apiKeys: List<ApiKeyItem>,
) {
    val tokenRanking = channels
        .filter { it.stats != null }
        .sortedByDescending { (it.stats?.inputToken ?: 0) + (it.stats?.outputToken ?: 0) }
        .take(5)
    val requestRanking = channels
        .filter { it.stats != null }
        .sortedByDescending { (it.stats?.requestSuccess ?: 0) + (it.stats?.requestFailed ?: 0) }
        .take(5)
    val apiKeyRanking = apiKeyStats
        .sortedByDescending { it.requestSuccess + it.requestFailed }
        .take(5)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        RankingSectionCard(
            title = stringResource(R.string.home_rank_token_title),
            items = tokenRanking.mapIndexed { index, channel ->
                val totalTokens = (channel.stats?.inputToken ?: 0) + (channel.stats?.outputToken ?: 0)
                val maxTokens = tokenRanking.firstOrNull()?.let { (it.stats?.inputToken ?: 0) + (it.stats?.outputToken ?: 0) } ?: 1L
                Triple(index + 1, channel.name, RankContent(
                    subtitle = stringResource(R.string.home_rank_channel_tokens_subtitle, formatCount(totalTokens)),
                    value = formatCount(totalTokens),
                    progress = totalTokens.toFloat() / maxTokens.toFloat(),
                ))
            },
            accent = OctopusTones.Token,
        )
        RankingSectionCard(
            title = stringResource(R.string.home_rank_request_title),
            items = requestRanking.mapIndexed { index, channel ->
                val requests = (channel.stats?.requestSuccess ?: 0) + (channel.stats?.requestFailed ?: 0)
                val maxRequest = requestRanking.firstOrNull()?.let { (it.stats?.requestSuccess ?: 0) + (it.stats?.requestFailed ?: 0) } ?: 1L
                Triple(index + 1, channel.name, RankContent(
                    subtitle = stringResource(R.string.home_rank_channel_requests_subtitle, formatCount(requests)),
                    value = formatCount(requests),
                    progress = requests.toFloat() / maxRequest.toFloat(),
                ))
            },
            accent = OctopusTones.Request,
        )
        RankingSectionCard(
            title = stringResource(R.string.home_rank_apikey_title),
            items = apiKeyRanking.mapIndexed { index, entry ->
                val requests = entry.requestSuccess + entry.requestFailed
                val maxRequest = apiKeyRanking.firstOrNull()?.let { it.requestSuccess + it.requestFailed } ?: 1L
                val name = apiKeys.firstOrNull { it.id == entry.apiKeyId }?.name?.ifBlank { null }
                    ?: stringResource(R.string.apikey_fallback_name, entry.apiKeyId)
                Triple(index + 1, name, RankContent(
                    subtitle = stringResource(
                        R.string.home_rank_apikey_subtitle,
                        formatMoney(entry.inputCost + entry.outputCost),
                    ),
                    value = formatCount(requests),
                    progress = requests.toFloat() / maxRequest.toFloat(),
                ))
            },
            accent = OctopusTones.SuccessRate,
        )
    }
}

@Composable
private fun RankingSectionCard(
    title: String,
    items: List<Triple<Int, String, RankContent>>,
    accent: Color,
) {
    SectionCard(title = title) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (items.isEmpty()) {
                Text(
                    text = stringResource(R.string.home_rank_empty),
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    style = MiuixTheme.textStyles.body2,
                )
            } else {
                items.forEach { (rank, label, content) ->
                    DashboardRankRow(
                        rank = rank,
                        title = label,
                        subtitle = content.subtitle,
                        value = content.value,
                        accent = accent,
                        progress = content.progress,
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardRankRow(
    rank: Int,
    title: String,
    subtitle: String,
    value: String,
    accent: Color,
    progress: Float,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            RankBadge(rank = rank)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = title,
                    style = MiuixTheme.textStyles.main,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitle,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    style = MiuixTheme.textStyles.body2,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = value,
                color = accent,
                style = MiuixTheme.textStyles.main,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        ProgressToneBar(progress = progress, color = accent)
    }
}

private data class RankContent(
    val subtitle: String,
    val value: String,
    val progress: Float,
)

private data class StatsSnapshot(
    val requestCount: Long = 0L,
    val costValue: Double = 0.0,
    val tokenValue: Long = 0L,
    val waitValue: Long = 0L,
    val inputCost: Double = 0.0,
    val inputToken: Long = 0L,
    val outputCost: Double = 0.0,
    val outputToken: Long = 0L,
)
