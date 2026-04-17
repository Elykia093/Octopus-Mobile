package com.elykia.octopus.feature.apikey

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
fun ApiKeyScreen(viewModel: ApiKeyViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("API Keys", style = MiuixTheme.textStyles.title2)
                Button(onClick = { viewModel.loadApiKeys(isRefresh = true) }) {
                    Text("Refresh")
                }
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading && uiState.items.isEmpty()) {
            LoadingPane(title = "API Keys")
            return@Scaffold
        }
        
        if (uiState.error != null && uiState.items.isEmpty()) {
            ErrorPane(message = uiState.error!!, onRetry = { viewModel.loadApiKeys(isRefresh = true) })
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SectionLabel(title = "Total Keys: ${uiState.totalCount}")
            }

            items(uiState.items) { key ->
                AppListCard {
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = key.name.ifBlank { "Key #${key.id}" }, style = MiuixTheme.textStyles.title4)
                            Text(
                                text = if (key.status == 1) "Enabled" else "Disabled",
                                color = if (key.status == 1) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurfaceVariantSummary
                            )
                        }
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = if (key.expireAt == 0L) "Never expires" else "Expires at ${key.expireAt}",
                            style = MiuixTheme.textStyles.body2,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                        )
                    }
                }
            }

            if (uiState.isLoading) {
                item {
                    Text("Loading more...", modifier = Modifier.padding(16.dp))
                }
            } else if (uiState.items.size < uiState.totalCount) {
                item {
                    Button(onClick = { viewModel.loadApiKeys() }, modifier = Modifier.fillMaxWidth()) {
                        Text("Load More")
                    }
                }
            }
        }
    }
}
