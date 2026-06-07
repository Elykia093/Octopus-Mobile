package com.elykia.octopus.core.designsystem

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import top.yukonga.miuix.kmp.theme.MiuixTheme

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
