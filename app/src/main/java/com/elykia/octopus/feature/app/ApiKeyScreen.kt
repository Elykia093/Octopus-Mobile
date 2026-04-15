package com.elykia.octopus.feature.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.elykia.octopus.R
import com.elykia.octopus.core.data.model.ApiKeyItem
import com.elykia.octopus.core.designsystem.AppInfoChip
import com.elykia.octopus.core.designsystem.AppListCard
import com.elykia.octopus.core.designsystem.AppPageScaffold
import com.elykia.octopus.core.designsystem.DangerConfirmDialog
import com.elykia.octopus.core.designsystem.EmptyPane
import com.elykia.octopus.core.designsystem.ErrorPane
import com.elykia.octopus.core.designsystem.FloatingCreateButton
import com.elykia.octopus.core.designsystem.LoadingPane
import com.elykia.octopus.core.designsystem.PageActionButton
import com.elykia.octopus.core.designsystem.SearchField
import com.elykia.octopus.core.designsystem.formatMoney
import com.elykia.octopus.core.designsystem.icons.AppMiuixIcons
import com.elykia.octopus.feature.setting.SettingViewModel
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun ApiKeyScreen(
    contentPadding: PaddingValues,
    viewModel: SettingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var searchTerm by remember { mutableStateOf("") }
    var deletingId by remember { mutableStateOf<Int?>(null) }
    var editingItem by remember { mutableStateOf<ApiKeyItem?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }

    when {
        uiState.loading -> LoadingPane(title = stringResource(R.string.apikey_title))
        uiState.error != null -> ErrorPane(message = uiState.error ?: stringResource(R.string.error_title), onRetry = viewModel::refresh)
        uiState.apiKeys.isEmpty() -> EmptyPane(title = stringResource(R.string.apikey_title), summary = stringResource(R.string.apikey_empty))
        else -> {
            val keys = uiState.apiKeys
                .filter { searchTerm.isBlank() || it.name.contains(searchTerm, ignoreCase = true) }
                .sortedByDescending { it.enabled }

            Box(modifier = Modifier.fillMaxSize()) {
                AppPageScaffold(
                    title = stringResource(R.string.apikey_title),
                    actions = {
                        PageActionButton(
                            icon = AppMiuixIcons.Refresh,
                            contentDescription = stringResource(R.string.common_refresh),
                            onClick = viewModel::refresh,
                        )
                    },
                    contentPadding = contentPadding,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SearchField(
                            value = searchTerm,
                            onValueChange = { searchTerm = it },
                            hint = stringResource(R.string.setting_apikey_title),
                        )
                        keys.forEach { item ->
                            ApiKeyRow(
                                item = item,
                                onToggle = { viewModel.setApiKeyEnabled(item, it) },
                                onEdit = { editingItem = item },
                                onDelete = { deletingId = item.id },
                            )
                        }
                    }
                }

                FloatingCreateButton(
                    text = stringResource(R.string.action_create),
                    icon = AppMiuixIcons.Add,
                    onClick = { showCreateDialog = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 20.dp, bottom = 94.dp),
                )
            }

            DangerConfirmDialog(
                visible = deletingId != null,
                title = stringResource(R.string.common_delete),
                summary = stringResource(R.string.setting_apikey_summary),
                onConfirm = {
                    deletingId?.let(viewModel::deleteApiKey)
                    deletingId = null
                },
                onDismiss = { deletingId = null },
            )

            ApiKeyEditorDialog(
                visible = showCreateDialog,
                title = stringResource(R.string.apikey_create_title),
                initialItem = null,
                onConfirm = { name, expireAt, maxCost, supportedModels, enabled ->
                    viewModel.createApiKey(name, expireAt, maxCost, supportedModels, enabled)
                    showCreateDialog = false
                },
                onDismiss = { showCreateDialog = false },
            )

            ApiKeyEditorDialog(
                visible = editingItem != null,
                title = stringResource(R.string.apikey_edit_title),
                initialItem = editingItem,
                onConfirm = { name, expireAt, maxCost, supportedModels, enabled ->
                    editingItem?.let { current ->
                        viewModel.updateApiKey(
                            current.copy(
                                name = name,
                                expireAt = expireAt,
                                maxCost = maxCost,
                                supportedModels = supportedModels,
                                enabled = enabled,
                            )
                        )
                    }
                    editingItem = null
                },
                onDismiss = { editingItem = null },
            )

            uiState.createdApiKey?.let { created ->
                CreatedApiKeyDialog(
                    item = created,
                    onCopy = { copyApiKey(context, created.apiKey) },
                    onDismiss = viewModel::dismissCreatedApiKey,
                )
            }
        }
    }
}

@Composable
private fun ApiKeyRow(
    item: ApiKeyItem,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    AppListCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Switch(checked = item.enabled, onCheckedChange = onToggle)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = item.name,
                    style = MiuixTheme.textStyles.main,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = if (item.apiKey.length > 16) item.apiKey.take(16) + "..." else item.apiKey,
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    fontFamily = FontFamily.Monospace,
                )
                item.supportedModels
                    ?.split(',')
                    ?.map { it.trim() }
                    ?.filter { it.isNotBlank() }
                    ?.take(3)
                    ?.takeIf { it.isNotEmpty() }
                    ?.let { models ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            models.forEach { model ->
                                AppInfoChip(text = model, icon = AppMiuixIcons.Token)
                            }
                        }
                    }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = AppMiuixIcons.Create,
                    contentDescription = stringResource(R.string.action_edit),
                    tint = MiuixTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(18.dp)
                        .padding(1.dp)
                        .clickable(onClick = onEdit),
                )
                Icon(
                    imageVector = AppMiuixIcons.Delete,
                    contentDescription = stringResource(R.string.common_delete),
                    tint = MiuixTheme.colorScheme.error,
                    modifier = Modifier
                        .size(18.dp)
                        .padding(start = 2.dp)
                        .clickable(onClick = onDelete),
                )
            }
        }
    }
}

@Composable
private fun CreatedApiKeyDialog(
    item: ApiKeyItem,
    onCopy: () -> Unit,
    onDismiss: () -> Unit,
) {
    OverlayDialog(
        show = true,
        title = stringResource(R.string.apikey_created_title),
        summary = stringResource(R.string.apikey_created_summary),
        onDismissRequest = onDismiss,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = stringResource(R.string.apikey_created_value_label),
                style = MiuixTheme.textStyles.main,
                fontWeight = FontWeight.SemiBold,
            )
            AppListCard {
                Text(
                    text = item.apiKey,
                    style = MiuixTheme.textStyles.body2,
                    fontFamily = FontFamily.Monospace,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppInfoChip(
                    text = item.expireAt?.takeIf { it > 0 }?.toString() ?: stringResource(R.string.common_never_expires),
                    icon = AppMiuixIcons.Time,
                )
                AppInfoChip(
                    text = item.maxCost?.let(::formatMoney) ?: stringResource(R.string.common_unknown),
                    icon = AppMiuixIcons.Cost,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(text = stringResource(R.string.common_copy), onClick = onCopy)
                TextButton(text = stringResource(R.string.common_confirm), onClick = onDismiss)
            }
        }
    }
}

@Composable
private fun ApiKeyEditorDialog(
    visible: Boolean,
    title: String,
    initialItem: ApiKeyItem?,
    onConfirm: (String, Long, Double, String, Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    if (!visible) return

    var name by remember(initialItem?.id, visible) { mutableStateOf(initialItem?.name.orEmpty()) }
    var expireAt by remember(initialItem?.id, visible) { mutableStateOf(initialItem?.expireAt?.toString().orEmpty()) }
    var maxCost by remember(initialItem?.id, visible) { mutableStateOf(initialItem?.maxCost?.toString().orEmpty()) }
    var supportedModels by remember(initialItem?.id, visible) { mutableStateOf(initialItem?.supportedModels.orEmpty()) }
    var enabled by remember(initialItem?.id, visible) { mutableStateOf(initialItem?.enabled ?: true) }

    OverlayDialog(
        show = visible,
        title = title,
        summary = stringResource(R.string.setting_apikey_summary),
        onDismissRequest = onDismiss,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            TextField(
                value = name,
                onValueChange = { name = it },
                label = stringResource(R.string.apikey_name_hint),
                useLabelAsPlaceholder = true,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            TextField(
                value = maxCost,
                onValueChange = { maxCost = it },
                label = stringResource(R.string.apikey_max_cost_hint),
                useLabelAsPlaceholder = true,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )
            TextField(
                value = expireAt,
                onValueChange = { expireAt = it },
                label = stringResource(R.string.apikey_expire_at_hint),
                useLabelAsPlaceholder = true,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
            TextField(
                value = supportedModels,
                onValueChange = { supportedModels = it },
                label = stringResource(R.string.apikey_supported_models_hint),
                useLabelAsPlaceholder = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(text = stringResource(R.string.apikey_enabled_label), style = MiuixTheme.textStyles.main)
                Switch(checked = enabled, onCheckedChange = { enabled = it })
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(text = stringResource(R.string.common_cancel), onClick = onDismiss)
                TextButton(
                    text = stringResource(R.string.common_confirm),
                    enabled = name.isNotBlank(),
                    colors = ButtonDefaults.textButtonColorsPrimary(),
                    onClick = {
                        onConfirm(
                            name.trim(),
                            expireAt.toLongOrNull() ?: 0L,
                            maxCost.toDoubleOrNull() ?: 0.0,
                            supportedModels.trim(),
                            enabled,
                        )
                    },
                )
            }
        }
    }
}

private fun copyApiKey(context: Context, value: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
    clipboard.setPrimaryClip(ClipData.newPlainText("api_key", value))
}
