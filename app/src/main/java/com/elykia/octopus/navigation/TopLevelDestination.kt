package com.elykia.octopus.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import com.elykia.octopus.core.designsystem.icons.AppMiuixIcons
import top.yukonga.miuix.kmp.basic.Icon

enum class TopLevelDestination(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val iconTextId: String,
    val titleTextId: String,
    val route: Any
) {
    DASHBOARD(
        selectedIcon = AppMiuixIcons.Home,
        unselectedIcon = AppMiuixIcons.Home,
        iconTextId = "仪表盘",
        titleTextId = "仪表盘",
        route = DashboardRoute
    ),
    CHANNEL(
        selectedIcon = AppMiuixIcons.List,
        unselectedIcon = AppMiuixIcons.List,
        iconTextId = "渠道",
        titleTextId = "渠道",
        route = ChannelRoute
    ),
    API_KEY(
        selectedIcon = AppMiuixIcons.VpnKey,
        unselectedIcon = AppMiuixIcons.VpnKey,
        iconTextId = "令牌",
        titleTextId = "令牌",
        route = ApiKeyRoute
    ),
    LOG(
        selectedIcon = AppMiuixIcons.ReceiptLong,
        unselectedIcon = AppMiuixIcons.ReceiptLong,
        iconTextId = "日志",
        titleTextId = "日志",
        route = LogRoute
    ),
    SETTING(
        selectedIcon = AppMiuixIcons.Settings,
        unselectedIcon = AppMiuixIcons.Settings,
        iconTextId = "设置",
        titleTextId = "设置",
        route = SettingRoute
    )
}
