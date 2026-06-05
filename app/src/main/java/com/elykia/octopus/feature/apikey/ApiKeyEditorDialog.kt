package com.elykia.octopus.feature.apikey

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.elykia.octopus.R
import com.elykia.octopus.core.data.model.ApiKeyItem
import com.elykia.octopus.core.designsystem.DialogScrollableColumn
import com.elykia.octopus.core.designsystem.OperationErrorCard
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun ApiKeyEditorDialog(
    visible: Boolean,
    title: String,
    initialItem: ApiKeyItem?,
    submitting: Boolean,
    operationError: String?,
    onConfirm: (String, Long, Double, String, Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    if (!visible) return

    var name by remember(initialItem?.id, initialItem?.name, visible) {
        mutableStateOf(initialItem?.name.orEmpty())
    }
    var expireAt by remember(initialItem?.id, initialItem?.expireAt, visible) {
        mutableStateOf(initialItem?.expireAt?.toString().orEmpty())
    }
    var maxCost by remember(initialItem?.id, initialItem?.maxCost, visible) {
        mutableStateOf(initialItem?.maxCost?.toString().orEmpty())
    }
    var supportedModels by remember(initialItem?.id, initialItem?.supportedModels, visible) {
        mutableStateOf(initialItem?.supportedModels.orEmpty())
    }
    var enabled by remember(initialItem?.id, initialItem?.enabled, visible) {
        mutableStateOf(initialItem?.enabled ?: true)
    }
    var validationIssue by remember(
        initialItem?.id,
        initialItem?.expireAt,
        initialItem?.maxCost,
        visible,
    ) {
        mutableStateOf<ApiKeyEditorValidationIssue?>(null)
    }
    val editorScrollState = rememberScrollState()
    val currentValidationIssue = apiKeyEditorValidationIssue(expireAt = expireAt, maxCost = maxCost)
        ?: validationIssue

    OverlayDialog(
        show = visible,
        title = title,
        summary = stringResource(R.string.setting_apikey_summary),
        onDismissRequest = {
            if (!submitting) onDismiss()
        },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DialogScrollableColumn(
                fraction = 0.58f,
                scrollState = editorScrollState,
            ) {
                operationError?.takeIf { it.isNotBlank() }?.let { error ->
                    OperationErrorCard(message = error)
                }
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = stringResource(R.string.apikey_name_hint),
                    useLabelAsPlaceholder = true,
                    singleLine = true,
                    enabled = !submitting,
                    modifier = Modifier.fillMaxWidth(),
                )
                TextField(
                    value = maxCost,
                    onValueChange = {
                        maxCost = it
                        validationIssue = null
                    },
                    label = stringResource(R.string.apikey_max_cost_hint),
                    useLabelAsPlaceholder = true,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    enabled = !submitting,
                    modifier = Modifier.fillMaxWidth(),
                )
                TextField(
                    value = expireAt,
                    onValueChange = {
                        expireAt = it
                        validationIssue = null
                    },
                    label = stringResource(R.string.apikey_expire_at_hint),
                    useLabelAsPlaceholder = true,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled = !submitting,
                    modifier = Modifier.fillMaxWidth(),
                )
                currentValidationIssue?.let { issue ->
                    OperationErrorCard(message = apiKeyEditorValidationMessage(issue))
                }
                TextField(
                    value = supportedModels,
                    onValueChange = { supportedModels = it },
                    label = stringResource(R.string.apikey_supported_models_hint),
                    useLabelAsPlaceholder = true,
                    singleLine = false,
                    maxLines = 3,
                    enabled = !submitting,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(text = stringResource(R.string.apikey_enabled_label), style = MiuixTheme.textStyles.main)
                    Switch(checked = enabled, onCheckedChange = { if (!submitting) enabled = it })
                }
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
                    enabled = canSubmitApiKeyEditor(
                        name = name,
                        expireAt = expireAt,
                        maxCost = maxCost,
                        submitting = submitting,
                    ),
                    colors = ButtonDefaults.textButtonColorsPrimary(),
                    onClick = {
                        val parsedValues = parseApiKeyEditorValues(
                            expireAt = expireAt,
                            maxCost = maxCost,
                        ).getOrElse { exception ->
                            validationIssue = (exception as? ApiKeyEditorValidationException)?.issue
                                ?: ApiKeyEditorValidationIssue.InvalidMaxCost
                            return@TextButton
                        }
                        onConfirm(
                            name.trim(),
                            parsedValues.expireAt,
                            parsedValues.maxCost,
                            supportedModels.trim(),
                            enabled,
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun apiKeyEditorValidationMessage(issue: ApiKeyEditorValidationIssue): String = when (issue) {
    ApiKeyEditorValidationIssue.InvalidExpireAt -> stringResource(R.string.apikey_invalid_expire_at)
    ApiKeyEditorValidationIssue.InvalidMaxCost -> stringResource(R.string.apikey_invalid_max_cost)
}
