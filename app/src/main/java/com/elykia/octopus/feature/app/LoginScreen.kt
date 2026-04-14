package com.elykia.octopus.feature.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.elykia.octopus.R
import com.elykia.octopus.core.designsystem.BrandTopBar
import com.elykia.octopus.core.designsystem.ErrorPane
import com.elykia.octopus.core.designsystem.PageContainer
import com.elykia.octopus.core.designsystem.SectionCard
import com.elykia.octopus.feature.auth.LoginViewModel
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.theme.MiuixTheme
import androidx.compose.foundation.text.KeyboardOptions

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoggedIn: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    PageContainer {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            BrandTopBar(title = stringResource(R.string.login_title), actions = emptyList())
            SectionCard(
                title = stringResource(R.string.login_title),
                summary = stringResource(R.string.login_summary),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                        label = stringResource(R.string.login_placeholder_password),
                        useLabelAsPlaceholder = true,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    TextField(
                        value = uiState.expireDays,
                        onValueChange = viewModel::updateExpireDays,
                        label = stringResource(R.string.login_label_expire_days),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        text = stringResource(R.string.login_label_expire_days),
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        style = MiuixTheme.textStyles.body2,
                    )
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
                            }
                        )
                    }
                }
            }
            uiState.error?.let { ErrorPane(message = it) }
        }
    }
}
