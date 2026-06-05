package com.elykia.octopus.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.elykia.octopus.R
import com.elykia.octopus.core.data.model.StatsDaily
import com.elykia.octopus.core.designsystem.SectionCard
import com.elykia.octopus.core.designsystem.TrendEntry
import com.elykia.octopus.core.designsystem.TrendLineChart
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * Dashboard 趋势图表区块
 */
@Composable
fun DashboardTrendSection(
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
