package com.elykia.octopus.feature.sitechannel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.elykia.octopus.R
import com.elykia.octopus.core.data.model.SiteChannelAccount
import com.elykia.octopus.core.data.model.SiteChannelCard
import com.elykia.octopus.core.data.model.SiteChannelGroup
import com.elykia.octopus.core.data.model.SiteModelRouteType
import com.elykia.octopus.core.designsystem.AppInfoChip
import com.elykia.octopus.core.designsystem.AppLazyPageScaffold
import com.elykia.octopus.core.designsystem.AppListCard
import com.elykia.octopus.core.designsystem.ErrorStateCard
import com.elykia.octopus.core.designsystem.InlineEmptyCard
import com.elykia.octopus.core.designsystem.LoadingStateCard
import com.elykia.octopus.core.designsystem.OctopusTokens
import com.elykia.octopus.core.designsystem.OperationErrorCard
import com.elykia.octopus.core.designsystem.PageActionButton
import com.elykia.octopus.core.designsystem.SearchField
import com.elykia.octopus.core.designsystem.SoftIconTile
import com.elykia.octopus.core.designsystem.ToolbarChip
import com.elykia.octopus.core.designsystem.icons.AppMiuixIcons
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun SiteChannelScreen(
    contentPadding: PaddingValues,
    viewModel: SiteChannelViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var searchVisible by remember { mutableStateOf(false) }
    var searchTerm by remember { mutableStateOf("") }

    val query = searchTerm.trim()
    val cards = uiState.cards.filter { card -> query.isBlank() || card.matches(query) }

    AppLazyPageScaffold(
        title = stringResource(R.string.site_channel_title),
        actions = {
            PageActionButton(
                icon = AppMiuixIcons.Refresh,
                contentDescription = stringResource(R.string.common_refresh),
                enabled = !uiState.loading,
                onClick = viewModel::refresh,
            )
            PageActionButton(
                icon = if (searchVisible) AppMiuixIcons.Close else AppMiuixIcons.Search,
                contentDescription = stringResource(R.string.action_open_search),
                enabled = !uiState.loading && !uiState.shouldShowPageError(),
                onClick = {
                    searchVisible = !searchVisible
                    if (!searchVisible) searchTerm = ""
                },
            )
        },
        contentPadding = contentPadding,
    ) {
        when {
            uiState.loading -> item { LoadingStateCard(title = stringResource(R.string.site_channel_title)) }
            uiState.shouldShowPageError() -> item {
                ErrorStateCard(
                    message = uiState.error ?: stringResource(R.string.error_title),
                    onRetry = viewModel::refresh,
                )
            }
            else -> {
                if (uiState.cards.isNotEmpty() && searchVisible) {
                    item {
                        SearchField(
                            value = searchTerm,
                            onValueChange = { searchTerm = it },
                            hint = stringResource(R.string.site_channel_search_hint),
                        )
                    }
                }
                uiState.error?.takeIf { it.isNotBlank() }?.let { error ->
                    item { OperationErrorCard(message = error) }
                }
                when {
                    uiState.cards.isEmpty() -> item {
                        InlineEmptyCard(
                            title = stringResource(R.string.site_channel_title),
                            summary = stringResource(R.string.site_channel_empty),
                        )
                    }
                    cards.isEmpty() -> item {
                        InlineEmptyCard(
                            title = stringResource(R.string.empty_title),
                            summary = stringResource(R.string.site_channel_search_empty),
                        )
                    }
                    else -> items(cards, key = { it.siteId }) { card ->
                        SiteChannelCardView(card = card)
                    }
                }
            }
        }
    }
}

@Composable
private fun SiteChannelCardView(card: SiteChannelCard) {
    val groups = card.accounts.flatMap { it.groups }
    val totalProjectedChannels = groups.flatMap { it.projectedChannelIds }.filter { it > 0 }.distinct().size
    val totalModels = groups.sumOf { it.models.size }
    val totalSourceKeys = groups.sumOf { it.sourceKeys.size }
    val maskedPendingKeys = groups.sumOf { it.maskedPendingKeyCount }

    AppListCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SoftIconTile(
                    icon = AppMiuixIcons.Sync,
                    contentDescription = card.siteName,
                    tint = if (card.enabled) OctopusTokens.Accent else OctopusTokens.TextSecondary,
                )
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = card.siteName.ifBlank { stringResource(R.string.site_channel_site_fallback, card.siteId) },
                        style = MiuixTheme.textStyles.title3,
                        fontWeight = FontWeight.SemiBold,
                        color = OctopusTokens.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = card.baseUrl.ifBlank { card.platform },
                        style = MiuixTheme.textStyles.body2,
                        color = OctopusTokens.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ToolbarChip(text = stringResource(if (card.enabled) R.string.common_enabled else R.string.common_disabled), selected = card.enabled)
                ToolbarChip(text = stringResource(R.string.site_channel_accounts_count, card.accounts.size))
                ToolbarChip(text = stringResource(R.string.site_channel_groups_count, groups.size))
                ToolbarChip(text = stringResource(R.string.site_channel_models_count, totalModels))
                ToolbarChip(text = stringResource(R.string.site_channel_projected_channels_count, totalProjectedChannels))
                ToolbarChip(text = stringResource(R.string.site_channel_source_keys_count, totalSourceKeys))
                if (maskedPendingKeys > 0) {
                    ToolbarChip(text = stringResource(R.string.site_channel_masked_pending_count, maskedPendingKeys), selected = true)
                }
            }
            card.accounts.take(MAX_ACCOUNTS_PER_CARD).forEach { account ->
                SiteChannelAccountBlock(account = account)
            }
            if (card.accounts.size > MAX_ACCOUNTS_PER_CARD) {
                AppInfoChip(
                    text = stringResource(R.string.site_channel_more_accounts, card.accounts.size - MAX_ACCOUNTS_PER_CARD),
                    icon = AppMiuixIcons.More,
                    tint = OctopusTokens.TextSecondary,
                )
            }
        }
    }
}

@Composable
private fun SiteChannelAccountBlock(account: SiteChannelAccount) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = account.accountName.ifBlank { stringResource(R.string.site_channel_account_fallback, account.accountId) },
                    style = MiuixTheme.textStyles.main,
                    fontWeight = FontWeight.SemiBold,
                    color = OctopusTokens.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stringResource(R.string.site_channel_account_summary, account.groups.size, account.modelsOrDeclaredCount()),
                    style = MiuixTheme.textStyles.body2,
                    color = OctopusTokens.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ToolbarChip(text = stringResource(if (account.enabled) R.string.common_enabled else R.string.common_disabled), selected = account.enabled)
            if (account.autoSync) ToolbarChip(text = stringResource(R.string.site_channel_auto_sync), selected = true)
            account.routeSummaries.filter { it.count > 0 }.take(ROUTE_SUMMARY_LIMIT).forEach { summary ->
                ToolbarChip(text = "${routeTypeLabel(summary.routeType)} ${summary.count}")
            }
        }
        account.groups.take(MAX_GROUPS_PER_ACCOUNT).forEach { group ->
            SiteChannelGroupBlock(group = group)
        }
        if (account.groups.size > MAX_GROUPS_PER_ACCOUNT) {
            Text(
                text = stringResource(R.string.site_channel_more_groups, account.groups.size - MAX_GROUPS_PER_ACCOUNT),
                style = MiuixTheme.textStyles.body2,
                color = OctopusTokens.TextSecondary,
            )
        }
    }
}

@Composable
private fun SiteChannelGroupBlock(group: SiteChannelGroup) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = group.groupName.ifBlank { group.groupKey.ifBlank { stringResource(R.string.common_unknown) } },
            style = MiuixTheme.textStyles.body1,
            fontWeight = FontWeight.Medium,
            color = OctopusTokens.TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ToolbarChip(text = modelSyncStatusLabel(group.modelSyncStatus), selected = group.modelSyncStatus == "synced")
            ToolbarChip(text = stringResource(R.string.site_channel_group_keys_count, group.enabledKeyCount, group.keyCount))
            ToolbarChip(text = stringResource(R.string.site_channel_group_models_count, group.models.size))
            ToolbarChip(text = stringResource(R.string.site_channel_group_channels_count, group.projectedChannelIds.count { it > 0 }))
            if (group.projectionDisabled) ToolbarChip(text = stringResource(R.string.site_channel_projection_disabled), selected = true)
            if (group.projectionSuspended) ToolbarChip(text = stringResource(R.string.site_channel_projection_suspended), selected = true)
            if (group.maskedPendingKeyCount > 0) ToolbarChip(text = stringResource(R.string.site_channel_masked_pending_count, group.maskedPendingKeyCount), selected = true)
        }
        projectedChannelLine(group)?.let { line ->
            Text(
                text = line,
                style = MiuixTheme.textStyles.body2,
                color = OctopusTokens.TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        modelPreviewLine(group)?.let { line ->
            Text(
                text = line,
                style = MiuixTheme.textStyles.body2,
                color = OctopusTokens.TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private fun SiteChannelCard.matches(query: String): Boolean {
    val normalized = query.lowercase()
    if (siteName.contains(normalized, ignoreCase = true) || baseUrl.contains(normalized, ignoreCase = true)) return true
    return accounts.any { account ->
        account.accountName.contains(normalized, ignoreCase = true) ||
            account.groups.any { group ->
                group.groupName.contains(normalized, ignoreCase = true) ||
                    group.groupKey.contains(normalized, ignoreCase = true) ||
                    group.projectedChannels.any { it.channelName.contains(normalized, ignoreCase = true) } ||
                    group.models.any { it.modelName.contains(normalized, ignoreCase = true) }
            }
    }
}

private fun SiteChannelAccount.modelsOrDeclaredCount(): Int =
    groups.sumOf { it.models.size }.takeIf { it > 0 } ?: modelCount

@Composable
private fun modelSyncStatusLabel(status: String): String = when (status) {
    "synced" -> stringResource(R.string.site_channel_sync_synced)
    "empty" -> stringResource(R.string.site_channel_sync_empty)
    "stale" -> stringResource(R.string.site_channel_sync_stale)
    "failed" -> stringResource(R.string.site_channel_sync_failed)
    "unresolved" -> stringResource(R.string.site_channel_sync_unresolved)
    "missing_key" -> stringResource(R.string.site_channel_sync_missing_key)
    "removed" -> stringResource(R.string.site_channel_sync_removed)
    else -> stringResource(R.string.site_channel_sync_idle)
}

@Composable
private fun routeTypeLabel(routeType: String): String = when (routeType) {
    SiteModelRouteType.OpenAiChat -> stringResource(R.string.site_channel_route_openai_chat)
    SiteModelRouteType.OpenAiResponse -> stringResource(R.string.site_channel_route_openai_response)
    SiteModelRouteType.Anthropic -> stringResource(R.string.site_channel_route_anthropic)
    SiteModelRouteType.Gemini -> stringResource(R.string.site_channel_route_gemini)
    SiteModelRouteType.Volcengine -> stringResource(R.string.site_channel_route_volcengine)
    SiteModelRouteType.OpenAiEmbedding -> stringResource(R.string.site_channel_route_embedding)
    else -> stringResource(R.string.common_unknown)
}

@Composable
private fun projectedChannelLine(group: SiteChannelGroup): String? {
    val names = group.projectedChannels
        .map { it.channelName.ifBlank { "#${it.channelId}" } }
        .ifEmpty { group.projectedChannelIds.filter { it > 0 }.map { "#$it" } }
    if (names.isEmpty()) return null
    return stringResource(R.string.site_channel_projected_channels_line, names.take(3).joinToString(", "))
}

@Composable
private fun modelPreviewLine(group: SiteChannelGroup): String? {
    val models = group.models.filterNot { it.disabled }.map { it.modelName }.filter { it.isNotBlank() }
    if (models.isEmpty()) return null
    return stringResource(R.string.site_channel_models_line, models.take(4).joinToString(", "))
}

private const val MAX_ACCOUNTS_PER_CARD = 2
private const val MAX_GROUPS_PER_ACCOUNT = 3
private const val ROUTE_SUMMARY_LIMIT = 4
