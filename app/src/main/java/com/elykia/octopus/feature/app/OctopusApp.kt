package com.elykia.octopus.feature.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.layout.Row
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.elykia.octopus.core.designsystem.DangerConfirmDialog
import com.elykia.octopus.core.designsystem.EmptyPane
import com.elykia.octopus.core.designsystem.ErrorPane
import com.elykia.octopus.core.designsystem.LoadingPane
import com.elykia.octopus.core.designsystem.OctopusTheme
import com.elykia.octopus.core.designsystem.SectionCard
import com.elykia.octopus.core.designsystem.SimpleList
import com.elykia.octopus.feature.auth.LoginViewModel
import com.elykia.octopus.feature.channel.ChannelViewModel
import com.elykia.octopus.feature.connection.ConnectionViewModel
import com.elykia.octopus.feature.group.GroupViewModel
import com.elykia.octopus.feature.home.HomeViewModel
import com.elykia.octopus.feature.log.LogViewModel
import com.elykia.octopus.feature.model.ModelViewModel
import com.elykia.octopus.feature.setting.SettingViewModel
import com.elykia.octopus.navigation.MainTab
import com.elykia.octopus.navigation.OctopusDestination
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarDisplayMode
import top.yukonga.miuix.kmp.basic.NavigationBarItem
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Create
import top.yukonga.miuix.kmp.icon.extended.Delete
import top.yukonga.miuix.kmp.icon.extended.Folder
import top.yukonga.miuix.kmp.icon.extended.Info
import top.yukonga.miuix.kmp.icon.extended.ListView
import top.yukonga.miuix.kmp.icon.extended.More
import top.yukonga.miuix.kmp.icon.extended.Settings
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun OctopusApp(
    appViewModel: AppViewModel = hiltViewModel(),
) {
    val navController = rememberNavController()
    val launchState by appViewModel.launchState.collectAsState()
    val themeMode by appViewModel.themeMode.collectAsState()

    OctopusTheme(themeMode = themeMode) {
        NavHost(navController = navController, startDestination = OctopusDestination.Launch.route) {
            composable(OctopusDestination.Launch.route) {
                when (val state = launchState) {
                    LaunchState.Loading -> LoadingPane(title = "Loading")
                    is LaunchState.NeedServer -> {
                        val viewModel = hiltViewModel<ConnectionViewModel>()
                        ConnectionRoute(
                            viewModel = viewModel,
                            onSaved = {
                                appViewModel.onServerConfigured()
                                navController.navigate(OctopusDestination.Login.route) {
                                    popUpTo(OctopusDestination.Launch.route) { inclusive = true }
                                }
                            },
                        )
                    }

                    is LaunchState.NeedLogin -> {
                        val viewModel = hiltViewModel<LoginViewModel>()
                        LoginRoute(
                            viewModel = viewModel,
                            onLoggedIn = {
                                appViewModel.onLoggedIn()
                                navController.navigate(OctopusDestination.Main.route) {
                                    popUpTo(OctopusDestination.Launch.route) { inclusive = true }
                                }
                            },
                        )
                    }

                    is LaunchState.Ready -> {
                        MainRoute(onLogout = { appViewModel.logout() })
                    }
                }
            }

            composable(OctopusDestination.Login.route) {
                val viewModel = hiltViewModel<LoginViewModel>()
                LoginRoute(
                    viewModel = viewModel,
                    onLoggedIn = {
                        appViewModel.onLoggedIn()
                        navController.navigate(OctopusDestination.Main.route) {
                            popUpTo(OctopusDestination.Login.route) { inclusive = true }
                        }
                    },
                )
            }

            composable(OctopusDestination.Connect.route) {
                val viewModel = hiltViewModel<ConnectionViewModel>()
                ConnectionRoute(
                    viewModel = viewModel,
                    onSaved = {
                        appViewModel.onServerConfigured()
                        navController.navigate(OctopusDestination.Login.route) {
                            popUpTo(OctopusDestination.Connect.route) { inclusive = true }
                        }
                    },
                )
            }

            composable(OctopusDestination.Main.route) {
                MainRoute(onLogout = { appViewModel.logout() })
            }
        }
    }
}

@Composable
private fun ConnectionRoute(
    viewModel: ConnectionViewModel,
    onSaved: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(title = "Connect Server", largeTitle = "Octopus")
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SectionCard(title = "Server URL", summary = "Point this app to your Octopus server.") {
                TextField(
                    value = uiState.serverUrl,
                    onValueChange = viewModel::updateServerUrl,
                    label = "https://your-octopus.example.com",
                    useLabelAsPlaceholder = true,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            uiState.error?.let { ErrorPane(message = it) }
            Button(
                onClick = { viewModel.save(onSaved) },
                enabled = !uiState.isSaving,
                colors = ButtonDefaults.buttonColorsPrimary(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = if (uiState.isSaving) "Saving..." else "Save and Continue")
            }
        }
    }
}

@Composable
private fun LoginRoute(
    viewModel: LoginViewModel,
    onLoggedIn: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(title = "Login", largeTitle = "Welcome back")
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SectionCard(title = "Account", summary = "Current server uses /api/v1/user/login.") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextField(
                        value = uiState.username,
                        onValueChange = viewModel::updateUsername,
                        label = "Username",
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    TextField(
                        value = uiState.password,
                        onValueChange = viewModel::updatePassword,
                        label = "Password",
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    TextField(
                        value = uiState.expireDays,
                        onValueChange = viewModel::updateExpireDays,
                        label = "Token days",
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            uiState.error?.let { ErrorPane(message = it) }
            Button(
                onClick = { viewModel.submit(onLoggedIn) },
                enabled = !uiState.isLoading,
                colors = ButtonDefaults.buttonColorsPrimary(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = if (uiState.isLoading) "Signing in..." else "Submit")
            }
        }
    }
}

@Composable
private fun MainRoute(
    onLogout: () -> Unit,
) {
    var currentTab by remember { mutableStateOf(MainTab.Home) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = currentTab.title,
                largeTitle = currentTab.title,
                actions = {
                    LogoutAction(onLogout = onLogout)
                },
            )
        },
        bottomBar = {
            NavigationBar(mode = NavigationBarDisplayMode.IconWithSelectedLabel) {
                MainTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        icon = tab.icon(),
                        label = tab.title,
                    )
                }
            }
        },
    ) { padding ->
        when (currentTab) {
            MainTab.Home -> HomeScreen(padding)
            MainTab.Channel -> ChannelScreen(padding)
            MainTab.Group -> GroupScreen(padding)
            MainTab.Model -> ModelScreen(padding)
            MainTab.Log -> LogScreen(padding)
            MainTab.Setting -> SettingScreen(padding)
        }
    }
}

@Composable
private fun RowScope.LogoutAction(onLogout: () -> Unit) {
    IconButton(onClick = onLogout) {
        Icon(imageVector = MiuixIcons.More, contentDescription = "Logout")
    }
}

private fun MainTab.icon() = when (this) {
    MainTab.Home -> MiuixIcons.Info
    MainTab.Channel -> MiuixIcons.ListView
    MainTab.Group -> MiuixIcons.Folder
    MainTab.Model -> MiuixIcons.Create
    MainTab.Log -> MiuixIcons.Delete
    MainTab.Setting -> MiuixIcons.Settings
}

@Composable
private fun HomeScreen(
    padding: PaddingValues,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    when {
        uiState.loading -> LoadingPane(title = "Loading")
        uiState.error != null -> ErrorPane(message = uiState.error ?: "Error", onRetry = viewModel::refresh)
        uiState.total == null -> EmptyPane(title = "Empty", summary = "No stats available")
        else -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    SectionCard(title = "Overview", summary = "Current Octopus totals") {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = "Input tokens: ${uiState.total.inputToken}")
                            Text(text = "Output tokens: ${uiState.total.outputToken}")
                            Text(text = "Cost: ${uiState.total.inputCost + uiState.total.outputCost}")
                            Text(text = "Success: ${uiState.total.requestSuccess}")
                            Text(text = "Failed: ${uiState.total.requestFailed}")
                        }
                    }
                }
                item {
                    SectionCard(title = "Daily", summary = "Recent daily trend") {
                        SimpleList(
                            entries = uiState.daily.take(7).map {
                                it.date to "${it.inputToken + it.outputToken} tokens"
                            }
                        )
                    }
                }
                item {
                    SectionCard(title = "Hourly", summary = "Recent hourly trend") {
                        SimpleList(
                            entries = uiState.hourly.take(8).map {
                                "${it.date} ${it.hour}:00" to "${it.inputToken + it.outputToken} tokens"
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChannelScreen(
    padding: PaddingValues,
    viewModel: ChannelViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var deletingId by remember { mutableStateOf<Int?>(null) }
    when {
        uiState.loading -> LoadingPane(title = "Loading channels")
        uiState.error != null -> ErrorPane(message = uiState.error ?: "Error", onRetry = viewModel::refresh)
        uiState.channels.isEmpty() -> EmptyPane(title = "No channels", summary = "Create channels from web or later mobile forms.")
        else -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(text = "Read-only list in first pass. Delete is enabled.", color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                SimpleList(
                    entries = uiState.channels.map { channel ->
                        channel.name to "type=${channel.type} enabled=${channel.enabled} keys=${channel.keys.size}"
                    },
                    onDelete = { index -> deletingId = uiState.channels[index].id },
                )
            }
            DangerConfirmDialog(
                visible = deletingId != null,
                title = "Delete channel",
                summary = "This will call /api/v1/channel/delete/{id}.",
                onConfirm = {
                    deletingId?.let(viewModel::delete)
                    deletingId = null
                },
                onDismiss = { deletingId = null },
            )
        }
    }
}

@Composable
private fun GroupScreen(
    padding: PaddingValues,
    viewModel: GroupViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var deletingId by remember { mutableStateOf<Int?>(null) }
    when {
        uiState.loading -> LoadingPane(title = "Loading groups")
        uiState.error != null -> ErrorPane(message = uiState.error ?: "Error", onRetry = viewModel::refresh)
        uiState.groups.isEmpty() -> EmptyPane(title = "No groups", summary = "Current first pass focuses on browsing and deletion.")
        else -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SimpleList(
                    entries = uiState.groups.map { group ->
                        group.name to "mode=${group.mode} items=${group.items.size} regex=${group.matchRegex.ifBlank { "-" }}"
                    },
                    onDelete = { index -> deletingId = uiState.groups[index].id },
                )
            }
            DangerConfirmDialog(
                visible = deletingId != null,
                title = "Delete group",
                summary = "This will call /api/v1/group/delete/{id}.",
                onConfirm = {
                    deletingId?.let(viewModel::delete)
                    deletingId = null
                },
                onDismiss = { deletingId = null },
            )
        }
    }
}

@Composable
private fun ModelScreen(
    padding: PaddingValues,
    viewModel: ModelViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    when {
        uiState.loading -> LoadingPane(title = "Loading models")
        uiState.error != null -> ErrorPane(message = uiState.error ?: "Error", onRetry = viewModel::refresh)
        uiState.models.isEmpty() -> EmptyPane(title = "No models", summary = "No model info found on server.")
        else -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    SectionCard(
                        title = "Model price sync",
                        summary = "Last update: ${uiState.lastUpdateTime ?: "unknown"}",
                        actions = {
                            TextButton(text = "Refresh", onClick = viewModel::refreshPrice)
                        },
                    ) {
                        Text(text = "Model count: ${uiState.models.size}")
                    }
                }
                item {
                    SectionCard(title = "Models", summary = "Pricing table") {
                        SimpleList(
                            entries = uiState.models.take(20).map { model ->
                                model.name to "in=${model.input} out=${model.output} cache=${model.cacheRead}/${model.cacheWrite}"
                            }
                        )
                    }
                }
                item {
                    SectionCard(title = "Channel mapping", summary = "Model to channel visibility") {
                        SimpleList(
                            entries = uiState.channels.take(20).map { item ->
                                item.name to "${item.channelName} enabled=${item.enabled}"
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LogScreen(
    padding: PaddingValues,
    viewModel: LogViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var confirmClear by remember { mutableStateOf(false) }
    when {
        uiState.loading -> LoadingPane(title = "Loading logs")
        uiState.error != null -> ErrorPane(message = uiState.error ?: "Error", onRetry = viewModel::refresh)
        uiState.logs.isEmpty() -> EmptyPane(title = "No logs", summary = "Relay logs are empty.")
        else -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    SectionCard(
                        title = "Relay logs",
                        summary = "First page from /api/v1/log/list",
                        actions = {
                            TextButton(text = "Clear", onClick = { confirmClear = true })
                        },
                    ) {
                        Text(text = "Entries: ${uiState.logs.size}")
                    }
                }
                items(uiState.logs) { log ->
                    SectionCard(
                        title = log.requestModelName,
                        summary = "${log.channelName} -> ${log.actualModelName}",
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(text = "cost=${log.cost} time=${log.useTime}ms attempts=${log.totalAttempts}")
                            if (log.error.isNotBlank()) {
                                Text(text = log.error, color = MiuixTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
            DangerConfirmDialog(
                visible = confirmClear,
                title = "Clear logs",
                summary = "This will call /api/v1/log/clear.",
                onConfirm = {
                    confirmClear = false
                    viewModel.clear()
                },
                onDismiss = { confirmClear = false },
            )
        }
    }
}

@Composable
private fun SettingScreen(
    padding: PaddingValues,
    viewModel: SettingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    when {
        uiState.loading -> LoadingPane(title = "Loading settings")
        uiState.error != null -> ErrorPane(message = uiState.error ?: "Error", onRetry = viewModel::refresh)
        else -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    SectionCard(
                        title = "Version",
                        summary = "Current: ${uiState.currentVersion ?: "unknown"}",
                        actions = {
                            TextButton(text = "Update", onClick = viewModel::triggerUpdate)
                        },
                    ) {
                        Text(text = "Latest: ${uiState.latestInfo?.tagName ?: "unknown"}")
                        uiState.latestInfo?.publishedAt?.let { Text(text = "Published: $it") }
                    }
                }
                item {
                    SectionCard(title = "Settings", summary = "Server-side setting list") {
                        SimpleList(
                            entries = uiState.settings.map { it.key to it.value }
                        )
                    }
                }
                item {
                    SectionCard(title = "API Keys", summary = "List and basic dashboard") {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            uiState.apiKeyDashboard?.let {
                                Text(text = "Dashboard: ${it.info.name} cost=${it.stats.inputCost + it.stats.outputCost}")
                            }
                            SimpleList(
                                entries = uiState.apiKeys.map { key ->
                                    key.name to "enabled=${key.enabled} expire=${key.expireAt ?: 0}"
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
