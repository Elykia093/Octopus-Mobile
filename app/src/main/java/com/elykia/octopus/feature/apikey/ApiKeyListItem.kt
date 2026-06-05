package com.elykia.octopus.feature.apikey

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.elykia.octopus.R
import com.elykia.octopus.core.data.model.ApiKeyItem
import com.elykia.octopus.core.designsystem.AppListCard
import com.elykia.octopus.core.designsystem.AppMetricRow
import com.elykia.octopus.core.designsystem.OctopusTokens
import com.elykia.octopus.core.designsystem.SoftIconTile
import com.elykia.octopus.core.designsystem.formatMoney
import com.elykia.octopus.core.designsystem.icons.AppMiuixIcons
import top.yukonga.miuix.kmp.basic.Checkbox
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun ApiKeyRow(
    item: ApiKeyItem,
    submitting: Boolean,
    selectionMode: Boolean,
    isSelected: Boolean,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSelect: () -> Unit,
    onCopy: () -> Unit,
) {
    val supportedModels = item.supportedModels
        ?.split(',')
        ?.map { it.trim() }
        ?.filter { it.isNotBlank() }
        .orEmpty()

    AppListCard(padding = PaddingValues(horizontal = 18.dp, vertical = 18.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (selectionMode) {
                    Checkbox(
                        state = if (isSelected) ToggleableState.On else ToggleableState.Off,
                        onClick = { onSelect() },
                        enabled = !submitting,
                    )
                }
                SoftIconTile(
                    icon = AppMiuixIcons.ApiKey,
                    contentDescription = item.name,
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = item.name.ifBlank { stringResource(R.string.apikey_fallback_name, item.id) },
                        style = MiuixTheme.textStyles.title3,
                        fontWeight = FontWeight.Bold,
                        color = OctopusTokens.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = item.apiKey.maskApiKey(),
                        style = MiuixTheme.textStyles.body2,
                        color = OctopusTokens.TextSecondary,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                ApiKeyStatusPill(
                    enabled = item.enabled,
                    clickable = !submitting && !selectionMode,
                    onClick = { onToggle(!item.enabled) },
                )
                if (!selectionMode) {
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
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AppMetricRow(
                    icon = AppMiuixIcons.Time,
                    label = stringResource(R.string.apikey_expire_at_label),
                    value = item.expireAt
                        ?.takeIf { it > 0L }
                        ?.toString()
                        ?: stringResource(R.string.apikey_expire_never),
                )
                AppMetricRow(
                    icon = AppMiuixIcons.Cost,
                    label = stringResource(R.string.apikey_max_cost_label),
                    value = item.maxCost
                        ?.takeIf { it > 0.0 }
                        ?.let(::formatMoney)
                        ?: stringResource(R.string.apikey_cost_unlimited),
                )
            }

            Text(
                text = supportedModels.modelSummary(),
                style = MiuixTheme.textStyles.body2,
                color = OctopusTokens.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ApiKeyStatusPill(
    enabled: Boolean,
    clickable: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (enabled) OctopusTokens.Accent else OctopusTokens.Muted)
            .then(if (clickable) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 14.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(if (enabled) R.string.apikey_enabled_summary else R.string.apikey_disabled_summary),
            color = if (enabled) OctopusTokens.OnAccent else OctopusTokens.TextSecondary,
            style = MiuixTheme.textStyles.body2,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun List<String>.modelSummary(): String {
    return if (isEmpty()) {
        stringResource(R.string.apikey_models_all)
    } else {
        val head = take(3).joinToString(", ")
        if (size > 3) {
            "$head · ${stringResource(R.string.apikey_models_more, size - 3)}"
        } else {
            head
        }
    }
}
