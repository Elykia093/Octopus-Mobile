package com.elykia.octopus.core.designsystem

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.elykia.octopus.R
import com.elykia.octopus.core.designsystem.icons.AppMiuixIcons
import kotlin.math.roundToInt
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.FloatingNavigationBar
import top.yukonga.miuix.kmp.basic.FloatingNavigationBarDisplayMode
import top.yukonga.miuix.kmp.basic.FloatingNavigationBarItem
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme

data class DockItem(
    val key: String,
    val icon: ImageVector,
    val label: String,
)

data class ActionIcon(
    val icon: ImageVector,
    val contentDescription: String,
    val enabled: Boolean = true,
    val onClick: () -> Unit,
)

data class OptionChipItem<T>(
    val value: T,
    val label: String,
)

@Composable
fun FloatingDockBar(
    items: List<DockItem>,
    selectedKey: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    mode: FloatingNavigationBarDisplayMode = FloatingNavigationBarDisplayMode.IconOnly,
) {
    FloatingNavigationBar(
        modifier = modifier,
        mode = mode,
        showDivider = true,
        shadowElevation = 10.dp,
        color = MiuixTheme.colorScheme.surfaceContainer,
    ) {
        items.forEach { item ->
            FloatingNavigationBarItem(
                selected = item.key == selectedKey,
                onClick = { onSelected(item.key) },
                icon = item.icon,
                label = item.label,
            )
        }
    }
}

@Composable
fun BrandTopBar(
    title: String,
    actions: List<ActionIcon>,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        insideMargin = PaddingValues(horizontal = 18.dp, vertical = 16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(MiuixTheme.colorScheme.secondaryContainer, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                OctopusBrandMark(size = 28.dp)
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = stringResource(R.string.brand_title),
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    style = MiuixTheme.textStyles.body2,
                )
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    style = MiuixTheme.textStyles.title2,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            actions.forEach { action ->
                IconButton(onClick = action.onClick, enabled = action.enabled) {
                    Icon(imageVector = action.icon, contentDescription = action.contentDescription)
                }
            }
        }
    }
}

@Composable
fun PageContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MiuixTheme.colorScheme.background),
    ) {
        content()
    }
}

@Composable
fun ScreenPane(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(14.dp),
    content: @Composable () -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = verticalArrangement,
    ) {
        item {
            content()
        }
    }
}

@Composable
fun LoadingPane(title: String) {
    StatePane(
        title = title,
        summary = stringResource(R.string.common_loading),
    )
}

@Composable
fun ErrorPane(
    message: String,
    onRetry: (() -> Unit)? = null,
) {
    StatePane(
        title = stringResource(R.string.error_title),
        summary = message,
        action = if (onRetry != null) {
            {
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColorsPrimary(),
                ) {
                    Text(text = stringResource(R.string.common_retry))
                }
            }
        } else {
            null
        },
    )
}

@Composable
fun EmptyPane(
    title: String,
    summary: String,
) {
    StatePane(title = title, summary = summary)
}

@Composable
fun StatePane(
    title: String,
    summary: String,
    action: (@Composable () -> Unit)? = null,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Card(insideMargin = PaddingValues(horizontal = 20.dp, vertical = 22.dp)) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OctopusBrandMark(size = 36.dp)
                Text(text = title, style = MiuixTheme.textStyles.title3, fontWeight = FontWeight.SemiBold)
                Text(
                    text = summary,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    style = MiuixTheme.textStyles.body2,
                )
                action?.invoke()
            }
        }
    }
}

@Composable
fun SectionCard(
    title: String,
    summary: String? = null,
    actions: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        insideMargin = PaddingValues(horizontal = 18.dp, vertical = 16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(text = title, style = MiuixTheme.textStyles.title3, fontWeight = FontWeight.SemiBold)
                    if (!summary.isNullOrBlank()) {
                        Text(
                            text = summary,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            style = MiuixTheme.textStyles.body2,
                        )
                    }
                }
                actions?.invoke()
            }
            content()
        }
    }
}

@Composable
fun ToolbarChip(
    text: String,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val colors = if (selected) {
        ButtonDefaults.textButtonColorsPrimary()
    } else {
        ButtonDefaults.textButtonColors()
    }
    TextButton(
        text = text,
        onClick = { onClick?.invoke() },
        enabled = true,
        colors = colors,
        minHeight = 34.dp,
        insideMargin = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
    )
}

@Composable
fun <T> OptionChipGroup(
    options: List<OptionChipItem<T>>,
    selectedValue: T,
    onSelect: (T) -> Unit,
    columns: Int = 3,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.chunked(columns).forEach { rowOptions ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowOptions.forEach { option ->
                    ToolbarChip(
                        text = option.label,
                        selected = option.value == selectedValue,
                        onClick = { onSelect(option.value) },
                    )
                }
            }
        }
    }
}

@Composable
fun SelectableListCard(
    title: String,
    summary: String,
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector = AppMiuixIcons.Channel,
) {
    AppListCard(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(MiuixTheme.colorScheme.secondaryContainer, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MiuixTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp),
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(text = title, style = MiuixTheme.textStyles.main, fontWeight = FontWeight.SemiBold)
                Text(
                    text = summary,
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
            if (selected) {
                Icon(
                    imageVector = AppMiuixIcons.Check,
                    contentDescription = stringResource(R.string.common_selected),
                    tint = MiuixTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
fun InlineEmptyCard(
    title: String,
    summary: String,
) {
    AppListCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = title, style = MiuixTheme.textStyles.main, fontWeight = FontWeight.SemiBold)
            Text(
                text = summary,
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        }
    }
}

@Composable
fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    modifier: Modifier = Modifier,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = hint,
        useLabelAsPlaceholder = true,
        singleLine = true,
        leadingIcon = {
            Icon(
                imageVector = AppMiuixIcons.Search,
                contentDescription = stringResource(R.string.action_open_search),
            )
        },
        trailingIcon = if (value.isNotEmpty()) {
            {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(
                        imageVector = AppMiuixIcons.Close,
                        contentDescription = stringResource(R.string.common_cancel),
                    )
                }
            }
        } else {
            null
        },
    )
}

@Composable
fun LabelValueCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        insideMargin = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = label,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                style = MiuixTheme.textStyles.body2,
            )
            Text(
                text = value,
                style = MiuixTheme.textStyles.main,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
fun NumberSettingCard(
    title: String,
    summary: String,
    value: String,
    onValueChange: (String) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        insideMargin = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(text = title, style = MiuixTheme.textStyles.main, fontWeight = FontWeight.Medium)
            Text(
                text = summary,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                style = MiuixTheme.textStyles.body2,
            )
            TextField(
                value = value,
                onValueChange = onValueChange,
                label = title,
                useLabelAsPlaceholder = true,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
        }
    }
}

@Composable
fun InfoRow(
    label: String,
    value: String,
    valueColor: Color = MiuixTheme.colorScheme.onBackground,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            style = MiuixTheme.textStyles.body2,
        )
        Text(
            text = value,
            color = valueColor,
            style = MiuixTheme.textStyles.main,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun SettingItemCard(
    title: String,
    summary: String,
    extra: String? = null,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        insideMargin = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text = title, style = MiuixTheme.textStyles.main, fontWeight = FontWeight.Medium)
            Text(
                text = summary,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                style = MiuixTheme.textStyles.body2,
            )
            if (!extra.isNullOrBlank()) {
                Text(
                    text = extra,
                    color = MiuixTheme.colorScheme.onSurface,
                    style = MiuixTheme.textStyles.body2,
                )
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    primaryLabel: String,
    primaryValue: String,
    secondaryLabel: String,
    secondaryValue: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        insideMargin = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = title, style = MiuixTheme.textStyles.title3, fontWeight = FontWeight.SemiBold)
            InfoRow(label = primaryLabel, value = primaryValue)
            InfoRow(label = secondaryLabel, value = secondaryValue)
        }
    }
}

@Composable
fun StatOverviewCard(
    title: String,
    value: String,
    summary: String,
    icon: ImageVector,
    accentColor: Color,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(124.dp),
        insideMargin = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = accentColor)
                Text(
                    text = title,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    style = MiuixTheme.textStyles.body2,
                )
            }
            Text(
                text = value,
                color = accentColor,
                style = MiuixTheme.textStyles.title1,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = summary,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                style = MiuixTheme.textStyles.body2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun RankBadge(
    rank: Int,
    modifier: Modifier = Modifier,
) {
    val (background, content) = when (rank) {
        1 -> Color(0xFFFF9500).copy(alpha = 0.16f) to Color(0xFFFF9500)
        2 -> Color(0xFF8E8E93).copy(alpha = 0.16f) to Color(0xFF8E8E93)
        3 -> Color(0xFFAF52DE).copy(alpha = 0.16f) to Color(0xFFAF52DE)
        else -> MiuixTheme.colorScheme.secondaryContainer to MiuixTheme.colorScheme.onSecondaryContainer
    }
    Box(
        modifier = modifier
            .size(26.dp)
            .background(background, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (rank <= 3) "★" else rank.toString(),
            color = content,
            style = MiuixTheme.textStyles.body2,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun ProgressToneBar(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val clamped = progress.coerceIn(0f, 1f)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp)
            .background(MiuixTheme.colorScheme.secondaryContainer, CircleShape),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(clamped)
                .height(6.dp)
                .background(color, CircleShape),
        )
    }
}

fun formatPercent(value: Double): String = "${(value * 100).roundToInt()}%"

@Composable
fun SimpleList(
    entries: List<Pair<String, String>>,
    onDelete: ((Int) -> Unit)? = null,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        entries.forEachIndexed { index, entry ->
            ListEntryCard(
                title = entry.first,
                summary = entry.second,
                trailing = if (onDelete != null) {
                    {
                        TextButton(
                            text = stringResource(R.string.common_delete),
                            onClick = { onDelete(index) },
                        )
                    }
                } else {
                    null
                },
            )
        }
    }
}

@Composable
fun ListEntryCard(
    title: String,
    summary: String,
    trailing: (@Composable RowScope.() -> Unit)? = null,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        insideMargin = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(text = title, style = MiuixTheme.textStyles.main, fontWeight = FontWeight.Medium)
                Text(
                    text = summary,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    style = MiuixTheme.textStyles.body2,
                )
            }
            if (trailing != null) {
                Row(content = trailing)
            }
        }
    }
}

@Composable
fun LabeledSection(
    @StringRes titleRes: Int,
    @StringRes summaryRes: Int,
    content: @Composable () -> Unit,
) {
    SectionCard(
        title = stringResource(titleRes),
        summary = stringResource(summaryRes),
        content = content,
    )
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
