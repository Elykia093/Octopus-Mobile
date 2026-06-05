package com.elykia.octopus.feature.channel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.elykia.octopus.R
import com.elykia.octopus.core.designsystem.DialogScrollableColumn
import com.elykia.octopus.core.designsystem.InlineEmptyCard
import com.elykia.octopus.core.designsystem.OctopusTokens
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * Channel 模型获取结果对话框
 */
@Composable
fun ChannelFetchResultDialog(
    visible: Boolean,
    models: List<String>,
    onDismiss: () -> Unit,
) {
    if (!visible) return

    OverlayDialog(
        show = visible,
        title = stringResource(R.string.channel_fetch_model_title),
        summary = stringResource(R.string.channel_fetch_model_summary),
        onDismissRequest = onDismiss,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (models.isEmpty()) {
                InlineEmptyCard(
                    title = stringResource(R.string.channel_fetch_model_title),
                    summary = stringResource(R.string.channel_fetch_model_empty),
                )
            } else {
                DialogScrollableColumn(fraction = 0.48f) {
                    models.forEach { model ->
                        Text(
                            text = model,
                            style = MiuixTheme.textStyles.body2,
                            color = OctopusTokens.TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(OctopusTokens.Muted.copy(alpha = 0.64f))
                                .padding(horizontal = 12.dp, vertical = 9.dp),
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(text = stringResource(R.string.action_close), onClick = onDismiss)
            }
        }
    }
}
