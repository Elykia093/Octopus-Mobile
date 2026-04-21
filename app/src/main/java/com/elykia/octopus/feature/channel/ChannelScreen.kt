package com.elykia.octopus.feature.channel

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
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun ChannelScreen(viewModel: ChannelViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = "渠道",
                actions = {
                    IconButton(onClick = { viewModel.loadChannels(isRefresh = true) }) {
                        Icon(imageVector = AppMiuixIcons.Refresh, contentDescription = "刷新")
                    }
                    IconButton(onClick = { /* TODO: 新增渠道 */ }) {
                        Icon(imageVector = AppMiuixIcons.Add, contentDescription = "新增渠道")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading && uiState.items.isEmpty()) {
            LoadingPane(title = "渠道列表")
            return@Scaffold
        }

        if (uiState.error != null && uiState.items.isEmpty()) {
            ErrorPane(message = "加载失败: ${uiState.error}", onRetry = { viewModel.loadChannels(isRefresh = true) })
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SectionLabel(
                    title = "已加载 ${uiState.items.size} 个渠道", 
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 8.dp)
                )
            }

            items(uiState.items) { channel ->
                Card(
                    modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()
                ) {
                    Column {
                        BasicComponent(
                            title = channel.name,
                            summary = "模型: ${if (channel.model.length > 40) channel.model.take(40) + "..." else channel.model}",
                            startAction = {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(if (channel.enabled) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.error)
                                )
                            },
                            endActions = {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MiuixTheme.colorScheme.surfaceContainerHigh)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "类型: ${channel.type}",
                                        style = MiuixTheme.textStyles.body2,
                                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                                    )
                                }
                            }
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "密钥: ${channel.keys.size} 个",
                                style = MiuixTheme.textStyles.body2,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                            )
                            Text(
                                text = if (channel.enabled) "已启用" else "已禁用",
                                style = MiuixTheme.textStyles.body2,
                                color = if (channel.enabled) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            if (uiState.isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        Text("加载中...", style = MiuixTheme.textStyles.body2, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.size(24.dp))
            }
        }
    }
}
