package com.elykia.octopus.feature.setting

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.elykia.octopus.R
import com.elykia.octopus.core.data.model.SettingItem
import com.elykia.octopus.core.designsystem.AppLazyPageScaffold
import com.elykia.octopus.core.designsystem.AppListCard
import com.elykia.octopus.core.designsystem.DangerConfirmDialog
import com.elykia.octopus.core.designsystem.ErrorStateCard
import com.elykia.octopus.core.designsystem.LoadingStateCard
import com.elykia.octopus.core.designsystem.OctopusBrandMark
import com.elykia.octopus.core.designsystem.OctopusTokens
import com.elykia.octopus.core.designsystem.OperationErrorCard
import com.elykia.octopus.core.designsystem.SecureVisibleWindow
import com.elykia.octopus.core.designsystem.SoftIconTile
import com.elykia.octopus.core.designsystem.icons.AppMiuixIcons
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.io.ByteArrayOutputStream
import java.io.IOException

@Composable
fun SettingScreen(
    contentPadding: PaddingValues,
    viewModel: SettingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var language by remember(uiState.language) { mutableStateOf(uiState.language) }
    var themeMode by remember(uiState.themeMode) { mutableIntStateOf(uiState.themeMode) }
    var editingItem by remember { mutableStateOf<SettingItem?>(null) }
    var accountAction by remember { mutableStateOf<AccountAction?>(null) }
    var confirmAction by remember { mutableStateOf<HighImpactSettingAction?>(null) }
    var pendingExportBytes by remember { mutableStateOf<ByteArray?>(null) }
    var pendingImportFile by remember { mutableStateOf<PendingImportFile?>(null) }

    val exportCancelled = stringResource(R.string.setting_export_cancelled)
    val exportFailed = stringResource(R.string.setting_export_failed)
    val exportSuccess = stringResource(R.string.setting_export_success)
    val importCancelled = stringResource(R.string.setting_import_cancelled)
    val importFailed = stringResource(R.string.setting_import_failed)
    val importTooLarge = stringResource(R.string.setting_import_too_large)
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        val bytes = pendingExportBytes
        pendingExportBytes = null
        if (uri == null || bytes == null) {
            viewModel.markDataTransferFailed(exportCancelled)
            return@rememberLauncherForActivityResult
        }
        scope.launch {
            writeBytesToUri(context, uri, bytes)
                .onSuccess { viewModel.markDataTransferSucceeded(exportSuccess) }
                .onFailure { viewModel.markDataTransferFailed(exportFailed) }
        }
    }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri == null) {
            viewModel.markDataTransferFailed(importCancelled)
            return@rememberLauncherForActivityResult
        }
        scope.launch {
            readBytesFromUri(context, uri)
                .onSuccess { bytes ->
                    pendingImportFile = PendingImportFile(
                        fileName = displayNameForUri(context, uri),
                        content = bytes,
                    )
                }
                .onFailure { error ->
                    viewModel.markDataTransferFailed(
                        if (error is ImportFileTooLargeException) importTooLarge else importFailed,
                    )
                }
        }
    }

    val languageLabel = when (language) {
        "zh-CN" -> stringResource(R.string.setting_language_zh_cn)
        "en" -> stringResource(R.string.setting_language_en)
        else -> stringResource(R.string.setting_language_system)
    }
    val themeLabel = when (themeMode) {
        1 -> stringResource(R.string.setting_theme_light)
        2 -> stringResource(R.string.setting_theme_dark)
        else -> stringResource(R.string.setting_theme_system)
    }

    AppLazyPageScaffold(
        title = stringResource(R.string.setting_title),
        contentPadding = contentPadding,
    ) {
        when {
            uiState.loading -> item {
                LoadingStateCard(title = stringResource(R.string.setting_title))
            }
            uiState.shouldShowSettingsPageError() -> item {
                ErrorStateCard(
                    message = uiState.error ?: stringResource(R.string.error_title),
                    onRetry = viewModel::refresh,
                )
            }
            else -> {
                item {
                    VersionCard(
                        currentVersion = uiState.currentVersion ?: stringResource(R.string.common_unknown),
                        latestVersion = uiState.latestInfo?.tagName ?: stringResource(R.string.common_unknown),
                        publishedAt = uiState.latestInfo?.publishedAt,
                    )
                }
                uiState.error?.takeIf { it.isNotBlank() }?.let { error ->
                    item { OperationErrorCard(message = error) }
                }
                uiState.versionInfoError?.takeIf { it.isNotBlank() }?.let { error ->
                    item { OperationErrorCard(message = error) }
                }

                item {
                    SettingSectionCard(title = stringResource(R.string.setting_account_title)) {
                        PreferenceRow(
                            icon = AppMiuixIcons.Info,
                            title = stringResource(R.string.setting_account_change_username),
                            value = uiState.username.ifBlank { stringResource(R.string.common_unknown) },
                            enabled = !uiState.actionSubmitting,
                            onClick = { accountAction = AccountAction.Username },
                        )
                        SettingDivider()
                        PreferenceRow(
                            icon = AppMiuixIcons.ApiKey,
                            title = stringResource(R.string.setting_account_change_password),
                            value = stringResource(R.string.setting_account_relogin_summary),
                            enabled = !uiState.actionSubmitting,
                            onClick = { accountAction = AccountAction.Password },
                        )
                    }
                }

                item {
                    SettingSectionCard(title = stringResource(R.string.setting_preferences_title)) {
                        PreferenceRow(
                            icon = AppMiuixIcons.Setting,
                            title = stringResource(R.string.setting_language_label),
                            value = languageLabel,
                            enabled = !uiState.actionSubmitting,
                            onClick = {
                                val nextLanguage = when (language) {
                                    "system" -> "zh-CN"
                                    "zh-CN" -> "en"
                                    else -> "system"
                                }
                                viewModel.updateAppearance(nextLanguage, themeMode)
                            },
                        )
                        SettingDivider()
                        PreferenceRow(
                            icon = AppMiuixIcons.Toggle,
                            title = stringResource(R.string.setting_theme_label),
                            value = themeLabel,
                            enabled = !uiState.actionSubmitting,
                            onClick = {
                                val nextThemeMode = when (themeMode) {
                                    0 -> 1
                                    1 -> 2
                                    else -> 0
                                }
                                viewModel.updateAppearance(language, nextThemeMode)
                            },
                        )
                    }
                }

                item {
                    SettingSectionCard(title = stringResource(R.string.setting_actions_title)) {
                        PreferenceRow(
                            icon = AppMiuixIcons.Refresh,
                            title = stringResource(R.string.action_check_update),
                            value = uiState.latestInfo?.tagName ?: stringResource(R.string.common_unknown),
                            enabled = !uiState.actionSubmitting,
                            onClick = viewModel::refreshLatestInfo,
                        )
                        SettingDivider()
                        PreferenceRow(
                            icon = AppMiuixIcons.Cost,
                            title = stringResource(R.string.setting_action_refresh_price),
                            value = uiState.modelLastUpdateTime ?: stringResource(R.string.common_unknown),
                            enabled = !uiState.actionSubmitting,
                            onClick = { confirmAction = HighImpactSettingAction.RefreshPrice },
                        )
                        uiState.modelLastUpdateError?.takeIf { it.isNotBlank() }?.let { error ->
                            SettingDivider()
                            OperationErrorCard(message = error)
                        }
                        SettingDivider()
                        PreferenceRow(
                            icon = AppMiuixIcons.Sync,
                            title = stringResource(R.string.setting_action_sync_channel),
                            value = uiState.channelLastSyncTime ?: stringResource(R.string.common_unknown),
                            enabled = !uiState.actionSubmitting,
                            onClick = { confirmAction = HighImpactSettingAction.SyncChannel },
                        )
                        uiState.channelLastSyncError?.takeIf { it.isNotBlank() }?.let { error ->
                            SettingDivider()
                            OperationErrorCard(message = error)
                        }
                        SettingDivider()
                        PreferenceRow(
                            icon = AppMiuixIcons.ArrowDown,
                            title = stringResource(R.string.setting_action_export_data),
                            value = stringResource(R.string.setting_export_default_summary),
                            enabled = !uiState.dataTransferSubmitting,
                            onClick = {
                                viewModel.clearDataTransferStatus()
                                viewModel.exportData { bytes ->
                                    pendingExportBytes = bytes
                                    exportLauncher.launch("octopus-settings.json")
                                }
                            },
                        )
                        SettingDivider()
                        PreferenceRow(
                            icon = AppMiuixIcons.ArrowUp,
                            title = stringResource(R.string.setting_action_import_data),
                            value = stringResource(R.string.setting_import_summary),
                            enabled = !uiState.dataTransferSubmitting,
                            onClick = {
                                viewModel.clearDataTransferStatus()
                                importLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                            },
                        )
                        SettingDivider()
                        PreferenceRow(
                            icon = AppMiuixIcons.ArrowDown,
                            title = stringResource(R.string.action_run_update),
                            value = stringResource(R.string.setting_update_now_summary),
                            enabled = !uiState.actionSubmitting,
                            onClick = { confirmAction = HighImpactSettingAction.RunUpdate },
                        )
                    }
                }

                uiState.actionMessage?.let { message ->
                    item {
                        DataTransferStatusCard(message = message, isError = false, onDismiss = viewModel::clearActionStatus)
                    }
                }
                uiState.actionError?.let { message ->
                    item {
                        DataTransferStatusCard(message = message, isError = true, onDismiss = viewModel::clearActionStatus)
                    }
                }
                uiState.dataTransferMessage?.let { message ->
                    item {
                        DataTransferStatusCard(message = message, isError = false, onDismiss = viewModel::clearDataTransferStatus)
                    }
                }
                uiState.dataTransferError?.let { message ->
                    item {
                        DataTransferStatusCard(message = message, isError = true, onDismiss = viewModel::clearDataTransferStatus)
                    }
                }

                val serverItems = uiState.sections.flatMap { it.items }
                item {
                    SettingSectionCard(title = stringResource(R.string.setting_server_title)) {
                        serverItems.forEachIndexed { index, item ->
                            if (index > 0) SettingDivider()
                            SettingItemRow(
                                item = item,
                                enabled = !uiState.actionSubmitting,
                                onEdit = { editingItem = item },
                                onToggle = { checked ->
                                    viewModel.updateSetting(item.key, if (checked) "true" else "false")
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    editingItem?.let { item ->
        SettingEditDialog(
            item = item,
            onDismiss = { editingItem = null },
            onConfirm = { newValue ->
                viewModel.updateSetting(item.key, newValue)
                editingItem = null
            },
        )
    }

    accountAction?.let { action ->
        when (action) {
            AccountAction.Username -> ChangeUsernameDialog(
                currentUsername = uiState.username,
                submitting = uiState.actionSubmitting,
                onConfirm = { username ->
                    accountAction = null
                    viewModel.changeUsername(username)
                },
                onDismiss = {
                    if (!uiState.actionSubmitting) accountAction = null
                },
            )
            AccountAction.Password -> ChangePasswordDialog(
                submitting = uiState.actionSubmitting,
                onConfirm = { oldPassword, newPassword ->
                    accountAction = null
                    viewModel.changePassword(oldPassword, newPassword)
                },
                onDismiss = {
                    if (!uiState.actionSubmitting) accountAction = null
                },
            )
        }
    }

    confirmAction?.let { action ->
        DangerConfirmDialog(
            visible = true,
            title = stringResource(action.titleRes),
            summary = stringResource(action.summaryRes),
            onConfirm = {
                confirmAction = null
                when (action) {
                    HighImpactSettingAction.RefreshPrice -> viewModel.refreshModelPrice()
                    HighImpactSettingAction.SyncChannel -> viewModel.syncChannelModels()
                    HighImpactSettingAction.RunUpdate -> viewModel.triggerUpdate()
                }
            },
            onDismiss = { confirmAction = null },
        )
    }

    pendingImportFile?.let { file ->
        DangerConfirmDialog(
            visible = true,
            title = stringResource(R.string.setting_import_confirm_title),
            summary = stringResource(R.string.setting_import_confirm_summary, file.fileName),
            onConfirm = {
                pendingImportFile = null
                viewModel.importData(file.fileName, file.content)
            },
            onDismiss = { pendingImportFile = null },
        )
    }
}

@Composable
private fun VersionCard(
    currentVersion: String,
    latestVersion: String,
    publishedAt: String?,
) {
    AppListCard(padding = PaddingValues(horizontal = 20.dp, vertical = 20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(OctopusTokens.PrimarySoft),
                contentAlignment = Alignment.Center,
            ) {
                OctopusBrandMark(size = 38.dp)
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = stringResource(R.string.setting_info_title),
                    style = MiuixTheme.textStyles.title3,
                    fontWeight = FontWeight.Bold,
                    color = OctopusTokens.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stringResource(R.string.setting_info_current, currentVersion),
                    style = MiuixTheme.textStyles.body2,
                    color = OctopusTokens.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stringResource(R.string.setting_info_latest, latestVersion),
                    style = MiuixTheme.textStyles.body2,
                    color = OctopusTokens.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!publishedAt.isNullOrBlank()) {
                    Text(
                        text = stringResource(R.string.setting_info_published, publishedAt),
                        style = MiuixTheme.textStyles.body2,
                        color = OctopusTokens.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingSectionCard(
    title: String,
    content: @Composable () -> Unit,
) {
    AppListCard(padding = PaddingValues(horizontal = 20.dp, vertical = 18.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = title,
                style = MiuixTheme.textStyles.title3,
                fontWeight = FontWeight.Bold,
                color = OctopusTokens.TextPrimary,
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                content()
            }
        }
    }
}

@Composable
private fun SettingDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(OctopusTokens.Border.copy(alpha = 0.72f)),
    )
}

@Composable
private fun PreferenceRow(
    icon: ImageVector,
    title: String,
    value: String? = null,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(OctopusTokens.Muted.copy(alpha = 0.48f))
            .border(1.dp, OctopusTokens.Border.copy(alpha = 0.44f), RoundedCornerShape(18.dp))
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingIconBox(icon = icon)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = title,
                style = MiuixTheme.textStyles.main,
                color = if (enabled) OctopusTokens.TextPrimary else OctopusTokens.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (value != null) {
                Text(
                    text = value,
                    style = MiuixTheme.textStyles.body2,
                    color = OctopusTokens.TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

private data class PendingImportFile(
    val fileName: String,
    val content: ByteArray,
)

private enum class AccountAction {
    Username,
    Password,
}

private enum class HighImpactSettingAction(
    val titleRes: Int,
    val summaryRes: Int,
) {
    RefreshPrice(
        titleRes = R.string.setting_action_refresh_price,
        summaryRes = R.string.setting_refresh_price_confirm_summary,
    ),
    SyncChannel(
        titleRes = R.string.setting_action_sync_channel,
        summaryRes = R.string.setting_sync_channel_confirm_summary,
    ),
    RunUpdate(
        titleRes = R.string.action_run_update,
        summaryRes = R.string.setting_update_now_summary,
    ),
}

@Composable
private fun DataTransferStatusCard(
    message: String,
    isError: Boolean,
    onDismiss: () -> Unit,
) {
    AppListCard(padding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = message,
                style = MiuixTheme.textStyles.body2,
                color = if (isError) MiuixTheme.colorScheme.error else OctopusTokens.TextPrimary,
                modifier = Modifier.weight(1f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            TextButton(text = stringResource(R.string.action_close), onClick = onDismiss)
        }
    }
}

@Composable
private fun SettingItemRow(
    item: SettingItem,
    enabled: Boolean,
    onEdit: () -> Unit,
    onToggle: (Boolean) -> Unit,
) {
    if (item.key == "relay_log_keep_enabled") {
        SwitchRow(
            icon = AppMiuixIcons.Log,
            title = settingItemTitle(item.key),
            checked = item.value == "true",
            enabled = enabled,
            onCheckedChange = onToggle,
        )
    } else {
        PreferenceRow(
            icon = settingItemIcon(item.key),
            title = settingItemTitle(item.key),
            value = item.value.ifBlank { stringResource(R.string.common_unknown) },
            enabled = enabled,
            onClick = onEdit,
        )
    }
}

@Composable
private fun SettingIconBox(
    icon: ImageVector,
) {
    SoftIconTile(
        icon = icon,
        contentDescription = null,
        modifier = Modifier.size(38.dp),
    )
}

@Composable
private fun SwitchRow(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(OctopusTokens.Muted.copy(alpha = 0.48f))
            .border(1.dp, OctopusTokens.Border.copy(alpha = 0.44f), RoundedCornerShape(18.dp))
            .padding(horizontal = 10.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingIconBox(icon = icon)
        Text(
            text = title,
            style = MiuixTheme.textStyles.main,
            color = if (enabled) OctopusTokens.TextPrimary else OctopusTokens.TextSecondary,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Switch(checked = checked, onCheckedChange = { if (enabled) onCheckedChange(it) })
    }
}

private fun settingItemIcon(key: String): ImageVector = when (key) {
    "proxy_url" -> AppMiuixIcons.Channel
    "stats_save_interval" -> AppMiuixIcons.Time
    "cors_allow_origins" -> AppMiuixIcons.ApiKey
    "relay_log_keep_period" -> AppMiuixIcons.Log
    "model_info_update_interval" -> AppMiuixIcons.Cost
    "sync_llm_interval" -> AppMiuixIcons.Sync
    "circuit_breaker_threshold" -> AppMiuixIcons.Info
    "circuit_breaker_cooldown" -> AppMiuixIcons.Time
    "circuit_breaker_max_cooldown" -> AppMiuixIcons.Time
    else -> AppMiuixIcons.Setting
}

@Composable
private fun settingItemTitle(key: String): String = when (key) {
    "proxy_url" -> stringResource(R.string.setting_proxy_url_label)
    "stats_save_interval" -> stringResource(R.string.setting_stats_save_interval_label)
    "cors_allow_origins" -> stringResource(R.string.setting_cors_allow_origins_label)
    "relay_log_keep_period" -> stringResource(R.string.setting_log_keep_period_label)
    "relay_log_keep_enabled" -> stringResource(R.string.setting_log_keep_enabled_label)
    "model_info_update_interval" -> stringResource(R.string.setting_model_info_update_interval_label)
    "sync_llm_interval" -> stringResource(R.string.setting_sync_llm_interval_label)
    "circuit_breaker_threshold" -> stringResource(R.string.setting_circuit_breaker_threshold_label)
    "circuit_breaker_cooldown" -> stringResource(R.string.setting_circuit_breaker_cooldown_label)
    "circuit_breaker_max_cooldown" -> stringResource(R.string.setting_circuit_breaker_max_cooldown_label)
    else -> key
}

private val NUMERIC_KEYS = setOf(
    "stats_save_interval",
    "model_info_update_interval",
    "sync_llm_interval",
    "relay_log_keep_period",
    "circuit_breaker_threshold",
    "circuit_breaker_cooldown",
    "circuit_breaker_max_cooldown",
)

internal enum class SettingValidationIssue {
    InvalidNumber,
    InvalidUrl,
    InvalidCors,
}

internal fun validateSettingValue(key: String, value: String): SettingValidationIssue? = when (key) {
    in NUMERIC_KEYS -> {
        val number = value.trim().toIntOrNull()
        val range = when (key) {
            "stats_save_interval" -> 1..1440
            "model_info_update_interval" -> 1..8760
            "sync_llm_interval" -> 1..8760
            "relay_log_keep_period" -> 1..3650
            "circuit_breaker_threshold" -> 1..1000
            "circuit_breaker_cooldown" -> 1..86400
            "circuit_breaker_max_cooldown" -> 1..604800
            else -> 1..Int.MAX_VALUE
        }
        if (number == null || number !in range) SettingValidationIssue.InvalidNumber else null
    }
    "proxy_url" -> if (value.isBlank() || value.isValidSettingUrl()) null else SettingValidationIssue.InvalidUrl
    "cors_allow_origins" -> if (value.isBlank() || value.hasValidCorsOrigins()) null else SettingValidationIssue.InvalidCors
    else -> null
}

internal fun canSubmitSettingEdit(key: String, value: String): Boolean =
    validateSettingValue(key, value) == null

internal enum class AccountValidationIssue {
    UsernameBlank,
    OldPasswordBlank,
    NewPasswordBlank,
    PasswordMismatch,
    PasswordTooShort,
}

internal fun validateUsernameChange(username: String): AccountValidationIssue? =
    if (username.trim().isBlank()) AccountValidationIssue.UsernameBlank else null

internal fun validatePasswordChange(
    oldPassword: String,
    newPassword: String,
    confirmPassword: String,
): AccountValidationIssue? = when {
    oldPassword.isBlank() -> AccountValidationIssue.OldPasswordBlank
    newPassword.isBlank() -> AccountValidationIssue.NewPasswordBlank
    newPassword != confirmPassword -> AccountValidationIssue.PasswordMismatch
    newPassword.length < MIN_ACCOUNT_PASSWORD_LENGTH -> AccountValidationIssue.PasswordTooShort
    else -> null
}

internal fun canSubmitUsernameChange(username: String, submitting: Boolean): Boolean =
    !submitting && validateUsernameChange(username) == null

internal fun canSubmitPasswordChange(
    oldPassword: String,
    newPassword: String,
    confirmPassword: String,
    submitting: Boolean,
): Boolean = !submitting && validatePasswordChange(oldPassword, newPassword, confirmPassword) == null

private fun String.isValidSettingUrl(): Boolean {
    val url = trim().toHttpUrlOrNull() ?: return false
    return url.scheme in setOf("http", "https") &&
        url.encodedUsername.isBlank() &&
        url.encodedPassword.isBlank()
}

private fun String.hasValidCorsOrigins(): Boolean =
    split(',')
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .all { it == "*" || it.isValidCorsOrigin() }

private fun String.isValidCorsOrigin(): Boolean {
    val url = toHttpUrlOrNull() ?: return false
    return url.scheme in setOf("http", "https") &&
        url.encodedUsername.isBlank() &&
        url.encodedPassword.isBlank() &&
        url.encodedPath == "/" &&
        url.encodedQuery == null &&
        url.encodedFragment == null
}

@Composable
private fun settingValidationMessage(issue: SettingValidationIssue): String = when (issue) {
    SettingValidationIssue.InvalidNumber -> stringResource(R.string.setting_edit_invalid_number)
    SettingValidationIssue.InvalidUrl -> stringResource(R.string.message_invalid_url)
    SettingValidationIssue.InvalidCors -> stringResource(R.string.setting_edit_invalid_cors)
}

@Composable
private fun accountValidationMessage(issue: AccountValidationIssue): String = when (issue) {
    AccountValidationIssue.UsernameBlank -> stringResource(R.string.setting_account_invalid_username)
    AccountValidationIssue.OldPasswordBlank -> stringResource(R.string.setting_account_invalid_old_password)
    AccountValidationIssue.NewPasswordBlank -> stringResource(R.string.setting_account_invalid_new_password)
    AccountValidationIssue.PasswordMismatch -> stringResource(R.string.setting_account_password_mismatch)
    AccountValidationIssue.PasswordTooShort -> stringResource(R.string.setting_account_password_too_short)
}

@Composable
private fun ChangeUsernameDialog(
    currentUsername: String,
    submitting: Boolean,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var username by remember(currentUsername) { mutableStateOf(currentUsername) }
    var validationIssue by remember(currentUsername) { mutableStateOf<AccountValidationIssue?>(null) }
    val currentValidationIssue = validationIssue

    OverlayDialog(
        show = true,
        title = stringResource(R.string.setting_account_change_username),
        summary = stringResource(R.string.setting_account_relogin_summary),
        onDismissRequest = onDismiss,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            TextField(
                value = username,
                onValueChange = {
                    username = it
                    validationIssue = null
                },
                label = stringResource(R.string.setting_account_new_username_hint),
                useLabelAsPlaceholder = true,
                singleLine = true,
                enabled = !submitting,
                modifier = Modifier.fillMaxWidth(),
            )
            currentValidationIssue?.let { issue ->
                OperationErrorCard(message = accountValidationMessage(issue))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(text = stringResource(R.string.common_cancel), enabled = !submitting, onClick = onDismiss)
                TextButton(
                    text = if (submitting) stringResource(R.string.common_saving) else stringResource(R.string.common_confirm),
                    enabled = canSubmitUsernameChange(username, submitting),
                    onClick = {
                        val issue = validateUsernameChange(username)
                        if (issue != null) {
                            validationIssue = issue
                            return@TextButton
                        }
                        onConfirm(username.trim())
                    },
                )
            }
        }
    }
}

@Composable
private fun ChangePasswordDialog(
    submitting: Boolean,
    onConfirm: (String, String) -> Unit,
    onDismiss: () -> Unit,
) {
    SecureVisibleWindow()
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var validationIssue by remember { mutableStateOf<AccountValidationIssue?>(null) }
    val currentValidationIssue = validationIssue
    val passwordVisualTransformation: VisualTransformation = PasswordVisualTransformation()

    OverlayDialog(
        show = true,
        title = stringResource(R.string.setting_account_change_password),
        summary = stringResource(R.string.setting_account_relogin_summary),
        onDismissRequest = onDismiss,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            TextField(
                value = oldPassword,
                onValueChange = {
                    oldPassword = it
                    validationIssue = null
                },
                label = stringResource(R.string.setting_account_old_password_hint),
                useLabelAsPlaceholder = true,
                singleLine = true,
                visualTransformation = passwordVisualTransformation,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                enabled = !submitting,
                modifier = Modifier.fillMaxWidth(),
            )
            TextField(
                value = newPassword,
                onValueChange = {
                    newPassword = it
                    validationIssue = null
                },
                label = stringResource(R.string.setting_account_new_password_hint),
                useLabelAsPlaceholder = true,
                singleLine = true,
                visualTransformation = passwordVisualTransformation,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                enabled = !submitting,
                modifier = Modifier.fillMaxWidth(),
            )
            TextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    validationIssue = null
                },
                label = stringResource(R.string.setting_account_confirm_password_hint),
                useLabelAsPlaceholder = true,
                singleLine = true,
                visualTransformation = passwordVisualTransformation,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                enabled = !submitting,
                modifier = Modifier.fillMaxWidth(),
            )
            currentValidationIssue?.let { issue ->
                OperationErrorCard(message = accountValidationMessage(issue))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(text = stringResource(R.string.common_cancel), enabled = !submitting, onClick = onDismiss)
                TextButton(
                    text = if (submitting) stringResource(R.string.common_saving) else stringResource(R.string.common_confirm),
                    enabled = canSubmitPasswordChange(oldPassword, newPassword, confirmPassword, submitting),
                    onClick = {
                        val issue = validatePasswordChange(oldPassword, newPassword, confirmPassword)
                        if (issue != null) {
                            validationIssue = issue
                            return@TextButton
                        }
                        onConfirm(oldPassword, newPassword)
                    },
                )
            }
        }
    }
}

@Composable
private fun SettingEditDialog(
    item: SettingItem,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    val isNumeric = item.key in NUMERIC_KEYS
    var editValue by remember(item.key, item.value) { mutableStateOf(item.value) }
    var validationIssue by remember(item.key, item.value) { mutableStateOf<SettingValidationIssue?>(null) }
    val currentValidationIssue = validateSettingValue(item.key, editValue) ?: validationIssue

    OverlayDialog(
        show = true,
        onDismissRequest = onDismiss,
    ) {
        Column(modifier = Modifier.padding(horizontal = 4.dp)) {
            Text(
                text = settingItemTitle(item.key),
                style = MiuixTheme.textStyles.title3,
                fontWeight = FontWeight.SemiBold,
            )
            TextField(
                value = editValue,
                onValueChange = {
                    editValue = it
                    validationIssue = null
                },
                label = stringResource(R.string.setting_edit_hint),
                useLabelAsPlaceholder = true,
                singleLine = isNumeric,
                maxLines = if (isNumeric) 1 else 4,
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (isNumeric) KeyboardType.Number else KeyboardType.Text,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
            )
            currentValidationIssue?.let { issue ->
                OperationErrorCard(
                    message = settingValidationMessage(issue),
                    modifier = Modifier.padding(top = 10.dp),
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(text = stringResource(R.string.common_cancel), onClick = onDismiss)
                TextButton(
                    text = stringResource(R.string.common_confirm),
                    enabled = canSubmitSettingEdit(item.key, editValue),
                    onClick = {
                        val issue = validateSettingValue(item.key, editValue)
                        if (issue != null) {
                            validationIssue = issue
                            return@TextButton
                        }
                        onConfirm(editValue.trim())
                    },
                )
            }
        }
    }
}

private suspend fun writeBytesToUri(
    context: Context,
    uri: Uri,
    bytes: ByteArray,
): Result<Unit> = withContext(Dispatchers.IO) {
    runCatching {
        val stream = context.contentResolver.openOutputStream(uri)
            ?: throw IOException("Cannot open destination file.")
        stream.use { it.write(bytes) }
    }
}

private suspend fun readBytesFromUri(
    context: Context,
    uri: Uri,
): Result<ByteArray> = withContext(Dispatchers.IO) {
    runCatching {
        context.contentResolver.openInputStream(uri)?.use { input ->
            val output = ByteArrayOutputStream()
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var total = 0
            while (true) {
                val read = input.read(buffer)
                if (read == -1) break
                total += read
                if (total > MAX_IMPORT_FILE_BYTES) {
                    throw ImportFileTooLargeException()
                }
                output.write(buffer, 0, read)
            }
            output.toByteArray()
        } ?: throw IOException("Cannot open import file.")
    }
}

private fun displayNameForUri(
    context: Context,
    uri: Uri,
): String {
    val projection = arrayOf(OpenableColumns.DISPLAY_NAME)
    val name = context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
    }
    return name
        ?.replace(Regex("""[\\/:*?"<>|\p{Cntrl}]"""), "_")
        ?.trim()
        ?.take(80)
        ?.takeIf { it.isNotBlank() }
        ?: "octopus-import.json"
}

private const val MAX_IMPORT_FILE_BYTES = 20 * 1024 * 1024
private const val MIN_ACCOUNT_PASSWORD_LENGTH = 6

private class ImportFileTooLargeException : IOException()
