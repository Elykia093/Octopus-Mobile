package com.elykia.octopus.feature.app

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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.elykia.octopus.R
import com.elykia.octopus.core.designsystem.ErrorPane
import com.elykia.octopus.core.designsystem.OctopusBrandMark
import com.elykia.octopus.feature.auth.LoginViewModel
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    showServerField: Boolean,
    currentServerUrl: String,
    onLoggedIn: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(showServerField, currentServerUrl) {
        viewModel.initFromConfig(hasServer = !showServerField, currentUrl = currentServerUrl)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 品牌标识区
        BrandHeader()

        Spacer(modifier = Modifier.height(24.dp))

        // 登录卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            insideMargin = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                // 服务器地址（仅在未配置时显示）
                if (uiState.showServerField) {
                    TextField(
                        value = uiState.serverUrl,
                        onValueChange = viewModel::updateServerUrl,
                        label = stringResource(R.string.connect_placeholder_server_url),
                        useLabelAsPlaceholder = true,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                // 用户名
                TextField(
                    value = uiState.username,
                    onValueChange = viewModel::updateUsername,
                    label = stringResource(R.string.login_placeholder_username),
                    useLabelAsPlaceholder = true,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                // 密码
                TextField(
                    value = uiState.password,
                    onValueChange = viewModel::updatePassword,
                    label = stringResource(R.string.login_placeholder_password),
                    useLabelAsPlaceholder = true,
                    singleLine = true,
                    visualTransformation = if (uiState.passwordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    modifier = Modifier.fillMaxWidth(),
                )

                // 登录按钮
                Button(
                    onClick = { viewModel.submit(onLoggedIn) },
                    enabled = !uiState.isLoading,
                    colors = ButtonDefaults.buttonColorsPrimary(),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = if (uiState.isLoading) {
                            stringResource(R.string.login_submitting)
                        } else {
                            stringResource(R.string.login_submit)
                        },
                    )
                }
            }
        }

        // 错误提示
        uiState.error?.let { errorMsg ->
            Spacer(modifier = Modifier.height(12.dp))
            ErrorPane(message = errorMsg)
        }
    }
}

@Composable
private fun BrandHeader() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // 品牌图标（渐变圆形背景 + 八爪鱼 Logo）
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MiuixTheme.colorScheme.primary,
                            MiuixTheme.colorScheme.secondaryContainer,
                        ),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            OctopusBrandMark(size = 40.dp)
        }

        // 标题
        Text(
            text = stringResource(R.string.brand_title),
            style = MiuixTheme.textStyles.title1,
            fontWeight = FontWeight.Bold,
        )

        // 副标题
        Text(
            text = stringResource(R.string.brand_subtitle),
            style = MiuixTheme.textStyles.body2,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
    }
}
