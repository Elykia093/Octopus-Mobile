package com.elykia.octopus.feature.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.elykia.octopus.R
import com.elykia.octopus.core.designsystem.DangerConfirmDialog
import com.elykia.octopus.core.designsystem.EmptyPane
import com.elykia.octopus.core.designsystem.ErrorPane
import com.elykia.octopus.core.designsystem.ListEntryCard
import com.elykia.octopus.core.designsystem.LoadingPane
import com.elykia.octopus.core.designsystem.SearchField
import com.elykia.octopus.core.designsystem.ScreenPane
import com.elykia.octopus.core.designsystem.SectionCard
import com.elykia.octopus.core.designsystem.ToolbarChip
import com.elykia.octopus.core.designsystem.formatMoney
import com.elykia.octopus.feature.log.LogViewModel

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
        uiState.logs.isEmpty() -> EmptyPane(title = stringResource(R.string.empty_title), summary = stringResource(R.string.log_empty))
        else -> {
            val filteredLogs = uiState.logs.filter { log ->
                searchTerm.isBlank() ||
                    log.requestModelName.contains(searchTerm, ignoreCase = true) ||
                    log.channelName.contains(searchTerm, ignoreCase = true) ||
                    log.actualModelName.contains(searchTerm, ignoreCase = true)
            }
            ScreenPane(contentPadding = contentPadding) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    SectionCard(
                        title = stringResource(R.string.log_card_title),
                        summary = stringResource(R.string.log_card_summary),
                        actions = {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                ToolbarChip(text = stringResource(R.string.common_search))
                                ToolbarChip(text = stringResource(R.string.log_toolbar_clear), selected = true, onClick = { confirmClear = true })
                            }
                        },
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            SearchField(
                                value = searchTerm,
                                onValueChange = { searchTerm = it },
                                hint = stringResource(R.string.log_search_hint),
                            )
                            filteredLogs.forEach { log ->
                                ListEntryCard(
                                    title = log.requestModelName,
                                    summary = stringResource(
                                        R.string.log_item_summary,
                                        log.channelName,
                                        log.actualModelName,
                                        formatMoney(log.cost),
                                        log.useTime,
                                        log.totalAttempts,
                                    ),
                                )
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
