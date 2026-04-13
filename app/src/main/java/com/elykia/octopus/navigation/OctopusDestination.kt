package com.elykia.octopus.navigation

sealed class OctopusDestination(val route: String) {
    data object Launch : OctopusDestination("launch")
    data object Connect : OctopusDestination("connect")
    data object Login : OctopusDestination("login")
    data object Main : OctopusDestination("main")
}

enum class MainTab(val route: String, val title: String) {
    Home("home", "Home"),
    Channel("channel", "Channel"),
    Group("group", "Group"),
    Model("model", "Model"),
    Log("log", "Log"),
    Setting("setting", "Setting"),
}
