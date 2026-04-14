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
import com.elykia.octopus.feature.group.GroupViewModel

@Composable
fun GroupScreen(
    contentPadding: PaddingValues,
    viewModel: GroupViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var deletingId by remember { mutableStateOf<Int?>(null) }
    var searchTerm by remember { mutableStateOf("") }
    var sortByItems by remember { mutableStateOf(true) }
    when {
        uiState.loading -> LoadingPane(title = stringResource(R.string.group_title))
        uiState.error != null -> ErrorPane(message = uiState.error ?: stringResource(R.string.error_title), onRetry = viewModel::refresh)
        uiState.groups.isEmpty() -> EmptyPane(title = stringResource(R.string.empty_title), summary = stringResource(R.string.group_empty))
        else -> {
            val filteredGroups = uiState.groups
                .filter { group -> searchTerm.isBlank() || group.name.contains(searchTerm, ignoreCase = true) }
                .let { groups ->
                    if (sortByItems) groups.sortedByDescending { it.items.size } else groups.sortedBy { it.name.lowercase() }
                }
            ScreenPane(contentPadding = contentPadding) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    SectionCard(
                        title = stringResource(R.string.group_title),
                        summary = stringResource(R.string.group_panel_summary),
                        actions = {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                ToolbarChip(text = stringResource(R.string.common_search))
                                ToolbarChip(
                                    text = if (sortByItems) stringResource(R.string.group_filter_member) else stringResource(R.string.group_sort_name_asc),
                                    selected = sortByItems,
                                    onClick = { sortByItems = !sortByItems },
                                )
                                ToolbarChip(text = stringResource(R.string.action_create), selected = true)
                            }
                        },
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            SearchField(
                                value = searchTerm,
                                onValueChange = { searchTerm = it },
                                hint = stringResource(R.string.group_search_hint),
                            )
                            SimpleList(
                                entries = filteredGroups.map { group ->
                                    group.name to stringResource(
                                        R.string.group_item_summary,
                                        group.mode,
                                        group.items.size,
                                        group.matchRegex.ifBlank { "-" },
                                    )
                                },
                                onDelete = { index -> deletingId = filteredGroups[index].id },
                            )
                        }
                    }
                }
            }
            DangerConfirmDialog(
                visible = deletingId != null,
                title = stringResource(R.string.group_delete_title),
                summary = stringResource(R.string.group_delete_summary),
                onConfirm = {
                    deletingId?.let(viewModel::delete)
                    deletingId = null
                },
                onDismiss = { deletingId = null },
            )
        }
    }
}
