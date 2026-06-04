package com.elykia.octopus.feature.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.elykia.octopus.R
import com.elykia.octopus.core.designsystem.LoadingPane
import com.elykia.octopus.core.designsystem.OctopusTheme
import com.elykia.octopus.feature.auth.LoginScreen
import com.elykia.octopus.feature.auth.LoginViewModel
import com.elykia.octopus.navigation.OctopusDestination

@Composable
fun OctopusApp(
    appViewModel: AppViewModel = hiltViewModel(),
) {
    val navController = rememberNavController()
    val launchState by appViewModel.launchState.collectAsStateWithLifecycle()
    val securityMessage by appViewModel.securityMessage.collectAsStateWithLifecycle()
    val themeMode by appViewModel.themeMode.collectAsStateWithLifecycle()
    val backStackEntry by navController.currentBackStackEntryAsState()

    LaunchedEffect(launchState, backStackEntry?.destination?.route) {
        val currentRoute = backStackEntry?.destination?.route
        when (launchState) {
            is LaunchState.NeedServer, is LaunchState.NeedLogin -> {
                if (currentRoute != OctopusDestination.Login.route) {
                    navController.navigate(OctopusDestination.Login.route) {
                        popUpTo(navController.graph.findStartDestination().id)
                        launchSingleTop = true
                    }
                }
            }

            is LaunchState.Ready -> {
                if (currentRoute != OctopusDestination.Main.route) {
                    navController.navigate(OctopusDestination.Main.route) {
                        popUpTo(navController.graph.findStartDestination().id)
                        launchSingleTop = true
                    }
                }
            }

            LaunchState.Loading -> Unit
        }
    }

    OctopusTheme(themeMode = themeMode) {
        NavHost(navController = navController, startDestination = OctopusDestination.Launch.route) {
            composable(OctopusDestination.Launch.route) {
                when (launchState) {
                    LaunchState.Loading -> LoadingPane(title = stringResource(R.string.app_name))
                    is LaunchState.NeedServer -> {
                        val viewModel = hiltViewModel<LoginViewModel>()
                        LoginScreen(
                            viewModel = viewModel,
                            showServerField = true,
                            currentServerUrl = "",
                            securityMessage = securityMessage,
                            onLoggedIn = appViewModel::onLoggedIn,
                        )
                    }

                    is LaunchState.NeedLogin -> {
                        val viewModel = hiltViewModel<LoginViewModel>()
                        LoginScreen(
                            viewModel = viewModel,
                            showServerField = false,
                            currentServerUrl = (launchState as LaunchState.NeedLogin).config.baseUrl,
                            securityMessage = securityMessage,
                            onLoggedIn = appViewModel::onLoggedIn,
                        )
                    }

                    is LaunchState.Ready -> {
                        MainShell(
                            onLogout = appViewModel::logout,
                            securityMessage = securityMessage,
                            onClearSecurityMessage = appViewModel::clearSecurityMessage,
                        )
                    }
                }
            }

            composable(OctopusDestination.Login.route) {
                val viewModel = hiltViewModel<LoginViewModel>()
                val showServer = launchState is LaunchState.NeedServer
                val serverUrl = when (launchState) {
                    is LaunchState.NeedServer -> (launchState as LaunchState.NeedServer).config.baseUrl
                    is LaunchState.NeedLogin -> (launchState as LaunchState.NeedLogin).config.baseUrl
                    else -> ""
                }
                LoginScreen(
                    viewModel = viewModel,
                    showServerField = showServer,
                    currentServerUrl = serverUrl,
                    securityMessage = securityMessage,
                    onLoggedIn = appViewModel::onLoggedIn,
                )
            }

            composable(OctopusDestination.Main.route) {
                MainShell(
                    onLogout = appViewModel::logout,
                    securityMessage = securityMessage,
                    onClearSecurityMessage = appViewModel::clearSecurityMessage,
                )
            }
        }
    }
}
