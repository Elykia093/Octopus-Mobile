package com.elykia.octopus.feature.apikey

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.elykia.octopus.R
import com.elykia.octopus.core.data.model.ApiKeyItem
import com.elykia.octopus.core.designsystem.AppInfoChip
import com.elykia.octopus.core.designsystem.AppListCard
import com.elykia.octopus.core.designsystem.SecureVisibleWindow
import com.elykia.octopus.core.designsystem.formatMoney
import com.elykia.octopus.core.designsystem.icons.AppMiuixIcons
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun CreatedApiKeyDialog(
    item: ApiKeyItem,
    onCopy: () -> Unit,
    onDismiss: () -> Unit,
) {
    SecureVisibleWindow()

    OverlayDialog(
        show = true,
        title = stringResource(R.string.apikey_created_title),
        summary = stringResource(R.string.apikey_created_summary),
        onDismissRequest = onDismiss,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = stringResource(R.string.apikey_created_value_label),
                style = MiuixTheme.textStyles.main,
                fontWeight = FontWeight.SemiBold,
            )
            AppListCard {
                Text(
                    text = item.apiKey,
                    style = MiuixTheme.textStyles.body2,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppInfoChip(
                    text = item.expireAt?.takeIf { it > 0 }?.toString() ?: stringResource(R.string.common_never_expires),
                    icon = AppMiuixIcons.Time,
                )
                AppInfoChip(
                    text = item.maxCost
                        ?.takeIf { it > 0.0 }
                        ?.let(::formatMoney)
                        ?: stringResource(R.string.apikey_cost_unlimited),
                    icon = AppMiuixIcons.Cost,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(text = stringResource(R.string.common_copy), onClick = onCopy)
                TextButton(text = stringResource(R.string.common_confirm), onClick = onDismiss)
            }
        }
    }
}
