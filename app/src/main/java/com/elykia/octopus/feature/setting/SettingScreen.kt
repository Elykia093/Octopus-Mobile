package com.elykia.octopus.feature.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.elykia.octopus.R
import com.elykia.octopus.core.data.model.SettingItem
import com.elykia.octopus.core.designsystem.AppListCard
import com.elykia.octopus.core.designsystem.AppPageScaffold
import com.elykia.octopus.core.designsystem.ErrorStateCard
import com.elykia.octopus.core.designsystem.LoadingStateCard
import com.elykia.octopus.core.designsystem.OctopusBrandMark
import com.elykia.octopus.core.designsystem.OctopusTokens
import com.elykia.octopus.core.designsystem.icons.AppMiuixIcons
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun SettingScreen(
    contentPadding: PaddingValues,
    viewModel: SettingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var language by remember(uiState.language) { mutableStateOf(uiState.language) }
    var themeMode by remember(uiState.themeMode) { mutableIntStateOf(uiState.themeMode) }
    var editingItem by remember { mutableStateOf<SettingItem?>(null) }

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

    AppPageScaffold(
        title = stringResource(R.string.setting_title),
        contentPadding = contentPadding,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            when {
                uiState.loading -> LoadingStateCard(title = stringResource(R.string.setting_title))
                uiState.error != null -> ErrorStateCard(
                    message = uiState.error ?: stringResource(R.string.error_title),
                    onRetry = viewModel::refresh,
                )
                else -> {
                    VersionCard(
                        currentVersion = uiState.currentVersion ?: stringResource(R.string.common_unknown),
                        latestVersion = uiState.latestInfo?.tagName ?: stringResource(R.string.common_unknown),
                        publishedAt = uiState.latestInfo?.publishedAt,
                    )

                    SettingSectionCard(title = stringResource(R.string.setting_preferences_title)) {
                        PreferenceRow(
                            icon = AppMiuixIcons.Setting,
                            title = stringResource(R.string.setting_language_label),
                            value = languageLabel,
                            onClick = {
                                language = when (language) {
                                    "system" -> "zh-CN"
                                    "zh-CN" -> "en"
                                    else -> "system"
                                }
                                viewModel.updateAppearance(language, themeMode)
                            },
                        )
                        SettingDivider()
                        PreferenceRow(
                            icon = AppMiuixIcons.Toggle,
                            title = stringResource(R.string.setting_theme_label),
                            value = themeLabel,
                            onClick = {
                                themeMode = when (themeMode) {
                                    0 -> 1
                                    1 -> 2
                                    else -> 0
                                }
                                viewModel.updateAppearance(language, themeMode)
                            },
                        )
                    }

                    SettingSectionCard(title = stringResource(R.string.setting_actions_title)) {
                        PreferenceRow(
                            icon = AppMiuixIcons.Refresh,
                            title = stringResource(R.string.action_check_update),
                            value = uiState.latestInfo?.tagName ?: stringResource(R.string.common_unknown),
                            onClick = viewModel::refreshLatestInfo,
                        )
                        SettingDivider()
                        PreferenceRow(
                            icon = AppMiuixIcons.Cost,
                            title = stringResource(R.string.setting_action_refresh_price),
                            value = uiState.modelLastUpdateTime ?: stringResource(R.string.common_unknown),
                            onClick = viewModel::refreshModelPrice,
                        )
                        SettingDivider()
                        PreferenceRow(
                            icon = AppMiuixIcons.Sync,
                            title = stringResource(R.string.setting_action_sync_channel),
                            onClick = viewModel::syncChannelModels,
                        )
                    }

                    val serverItems = uiState.sections.flatMap { it.items }
                    SettingSectionCard(title = stringResource(R.string.setting_server_title)) {
                        serverItems.forEachIndexed { index, item ->
                            if (index > 0) SettingDivider()
                            SettingItemRow(
                                item = item,
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
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingIconBox(icon = icon)
        Text(
            text = title,
            style = MiuixTheme.textStyles.main,
            color = OctopusTokens.TextPrimary,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (value != null) {
            Text(
                text = value,
                style = MiuixTheme.textStyles.body2,
                color = OctopusTokens.TextSecondary,
                modifier = Modifier.weight(0.9f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.End,
            )
        }
    }
}

@Composable
private fun SettingItemRow(
    item: SettingItem,
    onEdit: () -> Unit,
    onToggle: (Boolean) -> Unit,
) {
    if (item.key == "relay_log_keep_enabled") {
        SwitchRow(
            icon = AppMiuixIcons.Log,
            title = settingItemTitle(item.key),
            checked = item.value == "true",
            onCheckedChange = onToggle,
        )
    } else {
        PreferenceRow(
            icon = settingItemIcon(item.key),
            title = settingItemTitle(item.key),
            value = item.value.ifBlank { stringResource(R.string.common_unknown) },
            onClick = onEdit,
        )
    }
}

@Composable
private fun SettingIconBox(
    icon: ImageVector,
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(OctopusTokens.PrimarySoft),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = OctopusTokens.Accent,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun SwitchRow(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingIconBox(icon = icon)
        Text(
            text = title,
            style = MiuixTheme.textStyles.main,
            color = OctopusTokens.TextPrimary,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
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

@Composable
private fun SettingEditDialog(
    item: SettingItem,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    val isNumeric = item.key in NUMERIC_KEYS
    var editValue by remember { mutableStateOf(item.value) }

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
                onValueChange = { editValue = it },
                label = stringResource(R.string.setting_edit_hint),
                useLabelAsPlaceholder = true,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(text = stringResource(R.string.common_cancel), onClick = onDismiss)
                TextButton(
                    text = stringResource(R.string.common_confirm),
                    onClick = {
                        if (isNumeric && editValue.toDoubleOrNull() == null) return@TextButton
                        onConfirm(editValue)
                    },
                )
            }
        }
    }
}
