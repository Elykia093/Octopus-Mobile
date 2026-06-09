package com.elykia.octopus.feature.sitechannel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import com.elykia.octopus.core.data.model.SiteChannelModel
import com.elykia.octopus.core.data.model.SiteModelRouteType
import com.elykia.octopus.core.data.model.SiteProjectedChannelSettings
import com.elykia.octopus.core.data.model.SiteProjectedChannelSettingsUpdateRequest
import com.elykia.octopus.core.data.model.SiteSourceKey
import com.elykia.octopus.core.data.model.SiteSourceKeyAddRequest
import com.elykia.octopus.core.data.model.SiteSourceKeyUpdateItem
import com.elykia.octopus.core.data.model.SiteSourceKeyUpdateRequest
import com.elykia.octopus.core.designsystem.AppInfoChip
import com.elykia.octopus.core.designsystem.AppLazyPageScaffold
import com.elykia.octopus.core.designsystem.AppListCard
import com.elykia.octopus.core.designsystem.DialogScrollableColumn
import com.elykia.octopus.core.designsystem.ErrorStateCard
import com.elykia.octopus.core.designsystem.InlineEmptyCard
import com.elykia.octopus.core.designsystem.LoadingStateCard
import com.elykia.octopus.core.designsystem.OctopusTokens
import com.elykia.octopus.core.designsystem.OperationErrorCard
import com.elykia.octopus.core.designsystem.OptionChipGroup
import com.elykia.octopus.core.designsystem.OptionChipItem
import com.elykia.octopus.core.designsystem.PageActionButton
import com.elykia.octopus.core.designsystem.SearchField
import com.elykia.octopus.core.designsystem.SecureVisibleWindow
import com.elykia.octopus.core.designsystem.SoftIconTile
import com.elykia.octopus.core.designsystem.ToolbarChip
import com.elykia.octopus.core.designsystem.icons.AppMiuixIcons
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun SiteChannelScreen(
    contentPadding: PaddingValues,
    viewModel: SiteChannelViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var searchVisible by remember { mutableStateOf(false) }
    var searchTerm by remember { mutableStateOf("") }
    var createKeyTarget by remember { mutableStateOf<SiteChannelGroupTarget?>(null) }
    var sourceKeyTarget by remember { mutableStateOf<SiteChannelGroupTarget?>(null) }
    var manualModelTarget by remember { mutableStateOf<SiteChannelGroupTarget?>(null) }
    var projectedSettingsTarget by remember { mutableStateOf<SiteChannelGroupTarget?>(null) }
    var routeTarget by remember { mutableStateOf<SiteChannelModelTarget?>(null) }

    val query = searchTerm.trim()
    val cards = uiState.cards.filter { card -> query.isBlank() || card.matches(query) }

    AppLazyPageScaffold(
        title = stringResource(R.string.site_channel_title),
        actions = {
            PageActionButton(
                icon = AppMiuixIcons.Refresh,
                contentDescription = stringResource(R.string.common_refresh),
                enabled = !uiState.loading && !uiState.submitting,
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
                uiState.operationError?.takeIf { it.isNotBlank() }?.let { error ->
                    item { OperationErrorCard(message = error, onDismiss = viewModel::clearOperationFeedback) }
                }
                uiState.operationMessage?.takeIf { it.isNotBlank() }?.let { message ->
                    item { AppInfoChip(text = message, icon = AppMiuixIcons.Info, tint = OctopusTokens.Accent) }
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
                        SiteChannelCardView(
                            card = card,
                            submitting = uiState.submitting,
                            onCreateKey = { createKeyTarget = it },
                            onEditSourceKeys = { sourceKeyTarget = it },
                            onAddManualModels = { manualModelTarget = it },
                            onEditProjectedSettings = { projectedSettingsTarget = it },
                            onEditRoute = { routeTarget = it },
                            onToggleProjection = { target ->
                                viewModel.setGroupProjection(
                                    siteId = target.siteId,
                                    accountId = target.account.accountId,
                                    groupKey = target.group.groupKey,
                                    projectionDisabled = !target.group.projectionDisabled,
                                )
                            },
                            onResetRoutes = { siteId, account ->
                                viewModel.resetModelRoutes(siteId = siteId, accountId = account.accountId)
                            },
                            onToggleModelDisabled = { target ->
                                viewModel.setModelDisabled(
                                    siteId = target.siteId,
                                    accountId = target.account.accountId,
                                    groupKey = target.group.groupKey,
                                    model = target.model,
                                    disabled = !target.model.disabled,
                                )
                            },
                            onDeleteManualModel = { target ->
                                viewModel.deleteManualModel(
                                    siteId = target.siteId,
                                    accountId = target.account.accountId,
                                    groupKey = target.group.groupKey,
                                    model = target.model,
                                )
                            },
                        )
                    }
                }
            }
        }
    }

    CreateProjectionKeyDialog(
        target = createKeyTarget,
        submitting = uiState.submitting,
        operationError = uiState.operationError,
        onConfirm = { target, name ->
            viewModel.createKey(target.siteId, target.account.accountId, target.group.groupKey, name) {
                createKeyTarget = null
                viewModel.clearOperationFeedback()
            }
        },
        onDismiss = {
            if (!uiState.submitting) {
                createKeyTarget = null
                viewModel.clearOperationFeedback()
            }
        },
    )

    SourceKeyEditorDialog(
        target = sourceKeyTarget,
        submitting = uiState.submitting,
        operationError = uiState.operationError,
        onConfirm = { target, request ->
            viewModel.updateSourceKeys(target.siteId, target.account.accountId, request) {
                sourceKeyTarget = null
                viewModel.clearOperationFeedback()
            }
        },
        onDismiss = {
            if (!uiState.submitting) {
                sourceKeyTarget = null
                viewModel.clearOperationFeedback()
            }
        },
    )

    ManualModelDialog(
        target = manualModelTarget,
        submitting = uiState.submitting,
        operationError = uiState.operationError,
        onConfirm = { target, names, routeType ->
            viewModel.addManualModels(target.siteId, target.account.accountId, target.group.groupKey, names, routeType) {
                manualModelTarget = null
                viewModel.clearOperationFeedback()
            }
        },
        onDismiss = {
            if (!uiState.submitting) {
                manualModelTarget = null
                viewModel.clearOperationFeedback()
            }
        },
    )

    ModelRouteDialog(
        target = routeTarget,
        submitting = uiState.submitting,
        operationError = uiState.operationError,
        onConfirm = { target, routeType ->
            viewModel.updateModelRoute(
                siteId = target.siteId,
                accountId = target.account.accountId,
                groupKey = target.group.groupKey,
                model = target.model,
                routeType = routeType,
            ) {
                routeTarget = null
                viewModel.clearOperationFeedback()
            }
        },
        onDismiss = {
            if (!uiState.submitting) {
                routeTarget = null
                viewModel.clearOperationFeedback()
            }
        },
    )

    ProjectedSettingsDialog(
        target = projectedSettingsTarget,
        submitting = uiState.submitting,
        operationError = uiState.operationError,
        onConfirm = { target, request ->
            viewModel.updateProjectedChannelSettings(target.siteId, target.account.accountId, request) {
                projectedSettingsTarget = null
                viewModel.clearOperationFeedback()
            }
        },
        onDismiss = {
            if (!uiState.submitting) {
                projectedSettingsTarget = null
                viewModel.clearOperationFeedback()
            }
        },
    )
}

@Composable
private fun SiteChannelCardView(
    card: SiteChannelCard,
    submitting: Boolean,
    onCreateKey: (SiteChannelGroupTarget) -> Unit,
    onEditSourceKeys: (SiteChannelGroupTarget) -> Unit,
    onAddManualModels: (SiteChannelGroupTarget) -> Unit,
    onEditProjectedSettings: (SiteChannelGroupTarget) -> Unit,
    onEditRoute: (SiteChannelModelTarget) -> Unit,
    onToggleProjection: (SiteChannelGroupTarget) -> Unit,
    onResetRoutes: (Int, SiteChannelAccount) -> Unit,
    onToggleModelDisabled: (SiteChannelModelTarget) -> Unit,
    onDeleteManualModel: (SiteChannelModelTarget) -> Unit,
) {
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
                SiteChannelAccountBlock(
                    siteId = card.siteId,
                    account = account,
                    submitting = submitting,
                    onCreateKey = onCreateKey,
                    onEditSourceKeys = onEditSourceKeys,
                    onAddManualModels = onAddManualModels,
                    onEditProjectedSettings = onEditProjectedSettings,
                    onEditRoute = onEditRoute,
                    onToggleProjection = onToggleProjection,
                    onResetRoutes = onResetRoutes,
                    onToggleModelDisabled = onToggleModelDisabled,
                    onDeleteManualModel = onDeleteManualModel,
                )
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
private fun SiteChannelAccountBlock(
    siteId: Int,
    account: SiteChannelAccount,
    submitting: Boolean,
    onCreateKey: (SiteChannelGroupTarget) -> Unit,
    onEditSourceKeys: (SiteChannelGroupTarget) -> Unit,
    onAddManualModels: (SiteChannelGroupTarget) -> Unit,
    onEditProjectedSettings: (SiteChannelGroupTarget) -> Unit,
    onEditRoute: (SiteChannelModelTarget) -> Unit,
    onToggleProjection: (SiteChannelGroupTarget) -> Unit,
    onResetRoutes: (Int, SiteChannelAccount) -> Unit,
    onToggleModelDisabled: (SiteChannelModelTarget) -> Unit,
    onDeleteManualModel: (SiteChannelModelTarget) -> Unit,
) {
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
            ToolbarChip(
                text = stringResource(R.string.site_channel_action_reset_routes),
                onClick = if (submitting) null else { { onResetRoutes(siteId, account) } },
            )
        }
        account.groups.take(MAX_GROUPS_PER_ACCOUNT).forEach { group ->
            SiteChannelGroupBlock(
                target = SiteChannelGroupTarget(siteId = siteId, account = account, group = group),
                submitting = submitting,
                onCreateKey = onCreateKey,
                onEditSourceKeys = onEditSourceKeys,
                onAddManualModels = onAddManualModels,
                onEditProjectedSettings = onEditProjectedSettings,
                onEditRoute = onEditRoute,
                onToggleProjection = onToggleProjection,
                onToggleModelDisabled = onToggleModelDisabled,
                onDeleteManualModel = onDeleteManualModel,
            )
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
private fun SiteChannelGroupBlock(
    target: SiteChannelGroupTarget,
    submitting: Boolean,
    onCreateKey: (SiteChannelGroupTarget) -> Unit,
    onEditSourceKeys: (SiteChannelGroupTarget) -> Unit,
    onAddManualModels: (SiteChannelGroupTarget) -> Unit,
    onEditProjectedSettings: (SiteChannelGroupTarget) -> Unit,
    onEditRoute: (SiteChannelModelTarget) -> Unit,
    onToggleProjection: (SiteChannelGroupTarget) -> Unit,
    onToggleModelDisabled: (SiteChannelModelTarget) -> Unit,
    onDeleteManualModel: (SiteChannelModelTarget) -> Unit,
) {
    val group = target.group
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
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ToolbarChip(
                text = stringResource(if (group.projectionDisabled) R.string.site_channel_action_enable_projection else R.string.site_channel_action_disable_projection),
                selected = !group.projectionDisabled,
                onClick = if (submitting) null else { { onToggleProjection(target) } },
            )
            ToolbarChip(text = stringResource(R.string.site_channel_action_source_keys), onClick = if (submitting) null else { { onEditSourceKeys(target) } })
            ToolbarChip(text = stringResource(R.string.site_channel_action_create_projection_key), onClick = if (submitting) null else { { onCreateKey(target) } })
            ToolbarChip(
                text = stringResource(R.string.site_channel_action_projected_settings),
                onClick = if (submitting || group.projectedChannels.isEmpty()) null else { { onEditProjectedSettings(target) } },
            )
            ToolbarChip(text = stringResource(R.string.site_channel_action_add_manual_model), onClick = if (submitting) null else { { onAddManualModels(target) } })
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
        group.models.take(MAX_MODELS_PER_GROUP).forEach { model ->
            SiteChannelModelRow(
                target = SiteChannelModelTarget(target.siteId, target.account, group, model),
                submitting = submitting,
                onEditRoute = onEditRoute,
                onToggleDisabled = onToggleModelDisabled,
                onDeleteManualModel = onDeleteManualModel,
            )
        }
        if (group.models.size > MAX_MODELS_PER_GROUP) {
            Text(
                text = stringResource(R.string.site_channel_more_models, group.models.size - MAX_MODELS_PER_GROUP),
                style = MiuixTheme.textStyles.body2,
                color = OctopusTokens.TextSecondary,
            )
        }
    }
}

@Composable
private fun SiteChannelModelRow(
    target: SiteChannelModelTarget,
    submitting: Boolean,
    onEditRoute: (SiteChannelModelTarget) -> Unit,
    onToggleDisabled: (SiteChannelModelTarget) -> Unit,
    onDeleteManualModel: (SiteChannelModelTarget) -> Unit,
) {
    val model = target.model
    AppListCard(padding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = model.modelName.ifBlank { stringResource(R.string.common_unknown) },
                        style = MiuixTheme.textStyles.body1,
                        fontWeight = FontWeight.Medium,
                        color = if (model.disabled) OctopusTokens.TextSecondary else OctopusTokens.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = routeTypeLabel(model.routeType),
                        style = MiuixTheme.textStyles.body2,
                        color = OctopusTokens.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ToolbarChip(text = routeTypeLabel(model.routeType))
                if (model.disabled) ToolbarChip(text = stringResource(R.string.common_disabled), selected = true)
                if (model.source == "manual") ToolbarChip(text = stringResource(R.string.site_channel_manual_badge), selected = true)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    text = stringResource(R.string.site_channel_action_route),
                    enabled = !submitting && !model.disabled,
                    onClick = { onEditRoute(target) },
                )
                TextButton(
                    text = stringResource(if (model.disabled) R.string.site_channel_action_enable_model else R.string.site_channel_action_disable_model),
                    enabled = !submitting,
                    onClick = { onToggleDisabled(target) },
                )
                if (model.source == "manual") {
                    TextButton(
                        text = stringResource(R.string.common_delete),
                        enabled = !submitting,
                        onClick = { onDeleteManualModel(target) },
                    )
                }
            }
        }
    }
}

@Composable
private fun CreateProjectionKeyDialog(
    target: SiteChannelGroupTarget?,
    submitting: Boolean,
    operationError: String?,
    onConfirm: (SiteChannelGroupTarget, String) -> Unit,
    onDismiss: () -> Unit,
) {
    if (target == null) return
    var name by remember(target.group.groupKey) { mutableStateOf("") }

    OverlayDialog(
        show = true,
        title = stringResource(R.string.site_channel_create_key_title),
        summary = target.group.groupTitle(),
        onDismissRequest = { if (!submitting) onDismiss() },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            operationError?.takeIf { it.isNotBlank() }?.let { OperationErrorCard(message = it) }
            TextField(
                value = name,
                onValueChange = { name = it },
                label = stringResource(R.string.site_channel_key_name_label),
                useLabelAsPlaceholder = true,
                singleLine = true,
                enabled = !submitting,
                modifier = Modifier.fillMaxWidth(),
            )
            DialogButtons(
                submitting = submitting,
                onDismiss = onDismiss,
                onConfirm = { onConfirm(target, name) },
            )
        }
    }
}

@Composable
private fun SourceKeyEditorDialog(
    target: SiteChannelGroupTarget?,
    submitting: Boolean,
    operationError: String?,
    onConfirm: (SiteChannelGroupTarget, SiteSourceKeyUpdateRequest) -> Unit,
    onDismiss: () -> Unit,
) {
    if (target == null) return

    var items by remember(target.group.groupKey) { mutableStateOf(target.group.sourceKeyEditorItems()) }
    val request = remember(target.group, items) { buildSourceKeyUpdateRequest(target.group, items) }
    val hasSecretInput = items.any { it.token.isNotBlank() }
    val scrollState = rememberScrollState()

    if (hasSecretInput) {
        SecureVisibleWindow()
    }

    OverlayDialog(
        show = true,
        title = stringResource(R.string.site_channel_source_keys_title),
        summary = stringResource(R.string.site_channel_source_keys_summary, target.group.groupTitle(), target.group.sourceKeys.size),
        onDismissRequest = { if (!submitting) onDismiss() },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DialogScrollableColumn(fraction = 0.66f, scrollState = scrollState) {
                operationError?.takeIf { it.isNotBlank() }?.let { OperationErrorCard(message = it) }
                items.forEachIndexed { index, item ->
                    SourceKeyEditorRow(
                        item = item,
                        submitting = submitting,
                        onChange = { next ->
                            items = items.toMutableList().also { it[index] = next }
                        },
                        onDelete = {
                            items = items.toMutableList().also { it.removeAt(index) }
                        },
                    )
                }
                TextButton(
                    text = stringResource(R.string.site_channel_add_key_row),
                    enabled = !submitting,
                    onClick = {
                        items = items + SourceKeyEditorItem(
                            enabled = true,
                            token = "",
                            name = "",
                            isNew = true,
                        )
                    },
                )
            }
            DialogButtons(
                submitting = submitting,
                confirmEnabled = sourceKeyRequestHasChanges(request),
                onDismiss = onDismiss,
                onConfirm = { onConfirm(target, request) },
            )
        }
    }
}

@Composable
private fun SourceKeyEditorRow(
    item: SourceKeyEditorItem,
    submitting: Boolean,
    onChange: (SourceKeyEditorItem) -> Unit,
    onDelete: () -> Unit,
) {
    AppListCard(padding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = item.id?.let { stringResource(R.string.site_channel_source_key_existing, it) }
                        ?: stringResource(R.string.site_channel_source_key_new),
                    style = MiuixTheme.textStyles.main,
                    fontWeight = FontWeight.Medium,
                )
                Switch(checked = item.enabled, onCheckedChange = { if (!submitting) onChange(item.copy(enabled = it)) })
            }
            item.tokenMasked?.takeIf { it.isNotBlank() }?.let { masked ->
                Text(
                    text = stringResource(R.string.site_channel_source_key_masked, masked),
                    style = MiuixTheme.textStyles.body2,
                    color = OctopusTokens.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            TextField(
                value = item.name,
                onValueChange = { onChange(item.copy(name = it)) },
                label = stringResource(R.string.site_channel_source_key_name_label),
                useLabelAsPlaceholder = true,
                singleLine = true,
                enabled = !submitting,
                modifier = Modifier.fillMaxWidth(),
            )
            TextField(
                value = item.token,
                onValueChange = { onChange(item.copy(token = it)) },
                label = stringResource(R.string.site_channel_source_key_token_label),
                useLabelAsPlaceholder = true,
                singleLine = true,
                enabled = !submitting,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(text = stringResource(R.string.site_channel_delete_key), enabled = !submitting, onClick = onDelete)
            }
        }
    }
}

@Composable
private fun ManualModelDialog(
    target: SiteChannelGroupTarget?,
    submitting: Boolean,
    operationError: String?,
    onConfirm: (SiteChannelGroupTarget, List<String>, String) -> Unit,
    onDismiss: () -> Unit,
) {
    if (target == null) return
    var namesText by remember(target.group.groupKey) { mutableStateOf("") }
    var routeType by remember(target.group.groupKey) { mutableStateOf(SiteModelRouteType.OpenAiChat) }

    OverlayDialog(
        show = true,
        title = stringResource(R.string.site_channel_manual_model_title),
        summary = target.group.groupTitle(),
        onDismissRequest = { if (!submitting) onDismiss() },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            operationError?.takeIf { it.isNotBlank() }?.let { OperationErrorCard(message = it) }
            TextField(
                value = namesText,
                onValueChange = { namesText = it },
                label = stringResource(R.string.site_channel_manual_models_label),
                useLabelAsPlaceholder = true,
                singleLine = false,
                maxLines = 4,
                enabled = !submitting,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(text = stringResource(R.string.site_channel_route_type_label), style = MiuixTheme.textStyles.body2, color = OctopusTokens.TextSecondary)
            OptionChipGroup(
                options = routeTypeOptions(),
                selectedValue = routeType,
                onSelect = { if (!submitting) routeType = it },
                columns = 2,
            )
            DialogButtons(
                submitting = submitting,
                confirmEnabled = parseModelNames(namesText).isNotEmpty(),
                onDismiss = onDismiss,
                onConfirm = { onConfirm(target, parseModelNames(namesText), routeType) },
            )
        }
    }
}

@Composable
private fun ModelRouteDialog(
    target: SiteChannelModelTarget?,
    submitting: Boolean,
    operationError: String?,
    onConfirm: (SiteChannelModelTarget, String) -> Unit,
    onDismiss: () -> Unit,
) {
    if (target == null) return
    var routeType by remember(target.model.modelName, target.group.groupKey) { mutableStateOf(target.model.routeType) }

    OverlayDialog(
        show = true,
        title = stringResource(R.string.site_channel_model_route_title),
        summary = target.model.modelName,
        onDismissRequest = { if (!submitting) onDismiss() },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            operationError?.takeIf { it.isNotBlank() }?.let { OperationErrorCard(message = it) }
            OptionChipGroup(
                options = routeTypeOptions(),
                selectedValue = routeType,
                onSelect = { if (!submitting) routeType = it },
                columns = 2,
            )
            DialogButtons(
                submitting = submitting,
                confirmEnabled = routeType != target.model.routeType,
                onDismiss = onDismiss,
                onConfirm = { onConfirm(target, routeType) },
            )
        }
    }
}

@Composable
private fun ProjectedSettingsDialog(
    target: SiteChannelGroupTarget?,
    submitting: Boolean,
    operationError: String?,
    onConfirm: (SiteChannelGroupTarget, List<SiteProjectedChannelSettingsUpdateRequest>) -> Unit,
    onDismiss: () -> Unit,
) {
    if (target == null) return
    var items by remember(target.group.groupKey) { mutableStateOf(target.group.projectedChannels.map { it.toEditorItem() }) }
    val request = remember(target.group, items) { buildProjectedSettingsRequest(target.group.projectedChannels, items) }
    val scrollState = rememberScrollState()

    OverlayDialog(
        show = true,
        title = stringResource(R.string.site_channel_projected_settings_title),
        summary = target.group.groupTitle(),
        onDismissRequest = { if (!submitting) onDismiss() },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DialogScrollableColumn(fraction = 0.66f, scrollState = scrollState) {
                operationError?.takeIf { it.isNotBlank() }?.let { OperationErrorCard(message = it) }
                items.forEachIndexed { index, item ->
                    ProjectedSettingsRow(
                        item = item,
                        submitting = submitting,
                        onChange = { next ->
                            items = items.toMutableList().also { it[index] = next }
                        },
                    )
                }
            }
            DialogButtons(
                submitting = submitting,
                confirmEnabled = request.isNotEmpty(),
                onDismiss = onDismiss,
                onConfirm = { onConfirm(target, request) },
            )
        }
    }
}

@Composable
private fun ProjectedSettingsRow(
    item: ProjectedSettingsEditorItem,
    submitting: Boolean,
    onChange: (ProjectedSettingsEditorItem) -> Unit,
) {
    AppListCard(padding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = item.channelName.ifBlank { "#${item.channelId}" },
                style = MiuixTheme.textStyles.main,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(text = stringResource(R.string.site_channel_auto_group_label), style = MiuixTheme.textStyles.body2, color = OctopusTokens.TextSecondary)
            OptionChipGroup(
                options = autoGroupOptions(),
                selectedValue = item.autoGroup,
                onSelect = { if (!submitting) onChange(item.copy(autoGroup = it)) },
                columns = 2,
            )
            TextField(
                value = item.paramOverride,
                onValueChange = { onChange(item.copy(paramOverride = it)) },
                label = stringResource(R.string.site_channel_param_override_label),
                useLabelAsPlaceholder = true,
                singleLine = false,
                maxLines = 4,
                enabled = !submitting,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun DialogButtons(
    submitting: Boolean,
    confirmEnabled: Boolean = true,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        TextButton(text = stringResource(R.string.common_cancel), enabled = !submitting, onClick = onDismiss)
        TextButton(
            text = stringResource(if (submitting) R.string.common_saving else R.string.common_confirm),
            enabled = !submitting && confirmEnabled,
            onClick = onConfirm,
        )
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
private fun routeTypeOptions(): List<OptionChipItem<String>> = listOf(
    OptionChipItem(SiteModelRouteType.OpenAiChat, routeTypeLabel(SiteModelRouteType.OpenAiChat)),
    OptionChipItem(SiteModelRouteType.OpenAiResponse, routeTypeLabel(SiteModelRouteType.OpenAiResponse)),
    OptionChipItem(SiteModelRouteType.Anthropic, routeTypeLabel(SiteModelRouteType.Anthropic)),
    OptionChipItem(SiteModelRouteType.Gemini, routeTypeLabel(SiteModelRouteType.Gemini)),
    OptionChipItem(SiteModelRouteType.Volcengine, routeTypeLabel(SiteModelRouteType.Volcengine)),
    OptionChipItem(SiteModelRouteType.OpenAiEmbedding, routeTypeLabel(SiteModelRouteType.OpenAiEmbedding)),
)

@Composable
private fun autoGroupOptions(): List<OptionChipItem<Int>> = (0..3).map { value ->
    OptionChipItem(value, autoGroupLabel(value))
}

@Composable
private fun autoGroupLabel(value: Int): String = when (value) {
    0 -> stringResource(R.string.channel_auto_group_none)
    1 -> stringResource(R.string.channel_auto_group_fuzzy)
    2 -> stringResource(R.string.channel_auto_group_exact)
    3 -> stringResource(R.string.channel_auto_group_regex)
    else -> stringResource(R.string.channel_auto_group_unknown, value)
}

@Composable
private fun projectedChannelLine(group: SiteChannelGroup): String? {
    val names = group.projectedChannels
        .map { it.channelName.ifBlank { "#${it.channelId}" } }
        .ifEmpty { group.projectedChannelIds.filter { it > 0 }.map { "#$it" } }
    if (names.isEmpty()) return null
    return stringResource(R.string.site_channel_projected_channels_line, names.take(3).joinToString(", "))
}

private fun parseModelNames(value: String): List<String> =
    value.split('\n', ',').map { it.trim() }.filter { it.isNotBlank() }.distinct()

private fun SiteChannelGroup.groupTitle(): String =
    groupName.ifBlank { groupKey.ifBlank { "#" } }

private fun SiteChannelGroup.sourceKeyEditorItems(): List<SourceKeyEditorItem> =
    sourceKeys.map { key ->
        SourceKeyEditorItem(
            id = key.id,
            enabled = key.enabled,
            token = "",
            tokenMasked = key.tokenMasked,
            name = key.name,
            isNew = false,
        )
    }

private fun buildSourceKeyUpdateRequest(
    group: SiteChannelGroup,
    items: List<SourceKeyEditorItem>,
): SiteSourceKeyUpdateRequest {
    val originalById = group.sourceKeys.associateBy { it.id }
    val nextIds = items.mapNotNull { it.id }.toSet()
    val keysToDelete = group.sourceKeys.filter { it.id !in nextIds }.map { it.id }
    val keysToAdd = items
        .filter { it.id == null && it.token.trim().isNotBlank() }
        .map { item ->
            SiteSourceKeyAddRequest(
                enabled = item.enabled,
                token = item.token.trim(),
                name = item.name.trim().ifBlank { null },
            )
        }
    val keysToUpdate = items.mapNotNull { item ->
        val id = item.id ?: return@mapNotNull null
        val original = originalById[id] ?: return@mapNotNull null
        val token = item.token.trim().ifBlank { null }
        val name = item.name.trim().takeIf { it != original.name }
        val enabled = item.enabled.takeIf { it != original.enabled }
        if (token == null && name == null && enabled == null) {
            null
        } else {
            SiteSourceKeyUpdateItem(
                id = id,
                enabled = enabled,
                token = token,
                name = name,
            )
        }
    }
    return SiteSourceKeyUpdateRequest(
        groupKey = group.groupKey,
        keysToAdd = keysToAdd.ifEmpty { null },
        keysToUpdate = keysToUpdate.ifEmpty { null },
        keysToDelete = keysToDelete.ifEmpty { null },
    )
}

private fun sourceKeyRequestHasChanges(request: SiteSourceKeyUpdateRequest): Boolean =
    !request.keysToAdd.isNullOrEmpty() ||
        !request.keysToUpdate.isNullOrEmpty() ||
        !request.keysToDelete.isNullOrEmpty()

private fun SiteProjectedChannelSettings.toEditorItem(): ProjectedSettingsEditorItem =
    ProjectedSettingsEditorItem(
        channelId = channelId,
        channelName = channelName,
        autoGroup = autoGroup,
        paramOverride = paramOverride,
    )

private fun buildProjectedSettingsRequest(
    original: List<SiteProjectedChannelSettings>,
    items: List<ProjectedSettingsEditorItem>,
): List<SiteProjectedChannelSettingsUpdateRequest> {
    val originalById = original.associateBy { it.channelId }
    return items.mapNotNull { item ->
        val source = originalById[item.channelId] ?: return@mapNotNull null
        if (item.autoGroup == source.autoGroup && item.paramOverride.trim() == source.paramOverride.trim()) {
            null
        } else {
            SiteProjectedChannelSettingsUpdateRequest(
                channelId = item.channelId,
                autoGroup = item.autoGroup,
                paramOverride = item.paramOverride.trim(),
            )
        }
    }
}

private data class SiteChannelGroupTarget(
    val siteId: Int,
    val account: SiteChannelAccount,
    val group: SiteChannelGroup,
)

private data class SiteChannelModelTarget(
    val siteId: Int,
    val account: SiteChannelAccount,
    val group: SiteChannelGroup,
    val model: SiteChannelModel,
)

private data class SourceKeyEditorItem(
    val id: Int? = null,
    val enabled: Boolean,
    val token: String,
    val tokenMasked: String? = null,
    val name: String,
    val isNew: Boolean,
)

private data class ProjectedSettingsEditorItem(
    val channelId: Int,
    val channelName: String,
    val autoGroup: Int,
    val paramOverride: String,
)

private const val MAX_ACCOUNTS_PER_CARD = 2
private const val MAX_GROUPS_PER_ACCOUNT = 3
private const val MAX_MODELS_PER_GROUP = 3
private const val ROUTE_SUMMARY_LIMIT = 4
