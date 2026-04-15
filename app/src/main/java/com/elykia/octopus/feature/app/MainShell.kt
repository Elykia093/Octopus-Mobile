package com.elykia.octopus.feature.app

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.elykia.octopus.R
import com.elykia.octopus.core.designsystem.DockItem
import com.elykia.octopus.core.designsystem.FloatingDockBar
import com.elykia.octopus.core.designsystem.PageContainer
import com.elykia.octopus.navigation.MainTab
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.FloatingNavigationBarDisplayMode

@Composable
fun MainShell(
    onLogout: () -> Unit,
) {
    var currentTab by rememberSaveable { mutableStateOf(MainTab.Home) }

    val dockItems = MainTab.entries.map { tab ->
        DockItem(
            key = tab.route,
            icon = tab.icon,
            label = stringResource(tab.titleRes),
        )
    }

    Scaffold(
        bottomBar = {
            FloatingDockBar(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                items = dockItems,
                selectedKey = currentTab.route,
                onSelected = { route -> currentTab = MainTab.entries.first { it.route == route } },
                mode = FloatingNavigationBarDisplayMode.IconAndText,
            )
        },
    ) { padding ->
        PageContainer {
            when (currentTab) {
                MainTab.Home -> HomeScreen(contentPadding = PaddingValues(start = 16.dp, top = 8.dp + padding.calculateTopPadding(), end = 16.dp, bottom = 110.dp), onLogout = onLogout)
                MainTab.Channel -> ChannelScreen(contentPadding = PaddingValues(start = 16.dp, top = 8.dp + padding.calculateTopPadding(), end = 16.dp, bottom = 110.dp))
                MainTab.Group -> GroupScreen(contentPadding = PaddingValues(start = 16.dp, top = 8.dp + padding.calculateTopPadding(), end = 16.dp, bottom = 110.dp))
                MainTab.ApiKey -> ApiKeyScreen(contentPadding = PaddingValues(start = 16.dp, top = 8.dp + padding.calculateTopPadding(), end = 16.dp, bottom = 110.dp))
                MainTab.Log -> LogScreen(contentPadding = PaddingValues(start = 16.dp, top = 8.dp + padding.calculateTopPadding(), end = 16.dp, bottom = 110.dp))
                MainTab.Setting -> SettingScreen(contentPadding = PaddingValues(start = 16.dp, top = 8.dp + padding.calculateTopPadding(), end = 16.dp, bottom = 110.dp))
            }
        }
    }
}
