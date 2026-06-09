package com.elykia.octopus.feature.channel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.elykia.octopus.R
import com.elykia.octopus.core.data.model.BaseUrl
import com.elykia.octopus.core.data.model.Channel
import com.elykia.octopus.core.data.model.CustomHeader
import com.elykia.octopus.core.data.model.ProxyConfiguration
import com.elykia.octopus.core.data.model.ProxyMode
import com.elykia.octopus.core.designsystem.DialogScrollableColumn
import com.elykia.octopus.core.designsystem.OctopusTokens
import com.elykia.octopus.core.designsystem.OperationErrorCard
import com.elykia.octopus.core.designsystem.SecureVisibleWindow
import com.elykia.octopus.core.designsystem.ToolbarChip
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun ChannelEditorDialog(
    visible: Boolean,
    title: String,
    initialChannel: Channel?,
    proxyConfigurations: List<ProxyConfiguration>,
    proxyConfigurationError: String?,
    submitting: Boolean,
    operationError: String?,
    onFetchModels: (ChannelEditorValues) -> Unit,
    onConfirm: (ChannelEditorValues) -> Unit,
    onDismiss: () -> Unit,
) {
    if (!visible) return

    SecureVisibleWindow()

    var values by remember(initialChannel?.id, visible) {
        mutableStateOf(initialChannel.toEditorValues())
    }
    val fetchRequiresKey = values.keys.none { it.channelKey.isNotBlank() }
    val editorScrollState = rememberScrollState()

    OverlayDialog(
        show = visible,
        title = title,
        summary = stringResource(R.string.channel_panel_summary),
        onDismissRequest = {
            if (!submitting) onDismiss()
        },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DialogScrollableColumn(
                fraction = 0.68f,
                scrollState = editorScrollState,
            ) {
                operationError?.takeIf { it.isNotBlank() }?.let { error ->
                    OperationErrorCard(message = error)
                }

                TextField(
                    value = values.name,
                    onValueChange = { values = values.copy(name = it) },
                    label = stringResource(R.string.channel_name_hint),
                    useLabelAsPlaceholder = true,
                    singleLine = true,
                    enabled = !submitting,
                    modifier = Modifier.fillMaxWidth(),
                )

                Text(
                    text = stringResource(R.string.channel_type_label),
                    style = MiuixTheme.textStyles.main,
                    fontWeight = FontWeight.SemiBold,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    (0..5).forEach { optionType ->
                        ChannelTypeOption(type = optionType, selectedType = values.type) {
                            if (!submitting) values = values.copy(type = optionType)
                        }
                    }
                }

                ChannelSwitchRow(
                    label = stringResource(R.string.channel_enabled_label),
                    checked = values.enabled,
                    enabled = !submitting,
                    onCheckedChange = { values = values.copy(enabled = it) },
                )
                ChannelProxySelector(
                    proxyMode = values.proxyMode,
                    selectedProxyConfigId = values.proxyConfigId,
                    proxyConfigurations = proxyConfigurations,
                    proxyConfigurationError = proxyConfigurationError,
                    submitting = submitting,
                    onModeChange = { mode ->
                        values = values.copy(
                            proxyMode = mode,
                            proxyConfigId = values.proxyConfigId.takeIf { mode == ProxyMode.Pool },
                        )
                    },
                    onProxySelect = { values = values.copy(proxyConfigId = it) },
                )
                ChannelSwitchRow(
                    label = stringResource(R.string.channel_auto_sync_label),
                    checked = values.autoSync,
                    enabled = !submitting,
                    onCheckedChange = { values = values.copy(autoSync = it) },
                )

                EditableBaseUrlList(
                    baseUrls = values.baseUrls,
                    submitting = submitting,
                    onChange = { values = values.copy(baseUrls = it) },
                )
                EditableKeyList(
                    keys = values.keys,
                    submitting = submitting,
                    onChange = { values = values.copy(keys = it) },
                )

                TextField(
                    value = values.model,
                    onValueChange = { values = values.copy(model = it) },
                    label = stringResource(R.string.channel_model_hint),
                    useLabelAsPlaceholder = true,
                    singleLine = false,
                    maxLines = 3,
                    enabled = !submitting,
                    modifier = Modifier.fillMaxWidth(),
                )
                TextField(
                    value = values.customModel,
                    onValueChange = { values = values.copy(customModel = it) },
                    label = stringResource(R.string.channel_custom_model_hint),
                    useLabelAsPlaceholder = true,
                    singleLine = false,
                    maxLines = 3,
                    enabled = !submitting,
                    modifier = Modifier.fillMaxWidth(),
                )

                Text(
                    text = stringResource(R.string.channel_advanced_title),
                    style = MiuixTheme.textStyles.main,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(R.string.channel_auto_group_label),
                    style = MiuixTheme.textStyles.body2,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    (0..3).forEach { autoGroup ->
                        ToolbarChip(
                            text = autoGroupName(autoGroup),
                            selected = values.autoGroup == autoGroup,
                            onClick = {
                                if (!submitting) values = values.copy(autoGroup = autoGroup)
                            },
                        )
                    }
                }
                TextField(
                    value = values.channelProxy,
                    onValueChange = { values = values.copy(channelProxy = it) },
                    label = stringResource(R.string.channel_proxy_url_hint),
                    useLabelAsPlaceholder = true,
                    singleLine = true,
                    enabled = !submitting,
                    modifier = Modifier.fillMaxWidth(),
                )
                EditableHeaderList(
                    headers = values.customHeader,
                    submitting = submitting,
                    onChange = { values = values.copy(customHeader = it) },
                )
                TextField(
                    value = values.matchRegex,
                    onValueChange = { values = values.copy(matchRegex = it) },
                    label = stringResource(R.string.channel_match_regex_hint),
                    useLabelAsPlaceholder = true,
                    singleLine = true,
                    enabled = !submitting,
                    modifier = Modifier.fillMaxWidth(),
                )
                TextField(
                    value = values.paramOverride,
                    onValueChange = { values = values.copy(paramOverride = it) },
                    label = stringResource(R.string.channel_param_override_hint),
                    useLabelAsPlaceholder = true,
                    singleLine = false,
                    maxLines = 5,
                    enabled = !submitting,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
            ) {
                TextButton(
                    text = stringResource(
                        if (fetchRequiresKey) {
                            R.string.channel_fetch_model_needs_key
                        } else {
                            R.string.action_fetch_model
                        },
                    ),
                    enabled = !submitting && !fetchRequiresKey && hasValidChannelBaseUrls(values.baseUrls),
                    onClick = { onFetchModels(values) },
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(text = stringResource(R.string.common_cancel), enabled = !submitting, onClick = onDismiss)
                TextButton(
                    text = if (submitting) {
                        stringResource(R.string.common_saving)
                    } else {
                        stringResource(R.string.common_confirm)
                    },
                    enabled = canSubmitChannelEditor(values = values, submitting = submitting),
                    onClick = { onConfirm(values) },
                )
            }
        }
    }
}

@Composable
private fun EditableBaseUrlList(
    baseUrls: List<BaseUrl>,
    submitting: Boolean,
    onChange: (List<BaseUrl>) -> Unit,
) {
    ChannelSectionHeader(
        title = stringResource(R.string.channel_base_urls_label, baseUrls.size),
        enabled = !submitting,
        onAdd = { onChange(baseUrls + BaseUrl(url = "")) },
    )
    baseUrls.forEachIndexed { index, baseUrl ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextField(
                value = baseUrl.url,
                onValueChange = { next ->
                    onChange(baseUrls.mapIndexed { i, item -> if (i == index) item.copy(url = next) else item })
                },
                label = stringResource(R.string.channel_base_url_hint),
                useLabelAsPlaceholder = true,
                singleLine = true,
                enabled = !submitting,
                modifier = Modifier.weight(1f),
            )
            TextButton(
                text = stringResource(R.string.common_delete),
                enabled = !submitting && baseUrls.size > 1,
                onClick = { onChange(baseUrls.filterIndexed { i, _ -> i != index }) },
            )
        }
    }
}

@Composable
private fun EditableKeyList(
    keys: List<ChannelKeyEditorItem>,
    submitting: Boolean,
    onChange: (List<ChannelKeyEditorItem>) -> Unit,
) {
    ChannelSectionHeader(
        title = stringResource(R.string.channel_keys_label, keys.size),
        enabled = !submitting,
        onAdd = { onChange(keys + ChannelKeyEditorItem()) },
    )
    keys.forEachIndexed { index, key ->
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextField(
                    value = key.channelKey,
                    onValueChange = { next ->
                        onChange(keys.mapIndexed { i, item -> if (i == index) item.copy(channelKey = next) else item })
                    },
                    label = if (key.id == null) {
                        stringResource(R.string.channel_api_key_hint)
                    } else {
                        stringResource(R.string.channel_api_key_replace_hint)
                    },
                    useLabelAsPlaceholder = true,
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    enabled = !submitting,
                    modifier = Modifier.weight(1f),
                )
                Switch(
                    checked = key.enabled,
                    onCheckedChange = { checked ->
                        if (!submitting) {
                            onChange(keys.mapIndexed { i, item -> if (i == index) item.copy(enabled = checked) else item })
                        }
                    },
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextField(
                    value = key.remark,
                    onValueChange = { next ->
                        onChange(keys.mapIndexed { i, item -> if (i == index) item.copy(remark = next) else item })
                    },
                    label = stringResource(R.string.channel_key_remark_hint),
                    useLabelAsPlaceholder = true,
                    singleLine = true,
                    enabled = !submitting,
                    modifier = Modifier.weight(1f),
                )
                TextButton(
                    text = stringResource(R.string.common_delete),
                    enabled = !submitting && keys.size > 1,
                    onClick = { onChange(keys.filterIndexed { i, _ -> i != index }) },
                )
            }
        }
    }
}

@Composable
private fun EditableHeaderList(
    headers: List<CustomHeader>,
    submitting: Boolean,
    onChange: (List<CustomHeader>) -> Unit,
) {
    ChannelSectionHeader(
        title = stringResource(R.string.channel_custom_headers_label, headers.size),
        enabled = !submitting,
        onAdd = { onChange(headers + CustomHeader(headerKey = "", headerValue = "")) },
    )
    headers.forEachIndexed { index, header ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextField(
                value = header.headerKey,
                onValueChange = { next ->
                    onChange(headers.mapIndexed { i, item -> if (i == index) item.copy(headerKey = next) else item })
                },
                label = stringResource(R.string.channel_header_key_hint),
                useLabelAsPlaceholder = true,
                singleLine = true,
                enabled = !submitting,
                modifier = Modifier.weight(1f),
            )
            TextField(
                value = header.headerValue,
                onValueChange = { next ->
                    onChange(headers.mapIndexed { i, item -> if (i == index) item.copy(headerValue = next) else item })
                },
                label = stringResource(R.string.channel_header_value_hint),
                useLabelAsPlaceholder = true,
                singleLine = true,
                enabled = !submitting,
                modifier = Modifier.weight(1f),
            )
            TextButton(
                text = stringResource(R.string.common_delete),
                enabled = !submitting && headers.size > 1,
                onClick = { onChange(headers.filterIndexed { i, _ -> i != index }) },
            )
        }
    }
}

@Composable
private fun ChannelSectionHeader(
    title: String,
    enabled: Boolean,
    onAdd: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MiuixTheme.textStyles.main,
            fontWeight = FontWeight.SemiBold,
        )
        TextButton(
            text = stringResource(R.string.channel_add_row),
            enabled = enabled,
            onClick = onAdd,
        )
    }
}

@Composable
private fun ChannelSwitchRow(
    label: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, style = MiuixTheme.textStyles.main)
        Switch(checked = checked, onCheckedChange = { if (enabled) onCheckedChange(it) })
    }
}

@Composable
private fun ChannelProxySelector(
    proxyMode: String,
    selectedProxyConfigId: Int?,
    proxyConfigurations: List<ProxyConfiguration>,
    proxyConfigurationError: String?,
    submitting: Boolean,
    onModeChange: (String) -> Unit,
    onProxySelect: (Int) -> Unit,
) {
    Text(
        text = stringResource(R.string.site_proxy_mode_label),
        style = MiuixTheme.textStyles.main,
        fontWeight = FontWeight.SemiBold,
    )
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(ProxyMode.Direct, ProxyMode.System, ProxyMode.Pool).forEach { mode ->
            ToolbarChip(
                text = channelProxyModeLabel(mode),
                selected = proxyMode == mode,
                onClick = { if (!submitting) onModeChange(mode) },
            )
        }
    }
    if (proxyMode != ProxyMode.Pool) return

    val options = proxyConfigurations.filter { it.enabled || it.id == selectedProxyConfigId }
    Text(
        text = stringResource(R.string.site_proxy_pool_config_label),
        style = MiuixTheme.textStyles.main,
        fontWeight = FontWeight.SemiBold,
    )
    proxyConfigurationError?.takeIf { it.isNotBlank() }?.let { OperationErrorCard(message = it) }
    if (options.isEmpty()) {
        Text(
            text = stringResource(R.string.site_proxy_pool_empty),
            style = MiuixTheme.textStyles.body2,
            color = OctopusTokens.TextSecondary,
        )
        return
    }
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { proxy ->
            ToolbarChip(
                text = proxyConfigurationLabel(proxy),
                selected = selectedProxyConfigId == proxy.id,
                onClick = { if (!submitting && proxy.enabled) onProxySelect(proxy.id) },
            )
        }
    }
    options.firstOrNull { it.id == selectedProxyConfigId && !it.enabled }?.let { proxy ->
        Text(
            text = stringResource(R.string.site_proxy_pool_disabled_selected, proxyConfigurationLabel(proxy)),
            style = MiuixTheme.textStyles.body2,
            color = MiuixTheme.colorScheme.error,
        )
    }
}

@Composable
private fun channelProxyModeLabel(mode: String): String = when (mode) {
    ProxyMode.System -> stringResource(R.string.site_proxy_system)
    ProxyMode.Pool -> stringResource(R.string.site_proxy_pool)
    else -> stringResource(R.string.site_proxy_direct)
}

@Composable
private fun proxyConfigurationLabel(proxy: ProxyConfiguration): String =
    proxy.name.ifBlank { "#${proxy.id}" } + if (proxy.enabled) "" else " ${stringResource(R.string.common_disabled)}"

@Composable
private fun ChannelTypeOption(
    type: Int,
    selectedType: Int,
    onSelect: () -> Unit,
) {
    ToolbarChip(
        text = channelTypeName(type),
        selected = selectedType == type,
        onClick = onSelect,
    )
}

@Composable
private fun autoGroupName(value: Int): String = when (value) {
    0 -> stringResource(R.string.channel_auto_group_none)
    1 -> stringResource(R.string.channel_auto_group_fuzzy)
    2 -> stringResource(R.string.channel_auto_group_exact)
    3 -> stringResource(R.string.channel_auto_group_regex)
    else -> stringResource(R.string.channel_auto_group_unknown, value)
}
