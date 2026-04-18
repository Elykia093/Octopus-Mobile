package com.elykia.octopus.navigation

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem

@Composable
fun AppBottomBar(
    destinations: List<TopLevelDestination>,
    currentDestination: (Any) -> Boolean,
    onNavigateToDestination: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.fillMaxWidth()
    ) {
        destinations.forEach { destination ->
            AppBottomBarItem(
                destination = destination,
                selected = currentDestination(destination.route),
                onClick = { onNavigateToDestination(destination) }
            )
        }
    }
}

@Composable
private fun RowScope.AppBottomBarItem(
    destination: TopLevelDestination,
    selected: Boolean,
    onClick: () -> Unit,
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = if (selected) destination.selectedIcon else destination.unselectedIcon,
        label = destination.iconTextId,
        modifier = Modifier,
        enabled = true
    )
}
