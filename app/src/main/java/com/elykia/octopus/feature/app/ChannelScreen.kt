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
import com.elykia.octopus.core.designsystem.LoadingPane
import com.elykia.octopus.core.designsystem.SearchField
import com.elykia.octopus.core.designsystem.ScreenPane
import com.elykia.octopus.core.designsystem.SectionCard
import com.elykia.octopus.core.designsystem.SimpleList
import com.elykia.octopus.core.designsystem.ToolbarChip
import com.elykia.octopus.feature.channel.ChannelViewModel

@Composable
fun ChannelScreen(
    contentPadding: PaddingValues,
    viewModel: ChannelViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var deletingId by remember { mutableStateOf<Int?>(null) }
    var searchTerm by remember { mutableStateOf("") }
    var enabledOnly by remember { mutableStateOf(false) }
    when {
        uiState.loading -> LoadingPane(title = stringResource(R.string.channel_title))
        uiState.error != null -> ErrorPane(message = uiState.error ?: stringResource(R.string.error_title), onRetry = viewModel::refresh)
        uiState.channels.isEmpty() -> EmptyPane(title = stringResource(R.string.empty_title), summary = stringResource(R.string.channel_empty))
        else -> {
            val filteredChannels = uiState.channels
                .filter { channel ->
                    val matchesSearch = searchTerm.isBlank() || channel.name.contains(searchTerm, ignoreCase = true)
                    val matchesEnabled = !enabledOnly || channel.enabled
                    matchesSearch && matchesEnabled
                }
                .sortedWith(compareByDescending<com.elykia.octopus.core.data.model.Channel> { it.enabled }.thenBy { it.name.lowercase() })
            ScreenPane(contentPadding = contentPadding) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    SectionCard(
                        title = stringResource(R.string.channel_title),
                        summary = stringResource(R.string.channel_panel_summary),
                        actions = {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                ToolbarChip(text = stringResource(R.string.common_search))
                                ToolbarChip(
                                    text = if (enabledOnly) stringResource(R.string.channel_filter_enabled) else stringResource(R.string.channel_filter_all),
                                    selected = enabledOnly,
                                    onClick = { enabledOnly = !enabledOnly },
                                )
                                ToolbarChip(text = stringResource(R.string.action_create), selected = true)
                            }
                        },
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            SearchField(
                                value = searchTerm,
                                onValueChange = { searchTerm = it },
                                hint = stringResource(R.string.channel_search_hint),
                            )
                            SimpleList(
                                entries = filteredChannels.map { channel ->
                                    channel.name to stringResource(
                                        R.string.channel_item_summary,
                                        channel.type,
                                        if (channel.enabled) stringResource(R.string.common_enabled) else stringResource(R.string.common_disabled),
                                        channel.keys.size,
                                    )
                                },
                                onDelete = { index -> deletingId = filteredChannels[index].id },
                            )
                        }
                    }
                }
            }
            DangerConfirmDialog(
                visible = deletingId != null,
                title = stringResource(R.string.channel_delete_title),
                summary = stringResource(R.string.channel_delete_summary),
                onConfirm = {
                    deletingId?.let(viewModel::delete)
                    deletingId = null
                },
                onDismiss = { deletingId = null },
            )
        }
    }
}
