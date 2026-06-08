package com.elykia.octopus.feature.model

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.elykia.octopus.R
import com.elykia.octopus.core.data.model.LlmInfo
import com.elykia.octopus.core.designsystem.AppListCard
import com.elykia.octopus.core.designsystem.AppMetricRow
import com.elykia.octopus.core.designsystem.OctopusTones
import com.elykia.octopus.core.designsystem.OctopusTokens
import com.elykia.octopus.core.designsystem.SoftIconTile
import com.elykia.octopus.core.designsystem.formatMoney
import com.elykia.octopus.core.designsystem.icons.AppMiuixIcons
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun ModelRow(
    model: LlmInfo,
    submitting: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    AppListCard(padding = PaddingValues(horizontal = 18.dp, vertical = 18.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                SoftIconTile(
                    icon = AppMiuixIcons.Model,
                    contentDescription = model.name,
                    tint = if (model.hasPricing()) OctopusTokens.Accent else OctopusTones.Orange,
                )
                Text(
                    text = model.name.ifBlank { stringResource(R.string.common_unknown) },
                    style = MiuixTheme.textStyles.title3,
                    fontWeight = FontWeight.Bold,
                    color = OctopusTokens.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    IconButton(onClick = onEdit, enabled = !submitting) {
                        Icon(
                            imageVector = AppMiuixIcons.Create,
                            contentDescription = stringResource(R.string.action_edit),
                            tint = OctopusTokens.TextSecondary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    IconButton(onClick = onDelete, enabled = !submitting) {
                        Icon(
                            imageVector = AppMiuixIcons.Delete,
                            contentDescription = stringResource(R.string.common_delete),
                            tint = OctopusTokens.TextSecondary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AppMetricRow(
                    icon = AppMiuixIcons.ArrowDown,
                    label = stringResource(R.string.model_input_price_label),
                    value = formatMoney(model.input),
                )
                AppMetricRow(
                    icon = AppMiuixIcons.ArrowUp,
                    label = stringResource(R.string.model_output_price_label),
                    value = formatMoney(model.output),
                )
                AppMetricRow(
                    icon = AppMiuixIcons.Token,
                    label = stringResource(R.string.model_cache_read_label),
                    value = formatMoney(model.cacheRead),
                    accentColor = OctopusTokens.TextSecondary,
                )
                AppMetricRow(
                    icon = AppMiuixIcons.Cost,
                    label = stringResource(R.string.model_cache_write_label),
                    value = formatMoney(model.cacheWrite),
                    accentColor = OctopusTokens.TextSecondary,
                )
            }

            Text(
                text = stringResource(
                    R.string.model_item_summary,
                    model.input,
                    model.output,
                    model.cacheRead,
                    model.cacheWrite,
                ),
                style = MiuixTheme.textStyles.body2,
                color = OctopusTokens.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
