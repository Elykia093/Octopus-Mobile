package com.elykia.octopus.feature.proxypool

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
import com.elykia.octopus.core.data.model.ProxyConfiguration
import com.elykia.octopus.core.data.model.ProxyConfigurationReference
import com.elykia.octopus.core.data.model.ProxyConfigurationReferenceType
import com.elykia.octopus.core.designsystem.AppInfoChip
import com.elykia.octopus.core.designsystem.AppLazyPageScaffold
import com.elykia.octopus.core.designsystem.AppListCard
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
import com.elykia.octopus.core.designsystem.icons.AppMiuixIcons
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun ProxyPoolScreen(
    contentPadding: PaddingValues,
    viewModel: ProxyPoolViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var searchVisible by remember { mutableStateOf(false) }
    var searchTerm by remember { mutableStateOf("") }
    var showCreateDialog by remember { mutableStateOf(false) }
    var editingProxyId by remember { mutableStateOf<Int?>(null) }
    var referencesProxyId by remember { mutableStateOf<Int?>(null) }
    var proxyToDelete by remember { mutableStateOf<ProxyConfiguration?>(null) }

    val query = searchTerm.trim()
    val proxies = uiState.proxies.filter { proxy ->
        query.isBlank() ||
            proxy.name.contains(query, ignoreCase = true) ||
            proxy.url.contains(query, ignoreCase = true) ||
            proxy.remark.contains(query, ignoreCase = true)
    }
    val editingProxy = editingProxyId?.let { id -> uiState.proxies.firstOrNull { it.id == id } }
    val referencesProxy = referencesProxyId?.let { id -> uiState.proxies.firstOrNull { it.id == id } }

    AppLazyPageScaffold(
        title = stringResource(R.string.proxy_pool_title),
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
            PageActionButton(
                icon = AppMiuixIcons.Add,
                contentDescription = stringResource(R.string.action_create),
                enabled = !uiState.loading && !uiState.shouldShowPageError() && !uiState.submitting,
                onClick = {
                    viewModel.clearOperationFeedback()
                    showCreateDialog = true
                },
            )
        },
        contentPadding = contentPadding,
    ) {
        when {
            uiState.loading -> item { LoadingStateCard(title = stringResource(R.string.proxy_pool_title)) }
            uiState.shouldShowPageError() -> item {
                ErrorStateCard(
                    message = uiState.error ?: stringResource(R.string.error_title),
                    onRetry = viewModel::refresh,
                )
            }
            else -> {
                if (uiState.proxies.isNotEmpty() && searchVisible) {
                    item {
                        SearchField(
                            value = searchTerm,
                            onValueChange = { searchTerm = it },
                            hint = stringResource(R.string.proxy_pool_search_hint),
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
                    uiState.proxies.isEmpty() -> item {
                        InlineEmptyCard(
                            title = stringResource(R.string.proxy_pool_title),
                            summary = stringResource(R.string.proxy_pool_empty),
                        )
                    }
                    proxies.isEmpty() -> item {
                        InlineEmptyCard(
                            title = stringResource(R.string.empty_title),
                            summary = stringResource(R.string.proxy_pool_search_empty),
                        )
                    }
                    else -> items(proxies, key = { it.id }) { proxy ->
                        ProxyConfigurationCard(
                            proxy = proxy,
                            submitting = uiState.submitting,
                            testing = uiState.testingKey == "saved-${proxy.id}",
                            onTest = { viewModel.testSavedProxy(proxy) },
                            onReferences = {
                                referencesProxyId = proxy.id
                                viewModel.loadReferences(proxy)
                            },
                            onToggle = { enabled -> viewModel.setProxyEnabled(proxy, enabled) },
                            onEdit = {
                                viewModel.clearOperationFeedback()
                                editingProxyId = proxy.id
                            },
                            onDelete = { proxyToDelete = proxy },
                        )
                    }
                }
            }
        }
    }

    ProxyConfigurationEditorDialog(
        visible = showCreateDialog,
        title = stringResource(R.string.proxy_pool_create_title),
        initialProxy = null,
        submitting = uiState.submitting,
        testing = uiState.testingKey == "draft",
        operationError = uiState.operationError,
        onTest = viewModel::testDraftProxy,
        onConfirm = { values ->
            viewModel.createProxy(values) {
                showCreateDialog = false
                viewModel.clearOperationFeedback()
            }
        },
        onDismiss = {
            if (!uiState.submitting) {
                showCreateDialog = false
                viewModel.clearOperationFeedback()
            }
        },
    )

    ProxyConfigurationEditorDialog(
        visible = editingProxy != null,
        title = stringResource(R.string.proxy_pool_edit_title),
        initialProxy = editingProxy,
        submitting = uiState.submitting,
        testing = uiState.testingKey == "draft",
        operationError = uiState.operationError,
        onTest = viewModel::testDraftProxy,
        onConfirm = { values ->
            editingProxy?.let { proxy ->
                viewModel.updateProxy(proxy, values) {
                    editingProxyId = null
                    viewModel.clearOperationFeedback()
                }
            }
        },
        onDismiss = {
            if (!uiState.submitting) {
                editingProxyId = null
                viewModel.clearOperationFeedback()
            }
        },
    )

    ProxyReferencesDialog(
        visible = referencesProxy != null,
        proxy = referencesProxy,
        loading = uiState.referencesLoading,
        error = uiState.referencesError,
        references = uiState.references,
        onDismiss = {
            referencesProxyId = null
            viewModel.clearReferences()
        },
    )

    DangerConfirmDialog(
        visible = proxyToDelete != null,
        title = stringResource(R.string.proxy_pool_delete_title),
        summary = stringResource(R.string.proxy_pool_delete_summary, proxyToDelete?.name.orEmpty()),
        onConfirm = {
            proxyToDelete?.let(viewModel::deleteProxy)
            proxyToDelete = null
        },
        onDismiss = { proxyToDelete = null },
    )
}

@Composable
private fun ProxyConfigurationCard(
    proxy: ProxyConfiguration,
    submitting: Boolean,
    testing: Boolean,
    onTest: () -> Unit,
    onReferences: () -> Unit,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    AppListCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SoftIconTile(
                    icon = AppMiuixIcons.Proxy,
                    contentDescription = proxy.name,
                    tint = if (proxy.enabled) OctopusTokens.Accent else OctopusTokens.TextSecondary,
                )
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = proxy.name.ifBlank { maskProxyUrl(proxy.url) },
                        style = MiuixTheme.textStyles.title3,
                        fontWeight = FontWeight.SemiBold,
                        color = OctopusTokens.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = maskProxyUrl(proxy.url),
                        style = MiuixTheme.textStyles.body2,
                        color = OctopusTokens.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Switch(checked = proxy.enabled, onCheckedChange = { if (!submitting) onToggle(it) })
            }
            if (proxy.remark.isNotBlank()) {
                Text(
                    text = proxy.remark,
                    style = MiuixTheme.textStyles.body2,
                    color = OctopusTokens.TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ToolbarChip(
                    text = stringResource(if (proxy.enabled) R.string.common_enabled else R.string.common_disabled),
                    selected = proxy.enabled,
                )
                ToolbarChip(
                    text = stringResource(R.string.proxy_pool_reference_count, proxy.referenceCount),
                    onClick = onReferences,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    text = stringResource(if (testing) R.string.proxy_pool_testing else R.string.proxy_pool_action_test),
                    enabled = !submitting && !testing,
                    onClick = onTest,
                )
                TextButton(
                    text = stringResource(R.string.action_edit),
                    enabled = !submitting,
                    onClick = onEdit,
                )
                TextButton(
                    text = stringResource(R.string.common_delete),
                    enabled = !submitting && proxy.referenceCount == 0,
                    onClick = onDelete,
                )
            }
        }
    }
}

@Composable
private fun ProxyConfigurationEditorDialog(
    visible: Boolean,
    title: String,
    initialProxy: ProxyConfiguration?,
    submitting: Boolean,
    testing: Boolean,
    operationError: String?,
    onTest: (ProxyPoolEditorValues, String) -> Unit,
    onConfirm: (ProxyPoolEditorValues) -> Unit,
    onDismiss: () -> Unit,
) {
    if (!visible) return

    var values by remember(initialProxy?.id, visible) { mutableStateOf(initialProxy.toEditorValues()) }
    var testUrl by remember(initialProxy?.id, visible) { mutableStateOf(DEFAULT_PROXY_TEST_URL) }
    val scrollState = rememberScrollState()

    if ("@" in values.url) {
        SecureVisibleWindow()
    }

    OverlayDialog(
        show = visible,
        title = title,
        summary = stringResource(R.string.proxy_pool_panel_summary),
        onDismissRequest = { if (!submitting) onDismiss() },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DialogScrollableColumn(
                fraction = 0.68f,
                scrollState = scrollState,
            ) {
                operationError?.takeIf { it.isNotBlank() }?.let { OperationErrorCard(message = it) }
                TextField(
                    value = values.name,
                    onValueChange = { values = values.copy(name = it) },
                    label = stringResource(R.string.proxy_pool_name_hint),
                    useLabelAsPlaceholder = true,
                    singleLine = true,
                    enabled = !submitting,
                    modifier = Modifier.fillMaxWidth(),
                )
                TextField(
                    value = values.url,
                    onValueChange = { values = values.copy(url = it) },
                    label = stringResource(R.string.proxy_pool_url_hint),
                    useLabelAsPlaceholder = true,
                    singleLine = true,
                    enabled = !submitting,
                    modifier = Modifier.fillMaxWidth(),
                )
                TextField(
                    value = values.remark,
                    onValueChange = { values = values.copy(remark = it) },
                    label = stringResource(R.string.proxy_pool_remark_hint),
                    useLabelAsPlaceholder = true,
                    singleLine = false,
                    maxLines = 3,
                    enabled = !submitting,
                    modifier = Modifier.fillMaxWidth(),
                )
                ProxySwitchRow(
                    label = stringResource(R.string.proxy_pool_enabled_label),
                    checked = values.enabled,
                    enabled = !submitting,
                    onCheckedChange = { values = values.copy(enabled = it) },
                )
                TextField(
                    value = testUrl,
                    onValueChange = { testUrl = it },
                    label = stringResource(R.string.proxy_pool_test_url_hint),
                    useLabelAsPlaceholder = true,
                    singleLine = true,
                    enabled = !submitting && !testing,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
            ) {
                TextButton(
                    text = stringResource(if (testing) R.string.proxy_pool_testing else R.string.proxy_pool_test_draft),
                    enabled = !submitting && !testing && values.url.isValidProxyUrl(),
                    onClick = { onTest(values, testUrl) },
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(text = stringResource(R.string.common_cancel), enabled = !submitting, onClick = onDismiss)
                TextButton(
                    text = stringResource(if (submitting) R.string.common_saving else R.string.common_confirm),
                    enabled = canSubmitProxyConfiguration(values, submitting),
                    onClick = { onConfirm(values) },
                )
            }
        }
    }
}

@Composable
private fun ProxyReferencesDialog(
    visible: Boolean,
    proxy: ProxyConfiguration?,
    loading: Boolean,
    error: String?,
    references: List<ProxyConfigurationReference>,
    onDismiss: () -> Unit,
) {
    if (!visible || proxy == null) return

    OverlayDialog(
        show = visible,
        title = stringResource(R.string.proxy_pool_references_title),
        summary = stringResource(R.string.proxy_pool_references_summary, proxy.name),
        onDismissRequest = onDismiss,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            when {
                loading -> LoadingStateCard(title = stringResource(R.string.proxy_pool_references_title))
                !error.isNullOrBlank() -> OperationErrorCard(message = error)
                references.isEmpty() -> InlineEmptyCard(
                    title = stringResource(R.string.proxy_pool_references_title),
                    summary = stringResource(R.string.proxy_pool_references_empty),
                )
                else -> references.forEach { reference ->
                    ProxyReferenceCard(reference = reference)
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(text = stringResource(R.string.action_close), onClick = onDismiss)
            }
        }
    }
}

@Composable
private fun ProxyReferenceCard(reference: ProxyConfigurationReference) {
    AppListCard {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = proxyReferenceTitle(reference),
                style = MiuixTheme.textStyles.main,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${proxyReferenceTypeLabel(reference)} · ${proxyReferenceLocation(reference)}",
                style = MiuixTheme.textStyles.body2,
                color = OctopusTokens.TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ProxySwitchRow(
    label: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, style = MiuixTheme.textStyles.main)
        Switch(checked = checked, onCheckedChange = { if (enabled) onCheckedChange(it) })
    }
}

@Composable
private fun proxyReferenceTitle(reference: ProxyConfigurationReference): String = when (reference.type) {
    ProxyConfigurationReferenceType.Site -> reference.siteName ?: "#${reference.siteId ?: 0}"
    ProxyConfigurationReferenceType.SiteAccount -> reference.siteAccountName ?: "#${reference.siteAccountId ?: 0}"
    ProxyConfigurationReferenceType.ManagedChannel -> {
        val channel = reference.channelName ?: "#${reference.channelId ?: 0}"
        stringResource(R.string.proxy_pool_reference_managed_channel_title, channel)
    }
    ProxyConfigurationReferenceType.Channel -> reference.channelName ?: "#${reference.channelId ?: 0}"
    else -> stringResource(R.string.common_unknown)
}

@Composable
private fun proxyReferenceTypeLabel(reference: ProxyConfigurationReference): String = when (reference.type) {
    ProxyConfigurationReferenceType.Site -> stringResource(R.string.proxy_pool_reference_type_site)
    ProxyConfigurationReferenceType.SiteAccount -> stringResource(R.string.proxy_pool_reference_type_site_account)
    ProxyConfigurationReferenceType.ManagedChannel -> stringResource(R.string.proxy_pool_reference_type_managed_channel)
    ProxyConfigurationReferenceType.Channel -> stringResource(R.string.proxy_pool_reference_type_channel)
    else -> stringResource(R.string.proxy_pool_reference_type_reference)
}

@Composable
private fun proxyReferenceLocation(reference: ProxyConfigurationReference): String = when (reference.type) {
    ProxyConfigurationReferenceType.Site -> if (reference.siteArchived) {
        stringResource(R.string.proxy_pool_reference_location_archived_site)
    } else {
        stringResource(R.string.proxy_pool_reference_location_site)
    }
    ProxyConfigurationReferenceType.SiteAccount -> {
        val site = reference.siteName
        if (site.isNullOrBlank()) {
            stringResource(R.string.proxy_pool_reference_location_site_account)
        } else {
            stringResource(R.string.proxy_pool_reference_location_site_named, site)
        }
    }
    ProxyConfigurationReferenceType.ManagedChannel -> {
        val site = reference.siteName
        if (site.isNullOrBlank()) {
            stringResource(R.string.proxy_pool_reference_location_managed_channel)
        } else {
            stringResource(R.string.proxy_pool_reference_location_managed_channel_under_site, site)
        }
    }
    ProxyConfigurationReferenceType.Channel -> stringResource(R.string.proxy_pool_reference_location_channel)
    else -> stringResource(R.string.common_unknown)
}
