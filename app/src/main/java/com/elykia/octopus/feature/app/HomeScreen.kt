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
            val requestCount = if (showToday && today != null) {
                today.requestSuccess + today.requestFailed
            } else {
                total.requestSuccess + total.requestFailed
            }
            val costValue = if (showToday && today != null) {
                today.inputCost + today.outputCost
            } else {
                total.inputCost + total.outputCost
            }
            val tokenValue = if (showToday && today != null) {
                today.inputToken + today.outputToken
            } else {
                total.inputToken + total.outputToken
            }
            val waitValue = if (showToday && today != null) today.waitTime else total.waitTime
            val successValue = if (showToday && today != null) {
                if (requestCount == 0L) 0.0 else today.requestSuccess.toDouble() / requestCount.toDouble()
            } else {
                val totalRequests = total.requestSuccess + total.requestFailed
                if (totalRequests == 0L) 0.0 else total.requestSuccess.toDouble() / totalRequests.toDouble()
            }

            AppPageScaffold(
                title = stringResource(R.string.home_title),
                actions = {
                    AppInfoChip(
                        text = if (showToday) stringResource(R.string.home_scope_today) else stringResource(R.string.home_scope_total),
                        icon = if (showToday) AppMiuixIcons.Today else AppMiuixIcons.Total,
                        tint = MiuixTheme.colorScheme.primary,
                    )
                    PageActionButton(
                        icon = AppMiuixIcons.SwitchMode,
                        contentDescription = stringResource(R.string.home_toggle_scope),
                        onClick = { showToday = !showToday },
                    )
                    PageActionButton(
                        icon = AppMiuixIcons.Refresh,
                        contentDescription = stringResource(R.string.common_refresh),
                        onClick = viewModel::refresh,
                    )
                },
                contentPadding = contentPadding,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    DashboardOverviewSection(
                        requestCount = requestCount,
                        costValue = costValue,
                        tokenValue = tokenValue,
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
    costValue: Double,
    tokenValue: Long,
    waitValue: Long,
    successValue: Double,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            StatOverviewCard(
                title = stringResource(R.string.home_stat_requests),
                value = formatCount(requestCount),
                summary = stringResource(R.string.home_stat_requests_summary),
                icon = AppMiuixIcons.Request,
                accentColor = Color(0xFF007AFF),
            )
            StatOverviewCard(
                title = stringResource(R.string.home_stat_cost),
                value = formatMoney(costValue),
                summary = stringResource(R.string.home_stat_cost_summary),
                icon = AppMiuixIcons.Cost,
                accentColor = Color(0xFF34C759),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            StatOverviewCard(
                title = stringResource(R.string.home_stat_tokens),
                value = formatCount(tokenValue),
                summary = stringResource(R.string.home_stat_tokens_summary),
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
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (daily.isEmpty()) {
                Text(
                    text = stringResource(R.string.home_rank_empty),
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    style = MiuixTheme.textStyles.body2,
                )
            } else {
                val visible = daily.takeLast(7).reversed()
                val maxRequest = visible.maxOfOrNull { it.requestSuccess + it.requestFailed }?.coerceAtLeast(1L) ?: 1L
                visible.forEach { item ->
                    val requests = item.requestSuccess + item.requestFailed
                    val cost = item.inputCost + item.outputCost
                    AppListCard {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = item.date, style = MiuixTheme.textStyles.main, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
                                Text(
                                    text = stringResource(R.string.home_activity_line, formatCount(requests), formatMoney(cost)),
                                    style = MiuixTheme.textStyles.body2,
                                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                )
                            }
                            com.elykia.octopus.core.designsystem.ProgressToneBar(
                                progress = requests.toFloat() / maxRequest.toFloat(),
                                color = Color(0xFF007AFF),
                            )
                        }
                    }
                }
            }
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
