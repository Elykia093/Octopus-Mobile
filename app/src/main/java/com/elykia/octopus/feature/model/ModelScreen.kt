package com.elykia.octopus.feature.model

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.elykia.octopus.R
import com.elykia.octopus.core.data.model.LlmChannel
import com.elykia.octopus.core.data.model.LlmInfo
import com.elykia.octopus.core.designsystem.AppInfoChip
import com.elykia.octopus.core.designsystem.AppListCard
import com.elykia.octopus.core.designsystem.AppMetricRow
import com.elykia.octopus.core.designsystem.AppLazyPageScaffold
import com.elykia.octopus.core.designsystem.DangerConfirmDialog
import com.elykia.octopus.core.designsystem.ErrorStateCard
import com.elykia.octopus.core.designsystem.InlineEmptyCard
import com.elykia.octopus.core.designsystem.LoadingStateCard
import com.elykia.octopus.core.designsystem.OctopusTokens
import com.elykia.octopus.core.designsystem.OperationErrorCard
import com.elykia.octopus.core.designsystem.OptionChipGroup
import com.elykia.octopus.core.designsystem.OptionChipItem
import com.elykia.octopus.core.designsystem.PageActionButton
import com.elykia.octopus.core.designsystem.SearchField
import com.elykia.octopus.core.designsystem.icons.AppMiuixIcons
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun ModelScreen(
    contentPadding: PaddingValues,
    viewModel: ModelViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var searchTerm by remember { mutableStateOf("") }
    var searchVisible by remember { mutableStateOf(false) }
    var filter by remember { mutableStateOf(ModelFilter.All) }
    var sort by remember { mutableStateOf(ModelSort.NameAsc) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var editingModelName by remember { mutableStateOf<String?>(null) }
    var deletingModelName by remember { mutableStateOf<String?>(null) }
    var confirmRefreshPrice by remember { mutableStateOf(false) }

    val visibleModels = remember(uiState.models, searchTerm, filter, sort) {
        filterAndSortModels(uiState.models, searchTerm, filter, sort)
    }
    val editingModel = editingModelName?.let { name ->
        uiState.models.firstOrNull { it.name == name }
    }

    AppLazyPageScaffold(
        title = stringResource(R.string.model_title),
        actions = {
            PageActionButton(
                icon = AppMiuixIcons.Refresh,
                contentDescription = stringResource(R.string.setting_action_refresh_price),
                enabled = !uiState.loading && !uiState.shouldShowModelPageError() && !uiState.submitting,
                onClick = { confirmRefreshPrice = true },
            )
            PageActionButton(
                icon = if (searchVisible) AppMiuixIcons.Close else AppMiuixIcons.Search,
                contentDescription = stringResource(R.string.action_open_search),
                enabled = !uiState.loading && !uiState.shouldShowModelPageError(),
                onClick = {
                    searchVisible = !searchVisible
                    if (!searchVisible) searchTerm = ""
                },
            )
            PageActionButton(
                icon = AppMiuixIcons.Add,
                contentDescription = stringResource(R.string.action_create),
                enabled = !uiState.loading && !uiState.shouldShowModelPageError() && !uiState.submitting,
                onClick = {
                    viewModel.clearOperationError()
                    showCreateDialog = true
                },
            )
        },
        contentPadding = contentPadding,
    ) {
        when {
            uiState.loading -> item {
                LoadingStateCard(title = stringResource(R.string.model_title))
            }
            uiState.shouldShowModelPageError() -> item {
                ErrorStateCard(
                    message = uiState.modelListError ?: stringResource(R.string.error_title),
                    onRetry = viewModel::refresh,
                )
            }
            else -> {
                item {
                    ModelSyncCard(
                        modelCount = uiState.models.size,
                        lastUpdate = uiState.modelLastUpdateTime ?: stringResource(R.string.common_unknown),
                    )
                }
                if (uiState.models.isNotEmpty() && searchVisible) {
                    item {
                        SearchField(
                            value = searchTerm,
                            onValueChange = { searchTerm = it },
                            hint = stringResource(R.string.model_search_hint),
                        )
                    }
                }
                if (uiState.models.isNotEmpty()) {
                    item {
                        ModelToolbar(
                            filter = filter,
                            onFilterChange = { filter = it },
                            sort = sort,
                            onSortChange = { sort = it },
                        )
                    }
                }
                if (!showCreateDialog && editingModel == null) {
                    uiState.modelListError?.takeIf { it.isNotBlank() }?.let { error ->
                        item { OperationErrorCard(message = error) }
                    }
                    uiState.operationError?.takeIf { it.isNotBlank() }?.let { error ->
                        item { OperationErrorCard(message = error) }
                    }
                    uiState.modelLastUpdateError?.takeIf { it.isNotBlank() }?.let { error ->
                        item { OperationErrorCard(message = error) }
                    }
                    uiState.modelChannelError?.takeIf { it.isNotBlank() }?.let { error ->
                        item { OperationErrorCard(message = error) }
                    }
                }
                when {
                    uiState.models.isEmpty() -> item {
                        InlineEmptyCard(
                            title = stringResource(R.string.model_title),
                            summary = stringResource(R.string.model_empty),
                        )
                    }
                    visibleModels.isEmpty() -> item {
                        InlineEmptyCard(
                            title = stringResource(R.string.empty_title),
                            summary = stringResource(R.string.model_search_empty),
                        )
                    }
                    else -> items(visibleModels, key = { it.name }) { model ->
                        ModelRow(
                            model = model,
                            submitting = uiState.submitting,
                            onEdit = {
                                viewModel.clearOperationError()
                                editingModelName = model.name
                            },
                            onDelete = { deletingModelName = model.name },
                        )
                    }
                }
                item {
                    ModelChannelMappingCard(modelChannels = uiState.modelChannels)
                }
            }
        }
    }

    ModelEditorDialog(
        visible = showCreateDialog,
        title = stringResource(R.string.model_create_title),
        initialModel = null,
        submitting = uiState.submitting,
        operationError = uiState.operationError,
        onConfirm = { model ->
            viewModel.createModel(model) {
                showCreateDialog = false
                viewModel.clearOperationError()
            }
        },
        onDismiss = {
            if (!uiState.submitting) {
                showCreateDialog = false
                viewModel.clearOperationError()
            }
        },
    )

    ModelEditorDialog(
        visible = editingModel != null,
        title = stringResource(R.string.model_edit_title),
        initialModel = editingModel,
        submitting = uiState.submitting,
        operationError = uiState.operationError,
        onConfirm = { model ->
            editingModel?.let {
                viewModel.updateModel(model.copy(name = it.name)) {
                    editingModelName = null
                    viewModel.clearOperationError()
                }
            }
        },
        onDismiss = {
            if (!uiState.submitting) {
                editingModelName = null
                viewModel.clearOperationError()
            }
        },
    )

    DangerConfirmDialog(
        visible = deletingModelName != null,
        title = stringResource(R.string.model_delete_title),
        summary = stringResource(R.string.model_delete_summary, deletingModelName.orEmpty()),
        onConfirm = {
            deletingModelName?.let(viewModel::deleteModel)
            deletingModelName = null
        },
        onDismiss = { deletingModelName = null },
    )

    DangerConfirmDialog(
        visible = confirmRefreshPrice,
        title = stringResource(R.string.model_refresh_price_title),
        summary = stringResource(R.string.setting_refresh_price_confirm_summary),
        onConfirm = {
            confirmRefreshPrice = false
            viewModel.refreshModelPrice()
        },
        onDismiss = { confirmRefreshPrice = false },
    )
}

@Composable
private fun ModelSyncCard(
    modelCount: Int,
    lastUpdate: String,
) {
    AppListCard(padding = PaddingValues(horizontal = 18.dp, vertical = 18.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = stringResource(R.string.model_sync_title),
                style = MiuixTheme.textStyles.title3,
                fontWeight = FontWeight.Bold,
                color = OctopusTokens.TextPrimary,
            )
            Text(
                text = stringResource(R.string.model_sync_summary),
                style = MiuixTheme.textStyles.body2,
                color = OctopusTokens.TextSecondary,
            )
            AppMetricRow(
                icon = AppMiuixIcons.Model,
                label = stringResource(R.string.model_sync_count_label),
                value = modelCount.toString(),
            )
            AppMetricRow(
                icon = AppMiuixIcons.Time,
                label = stringResource(R.string.model_sync_last_update_label),
                value = lastUpdate,
                accentColor = OctopusTokens.TextSecondary,
            )
        }
    }
}

@Composable
private fun ModelToolbar(
    filter: ModelFilter,
    onFilterChange: (ModelFilter) -> Unit,
    sort: ModelSort,
    onSortChange: (ModelSort) -> Unit,
) {
    AppListCard(padding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OptionChipGroup(
                options = listOf(
                    OptionChipItem(ModelFilter.All, stringResource(R.string.model_filter_all)),
                    OptionChipItem(ModelFilter.Priced, stringResource(R.string.model_filter_priced)),
                    OptionChipItem(ModelFilter.Free, stringResource(R.string.model_filter_free)),
                ),
                selectedValue = filter,
                onSelect = onFilterChange,
                columns = 3,
            )
            OptionChipGroup(
                options = listOf(
                    OptionChipItem(ModelSort.NameAsc, stringResource(R.string.model_sort_name_asc)),
                    OptionChipItem(ModelSort.NameDesc, stringResource(R.string.model_sort_name_desc)),
                ),
                selectedValue = sort,
                onSelect = onSortChange,
                columns = 2,
            )
        }
    }
}

@Composable
private fun ModelChannelMappingCard(
    modelChannels: List<LlmChannel>,
) {
    AppListCard(padding = PaddingValues(horizontal = 18.dp, vertical = 18.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = stringResource(R.string.model_mapping_title),
                style = MiuixTheme.textStyles.title3,
                fontWeight = FontWeight.Bold,
                color = OctopusTokens.TextPrimary,
            )
            Text(
                text = stringResource(R.string.model_mapping_summary),
                style = MiuixTheme.textStyles.body2,
                color = OctopusTokens.TextSecondary,
            )
            if (modelChannels.isEmpty()) {
                Text(
                    text = stringResource(R.string.model_mapping_empty),
                    style = MiuixTheme.textStyles.body2,
                    color = OctopusTokens.TextSecondary,
                )
            } else {
                modelChannels
                    .sortedWith(compareBy<LlmChannel> { it.channelId }.thenBy { it.name.lowercase() })
                    .take(8)
                    .forEach { mapping ->
                        ModelChannelMappingRow(mapping = mapping)
                    }
                if (modelChannels.size > 8) {
                    AppInfoChip(
                        text = stringResource(R.string.model_mapping_more, modelChannels.size - 8),
                        icon = AppMiuixIcons.More,
                    )
                }
            }
        }
    }
}

@Composable
private fun ModelChannelMappingRow(
    mapping: LlmChannel,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        AppInfoChip(
            text = if (mapping.enabled) stringResource(R.string.common_enabled) else stringResource(R.string.common_disabled),
            icon = AppMiuixIcons.Channel,
            tint = if (mapping.enabled) OctopusTokens.Accent else OctopusTokens.TextSecondary,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = mapping.name.ifBlank { stringResource(R.string.common_unknown) },
                style = MiuixTheme.textStyles.main,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = stringResource(
                    R.string.model_mapping_item_summary,
                    mapping.channelName.ifBlank { stringResource(R.string.common_unknown) },
                    mapping.channelId,
                ),
                style = MiuixTheme.textStyles.body2,
                color = OctopusTokens.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
