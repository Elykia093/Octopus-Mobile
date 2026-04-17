package com.elykia.octopus.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun LoginScreen(viewModel: LoginViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
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
                text = "Login",
                style = MiuixTheme.textStyles.title1,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Use your Octopus account or API Key",
                style = MiuixTheme.textStyles.body1,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Mode Selector
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { viewModel.updateMode(LoginMode.ADMIN) },
                    modifier = Modifier.weight(1f),
                    colors = if (uiState.mode == LoginMode.ADMIN) 
                                ButtonDefaults.buttonColors() 
                             else 
                                ButtonDefaults.buttonColors(
                                    color = MiuixTheme.colorScheme.surface,
                                )
                ) {
                    Text(text = "Admin", color = if (uiState.mode == LoginMode.ADMIN) MiuixTheme.colorScheme.onPrimary else MiuixTheme.colorScheme.onSurface)
                }
                
                Button(
                    onClick = { viewModel.updateMode(LoginMode.API_KEY) },
                    modifier = Modifier.weight(1f),
                    colors = if (uiState.mode == LoginMode.API_KEY) 
                                ButtonDefaults.buttonColors() 
                             else 
                                ButtonDefaults.buttonColors(
                                    color = MiuixTheme.colorScheme.surface,
                                )
                ) {
                    Text(text = "API Key", color = if (uiState.mode == LoginMode.API_KEY) MiuixTheme.colorScheme.onPrimary else MiuixTheme.colorScheme.onSurface)
                }
            }

            if (uiState.mode == LoginMode.ADMIN) {
                TextField(
                    value = uiState.username,
                    onValueChange = viewModel::updateUsername,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    singleLine = true,
                )
                
                TextField(
                    value = uiState.password,
                    onValueChange = viewModel::updatePassword,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    singleLine = true,
                )

                TextField(
                    value = uiState.expireDays,
                    onValueChange = viewModel::updateExpireDays,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    singleLine = true,
                )
            } else {
                TextField(
                    value = uiState.apiKey,
                    onValueChange = viewModel::updateApiKey,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    singleLine = true,
                )
            }

            if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = MiuixTheme.colorScheme.error,
                    style = MiuixTheme.textStyles.body2,
                    modifier = Modifier.padding(bottom = 16.dp).align(Alignment.Start)
                )
            }

            Button(
                onClick = viewModel::submit,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (uiState.isSubmitting) "Signing in..." else "Login")
            }
        }
    }
}
