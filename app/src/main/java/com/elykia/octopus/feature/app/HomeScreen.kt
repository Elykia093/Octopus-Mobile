package com.elykia.octopus.feature.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.elykia.octopus.R
import com.elykia.octopus.core.data.model.ApiKeyItem
import com.elykia.octopus.core.data.model.Channel
import com.elykia.octopus.core.data.model.StatsApiKeyEntry
import com.elykia.octopus.core.data.model.StatsDaily
import com.elykia.octopus.core.data.model.StatsTotal
import com.elykia.octopus.core.designsystem.AppInfoChip
import com.elykia.octopus.core.designsystem.AppListCard
import com.elykia.octopus.core.designsystem.AppPageScaffold
import com.elykia.octopus.core.designsystem.EmptyPane
import com.elykia.octopus.core.designsystem.ErrorPane
import com.elykia.octopus.core.designsystem.LoadingPane
import com.elykia.octopus.core.designsystem.PageActionButton
import com.elykia.octopus.core.designsystem.RankRow
import com.elykia.octopus.core.designsystem.SectionCard
import com.elykia.octopus.core.designsystem.StatOverviewCard
import com.elykia.octopus.core.designsystem.ToolbarChip
import com.elykia.octopus.core.designsystem.TrendLineChart
import com.elykia.octopus.core.designsystem.TrendEntry
import com.elykia.octopus.core.designsystem.formatCount
import com.elykia.octopus.core.designsystem.formatDurationMs
import com.elykia.octopus.core.designsystem.formatMoney
import com.elykia.octopus.feature.home.HomeViewModel
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import com.elykia.octopus.core.designsystem.icons.AppMiuixIcons

@Composable
fun HomeScreen(
    contentPadding: PaddingValues,
    onLogout: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var showToday by remember { mutableStateOf(true) }

    when {
        uiState.loading -> LoadingPane(title = stringResource(R.string.home_title))
        uiState.error != null -> ErrorPane(message = uiState.error ?: stringResource(R.string.error_title), onRetry = viewModel::refresh)
        uiState.total == null -> EmptyPane(title = stringResource(R.string.empty_title), summary = stringResource(R.string.home_empty))
        else -> {
            val total = uiState.total ?: return
            val today = uiState.today

            val (requestCount, costValue, tokenValue, waitValue, successCount, inputCostVal, inputTokenCount) = when (val s = if (showToday && today != null) today else total) {
                is StatsTotal -> StatsSnapshot(
                    requestCount = s.requestSuccess + s.requestFailed,
                    costValue = s.inputCost + s.outputCost,
                    tokenValue = s.inputToken + s.outputToken,
                    waitValue = s.waitTime,
                    successCount = s.requestSuccess,
                    inputCost = s.inputCost,
                    inputToken = s.inputToken,
                )
                is StatsDaily -> StatsSnapshot(
                    requestCount = s.requestSuccess + s.requestFailed,
                    costValue = s.inputCost + s.outputCost,
                    tokenValue = s.inputToken + s.outputToken,
                    waitValue = s.waitTime,
                    successCount = s.requestSuccess,
                    inputCost = s.inputCost,
                    inputToken = s.inputToken,
                )
                else -> StatsSnapshot()
            }
            val successValue = if (requestCount == 0L) 0.0 else successCount.toDouble() / requestCount.toDouble()

            AppPageScaffold(
                title = stringResource(R.string.home_title),
                actions = {
                    // SegmentedControl: Today / Total
                    Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
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
                    PageActionButton(
                        icon = AppMiuixIcons.Refresh,
                        contentDescription = stringResource(R.string.common_refresh),
                        onClick = viewModel::refresh,
                    )
                    PageActionButton(
                        icon = AppMiuixIcons.Logout,
                        contentDescription = stringResource(R.string.action_logout),
                        onClick = onLogout,
                    )
                },
                contentPadding = contentPadding,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    DashboardOverviewSection(
                        requestCount = requestCount,
                        successCount = successCount,
                        costValue = costValue,
                        inputCost = inputCostVal,
                        tokenValue = tokenValue,
                        inputToken = inputTokenCount,
                        waitValue = waitValue,
                        successValue = successValue,
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
    requestCount: Long,
    successCount: Long,
    costValue: Double,
    inputCost: Double,
    tokenValue: Long,
    inputToken: Long,
    waitValue: Long,
    successValue: Double,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            StatOverviewCard(
                title = stringResource(R.string.home_stat_requests),
                value = formatCount(requestCount),
                summary = stringResource(R.string.home_stat_success_count, formatCount(successCount)),
                icon = AppMiuixIcons.Request,
                accentColor = Color(0xFF007AFF),
            )
            StatOverviewCard(
                title = stringResource(R.string.home_stat_cost),
                value = formatMoney(costValue),
                summary = stringResource(R.string.home_stat_input_cost, formatMoney(inputCost)),
                icon = AppMiuixIcons.Cost,
                accentColor = Color(0xFF34C759),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            StatOverviewCard(
                title = stringResource(R.string.home_stat_tokens),
                value = formatCount(tokenValue),
                summary = stringResource(R.string.home_stat_input_tokens, formatCount(inputToken)),
                icon = AppMiuixIcons.Token,
                accentColor = Color(0xFFFF9500),
            )
            StatOverviewCard(
                title = stringResource(R.string.home_stat_success_rate),
                value = stringResource(R.string.home_percent_value, successValue * 100.0),
                summary = stringResource(R.string.home_stat_wait_summary, formatDurationMs(waitValue)),
                icon = AppMiuixIcons.Success,
                accentColor = Color(0xFFAF52DE),
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
            accent = Color(0xFFFF9500),
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
            accent = Color(0xFF007AFF),
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
            accent = Color(0xFFAF52DE),
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
                    RankRow(
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
    val successCount: Long = 0L,
    val inputCost: Double = 0.0,
    val inputToken: Long = 0L,
)
