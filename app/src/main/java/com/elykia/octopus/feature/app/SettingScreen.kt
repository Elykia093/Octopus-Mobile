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
import com.elykia.octopus.core.designsystem.ErrorPane
import com.elykia.octopus.core.designsystem.LabelValueCard
import com.elykia.octopus.core.designsystem.LoadingPane
import com.elykia.octopus.core.designsystem.NumberSettingCard
import com.elykia.octopus.core.designsystem.ScreenPane
import com.elykia.octopus.core.designsystem.SectionCard
import com.elykia.octopus.core.designsystem.SettingItemCard
import com.elykia.octopus.core.designsystem.ToolbarChip
import com.elykia.octopus.feature.setting.SettingViewModel

@Composable
fun SettingScreen(
    contentPadding: PaddingValues,
    viewModel: SettingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var language by remember(uiState.language) { mutableStateOf(uiState.language) }
    var themeMode by remember(uiState.themeMode) { mutableStateOf(uiState.themeMode) }
    val languageLabel = when (uiState.language) {
        "zh-CN" -> stringResource(R.string.setting_language_zh_cn)
        "en" -> stringResource(R.string.setting_language_en)
        else -> stringResource(R.string.setting_language_system)
    }
    val themeLabel = when (uiState.themeMode) {
        1 -> stringResource(R.string.setting_theme_light)
        2 -> stringResource(R.string.setting_theme_dark)
        else -> stringResource(R.string.setting_theme_system)
    }

    when {
        uiState.loading -> LoadingPane(title = stringResource(R.string.setting_title))
        uiState.error != null -> ErrorPane(message = uiState.error ?: stringResource(R.string.error_title), onRetry = viewModel::refresh)
        else -> {
            ScreenPane(contentPadding = contentPadding) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    SectionCard(
                        title = stringResource(R.string.setting_info_title),
                        summary = stringResource(R.string.setting_info_summary),
                        actions = {
                            ToolbarChip(text = stringResource(R.string.setting_action_update), selected = true, onClick = viewModel::triggerUpdate)
                        },
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            SettingItemCard(
                                title = stringResource(R.string.setting_info_current, uiState.currentVersion ?: stringResource(R.string.common_unknown)),
                                summary = stringResource(R.string.setting_info_latest, uiState.latestInfo?.tagName ?: stringResource(R.string.common_unknown)),
                                extra = uiState.latestInfo?.publishedAt?.let { stringResource(R.string.setting_info_published, it) },
                            )
                            SettingItemCard(
                                title = stringResource(R.string.setting_account_title),
                                summary = stringResource(R.string.setting_account_summary),
                                extra = uiState.username.ifBlank { stringResource(R.string.common_unknown) },
                            )
                            SettingItemCard(
                                title = stringResource(R.string.setting_appearance_title),
                                summary = stringResource(R.string.setting_appearance_summary),
                                extra = stringResource(R.string.setting_appearance_current, languageLabel, themeLabel),
                            )
                        }
                    }
                    uiState.sections.forEach { section ->
                        SectionCard(
                            title = when (section.key) {
                                "system" -> stringResource(R.string.setting_system_title)
                                "log" -> stringResource(R.string.setting_log_title)
                                "price" -> stringResource(R.string.setting_llm_price_title)
                                "sync" -> stringResource(R.string.setting_llm_sync_title)
                                "circuit" -> stringResource(R.string.setting_circuit_title)
                                else -> section.title
                            },
                            summary = when (section.key) {
                                "system" -> stringResource(R.string.setting_system_summary)
                                "log" -> stringResource(R.string.setting_log_summary)
                                "price" -> stringResource(R.string.setting_llm_price_summary)
                                "sync" -> stringResource(R.string.setting_llm_sync_summary)
                                "circuit" -> stringResource(R.string.setting_circuit_summary)
                                else -> section.summary
                            },
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                section.items.forEach { item ->
                                    when (item.key) {
                                        "proxy_url" -> LabelValueCard(
                                            label = stringResource(R.string.setting_proxy_url_label),
                                            value = item.value.ifBlank { stringResource(R.string.common_unknown) },
                                        )

                                        "stats_save_interval" -> LabelValueCard(
                                            label = stringResource(R.string.setting_stats_save_interval_label),
                                            value = stringResource(R.string.setting_stats_save_interval_value, item.value),
                                        )

                                        "cors_allow_origins" -> LabelValueCard(
                                            label = stringResource(R.string.setting_cors_allow_origins_label),
                                            value = item.value.ifBlank { stringResource(R.string.setting_cors_allow_origins_empty) },
                                        )

                                        "relay_log_keep_period" -> NumberSettingCard(
                                            title = stringResource(R.string.setting_log_keep_period_label),
                                            summary = stringResource(R.string.setting_log_keep_period_summary),
                                            value = item.value,
                                            onValueChange = { viewModel.updateSetting(item.key, it) },
                                        )

                                        "relay_log_keep_enabled" -> LabelValueCard(
                                            label = stringResource(R.string.setting_log_keep_enabled_label),
                                            value = if (item.value == "true") stringResource(R.string.common_enabled) else stringResource(R.string.common_disabled),
                                        )

                                        "model_info_update_interval" -> NumberSettingCard(
                                            title = stringResource(R.string.setting_model_info_update_interval_label),
                                            summary = stringResource(R.string.setting_model_info_update_interval_summary),
                                            value = item.value,
                                            onValueChange = { viewModel.updateSetting(item.key, it) },
                                        )

                                        "sync_llm_interval" -> NumberSettingCard(
                                            title = stringResource(R.string.setting_sync_llm_interval_label),
                                            summary = stringResource(R.string.setting_sync_llm_interval_summary),
                                            value = item.value,
                                            onValueChange = { viewModel.updateSetting(item.key, it) },
                                        )

                                        "circuit_breaker_threshold" -> NumberSettingCard(
                                            title = stringResource(R.string.setting_circuit_breaker_threshold_label),
                                            summary = stringResource(R.string.setting_circuit_breaker_threshold_summary),
                                            value = item.value,
                                            onValueChange = { viewModel.updateSetting(item.key, it) },
                                        )

                                        "circuit_breaker_cooldown" -> NumberSettingCard(
                                            title = stringResource(R.string.setting_circuit_breaker_cooldown_label),
                                            summary = stringResource(R.string.setting_circuit_breaker_cooldown_summary),
                                            value = item.value,
                                            onValueChange = { viewModel.updateSetting(item.key, it) },
                                        )

                                        "circuit_breaker_max_cooldown" -> NumberSettingCard(
                                            title = stringResource(R.string.setting_circuit_breaker_max_cooldown_label),
                                            summary = stringResource(R.string.setting_circuit_breaker_max_cooldown_summary),
                                            value = item.value,
                                            onValueChange = { viewModel.updateSetting(item.key, it) },
                                        )

                                        else -> SettingItemCard(title = item.key, summary = item.value)
                                    }
                                }
                            }
                        }
                    }
                    SectionCard(
                        title = stringResource(R.string.setting_apikey_title),
                        summary = stringResource(R.string.setting_apikey_summary),
                    ) {
                        if (uiState.apiKeys.isEmpty()) {
                            SettingItemCard(
                                title = stringResource(R.string.setting_apikey_title),
                                summary = stringResource(R.string.setting_apikey_empty),
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                uiState.apiKeys.forEach { key ->
                                    SettingItemCard(
                                        title = key.name,
                                        summary = stringResource(
                                            R.string.setting_apikey_item_summary,
                                            if (key.enabled) stringResource(R.string.common_enabled) else stringResource(R.string.common_disabled),
                                            key.expireAt?.toString() ?: stringResource(R.string.common_unknown),
                                        ),
                                        extra = key.supportedModels,
                                    )
                                }
                            }
                        }
                    }
                    SectionCard(
                        title = stringResource(R.string.setting_appearance_title),
                        summary = stringResource(R.string.setting_appearance_summary),
                        actions = {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                ToolbarChip(
                                    text = when (language) {
                                        "zh-CN" -> stringResource(R.string.setting_language_zh_cn)
                                        "en" -> stringResource(R.string.setting_language_en)
                                        else -> stringResource(R.string.setting_language_system)
                                    },
                                    selected = language == "zh-CN",
                                    onClick = {
                                        language = if (language == "zh-CN") "system" else "zh-CN"
                                        viewModel.updateAppearance(language, themeMode)
                                    },
                                )
                                ToolbarChip(
                                    text = when (themeMode) {
                                        1 -> stringResource(R.string.setting_theme_light)
                                        2 -> stringResource(R.string.setting_theme_dark)
                                        else -> stringResource(R.string.setting_theme_system)
                                    },
                                    selected = themeMode != 0,
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
                        },
                    ) {
                        SettingItemCard(
                            title = stringResource(R.string.setting_appearance_title),
                            summary = stringResource(R.string.setting_appearance_current, languageLabel, themeLabel),
                        )
                    }
                }
            }
        }
    }
}
