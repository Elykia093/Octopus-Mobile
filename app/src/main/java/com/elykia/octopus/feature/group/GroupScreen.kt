package com.elykia.octopus.feature.group

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
import com.elykia.octopus.core.data.model.Group
import com.elykia.octopus.core.data.model.GroupItem
import com.elykia.octopus.core.data.model.GroupMode
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

@Composable
fun GroupScreen(viewModel: GroupViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = "分组",
                actions = {
                    IconButton(onClick = { viewModel.loadGroups(isRefresh = true) }) {
                        Icon(imageVector = AppMiuixIcons.Refresh, contentDescription = "刷新")
                    }
                },
            )
        },
    ) { paddingValues ->
        if (uiState.isLoading && uiState.items.isEmpty()) {
            LoadingPane(title = "分组列表")
            return@Scaffold
        }

        if (uiState.error != null && uiState.items.isEmpty()) {
            ErrorPane(message = "加载失败: ${uiState.error}", onRetry = { viewModel.loadGroups(isRefresh = true) })
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                SectionLabel(
                    title = "已加载 ${uiState.items.size} 个分组",
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 8.dp),
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

            items(uiState.items) { group ->
                GroupCard(
                    group = group,
                    isPinning = uiState.pinningIds.contains(group.id),
                    onTogglePinned = { viewModel.togglePinned(group) },
                )
            }

            if (uiState.items.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("暂无分组", color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
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
private fun GroupCard(
    group: Group,
    isPinning: Boolean,
    onTogglePinned: () -> Unit,
) {
    Card(
        modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
    ) {
        Column {
            BasicComponent(
                title = group.name.ifBlank { "未命名分组" },
                summary = "模式: ${groupModeLabel(group.mode)} · 成员: ${group.items.size} · 正则: ${group.matchRegex.ifBlank { "-" }}",
                startAction = {
                    Icon(
                        imageVector = AppMiuixIcons.Group,
                        contentDescription = "分组",
                        tint = MiuixTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp),
                    )
                },
                endActions = {
                    IconButton(onClick = { if (!isPinning) onTogglePinned() }) {
                        Icon(
                            imageVector = if (group.pinned) AppMiuixIcons.Star else AppMiuixIcons.StarBorder,
                            contentDescription = if (group.pinned) "取消置顶" else "置顶",
                            tint = if (group.pinned) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                    }
                },
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                GroupChip("超时 ${group.firstTokenTimeOut}s")
                GroupChip("会话 ${group.sessionKeepTime}s")
                GroupChip(if (group.retryEnabled) "重试 ${group.maxRetries}" else "未启用重试")
                group.activePresetId?.let { GroupChip("预设 #$it") }
                if (group.pinned) GroupChip("已置顶")
            }

            val previewItems = group.items.take(3)
            if (previewItems.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    previewItems.forEach { item ->
                        GroupItemRow(item)
                    }
                    if (group.items.size > previewItems.size) {
                        Text(
                            text = "+${group.items.size - previewItems.size} 个成员",
                            style = MiuixTheme.textStyles.body2,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                    }
                }
            } else {
                Text(
                    text = "暂无成员",
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    modifier = Modifier.padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 16.dp),
                )
            }
        }
    }
}

@Composable
private fun GroupChip(text: String) {
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

@Composable
private fun GroupItemRow(item: GroupItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.modelName.ifBlank { "未设置模型" },
                style = MiuixTheme.textStyles.body1,
                color = MiuixTheme.colorScheme.onSurface,
            )
            Text(
                text = "渠道 ID ${item.channelId}",
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        }
        Text(
            text = "P${item.priority} / W${item.weight}",
            style = MiuixTheme.textStyles.body2,
            color = MiuixTheme.colorScheme.primary,
        )
    }
}

private fun groupModeLabel(mode: Int): String = when (mode) {
    GroupMode.ROUND_ROBIN -> "轮询"
    GroupMode.RANDOM -> "随机"
    GroupMode.FAILOVER -> "故障转移"
    GroupMode.WEIGHTED -> "加权"
    else -> "模式 $mode"
}
