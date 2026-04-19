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
    val isApiKeyMode = uiState.mode == LoginMode.API_KEY

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = "登录"
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
                    onClick = { viewModel.updateMode(LoginMode.ADMIN) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        color = if (!isApiKeyMode) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Text(
                        "管理员登录", 
                        color = if (!isApiKeyMode) MiuixTheme.colorScheme.onPrimary else MiuixTheme.colorScheme.onSurface
                    )
                }
                
                Button(
                    onClick = { viewModel.updateMode(LoginMode.API_KEY) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        color = if (isApiKeyMode) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Text(
                        "API Key 访问",
                        color = if (isApiKeyMode) MiuixTheme.colorScheme.onPrimary else MiuixTheme.colorScheme.onSurface
                    )
                }
            }

            if (isApiKeyMode) {
                Text(
                    text = "API Key",
                    style = MiuixTheme.textStyles.body2,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp, start = 4.dp)
                )
                TextField(
                    value = uiState.apiKey,
                    onValueChange = viewModel::updateApiKey,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                )
            } else {
                Text(
                    text = "用户名",
                    style = MiuixTheme.textStyles.body2,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp, start = 4.dp)
                )
                TextField(
                    value = uiState.username,
                    onValueChange = viewModel::updateUsername,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                )
                
                Text(
                    text = "密码",
                    style = MiuixTheme.textStyles.body2,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp, start = 4.dp)
                )
                TextField(
                    value = uiState.password,
                    onValueChange = viewModel::updatePassword,
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
                onClick = viewModel::submit,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSubmitting
            ) {
                Text(if (uiState.isSubmitting) "..." else "Login")
            }

            val context = androidx.compose.ui.platform.LocalContext.current
            top.yukonga.miuix.kmp.basic.TextButton(
                onClick = {
                    try {
                        val process = Runtime.getRuntime().exec("logcat -d -t 1500 -v threadtime")
                        val reader = java.io.BufferedReader(java.io.InputStreamReader(process.inputStream))
                        val log = java.lang.StringBuilder()
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            log.append(line).append("\n")
                        }
                        
                        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(android.content.Intent.EXTRA_SUBJECT, "Octopus Logs")
                            putExtra(android.content.Intent.EXTRA_TEXT, log.toString())
                        }
                        context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Logs"))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Export Logs", style = MiuixTheme.textStyles.body2, color = MiuixTheme.colorScheme.primary)
            }
        }
    }
}
