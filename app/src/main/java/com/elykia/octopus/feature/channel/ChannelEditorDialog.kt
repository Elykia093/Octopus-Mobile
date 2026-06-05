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
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.elykia.octopus.R
import com.elykia.octopus.core.data.model.Channel
import com.elykia.octopus.core.designsystem.DialogScrollableColumn
import com.elykia.octopus.core.designsystem.OperationErrorCard
import com.elykia.octopus.core.designsystem.SecureVisibleWindow
import com.elykia.octopus.core.designsystem.ToolbarChip
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme
import com.elykia.octopus.core.designsystem.icons.AppMiuixIcons

/**
 * Channel 编辑器对话框
 */
@Composable
fun ChannelEditorDialog(
    visible: Boolean,
    title: String,
    initialChannel: Channel?,
    submitting: Boolean,
    operationError: String?,
    onFetchModels: (Int, String, String, Boolean) -> Unit,
    onConfirm: (String, Int, Boolean, String, String, String, String, Boolean, Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    if (!visible) return

    SecureVisibleWindow()

    var name by remember(initialChannel?.id, visible) { mutableStateOf(initialChannel?.name.orEmpty()) }
    var type by remember(initialChannel?.id, visible) { mutableStateOf(initialChannel?.type ?: 0) }
    var enabled by remember(initialChannel?.id, visible) { mutableStateOf(initialChannel?.enabled ?: true) }
    var baseUrl by remember(initialChannel?.id, visible) {
        mutableStateOf(initialChannel?.baseUrls?.firstOrNull()?.url.orEmpty())
    }
    var apiKey by remember(initialChannel?.id, visible) {
        mutableStateOf("")
    }
    var apiKeyVisible by remember(initialChannel?.id, visible) { mutableStateOf(false) }
    var model by remember(initialChannel?.id, visible) { mutableStateOf(initialChannel?.model.orEmpty()) }
    var customModel by remember(initialChannel?.id, visible) { mutableStateOf(initialChannel?.customModel.orEmpty()) }
    var proxy by remember(initialChannel?.id, visible) { mutableStateOf(initialChannel?.proxy ?: false) }
    var autoSync by remember(initialChannel?.id, visible) { mutableStateOf(initialChannel?.autoSync ?: false) }
    val fetchRequiresNewKey = initialChannel != null && initialChannel.keys.isNotEmpty() && apiKey.isBlank()
    val basicEditSupported = initialChannel?.canUseBasicMobileEditor() ?: true
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
                fraction = 0.64f,
                scrollState = editorScrollState,
            ) {
                operationError?.takeIf { it.isNotBlank() }?.let { error ->
                    OperationErrorCard(message = error)
                }
                if (!basicEditSupported) {
                    OperationErrorCard(message = stringResource(R.string.channel_basic_editor_unsupported))
                }
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = stringResource(R.string.channel_name_hint),
                    useLabelAsPlaceholder = true,
                    singleLine = true,
                    enabled = !submitting && basicEditSupported,
                    modifier = Modifier.fillMaxWidth(),
                )
                TextField(
                    value = baseUrl,
                    onValueChange = { baseUrl = it },
                    label = stringResource(R.string.channel_base_url_hint),
                    useLabelAsPlaceholder = true,
                    singleLine = true,
                    enabled = !submitting && basicEditSupported,
                    modifier = Modifier.fillMaxWidth(),
                )
                TextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = if (initialChannel == null) {
                        stringResource(R.string.channel_api_key_hint)
                    } else {
                        stringResource(R.string.channel_api_key_replace_hint)
                    },
                    useLabelAsPlaceholder = true,
                    singleLine = true,
                    visualTransformation = if (apiKeyVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(
                            onClick = { apiKeyVisible = !apiKeyVisible },
                            enabled = !submitting && basicEditSupported,
                        ) {
                            Icon(
                                imageVector = if (apiKeyVisible) AppMiuixIcons.Info else AppMiuixIcons.ApiKey,
                                contentDescription = if (apiKeyVisible) {
                                    stringResource(R.string.login_action_hide_password)
                                } else {
                                    stringResource(R.string.login_action_show_password)
                                },
                            )
                        }
                    },
                    enabled = !submitting && basicEditSupported,
                    modifier = Modifier.fillMaxWidth(),
                )
                TextField(
                    value = model,
                    onValueChange = { model = it },
                    label = stringResource(R.string.channel_model_hint),
                    useLabelAsPlaceholder = true,
                    singleLine = true,
                    enabled = !submitting && basicEditSupported,
                    modifier = Modifier.fillMaxWidth(),
                )
                TextField(
                    value = customModel,
                    onValueChange = { customModel = it },
                    label = stringResource(R.string.channel_custom_model_hint),
                    useLabelAsPlaceholder = true,
                    singleLine = true,
                    enabled = !submitting && basicEditSupported,
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
                        ChannelTypeOption(type = optionType, selectedType = type) {
                            if (!submitting && basicEditSupported) type = optionType
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = stringResource(R.string.channel_enabled_label), style = MiuixTheme.textStyles.main)
                    Switch(checked = enabled, onCheckedChange = { if (!submitting && basicEditSupported) enabled = it })
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = stringResource(R.string.channel_proxy_label), style = MiuixTheme.textStyles.main)
                    Switch(checked = proxy, onCheckedChange = { if (!submitting && basicEditSupported) proxy = it })
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = stringResource(R.string.channel_auto_sync_label), style = MiuixTheme.textStyles.main)
                    Switch(checked = autoSync, onCheckedChange = { if (!submitting && basicEditSupported) autoSync = it })
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
            ) {
                TextButton(
                    text = stringResource(
                        if (fetchRequiresNewKey) {
                            R.string.channel_fetch_model_needs_key
                        } else {
                            R.string.action_fetch_model
                        },
                    ),
                    enabled = !submitting && basicEditSupported && !fetchRequiresNewKey,
                    onClick = { onFetchModels(type, baseUrl, apiKey, proxy) },
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
                    enabled = canSubmitChannelEditor(
                        name = name,
                        baseUrl = baseUrl,
                        submitting = submitting,
                        basicEditSupported = basicEditSupported,
                    ),
                    onClick = {
                        onConfirm(name.trim(), type, enabled, baseUrl.trim(), apiKey.trim(), model.trim(), customModel.trim(), proxy, autoSync)
                    },
                )
            }
        }
    }
}

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
