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
import androidx.compose.ui.unit.dp
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
                text = "启用",
                enabled = !submitting,
                onClick = onEnable,
            )
            TextButton(
                text = "禁用",
                enabled = !submitting,
                onClick = onDisable,
            )
            TextButton(
                text = "删除",
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
        title = "批量删除 API Key",
        summary = "确定要删除选中的 $selectedCount 个 API Key 吗？此操作不可撤销。",
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
        title = if (isEnable) "批量启用" else "批量禁用",
        summary = "确定要${if (isEnable) "启用" else "禁用"}选中的 $selectedCount 个 API Key 吗？",
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
                    text = "取消",
                    enabled = !submitting,
                    onClick = onDismiss,
                )
                TextButton(
                    text = if (submitting) "处理中..." else "确定",
                    enabled = !submitting,
                    onClick = { onConfirm(isEnable) },
                )
            }
        }
    }
}
