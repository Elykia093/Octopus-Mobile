package com.elykia.octopus.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.elykia.octopus.R
import com.elykia.octopus.core.designsystem.icons.AppMiuixIcons

sealed class OctopusDestination(val route: String) {
    data object Launch : OctopusDestination("launch")
    data object Login : OctopusDestination("login")
    data object Main : OctopusDestination("main")
    data object Dashboard : OctopusDestination("dashboard")
}

enum class MainTab(
    val route: String,
    @param:StringRes val titleRes: Int,
    val icon: ImageVector,
) {
    Home("home", R.string.nav_home, AppMiuixIcons.Home),
    Site("site", R.string.nav_site, AppMiuixIcons.Site),
    SiteChannel("site-channel", R.string.nav_site_channel, AppMiuixIcons.Sync),
    Channel("channel", R.string.nav_channel, AppMiuixIcons.Channel),
    ProxyPool("proxy-pool", R.string.nav_proxy_pool, AppMiuixIcons.Proxy),
    Group("group", R.string.nav_group, AppMiuixIcons.Group),
    Model("model", R.string.nav_model, AppMiuixIcons.Model),
    ApiKey("apikey", R.string.nav_apikey, AppMiuixIcons.ApiKey),
    Log("log", R.string.nav_log, AppMiuixIcons.Log),
    Setting("setting", R.string.nav_setting, AppMiuixIcons.Setting),
}
