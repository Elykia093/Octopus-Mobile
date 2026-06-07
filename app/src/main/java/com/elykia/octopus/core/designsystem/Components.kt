package com.elykia.octopus.core.designsystem

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.theme.MiuixTheme
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
    val dockShape = RoundedCornerShape(24.dp)
    val itemShape = RoundedCornerShape(16.dp)
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        if (items.isEmpty()) return@BoxWithConstraints

        val compactDock = items.size >= 6 && maxWidth < 344.dp
        val ultraCompactDock = items.size >= 6 && maxWidth < 316.dp
        val showLabels = items.size <= 6 && maxWidth >= 352.dp
        val sidePadding = when {
            ultraCompactDock -> 0.dp
            compactDock -> 4.dp
            else -> 8.dp
        }
        val gap = when {
            ultraCompactDock -> 0.dp
            compactDock -> 4.dp
            else -> 5.dp
        }
        val dockMaxWidth = minOf(maxWidth, 364.dp)
        val availableItemWidth = (dockMaxWidth - sidePadding * 2f - gap * (items.size - 1).toFloat()) / items.size.toFloat()
        val maxItemSize = if (showLabels) 50.dp else 48.dp
        val minItemSize = if (availableItemWidth >= 44.dp) 44.dp else availableItemWidth
        val itemSize = availableItemWidth.coerceIn(minItemSize, maxItemSize)
        val itemHeight = if (showLabels) 50.dp else itemSize
        val iconSize = if (showLabels) 20.dp else (itemSize * 0.48f).coerceIn(19.dp, 24.dp)
        val dockWidth = (itemSize * items.size.toFloat() + sidePadding * 2f + gap * (items.size - 1).toFloat()).coerceAtMost(364.dp)

        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .width(dockWidth)
                .shadow(
                    elevation = 8.dp,
                    shape = dockShape,
                    clip = false,
                    ambientColor = Color.Black.copy(alpha = 0.03f),
                    spotColor = Color.Black.copy(alpha = 0.08f),
                )
                .clip(dockShape)
                .background(OctopusTokens.Card.copy(alpha = 0.96f))
                .border(1.dp, OctopusTokens.Border.copy(alpha = 0.72f), dockShape)
                .padding(horizontal = sidePadding, vertical = if (showLabels) 5.dp else 7.dp),
            horizontalArrangement = Arrangement.spacedBy(gap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEach { item ->
                val selected = item.key == selectedKey
                Box(
                    modifier = Modifier
                        .width(itemSize)
                        .height(itemHeight)
                        .clip(itemShape)
                        .background(if (selected) OctopusTokens.SelectedNav.copy(alpha = 0.94f) else Color.Transparent)
                        .clickable { onSelected(item.key) },
                    contentAlignment = Alignment.Center,
                ) {
                    val itemColor = if (selected) OctopusTokens.TextPrimary else OctopusTokens.NavMuted
                    if (showLabels) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = itemColor,
                                modifier = Modifier.size(iconSize),
                            )
                            Text(
                                text = item.label,
                                style = MiuixTheme.textStyles.body2,
                                color = itemColor,
                                fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 10.sp,
                                lineHeight = 12.sp,
                            )
                        }
                    } else {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = itemColor,
                            modifier = Modifier.size(iconSize),
                        )
                    }
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
