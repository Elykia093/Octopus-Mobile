package com.elykia.octopus.feature.apikey

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.elykia.octopus.R
import com.elykia.octopus.core.data.model.ApiKeyDashboard
import com.elykia.octopus.core.data.model.ApiKeyStats
import com.elykia.octopus.core.designsystem.AppInfoChip
import com.elykia.octopus.core.designsystem.AppListCard
import com.elykia.octopus.core.designsystem.AppMetricRow
import com.elykia.octopus.core.designsystem.AppPageScaffold
import com.elykia.octopus.core.designsystem.ErrorStateCard
import com.elykia.octopus.core.designsystem.LoadingStateCard
import com.elykia.octopus.core.designsystem.OctopusTokens
import com.elykia.octopus.core.designsystem.OperationErrorCard
import com.elykia.octopus.core.designsystem.PageActionButton
import com.elykia.octopus.core.designsystem.PageContainer
import com.elykia.octopus.core.designsystem.ProgressToneBar
import com.elykia.octopus.core.designsystem.SoftIconTile
import com.elykia.octopus.core.designsystem.ToolbarChip
import com.elykia.octopus.core.designsystem.formatCount
import com.elykia.octopus.core.designsystem.formatDurationMs
import com.elykia.octopus.core.designsystem.formatMoney
import com.elykia.octopus.core.designsystem.icons.AppMiuixIcons
import java.util.Locale
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun ApiKeyDashboardScreen(
    onLogout: () -> Unit,
    securityMessage: String? = null,
    onClearSecurityMessage: () -> Unit = {},
    viewModel: ApiKeyDashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val dashboard = uiState.dashboard
    val error = uiState.error

    PageContainer {
        AppPageScaffold(
            title = stringResource(R.string.dashboard_title),
            actions = {
                PageActionButton(
                    icon = AppMiuixIcons.Refresh,
                    contentDescription = stringResource(R.string.common_refresh),
                    enabled = !uiState.loading,
                    onClick = viewModel::refresh,
                )
                PageActionButton(
                    icon = AppMiuixIcons.Logout,
                    contentDescription = stringResource(R.string.action_logout),
                    onClick = onLogout,
                )
            },
            contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 28.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                securityMessage?.takeIf { it.isNotBlank() }?.let { message ->
                    OperationErrorCard(message = message, onDismiss = onClearSecurityMessage)
                }

                when {
                    uiState.loading -> LoadingStateCard(title = stringResource(R.string.dashboard_title))
                    dashboard == null && error != null -> ErrorStateCard(
                        message = error,
                        onRetry = viewModel::refresh,
                    )
                    dashboard != null -> {
                        error?.takeIf { it.isNotBlank() }?.let { message ->
                            OperationErrorCard(message = message)
                        }
                        ApiKeyIdentityCard(dashboard)
                        ApiKeyStatsCard(dashboard.stats)
                        ApiKeyBreakdownCard(dashboard.stats)
                        ApiKeySupportedModelsCard(dashboard.info.supportedModels)
                    }
                }
            }
        }
    }
}

@Composable
private fun ApiKeyIdentityCard(dashboard: ApiKeyDashboard) {
    val context = LocalContext.current
    val info = dashboard.info
    val stats = dashboard.stats
    val usedCost = stats.inputCost + stats.outputCost
    val maxCost = info.maxCost ?: 0.0
    val hasQuota = maxCost > 0.0

    AppListCard(padding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SoftIconTile(
                    icon = AppMiuixIcons.ApiKey,
                    contentDescription = stringResource(R.string.dashboard_key_title),
                    tint = OctopusTokens.Accent,
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text = info.name.ifBlank { stringResource(R.string.apikey_fallback_name, info.id) },
                        style = MiuixTheme.textStyles.title3,
                        fontWeight = FontWeight.SemiBold,
                        color = OctopusTokens.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = stringResource(R.string.dashboard_key_title),
                        style = MiuixTheme.textStyles.body2,
                        color = OctopusTokens.TextSecondary,
                    )
                }
                AppInfoChip(
                    text = stringResource(if (info.enabled) R.string.common_enabled else R.string.common_disabled),
                    tint = if (info.enabled) OctopusTokens.Accent else OctopusTokens.TextSecondary,
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(OctopusTokens.Muted.copy(alpha = 0.68f))
                    .border(1.dp, OctopusTokens.Border.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text = stringResource(R.string.dashboard_key_label),
                        style = MiuixTheme.textStyles.body2,
                        color = OctopusTokens.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = info.apiKey.maskApiKey(),
                        style = MiuixTheme.textStyles.main,
                        fontWeight = FontWeight.Medium,
                        color = OctopusTokens.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                TextButton(
                    text = stringResource(R.string.common_copy),
                    enabled = info.apiKey.isNotBlank(),
                    onClick = { copyApiKey(context, info.apiKey) },
                )
            }

            AppMetricRow(
                icon = AppMiuixIcons.Time,
                label = stringResource(R.string.apikey_expire_at_label),
                value = formatApiKeyExpireAt(info.expireAt),
                accentColor = OctopusTokens.Accent,
            )
            AppMetricRow(
                icon = AppMiuixIcons.Cost,
                label = stringResource(R.string.apikey_max_cost_label),
                value = if (hasQuota) formatMoney(maxCost) else stringResource(R.string.apikey_cost_unlimited),
                accentColor = OctopusTokens.Accent,
            )
            if (hasQuota) {
                ProgressToneBar(
                    progress = (usedCost / maxCost).toFloat(),
                    color = OctopusTokens.Accent,
                )
            }
        }
    }
}

@Composable
private fun ApiKeyStatsCard(stats: ApiKeyStats) {
    val requestCount = stats.requestSuccess + stats.requestFailed
    val tokenCount = stats.inputToken + stats.outputToken
    val totalCost = stats.inputCost + stats.outputCost

    AppListCard(padding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = stringResource(R.string.dashboard_stats_title),
                style = MiuixTheme.textStyles.title3,
                fontWeight = FontWeight.SemiBold,
                color = OctopusTokens.TextPrimary,
            )
            AppMetricRow(
                icon = AppMiuixIcons.Request,
                label = stringResource(R.string.dashboard_stat_requests),
                value = formatCount(requestCount),
                accentColor = OctopusTokens.Accent,
            )
            AppMetricRow(
                icon = AppMiuixIcons.Success,
                label = stringResource(R.string.home_stat_success_rate),
                value = formatSuccessRate(stats),
                accentColor = OctopusTokens.Accent,
            )
            AppMetricRow(
                icon = AppMiuixIcons.Token,
                label = stringResource(R.string.dashboard_stat_tokens),
                value = formatCount(tokenCount),
                accentColor = OctopusTokens.Accent,
            )
            AppMetricRow(
                icon = AppMiuixIcons.Cost,
                label = stringResource(R.string.dashboard_stat_cost),
                value = formatMoney(totalCost),
                accentColor = OctopusTokens.Accent,
            )
            AppMetricRow(
                icon = AppMiuixIcons.Time,
                label = stringResource(R.string.dashboard_stat_wait),
                value = formatDurationMs(stats.waitTime),
                accentColor = OctopusTokens.Accent,
            )
        }
    }
}

@Composable
private fun ApiKeyBreakdownCard(stats: ApiKeyStats) {
    AppListCard(padding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = stringResource(R.string.home_total_title),
                style = MiuixTheme.textStyles.title3,
                fontWeight = FontWeight.SemiBold,
                color = OctopusTokens.TextPrimary,
            )
            ApiKeyBreakdownLine(
                title = stringResource(R.string.home_input_title),
                tokens = stats.inputToken,
                cost = stats.inputCost,
                icon = AppMiuixIcons.ArrowDown,
            )
            ApiKeyBreakdownLine(
                title = stringResource(R.string.home_output_title),
                tokens = stats.outputToken,
                cost = stats.outputCost,
                icon = AppMiuixIcons.ArrowUp,
            )
        }
    }
}

@Composable
private fun ApiKeyBreakdownLine(
    title: String,
    tokens: Long,
    cost: Double,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(OctopusTokens.Muted.copy(alpha = 0.68f))
            .border(1.dp, OctopusTokens.Border.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(horizontal = 11.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        SoftIconTile(icon = icon, contentDescription = title, tint = OctopusTokens.Accent, modifier = Modifier.size(34.dp))
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
                text = formatCount(tokens),
                style = MiuixTheme.textStyles.body2,
                color = OctopusTokens.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = formatMoney(cost),
            style = MiuixTheme.textStyles.main,
            fontWeight = FontWeight.SemiBold,
            color = OctopusTokens.Accent,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ApiKeySupportedModelsCard(supportedModels: String?) {
    val models = supportedModels.orEmpty()
        .split(',')
        .map { it.trim() }
        .filter { it.isNotBlank() }

    AppListCard(padding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = stringResource(R.string.apikey_supported_models_label),
                style = MiuixTheme.textStyles.title3,
                fontWeight = FontWeight.SemiBold,
                color = OctopusTokens.TextPrimary,
            )
            if (models.isEmpty()) {
                AppInfoChip(text = stringResource(R.string.apikey_models_all), icon = AppMiuixIcons.Model)
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    models.take(MAX_VISIBLE_MODELS).forEach { model ->
                        ToolbarChip(text = model)
                    }
                    if (models.size > MAX_VISIBLE_MODELS) {
                        AppInfoChip(
                            text = stringResource(R.string.apikey_models_more, models.size - MAX_VISIBLE_MODELS),
                            icon = AppMiuixIcons.More,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun formatApiKeyExpireAt(expireAt: Long?): String =
    formatApiKeyExpireAtRaw(expireAt).ifBlank { stringResource(R.string.apikey_expire_never) }

private fun formatSuccessRate(stats: ApiKeyStats): String {
    val total = stats.requestSuccess + stats.requestFailed
    if (total <= 0L) return "0%"
    return String.format(Locale.getDefault(), "%.1f%%", stats.requestSuccess * 100.0 / total)
}

private const val MAX_VISIBLE_MODELS = 24
