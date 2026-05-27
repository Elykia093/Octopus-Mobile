package com.elykia.octopus.feature.setting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.elykia.octopus.R
import com.elykia.octopus.core.data.model.SettingItem
import com.elykia.octopus.core.designsystem.AppInfoChip
import com.elykia.octopus.core.designsystem.AppListCard
import com.elykia.octopus.core.designsystem.AppPageScaffold
import com.elykia.octopus.core.designsystem.ErrorPane
import com.elykia.octopus.core.designsystem.LoadingPane
import com.elykia.octopus.core.designsystem.PageActionButton
import com.elykia.octopus.core.designsystem.SectionSpacer
import com.elykia.octopus.core.designsystem.icons.AppMiuixIcons
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
    val uiState by viewModel.uiState.collectAsState()
    var language by remember(uiState.language) { mutableStateOf(uiState.language) }
    var themeMode by remember(uiState.themeMode) { mutableStateOf(uiState.themeMode) }
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

    when {
        uiState.loading -> LoadingPane(title = stringResource(R.string.setting_title))
        uiState.error != null -> ErrorPane(message = uiState.error ?: stringResource(R.string.error_title), onRetry = viewModel::refresh)
        else -> {
            AppPageScaffold(
                title = stringResource(R.string.setting_title),
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
                    // 版本信息卡片
                    VersionCard(
                        currentVersion = uiState.currentVersion ?: stringResource(R.string.common_unknown),
                        latestVersion = uiState.latestInfo?.tagName ?: stringResource(R.string.common_unknown),
                        publishedAt = uiState.latestInfo?.publishedAt,
                    )

                    // 偏好设置区
                    SectionLabel(title = stringResource(R.string.setting_preferences_title))
                    PreferenceRow(
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
                    PreferenceRow(
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

                    SectionSpacer(height = 8)

                    // 操作区
                    SectionLabel(title = stringResource(R.string.setting_actions_title))
                    PreferenceRow(
                        title = stringResource(R.string.action_check_update),
                        value = uiState.latestInfo?.tagName ?: stringResource(R.string.common_unknown),
                        onClick = viewModel::refreshLatestInfo,
                    )
                    PreferenceRow(
                        title = stringResource(R.string.setting_action_refresh_price),
                        value = uiState.modelLastUpdateTime ?: stringResource(R.string.common_unknown),
                        onClick = viewModel::refreshModelPrice,
                    )
                    PreferenceRow(
                        title = stringResource(R.string.setting_action_sync_channel),
                        onClick = viewModel::syncChannelModels,
                    )

                    SectionSpacer(height = 8)

                    // 服务器设置区
                    SectionLabel(title = stringResource(R.string.setting_server_title))
                    uiState.sections.forEach { section ->
                        section.items.forEach { item ->
                            if (item.key == "relay_log_keep_enabled") {
                                // 布尔值：右侧 Switch
                                SwitchRow(
                                    title = settingItemTitle(item.key),
                                    checked = item.value == "true",
                                    onCheckedChange = { checked ->
                                        viewModel.updateSetting(item.key, if (checked) "true" else "false")
                                    },
                                )
                            } else {
                                // 其他：点击编辑
                                PreferenceRow(
                                    title = settingItemTitle(item.key),
                                    value = item.value.ifBlank { stringResource(R.string.common_unknown) },
                                    onClick = { editingItem = item },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // 编辑弹窗
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
    AppListCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AppInfoChip(text = stringResource(R.string.setting_info_title), icon = AppMiuixIcons.Info)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = stringResource(R.string.setting_info_current, currentVersion), style = MiuixTheme.textStyles.main, fontWeight = FontWeight.SemiBold)
                Text(text = stringResource(R.string.setting_info_latest, latestVersion), style = MiuixTheme.textStyles.body2, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                if (!publishedAt.isNullOrBlank()) {
                    Text(text = stringResource(R.string.setting_info_published, publishedAt), style = MiuixTheme.textStyles.body2, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(title: String) {
    Text(
        text = title,
        style = MiuixTheme.textStyles.title3,
        fontWeight = FontWeight.SemiBold,
        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
    )
}

@Composable
private fun PreferenceRow(
    title: String,
    value: String? = null,
    onClick: () -> Unit,
) {
    AppListCard(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = title, style = MiuixTheme.textStyles.main)
            if (value != null) {
                Text(
                    text = value,
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
        }
    }
}

@Composable
private fun SwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    AppListCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = title, style = MiuixTheme.textStyles.main)
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
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
