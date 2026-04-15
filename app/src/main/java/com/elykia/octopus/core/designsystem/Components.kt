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
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.nativeCanvas
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

/**
 * 双线趋势折线图（请求数 + 费用），纯 Canvas 实现。
 * 数据按时间从左到右排列。
 */
@Composable
fun TrendLineChart(
    entries: List<TrendEntry>,
    modifier: Modifier = Modifier,
    requestColor: Color = Color(0xFF007AFF),
    costColor: Color = Color(0xFF34C759),
) {
    if (entries.isEmpty()) return

    val textColor = MiuixTheme.colorScheme.onSurfaceVariantSummary
    val gridColor = MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.12f)
    val labelPaint = android.graphics.Paint().apply {
        color = textColor.hashCode()
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
            val padLeft = 52f
            val padRight = 52f
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
            entries.forEachIndexed { index, entry ->
                val x = padLeft + chartW * index / (entries.size - 1).coerceAtLeast(1)
                labelPaint.color = textColor.hashCode()
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
            labelPaint.color = requestColor.hashCode()
            labelPaint.textAlign = android.graphics.Paint.Align.RIGHT
            androidCanvas.drawText(formatCountCompact(maxRequest), padLeft - 6f, padTop + 10f, labelPaint)
            androidCanvas.drawText("0", padLeft - 6f, padTop + chartH, labelPaint)

            // 右 Y 轴标签（费用）
            labelPaint.color = costColor.hashCode()
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
