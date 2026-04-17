package com.elykia.octopus.feature.setting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun SettingScreen(viewModel: SettingViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Settings", 
            style = MiuixTheme.textStyles.title1,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Connected to: ${uiState.config.baseUrl}",
            style = MiuixTheme.textStyles.body1,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
        )

        Button(
            onClick = viewModel::logout,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                color = MiuixTheme.colorScheme.error,
            )
        ) {
            Text(if (uiState.isLoggingOut) "Logging out..." else "Logout", color = MiuixTheme.colorScheme.onError)
        }
        
        Button(
            onClick = viewModel::resetServerConfig,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                color = MiuixTheme.colorScheme.surface,
            )
        ) {
            Text("Reset Server Configuration", color = MiuixTheme.colorScheme.error)
        }
    }
}
