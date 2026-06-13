package com.elykia.octopus.feature.log

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.elykia.octopus.R
import com.elykia.octopus.core.data.model.ChannelAttempt
import com.elykia.octopus.core.data.model.Channel
import com.elykia.octopus.core.data.model.LogKeywordMode
import com.elykia.octopus.core.data.model.LogKeywordScope
import com.elykia.octopus.core.data.model.LogListFilter
import com.elykia.octopus.core.data.model.LogSiteActionTarget
import com.elykia.octopus.core.data.model.LogSiteActionTargets
import com.elykia.octopus.core.data.model.LogStatusFilter
import com.elykia.octopus.core.data.model.RelayLog
import com.elykia.octopus.core.designsystem.AppInfoChip
import com.elykia.octopus.core.designsystem.AppLazyPageScaffold
import com.elykia.octopus.core.designsystem.AppListCard
import com.elykia.octopus.core.designsystem.DangerConfirmDialog
import com.elykia.octopus.core.designsystem.DialogScrollableColumn
import com.elykia.octopus.core.designsystem.ErrorStateCard
import com.elykia.octopus.core.designsystem.InlineEmptyCard
import com.elykia.octopus.core.designsystem.LoadingStateCard
import com.elykia.octopus.core.designsystem.OctopusTones
import com.elykia.octopus.core.designsystem.OctopusTokens
import com.elykia.octopus.core.designsystem.OperationErrorCard
import com.elykia.octopus.core.designsystem.PageActionButton
import com.elykia.octopus.core.designsystem.SearchField
import com.elykia.octopus.core.designsystem.SecureVisibleWindow
import com.elykia.octopus.core.designsystem.ToolbarChip
import com.elykia.octopus.core.designsystem.formatCount
import com.elykia.octopus.core.designsystem.formatDurationMs
import com.elykia.octopus.core.designsystem.formatMoney
import com.elykia.octopus.core.designsystem.icons.AppMiuixIcons
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LogScreen(
    contentPadding: PaddingValues,
    viewModel: LogViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var confirmClear by remember { mutableStateOf(false) }
    var disableTargetToConfirm by remember { mutableStateOf<LogSiteActionTarget?>(null) }
    var searchVisible by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }

    val logs = uiState.logs
    val activeFilter = uiState.filter.hasActiveFilters()
    val searchActive = searchVisible || uiState.filter.keyword.isNotBlank()

    AppLazyPageScaffold(
        title = stringResource(R.string.log_title),
        actions = {
            PageActionButton(
                icon = AppMiuixIcons.Filter,
                contentDescription = stringResource(R.string.action_open_filter),
                enabled = !uiState.loading && !uiState.shouldShowPageError(),
                onClick = { showFilterDialog = true },
            )
            PageActionButton(
                icon = if (searchActive) AppMiuixIcons.Close else AppMiuixIcons.Search,
                contentDescription = stringResource(R.string.action_open_search),
                enabled = !uiState.loading && !uiState.shouldShowPageError(),
                onClick = {
                    if (searchActive) {
                        searchVisible = false
                        viewModel.updateFilter(uiState.filter.copy(keyword = ""))
                    } else {
                        searchVisible = true
                    }
                },
            )
            PageActionButton(
                icon = AppMiuixIcons.Delete,
                contentDescription = stringResource(R.string.log_toolbar_clear),
                enabled = !uiState.loading && !uiState.shouldShowPageError() && uiState.logs.isNotEmpty() && !uiState.clearing,
                onClick = { confirmClear = true },
            )
        },
        contentPadding = contentPadding,
    ) {
        when {
            uiState.loading -> item {
                LoadingStateCard(title = stringResource(R.string.log_title))
            }
            uiState.shouldShowPageError() -> item {
                ErrorStateCard(
                    message = uiState.error ?: stringResource(R.string.error_title),
                    onRetry = viewModel::refresh,
                )
            }
            else -> {
                if (searchActive) {
                    item {
                        SearchField(
                            value = uiState.filter.keyword,
                            onValueChange = {
                                viewModel.updateFilter(uiState.filter.copy(keyword = it))
                            },
                            hint = stringResource(R.string.log_search_hint),
                        )
                    }
                }
                if (activeFilter) {
                    item {
                        AppInfoChip(text = logFilterSummary(uiState.filter), icon = AppMiuixIcons.Filter, tint = OctopusTokens.Accent)
                    }
                }
                uiState.error?.takeIf { it.isNotBlank() }?.let { refreshError ->
                    item {
                        OperationErrorCard(message = refreshError)
                    }
                }
                uiState.clearError?.takeIf { it.isNotBlank() }?.let { clearError ->
                    item {
                        OperationErrorCard(message = clearError)
                    }
                }
                uiState.siteActionTargetsError?.takeIf { it.isNotBlank() }?.let { targetError ->
                    item {
                        OperationErrorCard(message = targetError)
                    }
                }
                uiState.modelDisableError?.takeIf { it.isNotBlank() }?.let { disableError ->
                    item {
                        OperationErrorCard(message = disableError, onDismiss = viewModel::clearModelDisableFeedback)
                    }
                }
                uiState.modelDisableMessage?.takeIf { it.isNotBlank() }?.let { disableMessage ->
                    item {
                        AppInfoChip(text = disableMessage, icon = AppMiuixIcons.Check, tint = OctopusTokens.Accent)
                    }
                }
                item {
                    AppInfoChip(
                        text = stringResource(if (uiState.streamConnected) R.string.log_stream_connected else R.string.log_stream_connecting),
                        icon = AppMiuixIcons.Sync,
                        tint = if (uiState.streamConnected) OctopusTokens.Accent else OctopusTokens.TextSecondary,
                    )
                }
                uiState.streamError?.takeIf { it.isNotBlank() }?.let { streamError ->
                    item {
                        OperationErrorCard(message = streamError)
                    }
                }
                when {
                    uiState.logs.isEmpty() -> item {
                        InlineEmptyCard(
                            title = stringResource(R.string.log_title),
                            summary = stringResource(if (activeFilter) R.string.log_search_empty else R.string.log_empty),
                        )
                    }
                    logs.isEmpty() -> item {
                        InlineEmptyCard(
                            title = stringResource(R.string.empty_title),
                            summary = stringResource(R.string.log_search_empty),
                        )
                    }
                    else -> {
                        items(logs, key = { it.id }) { log ->
                            LogRow(
                                log = log,
                                siteTargets = uiState.siteActionTargets[log.id],
                                modelDisableSubmitting = uiState.modelDisableSubmitting,
                                onClick = { viewModel.openDetail(log) },
                                onDisableModel = { target -> disableTargetToConfirm = target },
                            )
                        }
                        uiState.pagingError?.let { pagingError ->
                            item {
                                OperationErrorCard(message = pagingError)
                            }
                        }
                        if (uiState.hasMore) {
                            item {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                    ToolbarChip(
                                        text = stringResource(
                                            when {
                                                uiState.loadingMore -> R.string.common_loading
                                                uiState.pagingError != null -> R.string.common_retry
                                                else -> R.string.action_load_more
                                            },
                                        ),
                                        selected = false,
                                        onClick = if (uiState.loadingMore) null else viewModel::loadMore,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    DangerConfirmDialog(
        visible = confirmClear,
        title = stringResource(R.string.log_clear_title),
        summary = stringResource(R.string.log_clear_summary),
        onConfirm = {
            confirmClear = false
            viewModel.clear()
        },
        onDismiss = { confirmClear = false },
    )

    DangerConfirmDialog(
        visible = disableTargetToConfirm != null,
        title = stringResource(R.string.log_disable_model_title),
        summary = stringResource(
            R.string.log_disable_model_summary,
            disableTargetToConfirm?.siteName.orEmpty(),
            disableTargetToConfirm?.accountName.orEmpty(),
            disableTargetToConfirm?.groupName.orEmpty(),
            disableTargetToConfirm?.modelName.orEmpty(),
        ),
        onConfirm = {
            val target = disableTargetToConfirm
            if (target != null) {
                viewModel.disableSiteModel(target) {
                    disableTargetToConfirm = null
                }
            }
            disableTargetToConfirm = null
        },
        onDismiss = { disableTargetToConfirm = null },
    )

    LogDetailDialog(
        log = uiState.detailLog,
        loading = uiState.detailLoading,
        error = uiState.detailError,
        onDismiss = viewModel::closeDetail,
    )

    LogFilterDialog(
        visible = showFilterDialog,
        filter = uiState.filter,
        channels = uiState.channels,
        channelsLoading = uiState.channelsLoading,
        channelsError = uiState.channelsError,
        onApply = {
            showFilterDialog = false
            viewModel.updateFilter(it)
        },
        onRetryChannels = viewModel::loadChannels,
        onDismiss = { showFilterDialog = false },
    )
}

@Composable
private fun LogFilterDialog(
    visible: Boolean,
    filter: LogListFilter,
    channels: List<Channel>,
    channelsLoading: Boolean,
    channelsError: String?,
    onApply: (LogListFilter) -> Unit,
    onRetryChannels: () -> Unit,
    onDismiss: () -> Unit,
) {
    if (!visible) return
    var startTime by remember(visible, filter) { mutableStateOf(filter.startTime?.toString().orEmpty()) }
    var endTime by remember(visible, filter) { mutableStateOf(filter.endTime?.toString().orEmpty()) }
    var channelIds by remember(visible, filter) { mutableStateOf(filter.channelIds.filter { it > 0 }.toSet()) }
    var status by remember(visible, filter) { mutableStateOf(filter.status) }
    var keywordScope by remember(visible, filter) { mutableStateOf(filter.keywordScope) }
    var keywordMode by remember(visible, filter) { mutableStateOf(filter.keywordMode) }

    OverlayDialog(
        show = visible,
        title = stringResource(R.string.log_filter_title),
        summary = stringResource(R.string.log_filter_summary),
        onDismissRequest = onDismiss,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            LogFilterLabel(text = stringResource(R.string.log_filter_time_label))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LogTimestampField(
                    value = startTime,
                    onValueChange = { startTime = it },
                    label = stringResource(R.string.log_filter_start_time),
                    modifier = Modifier.weight(1f),
                )
                LogTimestampField(
                    value = endTime,
                    onValueChange = { endTime = it },
                    label = stringResource(R.string.log_filter_end_time),
                    modifier = Modifier.weight(1f),
                )
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ToolbarChip(
                    text = stringResource(R.string.log_filter_time_all),
                    selected = startTime.isBlank() && endTime.isBlank(),
                    onClick = {
                        startTime = ""
                        endTime = ""
                    },
                )
                listOf(
                    R.string.log_filter_time_1h to 3_600L,
                    R.string.log_filter_time_24h to 86_400L,
                    R.string.log_filter_time_7d to 604_800L,
                ).forEach { (labelRes, seconds) ->
                    ToolbarChip(
                        text = stringResource(labelRes),
                        selected = false,
                        onClick = {
                            startTime = ((System.currentTimeMillis() / 1000L) - seconds).coerceAtLeast(1L).toString()
                            endTime = ""
                        },
                    )
                }
            }
            LogFilterLabel(text = stringResource(R.string.log_filter_channel_label))
            when {
                channelsLoading -> Text(
                    text = stringResource(R.string.log_filter_channel_loading),
                    style = MiuixTheme.textStyles.body2,
                    color = OctopusTokens.TextSecondary,
                )
                channelsError != null -> Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = channelsError,
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.error,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(text = stringResource(R.string.common_retry), onClick = onRetryChannels)
                }
                channels.isEmpty() -> Text(
                    text = stringResource(R.string.log_filter_channel_empty),
                    style = MiuixTheme.textStyles.body2,
                    color = OctopusTokens.TextSecondary,
                )
                else -> FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ToolbarChip(
                        text = stringResource(R.string.log_filter_channel_all),
                        selected = channelIds.isEmpty(),
                        onClick = { channelIds = emptySet() },
                    )
                    channels.forEach { channel ->
                        ToolbarChip(
                            text = channel.name.ifBlank { "#${channel.id}" },
                            selected = channel.id in channelIds,
                            onClick = {
                                channelIds = if (channel.id in channelIds) {
                                    channelIds - channel.id
                                } else {
                                    channelIds + channel.id
                                }
                            },
                        )
                    }
                }
            }
            LogFilterLabel(text = stringResource(R.string.log_filter_status_label))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(LogStatusFilter.All, LogStatusFilter.Success, LogStatusFilter.Error).forEach { option ->
                    ToolbarChip(
                        text = logStatusFilterLabel(option),
                        selected = status == option,
                        onClick = { status = option },
                    )
                }
            }
            LogFilterLabel(text = stringResource(R.string.log_filter_scope_label))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(LogKeywordScope.Default, LogKeywordScope.Content).forEach { option ->
                    ToolbarChip(
                        text = logKeywordScopeLabel(option),
                        selected = keywordScope == option,
                        onClick = { keywordScope = option },
                    )
                }
            }
            LogFilterLabel(text = stringResource(R.string.log_filter_mode_label))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(LogKeywordMode.Default, LogKeywordMode.Prefix, LogKeywordMode.Exact, LogKeywordMode.Contains).forEach { option ->
                    ToolbarChip(
                        text = logKeywordModeLabel(option),
                        selected = keywordMode == option,
                        onClick = { keywordMode = option },
                    )
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(text = stringResource(R.string.common_cancel), onClick = onDismiss)
                TextButton(
                    text = stringResource(R.string.common_confirm),
                    onClick = {
                        onApply(
                            filter.copy(
                                startTime = startTime.toPositiveLongOrNull(),
                                endTime = endTime.toPositiveLongOrNull(),
                                channelIds = channelIds.toList().sorted(),
                                status = status,
                                keywordScope = keywordScope,
                                keywordMode = keywordMode,
                            )
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun LogTimestampField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    TextField(
        value = value,
        onValueChange = { next -> onValueChange(next.filter { it.isDigit() }) },
        label = label,
        modifier = modifier,
        useLabelAsPlaceholder = true,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    )
}

@Composable
private fun LogDetailDialog(
    log: RelayLog?,
    loading: Boolean,
    error: String?,
    onDismiss: () -> Unit,
) {
    if (log == null && !loading && error == null) return
    val visibleContent = listOfNotNull(log?.requestContent, log?.responseContent, log?.error)
        .any { it.isNotBlank() }
    if (visibleContent) {
        SecureVisibleWindow()
    }
    val scrollState = rememberScrollState()

    OverlayDialog(
        show = true,
        title = stringResource(R.string.log_detail_title),
        summary = log?.requestModelName ?: stringResource(R.string.log_detail_summary),
        onDismissRequest = onDismiss,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            when {
                loading && log?.requestContent.isNullOrBlank() && log?.responseContent.isNullOrBlank() ->
                    LoadingStateCard(title = stringResource(R.string.log_detail_title))
                error != null -> OperationErrorCard(message = error)
            }

            log?.let { detail ->
                DialogScrollableColumn(fraction = 0.72f, scrollState = scrollState) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        LogDetailBlock(
                            title = stringResource(R.string.log_request_content),
                            content = detail.requestContent,
                            fallback = stringResource(R.string.log_no_request_content),
                        )
                        LogDetailBlock(
                            title = stringResource(R.string.log_response_content),
                            content = detail.responseContent,
                            fallback = stringResource(R.string.log_no_response_content),
                        )
                        if (detail.error.isNotBlank()) {
                            LogDetailBlock(
                                title = stringResource(R.string.log_error_content),
                                content = detail.error,
                                fallback = "",
                                error = true,
                            )
                        }
                        LogTokenBreakdownBlock(log = detail)
                        LogAttemptsBlock(attempts = detail.attempts)
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
private fun LogDetailBlock(
    title: String,
    content: String,
    fallback: String,
    error: Boolean = false,
) {
    AppListCard(padding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = title,
                style = MiuixTheme.textStyles.main,
                fontWeight = FontWeight.SemiBold,
                color = OctopusTokens.TextPrimary,
            )
            Text(
                text = content.ifBlank { fallback },
                style = MiuixTheme.textStyles.body2,
                color = if (error) MiuixTheme.colorScheme.error else OctopusTokens.TextSecondary,
                fontFamily = FontFamily.Monospace,
            )
        }
    }
}

@Composable
private fun LogRow(
    log: RelayLog,
    siteTargets: LogSiteActionTargets?,
    modelDisableSubmitting: Boolean,
    onClick: () -> Unit,
    onDisableModel: (LogSiteActionTarget) -> Unit,
) {
    val hasError = log.error.isNotBlank()
    val statusColor = if (hasError) MiuixTheme.colorScheme.error else OctopusTokens.Accent
    val disableTargets = log.disableTargets(siteTargets)

    AppListCard(
        onClick = onClick,
        padding = PaddingValues(horizontal = 18.dp, vertical = 18.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            LogProviderMark(
                label = log.channelName.ifBlank { log.requestModelName },
                hasError = hasError,
                color = statusColor,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                LogRouteHeader(log = log)
                LogDiagnosticChips(log = log)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    LogMetricLine(
                        icon = AppMiuixIcons.Time,
                        label = stringResource(R.string.log_metric_time),
                        value = formatLogTime(log.time),
                        color = OctopusTokens.Accent,
                    )
                    LogMetricLine(
                        icon = AppMiuixIcons.Success,
                        label = stringResource(R.string.log_metric_first_token),
                        value = formatDurationMs(log.ftut.toLong()),
                        color = OctopusTones.Orange,
                    )
                    LogMetricLine(
                        icon = AppMiuixIcons.Toggle,
                        label = stringResource(R.string.log_metric_total_time),
                        value = formatDurationMs(log.useTime.toLong()),
                        color = OctopusTokens.TextSecondary,
                    )
                    LogMetricLine(
                        icon = AppMiuixIcons.ArrowDown,
                        label = stringResource(R.string.log_metric_input),
                        value = formatCount(log.headlineInputTokens().toLong()),
                        color = OctopusTokens.TextSecondary,
                    )
                    LogMetricLine(
                        icon = AppMiuixIcons.ArrowUp,
                        label = stringResource(R.string.log_metric_output),
                        value = formatCount(log.outputTokens.toLong()),
                        color = OctopusTokens.TextSecondary,
                    )
                    LogMetricLine(
                        icon = AppMiuixIcons.Cost,
                        label = stringResource(R.string.log_metric_cost),
                        value = formatMoney(log.cost),
                        color = OctopusTokens.Accent,
                        emphasize = true,
                    )
                }
                if (hasError) {
                    Text(
                        text = log.error,
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.error,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (disableTargets.isNotEmpty()) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        disableTargets.take(3).forEach { target ->
                            ToolbarChip(
                                text = stringResource(R.string.log_action_disable_model, target.modelName),
                                selected = false,
                                onClick = if (modelDisableSubmitting) null else ({ onDisableModel(target) }),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LogDiagnosticChips(log: RelayLog) {
    val apiKeyName = log.requestApiKeyName?.trim().orEmpty()
    val attemptCount = log.displayAttemptCount()
    val wsLabels = logWsLabels(log)

    if (apiKeyName.isBlank() && attemptCount <= 0 && wsLabels.isEmpty()) return

    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (apiKeyName.isNotBlank()) {
            AppInfoChip(
                text = stringResource(R.string.log_request_api_key, apiKeyName),
                icon = AppMiuixIcons.ApiKey,
            )
        }
        if (attemptCount > 0) {
            AppInfoChip(
                text = stringResource(R.string.log_attempt_count, attemptCount),
                icon = AppMiuixIcons.Sync,
                tint = if (log.error.isBlank()) OctopusTokens.Accent else MiuixTheme.colorScheme.error,
            )
        }
        wsLabels.forEach { label ->
            AppInfoChip(
                text = label,
                icon = AppMiuixIcons.Sync,
                tint = OctopusTones.Orange,
            )
        }
    }
}

@Composable
private fun LogProviderMark(
    label: String,
    hasError: Boolean,
    color: Color,
) {
    Box(
        modifier = Modifier
            .size(54.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(color.copy(alpha = if (hasError) 0.12f else 0.16f))
            .border(1.dp, color.copy(alpha = 0.32f), RoundedCornerShape(18.dp)),
        contentAlignment = Alignment.Center,
    ) {
        if (hasError) {
            Icon(
                imageVector = AppMiuixIcons.Close,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(24.dp),
            )
        } else {
            Text(
                text = label.firstOrNull()?.uppercaseChar()?.toString() ?: "#",
                style = MiuixTheme.textStyles.title3,
                fontWeight = FontWeight.Bold,
                color = color,
            )
        }
    }
}

@Composable
private fun LogTokenBreakdownBlock(log: RelayLog) {
    if (!log.hasInputTokenDetails()) return

    AppListCard(padding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.log_token_breakdown_title),
                style = MiuixTheme.textStyles.main,
                fontWeight = FontWeight.SemiBold,
                color = OctopusTokens.TextPrimary,
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AppInfoChip(
                    text = stringResource(R.string.log_token_transport_input, formatOptionalCount(log.transportInputTokens)),
                    icon = AppMiuixIcons.ArrowDown,
                )
                AppInfoChip(
                    text = stringResource(R.string.log_token_bill_input, formatOptionalCount(log.billInputTokens)),
                    icon = AppMiuixIcons.Cost,
                )
                AppInfoChip(
                    text = stringResource(R.string.log_token_cache_read, formatOptionalCount(log.cacheReadTokens)),
                    icon = AppMiuixIcons.Token,
                )
                AppInfoChip(
                    text = stringResource(R.string.log_token_cache_write, formatOptionalCount(log.cacheWriteTokens)),
                    icon = AppMiuixIcons.Token,
                )
            }
        }
    }
}

@Composable
private fun LogAttemptsBlock(attempts: List<ChannelAttempt>) {
    if (attempts.isEmpty()) return
    val summaries = remember(attempts) { mergeAdjacentAttempts(attempts) }

    AppListCard(padding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = stringResource(R.string.log_attempts_title),
                style = MiuixTheme.textStyles.main,
                fontWeight = FontWeight.SemiBold,
                color = OctopusTokens.TextPrimary,
            )
            summaries.forEach { summary ->
                LogAttemptRow(summary = summary)
            }
        }
    }
}

@Composable
private fun LogAttemptRow(summary: LogAttemptSummary) {
    val statusColor = logAttemptStatusColor(summary.status)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(statusColor.copy(alpha = 0.08f))
            .border(1.dp, statusColor.copy(alpha = 0.18f), RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp, vertical = 9.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AppInfoChip(
                text = logAttemptStatusLabel(summary.status),
                icon = if (summary.status == "success") AppMiuixIcons.Check else AppMiuixIcons.Close,
                tint = statusColor,
            )
            Text(
                text = "${summary.channelName} / ${summary.modelName}",
                style = MiuixTheme.textStyles.body2,
                fontWeight = FontWeight.Medium,
                color = OctopusTokens.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AppInfoChip(
                text = stringResource(
                    R.string.log_attempt_duration,
                    logAttemptNumber(summary),
                    formatDurationMs(summary.durationMs.toLong()),
                ),
                icon = AppMiuixIcons.Time,
            )
            if (summary.repeat > 1) {
                AppInfoChip(
                    text = stringResource(R.string.log_attempt_repeat, summary.repeat),
                    icon = AppMiuixIcons.Sync,
                )
            }
            if (summary.sticky) {
                AppInfoChip(
                    text = stringResource(R.string.log_attempt_sticky),
                    icon = AppMiuixIcons.Info,
                    tint = OctopusTones.Orange,
                )
            }
        }
        summary.message?.takeIf { it.isNotBlank() }?.let { message ->
            Text(
                text = message,
                style = MiuixTheme.textStyles.body2,
                color = if (summary.status == "failed") MiuixTheme.colorScheme.error else OctopusTokens.TextSecondary,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun LogRouteHeader(
    log: RelayLog,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = log.requestModelName.ifBlank { stringResource(R.string.common_unknown) },
            style = MiuixTheme.textStyles.main,
            fontWeight = FontWeight.SemiBold,
            color = OctopusTokens.TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = AppMiuixIcons.ArrowRight,
                contentDescription = null,
                tint = OctopusTokens.TextSecondary,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = log.actualModelName.ifBlank { stringResource(R.string.common_unknown) },
                style = MiuixTheme.textStyles.main,
                color = OctopusTokens.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
        }
        LogChannelPill(
            text = log.channelName.ifBlank { stringResource(R.string.common_unknown) },
        )
    }
}

@Composable
private fun LogChannelPill(
    text: String,
) {
    Box(
        modifier = Modifier
            .widthIn(max = 104.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(OctopusTokens.PrimarySoft)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MiuixTheme.textStyles.body2,
            color = OctopusTokens.Accent,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun LogMetricLine(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color,
    emphasize: Boolean = false,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = "$label $value",
            style = MiuixTheme.textStyles.body2,
            color = if (emphasize) color else OctopusTokens.TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun LogFilterLabel(text: String) {
    Text(
        text = text,
        style = MiuixTheme.textStyles.main,
        fontWeight = FontWeight.SemiBold,
        color = OctopusTokens.TextPrimary,
    )
}

@Composable
private fun logStatusFilterLabel(status: String): String = when (status) {
    LogStatusFilter.Success -> stringResource(R.string.log_filter_status_success)
    LogStatusFilter.Error -> stringResource(R.string.log_filter_status_error)
    else -> stringResource(R.string.log_filter_status_all)
}

@Composable
private fun logKeywordScopeLabel(scope: String): String = when (scope) {
    LogKeywordScope.Content -> stringResource(R.string.log_filter_scope_content)
    else -> stringResource(R.string.log_filter_scope_default)
}

@Composable
private fun logKeywordModeLabel(mode: String): String = when (mode) {
    LogKeywordMode.Prefix -> stringResource(R.string.log_filter_mode_prefix)
    LogKeywordMode.Exact -> stringResource(R.string.log_filter_mode_exact)
    LogKeywordMode.Contains -> stringResource(R.string.log_filter_mode_contains)
    else -> stringResource(R.string.log_filter_mode_default)
}

@Composable
private fun logFilterSummary(filter: LogListFilter): String {
    val parts = buildList {
        if (filter.startTime != null || filter.endTime != null) {
            add(stringResource(R.string.log_filter_time_summary, filter.startTime?.toString() ?: "-", filter.endTime?.toString() ?: "-"))
        }
        if (filter.channelIds.any { it > 0 }) {
            add(stringResource(R.string.log_filter_channel_summary, filter.channelIds.count { it > 0 }))
        }
        if (filter.status != LogStatusFilter.All) add(logStatusFilterLabel(filter.status))
        if (filter.keyword.isNotBlank()) add(stringResource(R.string.log_filter_keyword_summary, filter.keyword.trim()))
        if (filter.keywordScope != LogKeywordScope.Default) add(logKeywordScopeLabel(filter.keywordScope))
        if (filter.keywordMode != LogKeywordMode.Default) add(logKeywordModeLabel(filter.keywordMode))
    }
    return parts.joinToString(" · ").ifBlank { stringResource(R.string.log_filter_active) }
}

@Composable
private fun logWsLabels(log: RelayLog): List<String> = buildList {
    when (log.wsMode) {
        "continuation" -> add(stringResource(R.string.log_stream_ws_continuation))
        "replay" -> add(stringResource(R.string.log_stream_ws_replay))
        "fresh" -> add(stringResource(R.string.log_stream_ws))
        null -> if (log.usedWs) add(stringResource(R.string.log_stream_ws))
    }
    when (log.wsExecMode) {
        "passthrough" -> add(stringResource(R.string.log_stream_ws_passthrough))
        "transform" -> add(stringResource(R.string.log_stream_ws_transform))
    }
    when (log.wsRecovery) {
        "reconnect" -> add(stringResource(R.string.log_stream_ws_reconnect))
        "replay" -> add(stringResource(R.string.log_stream_ws_replay))
        "downgrade" -> add(stringResource(R.string.log_stream_ws_downgrade))
    }
}

@Composable
private fun logAttemptStatusLabel(status: String): String = when (status) {
    "success" -> stringResource(R.string.log_attempt_success)
    "circuit_break" -> stringResource(R.string.log_attempt_circuit_break)
    "skipped" -> stringResource(R.string.log_attempt_skipped)
    else -> stringResource(R.string.log_attempt_failed)
}

@Composable
private fun logAttemptStatusColor(status: String): Color = when (status) {
    "success" -> OctopusTokens.Accent
    "circuit_break" -> OctopusTones.Orange
    "skipped" -> OctopusTokens.TextSecondary
    else -> MiuixTheme.colorScheme.error
}

@Composable
private fun logAttemptNumber(summary: LogAttemptSummary): String =
    if (summary.firstAttemptNum != summary.lastAttemptNum) {
        stringResource(R.string.log_attempt_range, summary.firstAttemptNum, summary.lastAttemptNum)
    } else {
        stringResource(R.string.log_attempt_number, summary.firstAttemptNum)
    }

private fun formatOptionalCount(value: Int?): String =
    value?.let { formatCount(it.toLong()) } ?: "-"

private fun RelayLog.disableTargets(siteTargets: LogSiteActionTargets?): List<LogSiteActionTarget> {
    if (siteTargets == null) return emptyList()
    val attemptTargets = attempts.mapIndexedNotNull { index, attempt ->
        val target = siteTargets.attemptTargets.getOrNull(index)
        target?.takeIf {
            attempt.status == "failed" && it.canDisableModel && !it.modelDisabled
        }
    }
    val legacyTarget = siteTargets.legacyErrorTarget?.takeIf {
        attempts.isEmpty() && error.isNotBlank() && it.canDisableModel && !it.modelDisabled
    }
    return (attemptTargets + listOfNotNull(legacyTarget))
        .distinctBy { it.disableTargetKey() }
}

private fun LogSiteActionTarget.disableTargetKey(): String =
    "$siteId\u0000$accountId\u0000$groupKey\u0000$modelName"

private fun String.toPositiveLongOrNull(): Long? =
    toLongOrNull()?.takeIf { it > 0L }

private fun formatLogTime(timestampSeconds: Long): String {
    if (timestampSeconds <= 0L) return "-"
    val formatter = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
    return formatter.format(Date(timestampSeconds * 1000))
}
