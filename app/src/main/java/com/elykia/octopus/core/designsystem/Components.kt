package com.elykia.octopus.core.designsystem

import androidx.annotation.StringRes
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.elykia.octopus.R
import com.elykia.octopus.core.designsystem.icons.AppMiuixIcons
import kotlin.math.roundToInt
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.sp

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
) {
    val dockShape = RoundedCornerShape(28.dp)
    val itemShape = RoundedCornerShape(18.dp)
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        if (items.isEmpty()) return@BoxWithConstraints

        val compactDock = items.size >= 6 && maxWidth < 344.dp
        val ultraCompactDock = items.size >= 6 && maxWidth < 316.dp
        val sidePadding = when {
            ultraCompactDock -> 0.dp
            compactDock -> 4.dp
            else -> 10.dp
        }
        val gap = when {
            ultraCompactDock -> 0.dp
            compactDock -> 4.dp
            else -> 6.dp
        }
        val dockMaxWidth = minOf(maxWidth, 348.dp)
        val availableItemWidth = (dockMaxWidth - sidePadding * 2f - gap * (items.size - 1).toFloat()) / items.size.toFloat()
        val maxItemSize = 48.dp
        val minItemSize = if (availableItemWidth >= 44.dp) 44.dp else availableItemWidth
        val itemSize = availableItemWidth.coerceIn(minItemSize, maxItemSize)
        val iconSize = (itemSize * 0.48f).coerceIn(19.dp, 24.dp)
        val dockWidth = (itemSize * items.size.toFloat() + sidePadding * 2f + gap * (items.size - 1).toFloat()).coerceAtMost(348.dp)

        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .width(dockWidth)
                .shadow(
                    elevation = 12.dp,
                    shape = dockShape,
                    clip = false,
                    ambientColor = Color.Black.copy(alpha = 0.035f),
                    spotColor = Color.Black.copy(alpha = 0.11f),
                )
                .clip(dockShape)
                .background(OctopusTokens.Card.copy(alpha = 0.97f))
                .border(1.dp, OctopusTokens.Border.copy(alpha = 0.78f), dockShape)
                .padding(horizontal = sidePadding, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(gap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEach { item ->
                val selected = item.key == selectedKey
                Box(
                    modifier = Modifier
                        .size(itemSize)
                        .clip(itemShape)
                        .background(if (selected) OctopusTokens.SelectedNav.copy(alpha = 0.94f) else Color.Transparent)
                        .clickable { onSelected(item.key) },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = if (selected) OctopusTokens.TextPrimary else OctopusTokens.NavMuted,
                        modifier = Modifier.size(iconSize),
                    )
                }
            }
        }
    }
}

@Composable
fun AppSegmentButton(
    text: String,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = Modifier
            .clip(shape)
            .background(if (selected) OctopusTokens.SelectedNav else OctopusTokens.Muted.copy(alpha = 0.72f))
            .border(1.dp, if (selected) OctopusTokens.Accent.copy(alpha = 0.18f) else OctopusTokens.Border.copy(alpha = 0.62f), shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MiuixTheme.textStyles.body2,
            fontWeight = FontWeight.Medium,
            color = OctopusTokens.TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
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
            .background(OctopusTokens.Canvas),
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
fun LoadingStateCard(
    title: String,
    modifier: Modifier = Modifier,
) {
    InlineStateCard(
        title = title,
        summary = stringResource(R.string.common_loading),
        modifier = modifier,
    )
}

@Composable
fun ErrorStateCard(
    message: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
) {
    InlineStateCard(
        title = stringResource(R.string.error_title),
        summary = message,
        modifier = modifier,
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
fun EmptyStateCard(
    title: String,
    summary: String,
    modifier: Modifier = Modifier,
) {
    InlineStateCard(title = title, summary = summary, modifier = modifier)
}

@Composable
fun InlineStateCard(
    title: String,
    summary: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    AppListCard(
        modifier = modifier,
        padding = PaddingValues(horizontal = 22.dp, vertical = 24.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OctopusBrandMark(size = 42.dp)
            Text(
                text = title,
                style = MiuixTheme.textStyles.title3,
                fontWeight = FontWeight.Bold,
                color = OctopusTokens.TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = summary,
                color = OctopusTokens.TextSecondary,
                style = MiuixTheme.textStyles.body2,
            )
            action?.invoke()
        }
    }
}

@Composable
fun OperationErrorCard(
    message: String,
    modifier: Modifier = Modifier,
    onDismiss: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MiuixTheme.colorScheme.error.copy(alpha = 0.08f))
            .border(1.dp, MiuixTheme.colorScheme.error.copy(alpha = 0.24f), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = message,
            style = MiuixTheme.textStyles.body2,
            color = MiuixTheme.colorScheme.error,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (onDismiss != null) {
            TextButton(text = stringResource(R.string.action_close), onClick = onDismiss)
        }
    }
}

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
fun StatePane(
    title: String,
    summary: String,
    action: (@Composable () -> Unit)? = null,
) {
    PageContainer {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            AppListCard(
                modifier = Modifier.padding(horizontal = 28.dp),
                padding = PaddingValues(horizontal = 22.dp, vertical = 24.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OctopusBrandMark(size = 44.dp)
                    Text(
                        text = title,
                        style = MiuixTheme.textStyles.title3,
                        fontWeight = FontWeight.Bold,
                        color = OctopusTokens.TextPrimary,
                    )
                    Text(
                        text = summary,
                        color = OctopusTokens.TextSecondary,
                        style = MiuixTheme.textStyles.body2,
                    )
                    action?.invoke()
                }
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
    AppListCard(padding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
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
    AppSegmentButton(
        text = text,
        selected = selected,
        onClick = onClick,
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
                    tint = OctopusTokens.Accent,
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
                    tint = OctopusTokens.Accent,
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
fun RankBadge(
    rank: Int,
    modifier: Modifier = Modifier,
) {
    val (background, content) = OctopusTones.rank(rank)
        ?: (MiuixTheme.colorScheme.secondaryContainer to MiuixTheme.colorScheme.onSecondaryContainer)
    Box(
        modifier = modifier
            .size(26.dp)
            .background(background, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = rank.toString(),
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
    val clamped = progress.normalizedProgress()
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(5.dp)
            .background(MiuixTheme.colorScheme.secondaryContainer, CircleShape),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(clamped)
                .height(5.dp)
                .background(color, CircleShape),
        )
    }
}

internal fun Float.normalizedProgress(): Float =
    if (isNaN() || isInfinite()) 0f else coerceIn(0f, 1f)

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

/**
 * 双线趋势折线图（请求数 + 费用），纯 Canvas 实现。
 * 数据按时间从左到右排列。
 */
@Composable
fun TrendLineChart(
    entries: List<TrendEntry>,
    modifier: Modifier = Modifier,
    requestColor: Color = OctopusTones.Request,
    costColor: Color = OctopusTones.Cost,
) {
    if (entries.isEmpty()) return

    val textColor = MiuixTheme.colorScheme.onSurfaceVariantSummary
    val gridColor = MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.12f)
    val labelPaint = android.graphics.Paint().apply {
        color = textColor.toArgb()
        textSize = 28f
        isAntiAlias = true
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp),
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            val compactChart = w < 340.dp.toPx()
            val padLeft = if (compactChart) 42f else 52f
            val padRight = if (compactChart) 42f else 52f
            val padTop = 12f
            val padBottom = 28f
            val chartW = w - padLeft - padRight
            val chartH = h - padTop - padBottom

            if (entries.isEmpty() || chartW <= 0f || chartH <= 0f) return@Canvas

            val maxRequest = entries.maxOf { it.requests }.coerceAtLeast(1L)
            val maxCost = entries.maxOf { it.cost }.coerceAtLeast(0.01)

            // 水平网格线（3 条）
            for (i in 0..2) {
                val y = padTop + chartH * i / 2f
                drawLine(gridColor, Offset(padLeft, y), Offset(w - padRight, y), strokeWidth = 1f)
            }

            // X 轴标签
            val androidCanvas = drawContext.canvas.nativeCanvas
            val labelIndexes = when {
                entries.size <= 3 -> entries.indices.toSet()
                compactChart -> setOf(0, entries.lastIndex / 2, entries.lastIndex)
                chartW < 420.dp.toPx() -> entries.indices.filter { index ->
                    index == 0 || index == entries.lastIndex || index % 2 == 0
                }.toSet()
                else -> entries.indices.toSet()
            }
            entries.forEachIndexed { index, entry ->
                if (index !in labelIndexes) return@forEachIndexed
                val x = padLeft + chartW * index / (entries.size - 1).coerceAtLeast(1)
                labelPaint.color = textColor.toArgb()
                labelPaint.textAlign = android.graphics.Paint.Align.CENTER
                androidCanvas.drawText(entry.label, x, h - 4f, labelPaint)
            }

            // 构建路径的辅助函数
            fun buildSmoothPath(points: List<Offset>): Path {
                val path = Path()
                if (points.isEmpty()) return path
                path.moveTo(points.first().x, points.first().y)
                for (i in 1 until points.size) {
                    val prev = points[i - 1]
                    val curr = points[i]
                    val cpx = (prev.x + curr.x) / 2f
                    path.cubicTo(cpx, prev.y, cpx, curr.y, curr.x, curr.y)
                }
                return path
            }

            // 请求数折线
            val requestPoints = entries.mapIndexed { index, entry ->
                val x = padLeft + chartW * index / (entries.size - 1).coerceAtLeast(1)
                val y = padTop + chartH * (1f - entry.requests.toFloat() / maxRequest.toFloat())
                Offset(x, y)
            }

            // 费用折线
            val costPoints = entries.mapIndexed { index, entry ->
                val x = padLeft + chartW * index / (entries.size - 1).coerceAtLeast(1)
                val y = padTop + chartH * (1f - entry.cost.toFloat() / maxCost.toFloat())
                Offset(x, y)
            }

            // 请求数面积
            if (requestPoints.size >= 2) {
                val areaPath = Path().apply {
                    addPath(buildSmoothPath(requestPoints))
                    lineTo(requestPoints.last().x, padTop + chartH)
                    lineTo(requestPoints.first().x, padTop + chartH)
                    close()
                }
                drawPath(areaPath, requestColor.copy(alpha = 0.06f))
            }

            // 费用面积
            if (costPoints.size >= 2) {
                val areaPath = Path().apply {
                    addPath(buildSmoothPath(costPoints))
                    lineTo(costPoints.last().x, padTop + chartH)
                    lineTo(costPoints.first().x, padTop + chartH)
                    close()
                }
                drawPath(areaPath, costColor.copy(alpha = 0.06f))
            }

            // 请求数折线
            if (requestPoints.size >= 2) {
                drawPath(buildSmoothPath(requestPoints), requestColor, style = Stroke(width = 2.5f, cap = StrokeCap.Round))
            }

            // 费用折线
            if (costPoints.size >= 2) {
                drawPath(buildSmoothPath(costPoints), costColor, style = Stroke(width = 2.5f, cap = StrokeCap.Round))
            }

            // 数据点
            requestPoints.forEach { point ->
                drawCircle(requestColor, radius = 3f, center = point)
            }
            costPoints.forEach { point ->
                drawCircle(costColor, radius = 3f, center = point)
            }

            // 左 Y 轴标签（请求数）
            labelPaint.color = requestColor.toArgb()
            labelPaint.textAlign = android.graphics.Paint.Align.RIGHT
            androidCanvas.drawText(formatCountCompact(maxRequest), padLeft - 6f, padTop + 10f, labelPaint)
            androidCanvas.drawText("0", padLeft - 6f, padTop + chartH, labelPaint)

            // 右 Y 轴标签（费用）
            labelPaint.color = costColor.toArgb()
            labelPaint.textAlign = android.graphics.Paint.Align.LEFT
            androidCanvas.drawText(formatMoneyCompact(maxCost), w - padRight + 6f, padTop + 10f, labelPaint)
        }
    }
}

data class TrendEntry(
    val label: String,
    val requests: Long,
    val cost: Double,
)

private fun formatCountCompact(value: Long): String = when {
    value >= 1_000_000 -> "${(value / 100_000.0).roundToInt() / 10.0}M"
    value >= 1_000 -> "${(value / 100.0).roundToInt() / 10.0}K"
    else -> value.toString()
}

private fun formatMoneyCompact(value: Double): String = when {
    value >= 1_000 -> "$${(value).roundToInt()}"
    value >= 1 -> "$${(value * 10).roundToInt() / 10.0}"
    else -> "$${(value * 100).roundToInt() / 100.0}"
}
