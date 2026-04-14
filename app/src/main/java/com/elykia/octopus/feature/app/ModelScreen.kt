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
import com.elykia.octopus.core.designsystem.EmptyPane
import com.elykia.octopus.core.designsystem.ErrorPane
import com.elykia.octopus.core.designsystem.LoadingPane
import com.elykia.octopus.core.designsystem.SearchField
import com.elykia.octopus.core.designsystem.ScreenPane
import com.elykia.octopus.core.designsystem.SectionCard
import com.elykia.octopus.core.designsystem.SimpleList
import com.elykia.octopus.core.designsystem.ToolbarChip
import com.elykia.octopus.feature.model.ModelViewModel

@Composable
fun ModelScreen(
    contentPadding: PaddingValues,
    viewModel: ModelViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchTerm by remember { mutableStateOf("") }
    var pricedOnly by remember { mutableStateOf(false) }
    when {
        uiState.loading -> LoadingPane(title = stringResource(R.string.model_title))
        uiState.error != null -> ErrorPane(message = uiState.error ?: stringResource(R.string.error_title), onRetry = viewModel::refresh)
        uiState.models.isEmpty() -> EmptyPane(title = stringResource(R.string.empty_title), summary = stringResource(R.string.model_empty))
        else -> {
            val filteredModels = uiState.models
                .filter { model ->
                    val matchesSearch = searchTerm.isBlank() || model.name.contains(searchTerm, ignoreCase = true)
                    val matchesPrice = !pricedOnly || model.input > 0.0 || model.output > 0.0
                    matchesSearch && matchesPrice
                }
                .sortedBy { it.name.lowercase() }
            ScreenPane(contentPadding = contentPadding) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    SectionCard(
                        title = stringResource(R.string.model_sync_title),
                        summary = stringResource(R.string.model_sync_last_update, uiState.lastUpdateTime ?: stringResource(R.string.common_unknown)),
                        actions = {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                ToolbarChip(text = stringResource(R.string.common_refresh), selected = true, onClick = viewModel::refreshPrice)
                                ToolbarChip(
                                    text = if (pricedOnly) stringResource(R.string.model_filter_priced) else stringResource(R.string.model_filter_all),
                                    selected = pricedOnly,
                                    onClick = { pricedOnly = !pricedOnly },
                                )
                            }
                        },
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            SearchField(
                                value = searchTerm,
                                onValueChange = { searchTerm = it },
                                hint = stringResource(R.string.model_search_hint),
                            )
                            SimpleList(
                                entries = filteredModels.take(20).map { model ->
                                    model.name to stringResource(
                                        R.string.model_item_summary,
                                        model.input,
                                        model.output,
                                        model.cacheRead,
                                        model.cacheWrite,
                                    )
                                }
                            )
                        }
                    }
                    SectionCard(
                        title = stringResource(R.string.model_mapping_title),
                        summary = stringResource(R.string.model_mapping_summary),
                    ) {
                        SimpleList(
                            entries = uiState.channels.take(20).map { item ->
                                item.name to stringResource(
                                    R.string.model_mapping_item_summary,
                                    item.channelName,
                                    if (item.enabled) stringResource(R.string.common_enabled) else stringResource(R.string.common_disabled),
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
