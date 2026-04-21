package com.elykia.octopus.feature.log

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.elykia.octopus.core.designsystem.ErrorPane
import com.elykia.octopus.core.designsystem.LoadingPane
import com.elykia.octopus.core.designsystem.SectionLabel
import com.elykia.octopus.core.designsystem.icons.AppMiuixIcons
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun LogScreen(viewModel: LogViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = "请求日志",
                actions = {
                    IconButton(onClick = { viewModel.loadLogs(isRefresh = true) }) {
                        Icon(imageVector = AppMiuixIcons.Refresh, contentDescription = "刷新")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading && uiState.items.isEmpty()) {
            LoadingPane(title = "日志记录")
            return@Scaffold
        }

        if (uiState.error != null && uiState.items.isEmpty()) {
            ErrorPane(message = "加载失败: ${uiState.error}", onRetry = { viewModel.loadLogs(isRefresh = true) })
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SectionLabel(
                    title = "已加载 ${uiState.items.size} 条日志",
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 8.dp)
                )
            }

            items(uiState.items) { log ->
                val timeFormat = SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault())
                val timeString = timeFormat.format(Date(log.createdAt * 1000L))
                val isError = log.type == 2 || log.type == 3

                Card(
                    modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
                    colors = CardDefaults.defaultColors(
                        color = if (isError) MiuixTheme.colorScheme.errorContainer else MiuixTheme.colorScheme.surface,
                        contentColor = if (isError) MiuixTheme.colorScheme.onErrorContainer else MiuixTheme.colorScheme.onSurface
                    )
                ) {
                    Column {
                        BasicComponent(
                            title = log.modelName.ifBlank { "未知模型" },
                            summary = "Token: ${log.promptTokens} (入) / ${log.completionTokens} (出)",
                            startAction = {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(if (!isError) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onErrorContainer)
                                )
                            },
                            endActions = {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (log.elapsedTime > 3000) MiuixTheme.colorScheme.error else MiuixTheme.colorScheme.surfaceContainerHigh)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${log.elapsedTime} ms",
                                        style = MiuixTheme.textStyles.body2,
                                        color = if (log.elapsedTime > 3000) MiuixTheme.colorScheme.onError else MiuixTheme.colorScheme.onSurfaceVariantSummary
                                    )
                                }
                            }
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "用户: ${log.username.ifBlank { "-" }} | 令牌: ${log.tokenName.ifBlank { "-" }}",
                                style = MiuixTheme.textStyles.body2,
                                color = if (isError) MiuixTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f) else MiuixTheme.colorScheme.onSurfaceVariantSummary
                            )
                            Text(
                                text = timeString,
                                style = MiuixTheme.textStyles.body2,
                                color = if (isError) MiuixTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f) else MiuixTheme.colorScheme.onSurfaceVariantSummary
                            )
                        }

                        if (log.content.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isError) MiuixTheme.colorScheme.onErrorContainer.copy(alpha = 0.1f) else MiuixTheme.colorScheme.surfaceContainerHigh)
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = log.content,
                                    style = MiuixTheme.textStyles.body2,
                                    color = if (isError) MiuixTheme.colorScheme.onErrorContainer else MiuixTheme.colorScheme.onSurfaceVariantSummary
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            if (uiState.isLoading && uiState.items.isNotEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        Text("加载中...", style = MiuixTheme.textStyles.body2, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                    }
                }
            } else if (uiState.hasMore) {
                item {
                    Button(
                        onClick = { viewModel.loadLogs() },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            color = MiuixTheme.colorScheme.surfaceContainerHigh
                        )
                    ) {
                        Text("加载更多", color = MiuixTheme.colorScheme.onSurface)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.size(24.dp))
            }
        }
    }
}
