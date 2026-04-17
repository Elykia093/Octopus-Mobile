package com.elykia.octopus.feature.apikey

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
import com.elykia.octopus.core.designsystem.AppListCard
import com.elykia.octopus.core.designsystem.ErrorPane
import com.elykia.octopus.core.designsystem.LoadingPane
import com.elykia.octopus.core.designsystem.SectionLabel
import com.elykia.octopus.core.designsystem.icons.AppMiuixIcons
import com.elykia.octopus.feature.dashboard.util.formatCurrency
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun ApiKeyScreen(viewModel: ApiKeyViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = "令牌管理",
                titleCentered = true,
                actions = {
                    IconButton(onClick = { viewModel.loadApiKeys(isRefresh = true) }) {
                        Icon(imageVector = AppMiuixIcons.Refresh, contentDescription = "刷新")
                    }
                    IconButton(onClick = { /* TODO: 新增 API Key */ }) {
                        Icon(imageVector = AppMiuixIcons.Add, contentDescription = "新增令牌")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading && uiState.items.isEmpty()) {
            LoadingPane(title = "令牌列表")
            return@Scaffold
        }

        if (uiState.error != null && uiState.items.isEmpty()) {
            ErrorPane(message = "加载失败: ${uiState.error}", onRetry = { viewModel.loadApiKeys(isRefresh = true) })
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SectionLabel(
                    title = "共 ${uiState.totalCount} 个令牌",
                    modifier = Modifier.padding(horizontal = 16.dp, top = 8.dp)
                )
            }

            items(uiState.items) { key ->
                val timeFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val expireString = if (key.expireAt == 0L) "永不过期" else timeFormat.format(Date(key.expireAt * 1000L))
                val isExpired = key.expireAt > 0 && key.expireAt * 1000L < System.currentTimeMillis()

                AppListCard(
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // 状态指示器
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(if (key.status == 1 && !isExpired) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.error)
                                )
                                Spacer(modifier = Modifier.size(8.dp))
                                Text(
                                    text = key.name.ifBlank { "Token #${key.id}" },
                                    style = MiuixTheme.textStyles.title4,
                                    color = if (isExpired) MiuixTheme.colorScheme.error else MiuixTheme.colorScheme.onSurface
                                )
                            }

                            // 额度标签
                            val quotaText = if (key.maxQuota == 0L) "无限额度" else "已用: ${formatCurrency(key.usedQuota.toDouble() / 500000.0)}"
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MiuixTheme.colorScheme.surfaceContainerHigh)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = quotaText,
                                    style = MiuixTheme.textStyles.body2,
                                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.size(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "模型权限: ${if (key.models.isBlank()) "全部模型" else "部分限制"}",
                                style = MiuixTheme.textStyles.body2,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                            )
                            Text(
                                text = "过期时间: $expireString",
                                style = MiuixTheme.textStyles.body2,
                                color = if (isExpired) MiuixTheme.colorScheme.error else MiuixTheme.colorScheme.onSurfaceVariantSummary
                            )
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
            } else if (uiState.items.size < uiState.totalCount) {
                item {
                    Button(
                        onClick = { viewModel.loadApiKeys() },
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