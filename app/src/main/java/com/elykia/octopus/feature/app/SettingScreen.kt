package com.elykia.octopus.feature.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.elykia.octopus.core.designsystem.AppInfoChip
import com.elykia.octopus.core.designsystem.AppListCard
import com.elykia.octopus.core.designsystem.AppPageScaffold
import com.elykia.octopus.core.designsystem.ErrorPane
import com.elykia.octopus.core.designsystem.LoadingPane
import com.elykia.octopus.core.designsystem.PageActionButton
import com.elykia.octopus.core.designsystem.icons.AppMiuixIcons
import com.elykia.octopus.feature.setting.SettingViewModel
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun SettingScreen(
    contentPadding: PaddingValues,
    viewModel: SettingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var language by remember(uiState.language) { mutableStateOf(uiState.language) }
    var themeMode by remember(uiState.themeMode) { mutableStateOf(uiState.themeMode) }

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
                    VersionCard(
                        currentVersion = uiState.currentVersion ?: stringResource(R.string.common_unknown),
                        latestVersion = uiState.latestInfo?.tagName ?: stringResource(R.string.common_unknown),
                        publishedAt = uiState.latestInfo?.publishedAt,
                    )
                    PreferenceRow(
                        title = stringResource(R.string.action_check_update),
                        value = uiState.latestInfo?.tagName ?: stringResource(R.string.common_unknown),
                        summary = stringResource(R.string.setting_refresh_latest_summary),
                        onClick = viewModel::refreshLatestInfo,
                    )
                    PreferenceRow(
                        title = stringResource(R.string.action_run_update),
                        value = stringResource(R.string.setting_action_update),
                        summary = stringResource(R.string.setting_update_now_summary),
                        onClick = viewModel::triggerUpdate,
                    )
                    PreferenceRow(
                        title = stringResource(R.string.setting_language_system),
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
                        title = stringResource(R.string.setting_appearance_title),
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
                    PreferenceRow(
                        title = stringResource(R.string.setting_action_refresh_price),
                        value = uiState.modelLastUpdateTime ?: stringResource(R.string.common_unknown),
                        onClick = viewModel::refreshModelPrice,
                    )
                    PreferenceRow(
                        title = stringResource(R.string.setting_action_sync_channel),
                        value = stringResource(R.string.setting_llm_sync_title),
                        onClick = viewModel::syncChannelModels,
                    )
                    uiState.sections.forEach { section ->
                        Text(
                            text = sectionTitle(section.key),
                            style = MiuixTheme.textStyles.title3,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            section.items.forEach { item ->
                                PreferenceRow(
                                    title = settingItemTitle(item.key),
                                    value = item.value.ifBlank { stringResource(R.string.common_unknown) },
                                    onClick = {
                                        if (item.key == "relay_log_keep_enabled") {
                                            val toggled = if (item.value == "true") "false" else "true"
                                            viewModel.updateSetting(item.key, toggled)
                                        }
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
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
private fun PreferenceRow(
    title: String,
    value: String,
    summary: String? = null,
    onClick: () -> Unit,
) {
    AppListCard(onClick = onClick) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(text = title, style = MiuixTheme.textStyles.main)
                Text(
                    text = value,
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
            if (!summary.isNullOrBlank()) {
                Text(
                    text = summary,
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
        }
    }
}

@Composable
private fun sectionTitle(key: String): String = when (key) {
    "system" -> stringResource(R.string.setting_system_title)
    "log" -> stringResource(R.string.setting_log_title)
    "price" -> stringResource(R.string.setting_llm_price_title)
    "sync" -> stringResource(R.string.setting_llm_sync_title)
    "circuit" -> stringResource(R.string.setting_circuit_title)
    else -> key
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
