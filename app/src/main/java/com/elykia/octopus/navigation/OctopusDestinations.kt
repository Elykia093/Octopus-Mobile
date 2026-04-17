package com.elykia.octopus.navigation

import kotlinx.serialization.Serializable

// ----- Root Graph -----
@Serializable data object SetupRoute
@Serializable data object LoginRoute
@Serializable data object MainRoute

// ----- Main Graph (Bottom Tabs) -----
@Serializable data object DashboardRoute
@Serializable data object ChannelRoute
@Serializable data object LogRoute
@Serializable data object SettingRoute
