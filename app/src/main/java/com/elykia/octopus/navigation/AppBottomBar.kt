package com.elykia.octopus.navigation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import top.yukonga.miuix.kmp.basic.BottomNavigationBar
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.NavigationBarItem

@Composable
fun AppBottomBar(
    destinations: List<TopLevelDestination>,
    currentDestination: (Any) -> Boolean,
    onNavigateToDestination: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    BottomNavigationBar(
        modifier = modifier.fillMaxWidth()
    ) {
        destinations.forEach { destination ->
            val selected = currentDestination(destination.route)
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigateToDestination(destination) },
                icon = {
                    Icon(
                        imageVector = if (selected) destination.selectedIcon else destination.unselectedIcon,
                        contentDescription = destination.titleTextId
                    )
                },
                label = destination.iconTextId
            )
        }
    }
}
