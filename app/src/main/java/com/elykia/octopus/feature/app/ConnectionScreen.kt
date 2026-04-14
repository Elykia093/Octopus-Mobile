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
import androidx.compose.ui.unit.dp
import com.elykia.octopus.R
import com.elykia.octopus.core.designsystem.BrandTopBar
import com.elykia.octopus.core.designsystem.ErrorPane
import com.elykia.octopus.core.designsystem.OctopusBrandMark
import com.elykia.octopus.core.designsystem.PageContainer
import com.elykia.octopus.core.designsystem.SectionCard
import com.elykia.octopus.feature.connection.ConnectionViewModel
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun ConnectionScreen(
    viewModel: ConnectionViewModel,
    onSaved: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    PageContainer {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            BrandTopBar(title = stringResource(R.string.connect_title), actions = emptyList())
            SectionCard(
                title = stringResource(R.string.connect_title),
                summary = stringResource(R.string.connect_summary),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    OctopusBrandMark(size = 48.dp)
                    TextField(
                        value = uiState.serverUrl,
                        onValueChange = viewModel::updateServerUrl,
                        label = stringResource(R.string.connect_placeholder_server_url),
                        useLabelAsPlaceholder = true,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        text = stringResource(R.string.connect_label_server_url),
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        style = MiuixTheme.textStyles.body2,
                    )
                    Button(
                        onClick = { viewModel.save(onSaved) },
                        enabled = !uiState.isSaving,
                        colors = ButtonDefaults.buttonColorsPrimary(),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = if (uiState.isSaving) {
                                stringResource(R.string.connect_submitting)
                            } else {
                                stringResource(R.string.connect_submit)
                            }
                        )
                    }
                }
            }
            uiState.error?.let { ErrorPane(message = it) }
        }
    }
}

