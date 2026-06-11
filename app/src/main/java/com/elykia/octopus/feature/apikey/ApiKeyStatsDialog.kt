package com.elykia.octopus.feature.apikey

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.elykia.octopus.R
import com.elykia.octopus.core.data.model.ApiKeyItem
import com.elykia.octopus.core.data.model.StatsApiKeyEntry
import com.elykia.octopus.core.designsystem.AppMetricRow
import com.elykia.octopus.core.designsystem.InlineEmptyCard
import com.elykia.octopus.core.designsystem.formatCount
import com.elykia.octopus.core.designsystem.formatDurationMs
import com.elykia.octopus.core.designsystem.formatMoney
import com.elykia.octopus.core.designsystem.icons.AppMiuixIcons
import java.util.Locale
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.overlay.OverlayDialog

@Composable
internal fun ApiKeyStatsDialog(
    visible: Boolean,
    item: ApiKeyItem,
    stats: StatsApiKeyEntry?,
    onDismiss: () -> Unit,
) {
    if (!visible) return

    OverlayDialog(
        show = visible,
        title = item.name.ifBlank { stringResource(R.string.apikey_fallback_name, item.id) },
        summary = stringResource(R.string.apikey_stats_title),
        onDismissRequest = onDismiss,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (stats == null) {
                InlineEmptyCard(
                    title = stringResource(R.string.apikey_stats_title),
                    summary = stringResource(R.string.apikey_stats_empty),
                )
            } else {
                ApiKeyStatsMetrics(stats = stats)
            }
            TextButton(
                text = stringResource(R.string.action_close),
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ApiKeyStatsMetrics(stats: StatsApiKeyEntry) {
    val requestCount = stats.requestSuccess + stats.requestFailed
    val tokenCount = stats.inputToken + stats.outputToken
    val totalCost = stats.inputCost + stats.outputCost

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        AppMetricRow(
            icon = AppMiuixIcons.Request,
            label = stringResource(R.string.dashboard_stat_requests),
            value = formatCount(requestCount),
        )
        AppMetricRow(
            icon = AppMiuixIcons.Success,
            label = stringResource(R.string.home_stat_success_rate),
            value = formatApiKeyStatsSuccessRate(stats),
        )
        AppMetricRow(
            icon = AppMiuixIcons.Token,
            label = stringResource(R.string.dashboard_stat_tokens),
            value = formatCount(tokenCount),
        )
        AppMetricRow(
            icon = AppMiuixIcons.Cost,
            label = stringResource(R.string.dashboard_stat_cost),
            value = formatMoney(totalCost),
        )
        AppMetricRow(
            icon = AppMiuixIcons.Time,
            label = stringResource(R.string.dashboard_stat_wait),
            value = formatDurationMs(stats.waitTime),
        )
    }
}

internal fun formatApiKeyStatsSuccessRate(stats: StatsApiKeyEntry): String {
    val total = stats.requestSuccess + stats.requestFailed
    if (total <= 0L) return "0%"
    return String.format(Locale.getDefault(), "%.1f%%", stats.requestSuccess * 100.0 / total)
}
