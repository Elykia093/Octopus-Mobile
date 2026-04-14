package com.elykia.octopus.feature.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.elykia.octopus.R
import com.elykia.octopus.core.designsystem.EmptyPane
import com.elykia.octopus.core.designsystem.ErrorPane
import com.elykia.octopus.core.designsystem.InfoRow
import com.elykia.octopus.core.designsystem.LoadingPane
import com.elykia.octopus.core.designsystem.MetricCard
import com.elykia.octopus.core.designsystem.ScreenPane
import com.elykia.octopus.core.designsystem.SectionCard
import com.elykia.octopus.core.designsystem.SimpleList
import com.elykia.octopus.core.designsystem.ToolbarChip
import com.elykia.octopus.core.designsystem.formatCount
import com.elykia.octopus.core.designsystem.formatDurationMs
import com.elykia.octopus.core.designsystem.formatMoney
import com.elykia.octopus.feature.home.HomeViewModel

@Composable
fun HomeScreen(
    contentPadding: PaddingValues,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    when {
        uiState.loading -> LoadingPane(title = stringResource(R.string.home_title))
        uiState.error != null -> ErrorPane(message = uiState.error ?: stringResource(R.string.error_title), onRetry = viewModel::refresh)
        uiState.total == null -> EmptyPane(title = stringResource(R.string.empty_title), summary = stringResource(R.string.home_empty))
        else -> {
            val total = uiState.total ?: return
            ScreenPane(contentPadding = contentPadding) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        MetricCard(
                            title = stringResource(R.string.home_total_title),
                            primaryLabel = stringResource(R.string.home_total_requests),
                            primaryValue = formatCount(total.requestSuccess + total.requestFailed),
                            secondaryLabel = stringResource(R.string.home_total_wait_time),
                            secondaryValue = formatDurationMs(total.waitTime),
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        MetricCard(
                            title = stringResource(R.string.home_input_title),
                            primaryLabel = stringResource(R.string.home_total_tokens),
                            primaryValue = formatCount(total.inputToken),
                            secondaryLabel = stringResource(R.string.home_total_cost),
                            secondaryValue = formatMoney(total.inputCost),
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        MetricCard(
                            title = stringResource(R.string.home_output_title),
                            primaryLabel = stringResource(R.string.home_total_tokens),
                            primaryValue = formatCount(total.outputToken),
                            secondaryLabel = stringResource(R.string.home_total_cost),
                            secondaryValue = formatMoney(total.outputCost),
                        )
                    }
                    SectionCard(
                        title = stringResource(R.string.home_activity_title),
                        summary = stringResource(R.string.home_activity_summary),
                    ) {
                        SimpleList(
                            entries = uiState.daily.takeLast(7).reversed().map { stat ->
                                stat.date to stringResource(
                                    R.string.home_activity_line,
                                    formatCount(stat.requestSuccess + stat.requestFailed),
                                    formatMoney(stat.inputCost + stat.outputCost),
                                )
                            }
                        )
                    }
                    SectionCard(
                        title = stringResource(R.string.home_chart_title),
                        summary = stringResource(R.string.home_chart_summary),
                        actions = {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                ToolbarChip(text = stringResource(R.string.home_chart_period_today))
                                ToolbarChip(text = stringResource(R.string.home_chart_period_week))
                            }
                        },
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            uiState.hourly.takeLast(6).reversed().forEach { stat ->
                                InfoRow(
                                    label = "${stat.date} ${stat.hour}:00",
                                    value = stringResource(
                                        R.string.home_chart_line,
                                        formatCount(stat.inputToken + stat.outputToken),
                                        formatMoney(stat.inputCost + stat.outputCost),
                                    ),
                                )
                            }
                        }
                    }
                    SectionCard(
                        title = stringResource(R.string.home_rank_title),
                        summary = stringResource(R.string.home_rank_summary),
                        actions = {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                ToolbarChip(text = stringResource(R.string.home_rank_sort_cost))
                                ToolbarChip(text = stringResource(R.string.home_rank_sort_count), selected = true)
                                ToolbarChip(text = stringResource(R.string.home_rank_sort_tokens))
                            }
                        },
                    ) {
                        val ranked = uiState.channels
                            .sortedByDescending { (it.stats?.requestSuccess ?: 0) + (it.stats?.requestFailed ?: 0) }
                            .take(8)
                        if (ranked.isEmpty()) {
                            InfoRow(
                                label = stringResource(R.string.home_rank_empty),
                                value = stringResource(R.string.common_unknown),
                            )
                        } else {
                            SimpleList(
                                entries = ranked.mapIndexed { index, channel ->
                                    "${index + 1}. ${channel.name}" to stringResource(
                                        R.string.home_rank_item_summary,
                                        formatCount((channel.stats?.requestSuccess ?: 0) + (channel.stats?.requestFailed ?: 0)),
                                        formatCount((channel.stats?.inputToken ?: 0) + (channel.stats?.outputToken ?: 0)),
                                        formatMoney((channel.stats?.inputCost ?: 0.0) + (channel.stats?.outputCost ?: 0.0)),
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
