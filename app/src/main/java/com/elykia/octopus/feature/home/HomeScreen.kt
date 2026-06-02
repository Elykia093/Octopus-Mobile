package com.elykia.octopus.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
            when {
                uiState.loading -> LoadingStateCard(title = stringResource(R.string.home_title))
                uiState.error != null -> ErrorStateCard(
                    message = uiState.error ?: stringResource(R.string.error_title),
                    onRetry = viewModel::refresh,
                )
                total == null -> EmptyStateCard(
                    title = stringResource(R.string.empty_title),
                    summary = stringResource(R.string.home_empty),
                )
                else -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ToolbarChip(
                            text = stringResource(R.string.home_scope_today),
                            selected = showToday,
                            onClick = { showToday = true },
                        )
                        ToolbarChip(
                            text = stringResource(R.string.home_scope_total),
                            selected = !showToday,
                            onClick = { showToday = false },
                        )
                    }
                    DashboardOverviewSection(
                        snapshot = snapshot,
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
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        DashboardOverviewCard(
            title = stringResource(R.string.home_total_title),
            icon = AppMiuixIcons.Request,
            accent = OctopusTokens.Accent,
            metrics = listOf(
                OverviewMetric(
                    icon = AppMiuixIcons.Request,
                    label = stringResource(R.string.home_total_requests),
                    value = formatCount(snapshot.requestCount),
                ),
                OverviewMetric(
                    icon = AppMiuixIcons.Time,
                    label = stringResource(R.string.home_total_wait_time),
                    value = formatDurationMs(snapshot.waitValue),
                ),
            ),
        )
        DashboardOverviewCard(
            title = stringResource(R.string.home_all_title),
            icon = AppMiuixIcons.Total,
            accent = OctopusTokens.Accent,
            metrics = listOf(
                OverviewMetric(
                    icon = AppMiuixIcons.Token,
                    label = stringResource(R.string.home_total_tokens),
                    value = formatCount(snapshot.tokenValue),
                ),
                OverviewMetric(
                    icon = AppMiuixIcons.Cost,
                    label = stringResource(R.string.home_total_cost),
                    value = formatMoney(snapshot.costValue),
                ),
            ),
        )
        DashboardOverviewCard(
            title = stringResource(R.string.home_input_title),
            icon = AppMiuixIcons.ArrowDown,
            accent = OctopusTokens.Accent,
            metrics = listOf(
                OverviewMetric(
                    icon = AppMiuixIcons.ArrowDown,
                    label = stringResource(R.string.home_input_tokens),
                    value = formatCount(snapshot.inputToken),
                ),
                OverviewMetric(
                    icon = AppMiuixIcons.Cost,
                    label = stringResource(R.string.home_input_cost),
                    value = formatMoney(snapshot.inputCost),
                ),
            ),
        )
        DashboardOverviewCard(
            title = stringResource(R.string.home_output_title),
            icon = AppMiuixIcons.ArrowUp,
            accent = OctopusTokens.Accent,
            metrics = listOf(
                OverviewMetric(
                    icon = AppMiuixIcons.ArrowUp,
                    label = stringResource(R.string.home_output_tokens),
                    value = formatCount(snapshot.outputToken),
                ),
                OverviewMetric(
                    icon = AppMiuixIcons.Cost,
                    label = stringResource(R.string.home_output_cost),
                    value = formatMoney(snapshot.outputCost),
                ),
            ),
        )
    }
}

@Composable
private fun DashboardOverviewCard(
    title: String,
    icon: ImageVector,
    accent: Color,
    metrics: List<OverviewMetric>,
) {
    AppListCard(
        padding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
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
                        imageVector = icon,
                        contentDescription = title,
                        tint = accent,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Text(
                    text = title,
                    style = MiuixTheme.textStyles.main,
                    fontWeight = FontWeight.SemiBold,
                    color = OctopusTokens.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                metrics.forEach { metric ->
                    OverviewMetricLine(metric = metric, accent = accent)
                }
            }
        }
    }
}

@Composable
private fun OverviewMetricLine(
    metric: OverviewMetric,
    accent: Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(OctopusTokens.PrimarySoft),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = metric.icon,
                contentDescription = metric.label,
                tint = accent,
                modifier = Modifier.size(24.dp),
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = metric.label,
                style = MiuixTheme.textStyles.body1,
                color = OctopusTokens.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = metric.value,
                style = MiuixTheme.textStyles.title1,
                fontWeight = FontWeight.Medium,
                color = OctopusTokens.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
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

private data class OverviewMetric(
    val icon: ImageVector,
    val label: String,
    val value: String,
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
