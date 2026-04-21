package com.elykia.octopus

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.elykia.octopus.core.data.repository.AppRepository
import com.elykia.octopus.feature.apikey.ApiKeyScreen
import com.elykia.octopus.feature.auth.LoginScreen
import com.elykia.octopus.feature.channel.ChannelScreen
import com.elykia.octopus.feature.connection.SetupScreen
import com.elykia.octopus.feature.dashboard.DashboardScreen
import com.elykia.octopus.feature.log.LogScreen
import com.elykia.octopus.feature.setting.SettingScreen
import com.elykia.octopus.navigation.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import top.yukonga.miuix.kmp.basic.Scaffold
import javax.inject.Inject

data class AppState(
    val isLoading: Boolean = true,
    val isConfigured: Boolean = false,
    val isLoggedIn: Boolean = false,
    val isApiKeyMode: Boolean = false,
)

@HiltViewModel
class AppViewModel @Inject constructor(
    repository: AppRepository
) : ViewModel() {
    val appState: StateFlow<AppState> = combine(
        repository.serverConfig,
        repository.authState
    ) { config, auth ->
        AppState(
            isLoading = false,
            isConfigured = config.baseUrl.isNotBlank(),
            isLoggedIn = auth.token.isNotBlank(),
            isApiKeyMode = auth.isApiKeyMode
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppState(isLoading = true)
    )
}

@Composable
fun OctopusApp(viewModel: AppViewModel = hiltViewModel()) {
    val appState by viewModel.appState.collectAsState()
    val navController = rememberNavController()

    if (!appState.isLoading) {
        RootNavGraph(
            navController = navController,
            appState = appState
        )
    }
}

@Composable
fun RootNavGraph(
    navController: NavHostController,
    appState: AppState,
) {
    val startDest = when {
        !appState.isConfigured -> SetupRoute
        !appState.isLoggedIn -> LoginRoute
        else -> MainRoute
    }

    LaunchedEffect(startDest) {
        navController.navigate(startDest) {
            popUpTo(0) { inclusive = true }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDest,
        modifier = Modifier.fillMaxSize()
    ) {
        composable<SetupRoute> { SetupScreen() }
        composable<LoginRoute> { LoginScreen() }
        composable<MainRoute> { MainScreen(appState.isApiKeyMode) }
    }
}

@Composable
fun MainScreen(isApiKeyMode: Boolean) {
    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val currentRoute = currentDestination?.route.orEmpty()

    val isRouteMatch: (Any) -> Boolean = { route ->
        currentRoute.contains(route::class.simpleName.orEmpty())
    }

    val destinations = listOf(
        TopLevelDestination.DASHBOARD,
        if (isApiKeyMode) TopLevelDestination.API_KEY else TopLevelDestination.CHANNEL,
        TopLevelDestination.LOG,
        TopLevelDestination.SETTING
    )

    Scaffold(
        bottomBar = {
            AppBottomBar(
                destinations = destinations,
                currentDestination = isRouteMatch,
                onNavigateToDestination = { topLevelDest ->
                    bottomNavController.navigate(topLevelDest.route) {
                        popUpTo(bottomNavController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { paddingValues ->
        NavHost(
            navController = bottomNavController,
            startDestination = DashboardRoute,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable<DashboardRoute> {
                DashboardScreen(modifier = Modifier.fillMaxSize())
            }
            composable<ChannelRoute> { 
                ChannelScreen() 
            }
            composable<ApiKeyRoute> { 
                ApiKeyScreen() 
            }
            composable<LogRoute> { LogScreen() }
            composable<SettingRoute> { SettingScreen() }
        }
    }
}
