package com.elykia.octopus.core.designsystem

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.elykia.octopus.R
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.overlay.OverlayDialog

@Composable
fun DialogScrollableColumn(
    modifier: Modifier = Modifier,
    fraction: Float = 0.62f,
    scrollState: ScrollState = rememberScrollState(),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(12.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val maxDialogHeight = if (maxHeight == Dp.Infinity) {
            420.dp
        } else if (maxHeight <= 180.dp) {
            maxHeight
        } else {
            (maxHeight * fraction).coerceIn(180.dp, maxHeight)
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = maxDialogHeight)
                .imePadding()
                .navigationBarsPadding()
                .verticalScroll(scrollState),
            verticalArrangement = verticalArrangement,
            content = content,
        )
    }
}

@Composable
fun DangerConfirmDialog(
    visible: Boolean,
    title: String,
    summary: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    OverlayDialog(
        show = visible,
        title = title,
        summary = summary,
        onDismissRequest = onDismiss,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(text = stringResource(R.string.common_cancel), onClick = onDismiss)
            TextButton(text = stringResource(R.string.common_confirm), onClick = onConfirm)
        }
    }
}
