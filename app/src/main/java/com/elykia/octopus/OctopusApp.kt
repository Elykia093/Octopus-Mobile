package com.elykia.octopus

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.elykia.octopus.core.data.repository.AppRepository
import com.elykia.octopus.core.designsystem.icons.AppMiuixIcons
import com.elykia.octopus.feature.auth.LoginScreen
import com.elykia.octopus.feature.connection.SetupScreen
import com.elykia.octopus.feature.setting.SettingScreen
import com.elykia.octopus.navigation.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import javax.inject.Inject

data class AppState(
    val isLoading: Boolean = true,
    val isConfigured: Boolean = false,
    val isLoggedIn: Boolean = false,
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
            isLoggedIn = auth.token.isNotBlank()
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
        composable<MainRoute> { MainScreen() }
    }
}

@Composable
fun MainScreen() {
    val bottomNavController = rememberNavController()

    Scaffold(
        // BottomBar implementation with pure Miuix components later
    ) { paddingValues ->
        NavHost(
            navController = bottomNavController,
            startDestination = DashboardRoute,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable<DashboardRoute> { Text("Dashboard Screen (TODO)", modifier = Modifier.fillMaxSize().padding(16.dp)) }
            composable<ChannelRoute> { Text("Channel Screen (TODO)", modifier = Modifier.fillMaxSize().padding(16.dp)) }
            composable<LogRoute> { Text("Logs Screen (TODO)", modifier = Modifier.fillMaxSize().padding(16.dp)) }
            composable<SettingRoute> { SettingScreen() }
        }
    }
}
