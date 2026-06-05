package com.elykia.octopus.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.elykia.octopus.R
import com.elykia.octopus.core.data.model.ApiKeyItem
import com.elykia.octopus.core.data.model.Channel
import com.elykia.octopus.core.data.model.StatsApiKeyEntry
import com.elykia.octopus.core.designsystem.OctopusTones
import com.elykia.octopus.core.designsystem.ProgressToneBar
import com.elykia.octopus.core.designsystem.RankBadge
import com.elykia.octopus.core.designsystem.SectionCard
import com.elykia.octopus.core.designsystem.formatCount
import com.elykia.octopus.core.designsystem.formatMoney
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * Dashboard 排行榜区块
 */
@Composable
fun DashboardRankingSection(
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
