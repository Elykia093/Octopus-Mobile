package com.elykia.octopus.feature.model

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.elykia.octopus.core.data.model.LlmChannel
import com.elykia.octopus.core.data.model.LlmInfo
import com.elykia.octopus.core.designsystem.ErrorPane
import com.elykia.octopus.core.designsystem.LoadingPane
import com.elykia.octopus.core.designsystem.SectionLabel
import com.elykia.octopus.core.designsystem.icons.AppMiuixIcons
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.theme.MiuixTheme
import java.util.Locale

@Composable
fun ModelScreen(viewModel: ModelViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = "价格",
                actions = {
                    IconButton(onClick = { viewModel.syncPrice() }) {
                        Icon(imageVector = AppMiuixIcons.Sync, contentDescription = "同步价格")
                    }
                    IconButton(onClick = { viewModel.loadModels(isRefresh = true) }) {
                        Icon(imageVector = AppMiuixIcons.Refresh, contentDescription = "刷新")
                    }
                },
            )
        },
    ) { paddingValues ->
        if (uiState.isLoading && uiState.models.isEmpty()) {
            LoadingPane(title = "模型价格")
            return@Scaffold
        }

        if (uiState.error != null && uiState.models.isEmpty()) {
            ErrorPane(message = "加载失败: ${uiState.error}", onRetry = { viewModel.loadModels(isRefresh = true) })
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                ModelSummaryCard(
                    modelCount = uiState.models.size,
                    channelCount = uiState.channels.size,
                    lastUpdateTime = uiState.lastUpdateTime,
                    isSyncing = uiState.isSyncingPrice,
                )
            }

            uiState.error?.let { error ->
                item {
                    Text(
                        text = error,
                        color = MiuixTheme.colorScheme.error,
                        style = MiuixTheme.textStyles.body2,
                        modifier = Modifier.padding(horizontal = 24.dp),
                    )
                }
            }

            item {
                SectionLabel(
                    title = "全局模型价格",
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }

            items(uiState.models) { model ->
                ModelCard(
                    model = model,
                    channels = uiState.channelsByModel[model.name].orEmpty(),
                )
            }

            if (uiState.models.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("暂无模型价格", color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
private fun ModelSummaryCard(
    modelCount: Int,
    channelCount: Int,
    lastUpdateTime: String,
    isSyncing: Boolean,
) {
    Card(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth(),
    ) {
        BasicComponent(
            title = "模型价格",
            summary = "模型 $modelCount 个 · 渠道映射 $channelCount 条 · ${lastUpdateTime.ifBlank { "未同步" }}",
            startAction = {
                Icon(
                    imageVector = AppMiuixIcons.Paid,
                    contentDescription = "模型价格",
                    tint = MiuixTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                )
            },
            endActions = {
                if (isSyncing) {
                    Text(
                        text = "同步中",
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.primary,
                    )
                }
            },
        )
    }
}

@Composable
private fun ModelCard(
    model: LlmInfo,
    channels: List<LlmChannel>,
) {
    Card(
        modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
    ) {
        Column {
            BasicComponent(
                title = model.name.ifBlank { "未命名模型" },
                summary = "输入 ${priceText(model.input)} · 输出 ${priceText(model.output)} · 缓存 ${priceText(model.cacheRead)} / ${priceText(model.cacheWrite)}",
                startAction = {
                    Icon(
                        imageVector = AppMiuixIcons.Paid,
                        contentDescription = "价格",
                        tint = MiuixTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp),
                    )
                },
                endActions = {
                    Text(
                        text = "${channels.count { it.enabled }}/${channels.size}",
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                },
            )

            if (channels.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    channels.take(4).forEach { channel ->
                        ChannelChip(channel)
                    }
                    if (channels.size > 4) {
                        PriceChip("+${channels.size - 4} 个渠道")
                    }
                }
            } else {
                Text(
                    text = "暂无渠道映射",
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                )
            }
        }
    }
}

@Composable
private fun ChannelChip(channel: LlmChannel) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MiuixTheme.colorScheme.surfaceContainerHigh)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(50))
                .background(if (channel.enabled) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.error),
        )
        Text(
            text = channel.channelName.ifBlank { "渠道 ${channel.channelId}" },
            style = MiuixTheme.textStyles.body2,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
    }
}

@Composable
private fun PriceChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MiuixTheme.colorScheme.surfaceContainerHigh)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(
            text = text,
            style = MiuixTheme.textStyles.body2,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
    }
}

private fun priceText(value: Double): String = "$" + String.format(Locale.US, "%.4f", value)
