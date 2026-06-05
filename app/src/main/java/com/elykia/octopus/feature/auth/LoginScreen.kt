package com.elykia.octopus.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.elykia.octopus.R
import com.elykia.octopus.core.designsystem.AppListCard
import com.elykia.octopus.core.designsystem.OctopusBrandMark
import com.elykia.octopus.core.designsystem.OctopusTokens
import com.elykia.octopus.core.designsystem.PageContainer
import com.elykia.octopus.core.designsystem.SecureVisibleWindow
import com.elykia.octopus.core.designsystem.icons.AppMiuixIcons
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    showServerField: Boolean,
    currentServerUrl: String,
    securityMessage: String? = null,
    onLoggedIn: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val visibleError = uiState.error ?: loginInlineError(uiState)

    if (uiState.password.isNotBlank()) {
        SecureVisibleWindow()
    }

    LaunchedEffect(showServerField, currentServerUrl) {
        viewModel.initFromConfig(hasServer = !showServerField, currentUrl = currentServerUrl)
    }

    PageContainer {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 44.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BrandHeader(
                title = if (uiState.showServerField) {
                    stringResource(R.string.connect_title)
                } else {
                    stringResource(R.string.login_title)
                },
                summary = if (uiState.showServerField) {
                    stringResource(R.string.connect_summary)
                } else {
                    stringResource(R.string.login_summary)
                },
            )

            AppListCard(
                padding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
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

                    TextField(
                        value = uiState.username,
                        onValueChange = viewModel::updateUsername,
                        label = stringResource(R.string.login_placeholder_username),
                        useLabelAsPlaceholder = true,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    TextField(
                        value = uiState.password,
                        onValueChange = viewModel::updatePassword,
                        label = stringResource(R.string.login_label_password),
                        useLabelAsPlaceholder = true,
                        singleLine = true,
                        visualTransformation = if (uiState.passwordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        trailingIcon = {
                            IconButton(onClick = { viewModel.updatePasswordVisible(!uiState.passwordVisible) }) {
                                Icon(
                                    imageVector = if (uiState.passwordVisible) AppMiuixIcons.Info else AppMiuixIcons.ApiKey,
                                    contentDescription = if (uiState.passwordVisible) {
                                        stringResource(R.string.login_action_hide_password)
                                    } else {
                                        stringResource(R.string.login_action_show_password)
                                    },
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    TextField(
                        value = uiState.expireDays,
                        onValueChange = viewModel::updateExpireDays,
                        label = stringResource(R.string.login_label_expire_days),
                        useLabelAsPlaceholder = true,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Button(
                        onClick = { viewModel.submit(onLoggedIn) },
                        enabled = canSubmitLogin(uiState),
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

            visibleError?.let { errorMsg ->
                ErrorStrip(message = errorMsg)
            }
            securityMessage?.takeIf { it.isNotBlank() }?.let { message ->
                ErrorStrip(message = message)
            }
        }
    }
}

@Composable
private fun BrandHeader(
    title: String,
    summary: String,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OctopusBrandMark(size = 64.dp)
            Text(
                text = title,
                style = MiuixTheme.textStyles.title1,
                fontWeight = FontWeight.Bold,
                color = OctopusTokens.TextPrimary,
            )
        }
        Text(
            text = summary,
            style = MiuixTheme.textStyles.main,
            color = OctopusTokens.TextSecondary,
        )
    }
}

@Composable
private fun ErrorStrip(
    message: String,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MiuixTheme.colorScheme.error.copy(alpha = 0.08f))
            .border(1.dp, MiuixTheme.colorScheme.error.copy(alpha = 0.25f), RoundedCornerShape(18.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Text(
            text = message,
            style = MiuixTheme.textStyles.body2,
            color = MiuixTheme.colorScheme.error,
        )
    }
}
