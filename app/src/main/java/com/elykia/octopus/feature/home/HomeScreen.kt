package com.elykia.octopus.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.elykia.octopus.R
import com.elykia.octopus.core.data.model.StatsDaily
import com.elykia.octopus.core.data.model.StatsTotal
import com.elykia.octopus.core.designsystem.AppPageScaffold
import com.elykia.octopus.core.designsystem.EmptyStateCard
import com.elykia.octopus.core.designsystem.ErrorStateCard
import com.elykia.octopus.core.designsystem.LoadingStateCard
import com.elykia.octopus.core.designsystem.OperationErrorCard
import com.elykia.octopus.core.designsystem.PageActionButton
import com.elykia.octopus.core.designsystem.icons.AppMiuixIcons

@Composable
fun HomeScreen(
    contentPadding: PaddingValues,
    onLogout: () -> Unit = {},
    securityMessage: String? = null,
    onClearSecurityMessage: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showToday by remember { mutableStateOf(true) }

    val total = uiState.total
    val snapshot = when (val s = if (showToday && uiState.today != null) uiState.today else total) {
        is StatsTotal -> StatsSnapshot(
            requestCount = s.requestSuccess + s.requestFailed,
            costValue = s.inputCost + s.outputCost,
            tokenValue = s.inputToken + s.outputToken,
            waitValue = s.waitTime,
            inputCost = s.inputCost,
            inputToken = s.inputToken,
            outputCost = s.outputCost,
            outputToken = s.outputToken,
        )
        is StatsDaily -> StatsSnapshot(
            requestCount = s.requestSuccess + s.requestFailed,
            costValue = s.inputCost + s.outputCost,
            tokenValue = s.inputToken + s.outputToken,
            waitValue = s.waitTime,
            inputCost = s.inputCost,
            inputToken = s.inputToken,
            outputCost = s.outputCost,
            outputToken = s.outputToken,
        )
        else -> StatsSnapshot()
    }

    AppPageScaffold(
        title = stringResource(R.string.home_title),
        actions = {
            PageActionButton(
                icon = AppMiuixIcons.Logout,
                contentDescription = stringResource(R.string.action_logout),
                onClick = onLogout,
            )
        },
        contentPadding = contentPadding,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            securityMessage?.takeIf { it.isNotBlank() }?.let { message ->
                OperationErrorCard(message = message, onDismiss = onClearSecurityMessage)
            }
            when {
                uiState.loading -> LoadingStateCard(title = stringResource(R.string.home_title))
                uiState.shouldShowPageError() -> ErrorStateCard(
                    message = uiState.error ?: stringResource(R.string.error_title),
                    onRetry = viewModel::refresh,
                )
                total == null -> EmptyStateCard(
                    title = stringResource(R.string.empty_title),
                    summary = stringResource(R.string.home_empty),
                )
                else -> {
                    uiState.error?.takeIf { it.isNotBlank() }?.let { error ->
                        OperationErrorCard(message = error)
                    }
                    uiState.partialErrors().forEach { error ->
                        OperationErrorCard(message = error)
                    }
                    DashboardOverviewSection(
                        snapshot = snapshot,
                        showToday = showToday,
                        onScopeChange = { showToday = it },
                    )
                    DashboardTrendSection(daily = uiState.daily)
                    DashboardRankingSection(
                        channels = uiState.channels,
                        apiKeyStats = uiState.apiKeyStats,
                        apiKeys = uiState.apiKeys,
                    )
                }
            }
        }
    }
}
