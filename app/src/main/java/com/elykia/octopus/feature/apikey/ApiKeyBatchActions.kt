package com.elykia.octopus.feature.apikey

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.elykia.octopus.R
import com.elykia.octopus.core.designsystem.DangerConfirmDialog
import com.elykia.octopus.core.designsystem.OctopusTokens
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme

internal enum class ApiKeyBatchAction {
    ENABLE,
    DISABLE,
}

@Composable
internal fun ApiKeyBatchActionBar(
    contentPadding: PaddingValues,
    submitting: Boolean,
    onEnable: () -> Unit,
    onDisable: () -> Unit,
    onDelete: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(OctopusTokens.Card.copy(alpha = 0.95f))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(
                text = stringResource(R.string.batch_enable),
                enabled = !submitting,
                onClick = onEnable,
            )
            TextButton(
                text = stringResource(R.string.batch_disable),
                enabled = !submitting,
                onClick = onDisable,
            )
            TextButton(
                text = stringResource(R.string.batch_delete),
                enabled = !submitting,
                onClick = onDelete,
            )
        }
    }
}

@Composable
internal fun ApiKeyBatchDeleteConfirmDialog(
    visible: Boolean,
    selectedCount: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    DangerConfirmDialog(
        visible = visible,
        title = stringResource(R.string.batch_delete_apikeys_title),
        summary = stringResource(R.string.batch_delete_apikeys_summary, selectedCount),
        onConfirm = onConfirm,
        onDismiss = onDismiss,
    )
}

@Composable
internal fun ApiKeyBatchEnabledDialog(
    action: ApiKeyBatchAction?,
    selectedCount: Int,
    submitting: Boolean,
    progress: String?,
    onConfirm: (Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    val isEnable = when (action) {
        ApiKeyBatchAction.ENABLE -> true
        ApiKeyBatchAction.DISABLE -> false
        null -> return
    }

    OverlayDialog(
        show = true,
        title = stringResource(if (isEnable) R.string.batch_enable_title else R.string.batch_disable_title),
        summary = stringResource(
            if (isEnable) R.string.batch_enable_summary else R.string.batch_disable_summary,
            selectedCount,
        ),
        onDismissRequest = { if (!submitting) onDismiss() },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            progress?.let { text ->
                Text(
                    text = text,
                    style = MiuixTheme.textStyles.body2,
                    color = OctopusTokens.TextSecondary,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(
                    text = stringResource(R.string.common_cancel),
                    enabled = !submitting,
                    onClick = onDismiss,
                )
                TextButton(
                    text = if (submitting) {
                        stringResource(R.string.common_saving)
                    } else {
                        stringResource(R.string.common_confirm)
                    },
                    enabled = !submitting,
                    onClick = { onConfirm(isEnable) },
                )
            }
        }
    }
}
