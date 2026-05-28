package com.elykia.octopus.feature.log

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.elykia.octopus.R
import com.elykia.octopus.core.data.model.RelayLog
import com.elykia.octopus.core.designsystem.AppInfoChip
import com.elykia.octopus.core.designsystem.AppListCard
import com.elykia.octopus.core.designsystem.AppPageScaffold
import com.elykia.octopus.core.designsystem.DangerConfirmDialog
import com.elykia.octopus.core.designsystem.ErrorPane
import com.elykia.octopus.core.designsystem.InlineEmptyCard
import com.elykia.octopus.core.designsystem.LoadingPane
import com.elykia.octopus.core.designsystem.OctopusTones
import com.elykia.octopus.core.designsystem.PageActionButton
import com.elykia.octopus.core.designsystem.SearchField
import com.elykia.octopus.core.designsystem.ToolbarChip
import com.elykia.octopus.core.designsystem.formatMoney
import com.elykia.octopus.core.designsystem.icons.AppMiuixIcons
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LogScreen(
    contentPadding: PaddingValues,
    viewModel: LogViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var confirmClear by remember { mutableStateOf(false) }
    var searchTerm by remember { mutableStateOf("") }

    when {
        uiState.loading -> LoadingPane(title = stringResource(R.string.log_title))
        uiState.error != null -> ErrorPane(message = uiState.error ?: stringResource(R.string.error_title), onRetry = viewModel::refresh)
        else -> {
            val logs = uiState.logs.filter { log ->
                searchTerm.isBlank() ||
                    log.requestModelName.contains(searchTerm, ignoreCase = true) ||
                    log.channelName.contains(searchTerm, ignoreCase = true) ||
                    log.actualModelName.contains(searchTerm, ignoreCase = true)
            }

            AppPageScaffold(
                title = stringResource(R.string.log_title),
                actions = {
                    PageActionButton(
                        icon = AppMiuixIcons.Delete,
                        contentDescription = stringResource(R.string.log_toolbar_clear),
                        enabled = uiState.logs.isNotEmpty(),
                        onClick = { confirmClear = true },
                    )
                },
                contentPadding = contentPadding,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (uiState.logs.isNotEmpty()) {
                        SearchField(
                            value = searchTerm,
                            onValueChange = { searchTerm = it },
                            hint = stringResource(R.string.log_search_hint),
                        )
                    }
                    when {
                        uiState.logs.isEmpty() -> InlineEmptyCard(
                            title = stringResource(R.string.log_title),
                            summary = stringResource(R.string.log_empty),
                        )
                        logs.isEmpty() -> InlineEmptyCard(
                            title = stringResource(R.string.empty_title),
                            summary = stringResource(R.string.log_search_empty),
                        )
                        else -> {
                            logs.forEach { log ->
                                LogRow(log = log)
                            }
                            if (uiState.hasMore) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                    ToolbarChip(
                                        text = stringResource(R.string.action_load_more),
                                        selected = false,
                                        onClick = viewModel::loadMore,
                                    )
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
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun LogRow(
    log: RelayLog,
) {
    val hasError = log.error.isNotBlank()
    val statusColor = if (hasError) MiuixTheme.colorScheme.error else OctopusTones.Success

    AppListCard(padding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    imageVector = if (hasError) AppMiuixIcons.Close else AppMiuixIcons.Success,
                    contentDescription = log.requestModelName,
                    tint = statusColor,
                    modifier = Modifier.size(20.dp),
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = log.requestModelName.ifBlank { stringResource(R.string.common_unknown) },
                        style = MiuixTheme.textStyles.main,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = log.channelName.ifBlank { stringResource(R.string.common_unknown) },
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = formatMoney(log.cost),
                        style = MiuixTheme.textStyles.main,
                        fontWeight = FontWeight.SemiBold,
                        color = MiuixTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = formatLogTime(log.time),
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                }
            }
            Text(
                text = stringResource(R.string.log_row_actual_model, log.actualModelName),
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                AppInfoChip(text = stringResource(R.string.log_input_token, log.inputTokens), icon = AppMiuixIcons.ArrowUp)
                AppInfoChip(text = stringResource(R.string.log_output_token, log.outputTokens), icon = AppMiuixIcons.ArrowDown)
                AppInfoChip(text = stringResource(R.string.log_use_time, log.useTime), icon = AppMiuixIcons.Time)
            }
        }
    }
}

private fun formatLogTime(timestampSeconds: Long): String {
    if (timestampSeconds <= 0L) return "-"
    val formatter = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
    return formatter.format(Date(timestampSeconds * 1000))
}
