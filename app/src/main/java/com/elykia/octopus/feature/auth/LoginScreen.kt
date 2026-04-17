package com.elykia.octopus.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun LoginScreen(viewModel: LoginViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = "登录",
                titleCentered = true
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "欢迎回来",
                style = MiuixTheme.textStyles.title1,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "请使用您的账号或 API Key 登录",
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Mode Toggle
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { viewModel.toggleMode(false) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        color = if (!uiState.isApiKeyMode) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Text(
                        "管理员登录", 
                        color = if (!uiState.isApiKeyMode) MiuixTheme.colorScheme.onPrimary else MiuixTheme.colorScheme.onSurface
                    )
                }
                
                Button(
                    onClick = { viewModel.toggleMode(true) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        color = if (uiState.isApiKeyMode) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Text(
                        "API Key 访问",
                        color = if (uiState.isApiKeyMode) MiuixTheme.colorScheme.onPrimary else MiuixTheme.colorScheme.onSurface
                    )
                }
            }

            if (uiState.isApiKeyMode) {
                TextField(
                    value = uiState.apiKey,
                    onValueChange = viewModel::onApiKeyChange,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                )
            } else {
                TextField(
                    value = uiState.username,
                    onValueChange = viewModel::onUsernameChange,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                )
                
                // Note: Miuix TextField might not have VisualTransformation out of the box in the snapshot version, 
                // but we will use the standard setup or just pass the parameter if it supports it.
                TextField(
                    value = uiState.password,
                    onValueChange = viewModel::onPasswordChange,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                )
            }

            if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = MiuixTheme.colorScheme.error,
                    style = MiuixTheme.textStyles.body2,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Button(
                onClick = viewModel::login,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            ) {
                Text(if (uiState.isLoading) "登录中..." else "登录")
            }
        }
    }
}
