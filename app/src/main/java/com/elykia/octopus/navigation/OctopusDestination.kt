package com.elykia.octopus.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.elykia.octopus.R
import com.elykia.octopus.core.designsystem.icons.AppMiuixIcons

sealed class OctopusDestination(val route: String) {
    data object Launch : OctopusDestination("launch")
    data object Login : OctopusDestination("login")
    data object Main : OctopusDestination("main")
}

enum class MainTab(
    val route: String,
    @StringRes val titleRes: Int,
    val icon: ImageVector,
) {
    Home("home", R.string.nav_home, AppMiuixIcons.Home),
    Channel("channel", R.string.nav_channel, AppMiuixIcons.Channel),
    Group("group", R.string.nav_group, AppMiuixIcons.Group),
    ApiKey("apikey", R.string.nav_apikey, AppMiuixIcons.ApiKey),
    Log("log", R.string.nav_log, AppMiuixIcons.Log),
    Setting("setting", R.string.nav_setting, AppMiuixIcons.Setting),
}
