package com.elykia.octopus.feature.log

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.elykia.octopus.core.designsystem.AppListCard
import com.elykia.octopus.core.designsystem.ErrorPane
import com.elykia.octopus.core.designsystem.LoadingPane
import com.elykia.octopus.core.designsystem.SectionLabel
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun LogScreen(viewModel: LogViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Logs", style = MiuixTheme.textStyles.title2)
                Button(onClick = { viewModel.loadLogs(isRefresh = true) }) {
                    Text("Refresh")
                }
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading && uiState.items.isEmpty()) {
            LoadingPane(title = "Logs")
            return@Scaffold
        }
        
        if (uiState.error != null && uiState.items.isEmpty()) {
            ErrorPane(message = uiState.error!!, onRetry = { viewModel.loadLogs(isRefresh = true) })
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SectionLabel(title = "Total Logs: ${uiState.totalCount}")
            }

            items(uiState.items) { log ->
                AppListCard {
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = log.model.ifBlank { "Unknown Model" }, style = MiuixTheme.textStyles.title4)
                            Text(
                                text = "${log.use_time} ms",
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                            )
                        }
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = "Tokens: ${log.prompt_tokens} (in) / ${log.completion_tokens} (out)",
                            style = MiuixTheme.textStyles.body2,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                        )
                        if (log.content.isNotBlank()) {
                            Spacer(modifier = Modifier.size(4.dp))
                            Text(
                                text = "Status: ${log.content}",
                                style = MiuixTheme.textStyles.body2,
                                color = if (log.type == 2) MiuixTheme.colorScheme.error else MiuixTheme.colorScheme.onSurfaceVariantSummary
                            )
                        }
                    }
                }
            }

            if (uiState.isLoading) {
                item {
                    Text("Loading more...", modifier = Modifier.padding(16.dp))
                }
            } else if (uiState.items.size < uiState.totalCount) {
                item {
                    Button(onClick = { viewModel.loadLogs() }, modifier = Modifier.fillMaxWidth()) {
                        Text("Load More")
                    }
                }
            }
        }
    }
}
