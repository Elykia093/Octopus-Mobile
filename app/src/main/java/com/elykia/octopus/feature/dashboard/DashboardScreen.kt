package com.elykia.octopus.feature.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.elykia.octopus.core.data.model.TrendEntry
import com.elykia.octopus.core.designsystem.ErrorPane
import com.elykia.octopus.core.designsystem.LoadingPane
import com.elykia.octopus.feature.dashboard.util.formatCurrency
import com.elykia.octopus.feature.dashboard.util.formatNumber
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.theme.MiuixTheme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = "大盘"
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            if (uiState.isLoading) {
                LoadingPane("大盘数据")
            } else if (uiState.error != null) {
                ErrorPane(
                    message = "加载失败: ${uiState.error}",
                    onRetry = { viewModel.loadData(isRefresh = true) }
                )
            } else {
                DashboardContent(
                    state = uiState,
                    onToggleScope = { viewModel.toggleScope() },
                    onToggleTrend = { viewModel.toggleTrend() },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun DashboardContent(
    state: DashboardUiState,
    onToggleScope: () -> Unit,
    onToggleTrend: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val stats = state.stats

    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (stats != null) {
            // Scope Toggle
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (state.showToday) "今日数据" else "累计数据",
                    style = MiuixTheme.textStyles.title3,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = onToggleScope,
                    colors = ButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.primary
                    )
                ) {
                    Text(if (state.showToday) "切换为累计" else "切换为今日", color = MiuixTheme.colorScheme.onPrimary)
                }
            }

            // Summary Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryCard(
                    title = "请求数",
                    value = formatNumber(
                        (if (state.showToday) stats.daily.requestCount else stats.total.requestCount).toDouble()
                    ),
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = "消耗",
                    value = formatCurrency(
                        if (state.showToday) stats.daily.costValue else stats.total.costValue
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryCard(
                    title = "Token 数",
                    value = formatNumber(
                        (if (state.showToday) stats.daily.tokenValue else stats.total.tokenValue).toDouble()
                    ),
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = "输入消耗",
                    value = formatCurrency(
                        if (state.showToday) stats.daily.inputCost else stats.total.inputCost
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Trend Chart
            val trendData = if (state.showHourlyTrend) stats.trendHourly else stats.trendDaily
            if (trendData.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(MiuixTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (state.showHourlyTrend) "24小时趋势" else "14天趋势",
                                style = MiuixTheme.textStyles.title4,
                                fontWeight = FontWeight.Bold
                            )
                            Button(
                                onClick = onToggleTrend,
                                colors = ButtonDefaults.buttonColors(
                                    color = MiuixTheme.colorScheme.surfaceContainerHigh
                                )
                            ) {
                                Text(if (state.showHourlyTrend) "切换按天" else "切换按小时", color = MiuixTheme.colorScheme.onSurface)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        // Legend
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.width(12.dp).height(4.dp).clip(CircleShape).background(MiuixTheme.colorScheme.primary))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("请求数", style = MiuixTheme.textStyles.body2, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.width(12.dp).height(4.dp).clip(CircleShape).background(MiuixTheme.colorScheme.error))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("消耗", style = MiuixTheme.textStyles.body2, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        TrendChart(
                            stats = trendData,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = trendData.first().title,
                                style = MiuixTheme.textStyles.body2,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                            )
                            Text(
                                text = trendData.last().title,
                                style = MiuixTheme.textStyles.body2,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MiuixTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = title,
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                style = MiuixTheme.textStyles.title2,
                fontWeight = FontWeight.Bold,
                color = MiuixTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun TrendChart(
    stats: List<TrendEntry>,
    modifier: Modifier = Modifier
) {
    val primaryColor = MiuixTheme.colorScheme.primary
    val secondaryColor = MiuixTheme.colorScheme.error
    val gridColor = MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.1f)

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        if (stats.size < 2) return@Canvas

        val maxRequest = stats.maxOfOrNull { it.requestCount }?.takeIf { it > 0 } ?: 1L
        val maxCost = stats.maxOfOrNull { it.costValue }?.takeIf { it > 0.0 } ?: 1.0

        val stepX = width / (stats.size - 1)

        // Draw horizontal grid lines
        val gridLines = 4
        for (i in 0..gridLines) {
            val y = height * (i.toFloat() / gridLines)
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )
        }

        val requestPath = Path()
        val costPath = Path()

        val requestPoints = stats.mapIndexed { index, stat ->
            val x = index * stepX
            val y = height * 0.95f - (stat.requestCount.toFloat() / maxRequest.toFloat() * height * 0.9f)
            Offset(x, y)
        }
        
        val costPoints = stats.mapIndexed { index, stat ->
            val x = index * stepX
            val y = height * 0.95f - ((stat.costValue / maxCost).toFloat() * height * 0.9f)
            Offset(x, y)
        }

        // Smooth cubic bezier curves for Request
        requestPath.moveTo(requestPoints.first().x, requestPoints.first().y)
        for (i in 0 until requestPoints.size - 1) {
            val p1 = requestPoints[i]
            val p2 = requestPoints[i + 1]
            val cx = (p1.x + p2.x) / 2f
            requestPath.cubicTo(cx, p1.y, cx, p2.y, p2.x, p2.y)
        }

        // Smooth cubic bezier curves for Cost
        costPath.moveTo(costPoints.first().x, costPoints.first().y)
        for (i in 0 until costPoints.size - 1) {
            val p1 = costPoints[i]
            val p2 = costPoints[i + 1]
            val cx = (p1.x + p2.x) / 2f
            costPath.cubicTo(cx, p1.y, cx, p2.y, p2.x, p2.y)
        }

        // Gradient Fills
        val requestFillPath = Path().apply {
            addPath(requestPath)
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }
        drawPath(
            path = requestFillPath,
            brush = Brush.verticalGradient(
                colors = listOf(primaryColor.copy(alpha = 0.2f), primaryColor.copy(alpha = 0.0f)),
                startY = 0f,
                endY = height
            )
        )

        val costFillPath = Path().apply {
            addPath(costPath)
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }
        drawPath(
            path = costFillPath,
            brush = Brush.verticalGradient(
                colors = listOf(secondaryColor.copy(alpha = 0.2f), secondaryColor.copy(alpha = 0.0f)),
                startY = 0f,
                endY = height
            )
        )

        // Draw Lines
        drawPath(
            path = requestPath,
            color = primaryColor,
            style = Stroke(width = 3.dp.toPx())
        )
        drawPath(
            path = costPath,
            color = secondaryColor,
            style = Stroke(width = 3.dp.toPx())
        )
    }
}
