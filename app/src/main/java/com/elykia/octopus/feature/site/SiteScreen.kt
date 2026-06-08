package com.elykia.octopus.feature.site

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.elykia.octopus.R
import com.elykia.octopus.core.data.model.CustomHeader
import com.elykia.octopus.core.data.model.ProxyMode
import com.elykia.octopus.core.data.model.Site
import com.elykia.octopus.core.data.model.SiteAccount
import com.elykia.octopus.core.data.model.SiteCredentialType
import com.elykia.octopus.core.data.model.SitePlatform
import com.elykia.octopus.core.designsystem.AppInfoChip
import com.elykia.octopus.core.designsystem.AppLazyPageScaffold
import com.elykia.octopus.core.designsystem.AppListCard
import com.elykia.octopus.core.designsystem.AppMetricRow
import com.elykia.octopus.core.designsystem.DangerConfirmDialog
import com.elykia.octopus.core.designsystem.DialogScrollableColumn
import com.elykia.octopus.core.designsystem.ErrorStateCard
import com.elykia.octopus.core.designsystem.InlineEmptyCard
import com.elykia.octopus.core.designsystem.LoadingStateCard
import com.elykia.octopus.core.designsystem.OctopusTokens
import com.elykia.octopus.core.designsystem.OperationErrorCard
import com.elykia.octopus.core.designsystem.PageActionButton
import com.elykia.octopus.core.designsystem.SearchField
import com.elykia.octopus.core.designsystem.SecureVisibleWindow
import com.elykia.octopus.core.designsystem.SoftIconTile
import com.elykia.octopus.core.designsystem.ToolbarChip
import com.elykia.octopus.core.designsystem.formatMoney
import com.elykia.octopus.core.designsystem.icons.AppMiuixIcons
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun SiteScreen(
    contentPadding: PaddingValues,
    viewModel: SiteViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var searchVisible by remember { mutableStateOf(false) }
    var searchTerm by remember { mutableStateOf("") }
    var showCreateSite by remember { mutableStateOf(false) }
    var editingSiteId by remember { mutableStateOf<Int?>(null) }
    var accountDialogSiteId by remember { mutableStateOf<Int?>(null) }
    var editingAccountId by remember { mutableStateOf<Int?>(null) }
    var siteToArchive by remember { mutableStateOf<Site?>(null) }
    var siteToDelete by remember { mutableStateOf<Site?>(null) }
    var accountToDelete by remember { mutableStateOf<SiteAccount?>(null) }
    var showArchived by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }

    val editingSite = editingSiteId?.let { id -> uiState.sites.firstOrNull { it.id == id } }
    val accountDialogSite = accountDialogSiteId?.let { id -> uiState.sites.firstOrNull { it.id == id } }
    val editingAccount = editingAccountId?.let { id ->
        uiState.sites.asSequence().flatMap { it.accounts.orEmpty().asSequence() }.firstOrNull { it.id == id }
    }
    val query = searchTerm.trim()
    val sites = uiState.sites.filter { site ->
        query.isBlank() ||
            site.name.contains(query, ignoreCase = true) ||
            site.baseUrl.contains(query, ignoreCase = true) ||
            site.platform.contains(query, ignoreCase = true) ||
            site.accounts.orEmpty().any { it.name.contains(query, ignoreCase = true) }
    }

    AppLazyPageScaffold(
        title = stringResource(R.string.site_title),
        actions = {
            PageActionButton(
                icon = AppMiuixIcons.Sync,
                contentDescription = stringResource(R.string.site_action_sync_all),
                enabled = !uiState.loading && !uiState.shouldShowPageError() && uiState.sites.isNotEmpty() && !uiState.submitting,
                onClick = viewModel::syncAll,
            )
            PageActionButton(
                icon = AppMiuixIcons.Time,
                contentDescription = stringResource(R.string.site_action_checkin_all),
                enabled = !uiState.loading && !uiState.shouldShowPageError() && uiState.sites.isNotEmpty() && !uiState.submitting,
                onClick = viewModel::checkinAll,
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
            PageActionButton(
                icon = AppMiuixIcons.More,
                contentDescription = stringResource(R.string.site_archived_title),
                enabled = !uiState.loading && !uiState.shouldShowPageError(),
                onClick = {
                    showArchived = true
                    viewModel.refreshArchived()
                },
            )
            PageActionButton(
                icon = AppMiuixIcons.ArrowDown,
                contentDescription = stringResource(R.string.site_import_title),
                enabled = !uiState.loading && !uiState.shouldShowPageError() && !uiState.submitting,
                onClick = {
                    viewModel.clearOperationFeedback()
                    showImportDialog = true
                },
            )
            PageActionButton(
                icon = AppMiuixIcons.Add,
                contentDescription = stringResource(R.string.action_create),
                enabled = !uiState.loading && !uiState.shouldShowPageError() && !uiState.submitting,
                onClick = {
                    viewModel.clearOperationFeedback()
                    showCreateSite = true
                },
            )
        },
        contentPadding = contentPadding,
    ) {
        when {
            uiState.loading -> item { LoadingStateCard(title = stringResource(R.string.site_title)) }
            uiState.shouldShowPageError() -> item {
                ErrorStateCard(
                    message = uiState.error ?: stringResource(R.string.error_title),
                    onRetry = viewModel::refresh,
                )
            }
            else -> {
                if (uiState.sites.isNotEmpty() && searchVisible) {
                    item {
                        SearchField(
                            value = searchTerm,
                            onValueChange = { searchTerm = it },
                            hint = stringResource(R.string.site_search_hint),
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
                    uiState.sites.isEmpty() -> item {
                        InlineEmptyCard(
                            title = stringResource(R.string.site_title),
                            summary = stringResource(R.string.site_empty),
                        )
                    }
                    sites.isEmpty() -> item {
                        InlineEmptyCard(
                            title = stringResource(R.string.empty_title),
                            summary = stringResource(R.string.site_search_empty),
                        )
                    }
                    else -> items(sites, key = { it.id }) { site ->
                        SiteCard(
                            site = site,
                            submitting = uiState.submitting,
                            onEdit = {
                                viewModel.clearOperationFeedback()
                                editingSiteId = site.id
                            },
                            onToggle = { viewModel.setSiteEnabled(site, it) },
                            onAddAccount = {
                                viewModel.clearOperationFeedback()
                                accountDialogSiteId = site.id
                                editingAccountId = null
                            },
                            onEditAccount = { account ->
                                viewModel.clearOperationFeedback()
                                accountDialogSiteId = site.id
                                editingAccountId = account.id
                            },
                            onDeleteAccount = { accountToDelete = it },
                            onSyncAccount = viewModel::syncAccount,
                            onCheckinAccount = viewModel::checkinAccount,
                            onToggleAccount = viewModel::setAccountEnabled,
                            onArchive = { siteToArchive = site },
                            onDeleteSite = { siteToDelete = site },
                        )
                    }
                }
            }
        }
    }

    SiteEditorDialog(
        visible = showCreateSite,
        title = stringResource(R.string.site_create_title),
        initialSite = null,
        submitting = uiState.submitting,
        operationError = uiState.operationError,
        onConfirm = { values ->
            viewModel.createSite(values) {
                showCreateSite = false
                viewModel.clearOperationFeedback()
            }
        },
        onDismiss = {
            if (!uiState.submitting) {
                showCreateSite = false
                viewModel.clearOperationFeedback()
            }
        },
    )

    SiteEditorDialog(
        visible = editingSite != null,
        title = stringResource(R.string.site_edit_title),
        initialSite = editingSite,
        submitting = uiState.submitting,
        operationError = uiState.operationError,
        onConfirm = { values ->
            editingSite?.let { site ->
                viewModel.updateSite(site, values) {
                    editingSiteId = null
                    viewModel.clearOperationFeedback()
                }
            }
        },
        onDismiss = {
            if (!uiState.submitting) {
                editingSiteId = null
                viewModel.clearOperationFeedback()
            }
        },
    )

    SiteImportDialog(
        visible = showImportDialog,
        submitting = uiState.submitting,
        operationError = uiState.operationError,
        onConfirm = { source, payload ->
            viewModel.importSites(source, payload) {
                showImportDialog = false
            }
        },
        onDismiss = {
            if (!uiState.submitting) {
                showImportDialog = false
                viewModel.clearOperationFeedback()
            }
        },
    )

    SiteAccountEditorDialog(
        visible = accountDialogSite != null,
        title = if (editingAccount == null) {
            stringResource(R.string.site_account_create_title)
        } else {
            stringResource(R.string.site_account_edit_title)
        },
        site = accountDialogSite,
        initialAccount = editingAccount,
        submitting = uiState.submitting,
        operationError = uiState.operationError,
        onConfirm = { values ->
            val site = accountDialogSite ?: return@SiteAccountEditorDialog
            val account = editingAccount
            if (account == null) {
                viewModel.createAccount(site, values) {
                    accountDialogSiteId = null
                    viewModel.clearOperationFeedback()
                }
            } else {
                viewModel.updateAccount(account, values) {
                    accountDialogSiteId = null
                    editingAccountId = null
                    viewModel.clearOperationFeedback()
                }
            }
        },
        onDismiss = {
            if (!uiState.submitting) {
                accountDialogSiteId = null
                editingAccountId = null
                viewModel.clearOperationFeedback()
            }
        },
    )

    ArchivedSitesDialog(
        visible = showArchived,
        loading = uiState.archivedLoading,
        sites = uiState.archivedSites,
        submitting = uiState.submitting,
        onRestore = viewModel::restoreSite,
        onDismiss = { showArchived = false },
    )

    DangerConfirmDialog(
        visible = siteToArchive != null,
        title = stringResource(R.string.site_archive_title),
        summary = stringResource(R.string.site_archive_summary, siteToArchive?.name.orEmpty()),
        onConfirm = {
            siteToArchive?.let(viewModel::archiveSite)
            siteToArchive = null
        },
        onDismiss = { siteToArchive = null },
    )
    DangerConfirmDialog(
        visible = siteToDelete != null,
        title = stringResource(R.string.site_delete_title),
        summary = stringResource(R.string.site_delete_summary, siteToDelete?.name.orEmpty()),
        onConfirm = {
            siteToDelete?.let(viewModel::deleteSite)
            siteToDelete = null
        },
        onDismiss = { siteToDelete = null },
    )
    DangerConfirmDialog(
        visible = accountToDelete != null,
        title = stringResource(R.string.site_account_delete_title),
        summary = stringResource(R.string.site_account_delete_summary, accountToDelete?.name.orEmpty()),
        onConfirm = {
            accountToDelete?.let(viewModel::deleteAccount)
            accountToDelete = null
        },
        onDismiss = { accountToDelete = null },
    )
}

@Composable
private fun SiteImportDialog(
    visible: Boolean,
    submitting: Boolean,
    operationError: String?,
    onConfirm: (SiteImportSource, String) -> Unit,
    onDismiss: () -> Unit,
) {
    if (!visible) return
    var source by remember(visible) { mutableStateOf(SiteImportSource.AllApiHub) }
    var payload by remember(visible) { mutableStateOf("") }

    OverlayDialog(
        show = visible,
        title = stringResource(R.string.site_import_title),
        summary = stringResource(R.string.site_import_summary),
        onDismissRequest = { if (!submitting) onDismiss() },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            operationError?.takeIf { it.isNotBlank() }?.let { OperationErrorCard(message = it) }
            DialogLabel(text = stringResource(R.string.site_import_source_label))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SiteImportSource.entries.forEach { option ->
                    ToolbarChip(
                        text = importSourceLabel(option),
                        selected = source == option,
                        onClick = { if (!submitting) source = option },
                    )
                }
            }
            TextField(
                value = payload,
                onValueChange = { payload = it },
                label = stringResource(R.string.site_import_payload_hint),
                useLabelAsPlaceholder = true,
                singleLine = false,
                enabled = !submitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
            )
            DialogButtons(
                submitting = submitting,
                canSubmit = !submitting && payload.isNotBlank(),
                onDismiss = onDismiss,
                onConfirm = { onConfirm(source, payload) },
            )
        }
    }
}

@Composable
private fun SiteCard(
    site: Site,
    submitting: Boolean,
    onEdit: () -> Unit,
    onToggle: (Boolean) -> Unit,
    onAddAccount: () -> Unit,
    onEditAccount: (SiteAccount) -> Unit,
    onDeleteAccount: (SiteAccount) -> Unit,
    onSyncAccount: (SiteAccount) -> Unit,
    onCheckinAccount: (SiteAccount) -> Unit,
    onToggleAccount: (SiteAccount, Boolean) -> Unit,
    onArchive: () -> Unit,
    onDeleteSite: () -> Unit,
) {
    val accounts = site.accounts.orEmpty()
    val tokenCount = accounts.sumOf { it.tokens.orEmpty().size }
    val modelCount = accounts.sumOf { it.models.orEmpty().size }
    val groupCount = accounts.sumOf { it.userGroups.orEmpty().size }
    val balance = accounts.sumOf { it.balance }
    val used = accounts.sumOf { it.balanceUsed }

    AppListCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SoftIconTile(
                    icon = AppMiuixIcons.Site,
                    contentDescription = site.name,
                    tint = if (site.enabled) OctopusTokens.Accent else OctopusTokens.TextSecondary,
                )
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = site.name.ifBlank { site.baseUrl },
                        style = MiuixTheme.textStyles.title3,
                        fontWeight = FontWeight.SemiBold,
                        color = OctopusTokens.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "${platformLabel(site.platform)} · ${site.baseUrl}",
                        style = MiuixTheme.textStyles.body2,
                        color = OctopusTokens.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Switch(
                    checked = site.enabled,
                    onCheckedChange = { checked -> if (!submitting) onToggle(checked) },
                )
            }

            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AppInfoChip(text = stringResource(R.string.site_accounts_count, accounts.size), icon = AppMiuixIcons.ApiKey)
                AppInfoChip(text = stringResource(R.string.site_tokens_count, tokenCount), icon = AppMiuixIcons.Token)
                AppInfoChip(text = stringResource(R.string.site_models_count, modelCount), icon = AppMiuixIcons.Model)
                AppInfoChip(text = stringResource(R.string.site_groups_count, groupCount), icon = AppMiuixIcons.Group)
            }

            AppMetricRow(
                icon = AppMiuixIcons.Cost,
                label = stringResource(R.string.site_balance_summary, formatMoney(balance), formatMoney(used)),
                value = proxyModeLabel(site.proxyMode),
                accentColor = OctopusTokens.Accent,
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(text = stringResource(R.string.action_edit), enabled = !submitting, onClick = onEdit)
                TextButton(text = stringResource(R.string.site_action_account), enabled = !submitting, onClick = onAddAccount)
                TextButton(text = stringResource(R.string.site_action_archive), enabled = !submitting, onClick = onArchive)
                TextButton(text = stringResource(R.string.common_delete), enabled = !submitting, onClick = onDeleteSite)
            }

            if (accounts.isEmpty()) {
                Text(
                    text = stringResource(R.string.site_empty),
                    style = MiuixTheme.textStyles.body2,
                    color = OctopusTokens.TextSecondary,
                )
            } else {
                accounts.forEach { account ->
                    SiteAccountRow(
                        account = account,
                        submitting = submitting,
                        onEdit = { onEditAccount(account) },
                        onDelete = { onDeleteAccount(account) },
                        onSync = { onSyncAccount(account) },
                        onCheckin = { onCheckinAccount(account) },
                        onToggle = { onToggleAccount(account, it) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SiteAccountRow(
    account: SiteAccount,
    submitting: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSync: () -> Unit,
    onCheckin: () -> Unit,
    onToggle: (Boolean) -> Unit,
) {
    AppListCard(padding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = account.name,
                        style = MiuixTheme.textStyles.main,
                        fontWeight = FontWeight.SemiBold,
                        color = OctopusTokens.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = credentialLabel(account.credentialType),
                        style = MiuixTheme.textStyles.body2,
                        color = OctopusTokens.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Switch(checked = account.enabled, onCheckedChange = { if (!submitting) onToggle(it) })
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AppInfoChip(text = stringResource(R.string.site_tokens_count, account.tokens.orEmpty().size), icon = AppMiuixIcons.ApiKey)
                AppInfoChip(text = stringResource(R.string.site_groups_count, account.userGroups.orEmpty().size), icon = AppMiuixIcons.Group)
                AppInfoChip(text = stringResource(R.string.site_models_count, account.models.orEmpty().size), icon = AppMiuixIcons.Model)
                AppInfoChip(text = statusLabel(account.lastSyncStatus), icon = AppMiuixIcons.Sync)
            }
            Text(
                text = stringResource(R.string.site_last_sync, formatSiteTime(account.lastSyncAt), account.lastSyncMessage.ifBlank { statusLabel(account.lastSyncStatus) }),
                style = MiuixTheme.textStyles.body2,
                color = OctopusTokens.TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = stringResource(R.string.site_last_checkin, formatSiteTime(account.lastCheckinAt), account.lastCheckinMessage.ifBlank { statusLabel(account.lastCheckinStatus) }),
                style = MiuixTheme.textStyles.body2,
                color = OctopusTokens.TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(text = stringResource(R.string.site_action_sync), enabled = !submitting, onClick = onSync)
                TextButton(text = stringResource(R.string.site_action_checkin), enabled = !submitting, onClick = onCheckin)
                TextButton(text = stringResource(R.string.action_edit), enabled = !submitting, onClick = onEdit)
                TextButton(text = stringResource(R.string.common_delete), enabled = !submitting, onClick = onDelete)
            }
        }
    }
}

@Composable
private fun SiteEditorDialog(
    visible: Boolean,
    title: String,
    initialSite: Site?,
    submitting: Boolean,
    operationError: String?,
    onConfirm: (SiteEditorValues) -> Unit,
    onDismiss: () -> Unit,
) {
    if (!visible) return
    var values by remember(initialSite?.id, visible) { mutableStateOf(initialSite.toSiteEditorValues()) }
    val scrollState = rememberScrollState()

    OverlayDialog(
        show = visible,
        title = title,
        summary = stringResource(R.string.site_panel_summary),
        onDismissRequest = { if (!submitting) onDismiss() },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DialogScrollableColumn(fraction = 0.68f, scrollState = scrollState) {
                operationError?.takeIf { it.isNotBlank() }?.let { OperationErrorCard(message = it) }
                TextField(
                    value = values.name,
                    onValueChange = { values = values.copy(name = it) },
                    label = stringResource(R.string.site_name_hint),
                    useLabelAsPlaceholder = true,
                    singleLine = true,
                    enabled = !submitting,
                    modifier = Modifier.fillMaxWidth(),
                )
                TextField(
                    value = values.baseUrl,
                    onValueChange = { values = values.copy(baseUrl = it) },
                    label = stringResource(R.string.site_base_url_hint),
                    useLabelAsPlaceholder = true,
                    singleLine = true,
                    enabled = !submitting,
                    modifier = Modifier.fillMaxWidth(),
                )
                DialogLabel(text = stringResource(R.string.site_platform_label))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SitePlatform.entries.forEach { platform ->
                        ToolbarChip(
                            text = platformLabel(platform),
                            selected = values.platform == platform,
                            onClick = { if (!submitting) values = values.copy(platform = platform) },
                        )
                    }
                }
                DialogLabel(text = stringResource(R.string.site_proxy_mode_label))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(ProxyMode.Direct, ProxyMode.System).forEach { mode ->
                        ToolbarChip(
                            text = proxyModeLabel(mode),
                            selected = values.proxyMode == mode,
                            onClick = { if (!submitting) values = values.copy(proxyMode = mode) },
                        )
                    }
                }
                SiteSwitchRow(
                    label = stringResource(R.string.site_enabled_label),
                    checked = values.enabled,
                    enabled = !submitting,
                    onCheckedChange = { values = values.copy(enabled = it) },
                )
                SiteSwitchRow(
                    label = stringResource(R.string.site_pinned_label),
                    checked = values.isPinned,
                    enabled = !submitting,
                    onCheckedChange = { values = values.copy(isPinned = it) },
                )
                TextField(
                    value = values.externalCheckinUrl,
                    onValueChange = { values = values.copy(externalCheckinUrl = it) },
                    label = stringResource(R.string.site_external_checkin_hint),
                    useLabelAsPlaceholder = true,
                    singleLine = true,
                    enabled = !submitting,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    TextField(
                        value = values.sortOrder,
                        onValueChange = { values = values.copy(sortOrder = it) },
                        label = stringResource(R.string.site_sort_order_hint),
                        useLabelAsPlaceholder = true,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        enabled = !submitting,
                        modifier = Modifier.weight(1f),
                    )
                    TextField(
                        value = values.globalWeight,
                        onValueChange = { values = values.copy(globalWeight = it) },
                        label = stringResource(R.string.site_global_weight_hint),
                        useLabelAsPlaceholder = true,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        enabled = !submitting,
                        modifier = Modifier.weight(1f),
                    )
                }
                EditableSiteHeaderList(
                    headers = values.customHeader,
                    submitting = submitting,
                    onChange = { values = values.copy(customHeader = it) },
                )
            }
            DialogButtons(
                submitting = submitting,
                canSubmit = canSubmitSite(values, submitting),
                onDismiss = onDismiss,
                onConfirm = { onConfirm(values) },
            )
        }
    }
}

@Composable
private fun SiteAccountEditorDialog(
    visible: Boolean,
    title: String,
    site: Site?,
    initialAccount: SiteAccount?,
    submitting: Boolean,
    operationError: String?,
    onConfirm: (SiteAccountEditorValues) -> Unit,
    onDismiss: () -> Unit,
) {
    if (!visible || site == null) return
    var values by remember(site.id, initialAccount?.id, visible) { mutableStateOf(initialAccount.toSiteAccountEditorValues()) }
    val scrollState = rememberScrollState()
    if (values.password.isNotBlank() || values.accessToken.isNotBlank() || values.apiKey.isNotBlank() || values.refreshToken.isNotBlank()) {
        SecureVisibleWindow()
    }

    OverlayDialog(
        show = visible,
        title = title,
        summary = site.name,
        onDismissRequest = { if (!submitting) onDismiss() },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DialogScrollableColumn(fraction = 0.68f, scrollState = scrollState) {
                operationError?.takeIf { it.isNotBlank() }?.let { OperationErrorCard(message = it) }
                TextField(
                    value = values.name,
                    onValueChange = { values = values.copy(name = it) },
                    label = stringResource(R.string.site_account_name_hint),
                    useLabelAsPlaceholder = true,
                    singleLine = true,
                    enabled = !submitting,
                    modifier = Modifier.fillMaxWidth(),
                )
                DialogLabel(text = stringResource(R.string.site_credential_type_label))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    credentialOptions(site.platform).forEach { type ->
                        ToolbarChip(
                            text = credentialLabel(type),
                            selected = values.credentialType == type,
                            onClick = { if (!submitting) values = values.copy(credentialType = type) },
                        )
                    }
                }
                when (values.credentialType) {
                    SiteCredentialType.UsernamePassword -> {
                        TextField(
                            value = values.username,
                            onValueChange = { values = values.copy(username = it) },
                            label = stringResource(R.string.site_username_hint),
                            useLabelAsPlaceholder = true,
                            singleLine = true,
                            enabled = !submitting,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        SecretTextField(
                            value = values.password,
                            onValueChange = { values = values.copy(password = it) },
                            label = stringResource(R.string.site_password_hint),
                            enabled = !submitting,
                        )
                    }
                    SiteCredentialType.ApiKey -> SecretTextField(
                        value = values.apiKey,
                        onValueChange = { values = values.copy(apiKey = it) },
                        label = stringResource(R.string.site_api_key_hint),
                        enabled = !submitting,
                    )
                    else -> {
                        SecretTextField(
                            value = values.accessToken,
                            onValueChange = { values = values.copy(accessToken = it) },
                            label = stringResource(R.string.site_access_token_hint),
                            enabled = !submitting,
                        )
                        SecretTextField(
                            value = values.refreshToken,
                            onValueChange = { values = values.copy(refreshToken = it) },
                            label = stringResource(R.string.site_refresh_token_hint),
                            enabled = !submitting,
                        )
                        TextField(
                            value = values.tokenExpiresAt,
                            onValueChange = { values = values.copy(tokenExpiresAt = it) },
                            label = stringResource(R.string.site_token_expires_at_hint),
                            useLabelAsPlaceholder = true,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            enabled = !submitting,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        TextField(
                            value = values.platformUserId,
                            onValueChange = { values = values.copy(platformUserId = it) },
                            label = stringResource(R.string.site_platform_user_id_hint),
                            useLabelAsPlaceholder = true,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            enabled = !submitting,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
                DialogLabel(text = stringResource(R.string.site_proxy_mode_label))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(ProxyMode.Inherit, ProxyMode.Direct, ProxyMode.System).forEach { mode ->
                        ToolbarChip(
                            text = proxyModeLabel(mode),
                            selected = values.proxyMode == mode,
                            onClick = { if (!submitting) values = values.copy(proxyMode = mode) },
                        )
                    }
                }
                SiteSwitchRow(stringResource(R.string.site_enabled_label), values.enabled, !submitting) { values = values.copy(enabled = it) }
                SiteSwitchRow(stringResource(R.string.site_auto_sync_label), values.autoSync, !submitting) { values = values.copy(autoSync = it) }
                SiteSwitchRow(stringResource(R.string.site_auto_checkin_label), values.autoCheckin, !submitting) { values = values.copy(autoCheckin = it) }
                SiteSwitchRow(stringResource(R.string.site_random_checkin_label), values.randomCheckin, !submitting) { values = values.copy(randomCheckin = it) }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    TextField(
                        value = values.checkinIntervalHours,
                        onValueChange = { values = values.copy(checkinIntervalHours = it) },
                        label = stringResource(R.string.site_checkin_interval_hint),
                        useLabelAsPlaceholder = true,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        enabled = !submitting,
                        modifier = Modifier.weight(1f),
                    )
                    TextField(
                        value = values.checkinRandomWindowMinutes,
                        onValueChange = { values = values.copy(checkinRandomWindowMinutes = it) },
                        label = stringResource(R.string.site_checkin_window_hint),
                        useLabelAsPlaceholder = true,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        enabled = !submitting,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            DialogButtons(
                submitting = submitting,
                canSubmit = canSubmitSiteAccount(values, initialAccount, submitting, sitePlatform = site.platform),
                onDismiss = onDismiss,
                onConfirm = { onConfirm(values) },
            )
        }
    }
}

@Composable
private fun EditableSiteHeaderList(
    headers: List<CustomHeader>,
    submitting: Boolean,
    onChange: (List<CustomHeader>) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DialogLabel(text = stringResource(R.string.site_custom_headers_label, headers.size))
        TextButton(
            text = stringResource(R.string.channel_add_row),
            enabled = !submitting,
            onClick = { onChange(headers + CustomHeader(headerKey = "", headerValue = "")) },
        )
    }
    headers.forEachIndexed { index, header ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextField(
                value = header.headerKey,
                onValueChange = { next -> onChange(headers.mapIndexed { i, item -> if (i == index) item.copy(headerKey = next) else item }) },
                label = stringResource(R.string.channel_header_key_hint),
                useLabelAsPlaceholder = true,
                singleLine = true,
                enabled = !submitting,
                modifier = Modifier.weight(1f),
            )
            TextField(
                value = header.headerValue,
                onValueChange = { next -> onChange(headers.mapIndexed { i, item -> if (i == index) item.copy(headerValue = next) else item }) },
                label = stringResource(R.string.channel_header_value_hint),
                useLabelAsPlaceholder = true,
                singleLine = true,
                enabled = !submitting,
                modifier = Modifier.weight(1f),
            )
            TextButton(
                text = stringResource(R.string.common_delete),
                enabled = !submitting && headers.size > 1,
                onClick = { onChange(headers.filterIndexed { i, _ -> i != index }) },
            )
        }
    }
}

@Composable
private fun ArchivedSitesDialog(
    visible: Boolean,
    loading: Boolean,
    sites: List<Site>,
    submitting: Boolean,
    onRestore: (Site) -> Unit,
    onDismiss: () -> Unit,
) {
    if (!visible) return
    OverlayDialog(
        show = visible,
        title = stringResource(R.string.site_archived_title),
        summary = stringResource(R.string.site_panel_summary),
        onDismissRequest = onDismiss,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            when {
                loading -> LoadingStateCard(title = stringResource(R.string.site_archived_title))
                sites.isEmpty() -> InlineEmptyCard(
                    title = stringResource(R.string.site_archived_title),
                    summary = stringResource(R.string.site_archived_empty),
                )
                else -> sites.forEach { site ->
                    AppListCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(text = site.name, style = MiuixTheme.textStyles.main, fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = "${platformLabel(site.platform)} · ${formatSiteTime(site.archivedAt)}",
                                    style = MiuixTheme.textStyles.body2,
                                    color = OctopusTokens.TextSecondary,
                                )
                            }
                            TextButton(text = stringResource(R.string.site_action_restore), enabled = !submitting, onClick = { onRestore(site) })
                        }
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(text = stringResource(R.string.action_close), onClick = onDismiss)
            }
        }
    }
}

@Composable
private fun DialogButtons(
    submitting: Boolean,
    canSubmit: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        TextButton(text = stringResource(R.string.common_cancel), enabled = !submitting, onClick = onDismiss)
        TextButton(
            text = stringResource(if (submitting) R.string.common_saving else R.string.common_confirm),
            enabled = canSubmit,
            onClick = onConfirm,
        )
    }
}

@Composable
private fun SecretTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    enabled: Boolean,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        useLabelAsPlaceholder = true,
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun SiteSwitchRow(
    label: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, style = MiuixTheme.textStyles.main)
        Switch(checked = checked, onCheckedChange = { if (enabled) onCheckedChange(it) })
    }
}

@Composable
private fun DialogLabel(text: String) {
    Text(
        text = text,
        style = MiuixTheme.textStyles.main,
        fontWeight = FontWeight.SemiBold,
        color = OctopusTokens.TextPrimary,
    )
}

@Composable
private fun importSourceLabel(source: SiteImportSource): String = when (source) {
    SiteImportSource.AllApiHub -> stringResource(R.string.site_import_source_all_api_hub)
    SiteImportSource.MetApi -> stringResource(R.string.site_import_source_metapi)
}

@Composable
private fun credentialLabel(type: String): String = when (type) {
    SiteCredentialType.UsernamePassword -> stringResource(R.string.site_credential_username_password)
    SiteCredentialType.ApiKey -> stringResource(R.string.site_credential_api_key)
    else -> stringResource(R.string.site_credential_access_token)
}

@Composable
private fun proxyModeLabel(mode: String): String = when (mode) {
    ProxyMode.System -> stringResource(R.string.site_proxy_system)
    ProxyMode.Inherit -> stringResource(R.string.site_proxy_inherit)
    ProxyMode.Pool -> stringResource(R.string.site_proxy_pool)
    else -> stringResource(R.string.site_proxy_direct)
}

private fun credentialOptions(platform: String): List<String> = when (platform) {
    SitePlatform.OpenAi, SitePlatform.Claude, SitePlatform.Gemini, SitePlatform.Sub2Api ->
        listOf(SiteCredentialType.AccessToken, SiteCredentialType.ApiKey)
    else -> SiteCredentialType.entries
}

private fun platformLabel(platform: String): String = when (platform) {
    SitePlatform.NewApi -> "New API"
    SitePlatform.AnyRouter -> "AnyRouter"
    SitePlatform.OneApi -> "One API"
    SitePlatform.OneHub -> "One Hub"
    SitePlatform.DoneHub -> "Done Hub"
    SitePlatform.Sub2Api -> "Sub2API"
    SitePlatform.OpenAi -> "OpenAI"
    SitePlatform.Claude -> "Claude"
    SitePlatform.Gemini -> "Gemini"
    else -> platform
}

private fun statusLabel(status: String): String = when (status) {
    "success" -> "success"
    "partial" -> "partial"
    "failed" -> "failed"
    "skipped" -> "skipped"
    else -> "idle"
}

private fun formatSiteTime(value: String?): String {
    if (value.isNullOrBlank()) return "-"
    val parsed = runCatching { java.time.Instant.parse(value).toEpochMilli() }.getOrNull()
    val millis = parsed ?: return value
    return SimpleDateFormat("MM/dd HH:mm", Locale.getDefault()).format(Date(millis))
}
