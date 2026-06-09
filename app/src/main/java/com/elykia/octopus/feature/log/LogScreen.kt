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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.elykia.octopus.R
import com.elykia.octopus.core.data.model.LogKeywordMode
import com.elykia.octopus.core.data.model.LogKeywordScope
import com.elykia.octopus.core.data.model.LogListFilter
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
                                onClick = { viewModel.openDetail(log) },
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

    LogDetailDialog(
        log = uiState.detailLog,
        loading = uiState.detailLoading,
        error = uiState.detailError,
        onDismiss = viewModel::closeDetail,
    )

    LogFilterDialog(
        visible = showFilterDialog,
        filter = uiState.filter,
        onApply = {
            showFilterDialog = false
            viewModel.updateFilter(it)
        },
        onDismiss = { showFilterDialog = false },
    )
}

@Composable
private fun LogFilterDialog(
    visible: Boolean,
    filter: LogListFilter,
    onApply: (LogListFilter) -> Unit,
    onDismiss: () -> Unit,
) {
    if (!visible) return
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
    onClick: () -> Unit,
) {
    val hasError = log.error.isNotBlank()
    val statusColor = if (hasError) MiuixTheme.colorScheme.error else OctopusTokens.Accent

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
                        value = formatCount(log.inputTokens.toLong()),
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
            }
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
        if (filter.status != LogStatusFilter.All) add(logStatusFilterLabel(filter.status))
        if (filter.keyword.isNotBlank()) add(stringResource(R.string.log_filter_keyword_summary, filter.keyword.trim()))
        if (filter.keywordScope != LogKeywordScope.Default) add(logKeywordScopeLabel(filter.keywordScope))
        if (filter.keywordMode != LogKeywordMode.Default) add(logKeywordModeLabel(filter.keywordMode))
    }
    return parts.joinToString(" · ").ifBlank { stringResource(R.string.log_filter_active) }
}

private fun formatLogTime(timestampSeconds: Long): String {
    if (timestampSeconds <= 0L) return "-"
    val formatter = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
    return formatter.format(Date(timestampSeconds * 1000))
}
