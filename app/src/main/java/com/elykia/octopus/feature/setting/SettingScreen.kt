package com.elykia.octopus.feature.setting

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.elykia.octopus.core.designsystem.icons.AppMiuixIcons
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.theme.MiuixTheme
import java.io.BufferedReader
import java.io.InputStreamReader

@Composable
fun SettingScreen(viewModel: SettingViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = "设置"
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {

                // Server Config Card
                Column {
                    Text(
                        text = "连接信息",
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .padding(bottom = 8.dp)
                    )
                    
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            BasicComponent(
                                title = "服务器地址",
                                summary = uiState.config.baseUrl.ifBlank { "未配置" },
                                startAction = {
                                    Icon(
                                        imageVector = AppMiuixIcons.Home,
                                        contentDescription = "服务器地址",
                                        tint = MiuixTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            )
                            
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MiuixTheme.colorScheme.surfaceContainerHigh))

                            BasicComponent(
                                title = "身份模式",
                                summary = if (uiState.isApiKeyMode) "API Key 访问" else "管理员模式",
                                startAction = {
                                    Icon(
                                        imageVector = AppMiuixIcons.Group,
                                        contentDescription = "身份模式",
                                        tint = MiuixTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            )
                        }
                    }
                }

                // App Info Card
                Column {
                    Text(
                        text = "关于 Octopus",
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .padding(bottom = 8.dp)
                    )
                    
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            BasicComponent(
                                title = "版本",
                                summary = "v1.0.0 (New Architecture)",
                                startAction = {
                                    Icon(
                                        imageVector = AppMiuixIcons.Info,
                                        contentDescription = "版本",
                                        tint = MiuixTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            )
                        }
                    }
                }

                // Debug Card
                val context = LocalContext.current
                Column {
                    Text(
                        text = "调试",
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .padding(bottom = 8.dp)
                    )

                    Card(modifier = Modifier.fillMaxWidth()) {
                        BasicComponent(
                            title = "导出日志",
                            summary = "导出应用本地日志用于排查问题",
                            startAction = {
                                Icon(
                                    imageVector = AppMiuixIcons.Info,
                                    contentDescription = "导出日志",
                                    tint = MiuixTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            endActions = {
                                TextButton(
                                    onClick = {
                                        try {
                                            val process = Runtime.getRuntime().exec("logcat -d -t 1500 -v threadtime")
                                            val reader = BufferedReader(InputStreamReader(process.inputStream))
                                            val log = StringBuilder()
                                            var line: String?
                                            while (reader.readLine().also { line = it } != null) {
                                                log.append(line).append("\n")
                                            }
                                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(Intent.EXTRA_SUBJECT, "Octopus Logs")
                                                putExtra(Intent.EXTRA_TEXT, log.toString())
                                            }
                                            context.startActivity(Intent.createChooser(shareIntent, "分享日志"))
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                ) {
                                    Text("导出", style = MiuixTheme.textStyles.body2, color = MiuixTheme.colorScheme.primary)
                                }
                            }
                        )
                    }
                }

                // Actions
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = viewModel::logout,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            color = MiuixTheme.colorScheme.error,
                        )
                    ) {
                        Text(if (uiState.isLoggingOut) "正在注销..." else "退出登录", color = MiuixTheme.colorScheme.onError)
                    }

                    Button(
                        onClick = viewModel::resetServerConfig,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            color = MiuixTheme.colorScheme.surfaceContainerHigh,
                        )
                    ) {
                        Text("重置服务器配置", color = MiuixTheme.colorScheme.error)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
