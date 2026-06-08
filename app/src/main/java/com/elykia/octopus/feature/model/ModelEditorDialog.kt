package com.elykia.octopus.feature.model

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.elykia.octopus.R
import com.elykia.octopus.core.data.model.LlmInfo
import com.elykia.octopus.core.designsystem.DialogScrollableColumn
import com.elykia.octopus.core.designsystem.OperationErrorCard
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.overlay.OverlayDialog

internal enum class ModelEditorValidationIssue {
    BlankName,
    InvalidPrice,
}

internal data class ModelEditorValues(
    val name: String,
    val input: Double,
    val output: Double,
    val cacheRead: Double,
    val cacheWrite: Double,
) {
    fun toModel(): LlmInfo = LlmInfo(
        name = name,
        input = input,
        output = output,
        cacheRead = cacheRead,
        cacheWrite = cacheWrite,
    )
}

@Composable
internal fun ModelEditorDialog(
    visible: Boolean,
    title: String,
    initialModel: LlmInfo?,
    submitting: Boolean,
    operationError: String?,
    onConfirm: (LlmInfo) -> Unit,
    onDismiss: () -> Unit,
) {
    if (!visible) return

    var name by remember(initialModel?.name, visible) {
        mutableStateOf(initialModel?.name.orEmpty())
    }
    var input by remember(initialModel?.name, initialModel?.input, visible) {
        mutableStateOf(initialModel?.input?.toString().orEmpty())
    }
    var output by remember(initialModel?.name, initialModel?.output, visible) {
        mutableStateOf(initialModel?.output?.toString().orEmpty())
    }
    var cacheRead by remember(initialModel?.name, initialModel?.cacheRead, visible) {
        mutableStateOf(initialModel?.cacheRead?.toString().orEmpty())
    }
    var cacheWrite by remember(initialModel?.name, initialModel?.cacheWrite, visible) {
        mutableStateOf(initialModel?.cacheWrite?.toString().orEmpty())
    }
    var validationIssue by remember(initialModel?.name, visible) {
        mutableStateOf<ModelEditorValidationIssue?>(null)
    }
    val scrollState = rememberScrollState()
    val currentValidationIssue = parseModelEditorValues(name, input, output, cacheRead, cacheWrite)
        .exceptionOrNull()
        .toModelEditorIssue() ?: validationIssue

    OverlayDialog(
        show = visible,
        title = title,
        summary = stringResource(R.string.model_editor_summary),
        onDismissRequest = {
            if (!submitting) onDismiss()
        },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DialogScrollableColumn(
                fraction = 0.58f,
                scrollState = scrollState,
            ) {
                operationError?.takeIf { it.isNotBlank() }?.let { error ->
                    OperationErrorCard(message = error)
                }
                TextField(
                    value = name,
                    onValueChange = {
                        name = it
                        validationIssue = null
                    },
                    label = stringResource(R.string.model_name_hint),
                    useLabelAsPlaceholder = true,
                    singleLine = true,
                    enabled = initialModel == null && !submitting,
                    modifier = Modifier.fillMaxWidth(),
                )
                PriceField(
                    value = input,
                    label = stringResource(R.string.model_input_price_label),
                    enabled = !submitting,
                    onValueChange = {
                        input = it
                        validationIssue = null
                    },
                )
                PriceField(
                    value = output,
                    label = stringResource(R.string.model_output_price_label),
                    enabled = !submitting,
                    onValueChange = {
                        output = it
                        validationIssue = null
                    },
                )
                PriceField(
                    value = cacheRead,
                    label = stringResource(R.string.model_cache_read_label),
                    enabled = !submitting,
                    onValueChange = {
                        cacheRead = it
                        validationIssue = null
                    },
                )
                PriceField(
                    value = cacheWrite,
                    label = stringResource(R.string.model_cache_write_label),
                    enabled = !submitting,
                    onValueChange = {
                        cacheWrite = it
                        validationIssue = null
                    },
                )
                currentValidationIssue?.let { issue ->
                    OperationErrorCard(message = modelEditorValidationMessage(issue))
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
                    enabled = canSubmitModelEditor(name, input, output, cacheRead, cacheWrite, submitting),
                    colors = ButtonDefaults.textButtonColorsPrimary(),
                    onClick = {
                        parseModelEditorValues(name, input, output, cacheRead, cacheWrite)
                            .onSuccess { values -> onConfirm(values.toModel()) }
                            .onFailure { error ->
                                validationIssue = error.toModelEditorIssue()
                                    ?: ModelEditorValidationIssue.InvalidPrice
                            }
                    },
                )
            }
        }
    }
}

@Composable
private fun PriceField(
    value: String,
    label: String,
    enabled: Boolean,
    onValueChange: (String) -> Unit,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        useLabelAsPlaceholder = true,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun modelEditorValidationMessage(issue: ModelEditorValidationIssue): String = when (issue) {
    ModelEditorValidationIssue.BlankName -> stringResource(R.string.model_invalid_name)
    ModelEditorValidationIssue.InvalidPrice -> stringResource(R.string.model_invalid_price)
}

internal fun canSubmitModelEditor(
    name: String,
    input: String,
    output: String,
    cacheRead: String,
    cacheWrite: String,
    submitting: Boolean,
): Boolean = !submitting && parseModelEditorValues(name, input, output, cacheRead, cacheWrite).isSuccess

internal fun parseModelEditorValues(
    name: String,
    input: String,
    output: String,
    cacheRead: String,
    cacheWrite: String,
): Result<ModelEditorValues> = runCatching {
    val normalizedName = name.trim()
    if (normalizedName.isBlank()) {
        throw ModelEditorValidationException(ModelEditorValidationIssue.BlankName)
    }
    ModelEditorValues(
        name = normalizedName,
        input = input.parsePriceField(),
        output = output.parsePriceField(),
        cacheRead = cacheRead.parsePriceField(),
        cacheWrite = cacheWrite.parsePriceField(),
    )
}

private fun String.parsePriceField(): Double {
    val value = trim()
    if (value.isBlank()) return 0.0
    val parsed = value.toDoubleOrNull()
    if (parsed == null || !parsed.isFinite() || parsed < 0.0) {
        throw ModelEditorValidationException(ModelEditorValidationIssue.InvalidPrice)
    }
    return parsed
}

private class ModelEditorValidationException(
    val issue: ModelEditorValidationIssue,
) : IllegalArgumentException(issue.name)

private fun Throwable?.toModelEditorIssue(): ModelEditorValidationIssue? =
    (this as? ModelEditorValidationException)?.issue
