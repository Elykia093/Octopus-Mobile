package com.elykia.octopus.feature.app

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.navigationBarsPadding
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
import com.elykia.octopus.feature.apikey.ApiKeyScreen
import com.elykia.octopus.feature.channel.ChannelScreen
import com.elykia.octopus.feature.group.GroupScreen
import com.elykia.octopus.feature.home.HomeScreen
import com.elykia.octopus.feature.log.LogScreen
import com.elykia.octopus.feature.setting.SettingScreen
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
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                items = dockItems,
                selectedKey = currentTab.route,
                onSelected = { route -> currentTab = MainTab.entries.first { it.route == route } },
                mode = FloatingNavigationBarDisplayMode.IconOnly,
            )
        },
    ) { padding ->
        val contentPadding = mainContentPadding(padding)
        PageContainer {
            when (currentTab) {
                MainTab.Home -> HomeScreen(contentPadding = contentPadding, onLogout = onLogout)
                MainTab.Channel -> ChannelScreen(contentPadding = contentPadding)
                MainTab.Group -> GroupScreen(contentPadding = contentPadding)
                MainTab.ApiKey -> ApiKeyScreen(contentPadding = contentPadding)
                MainTab.Log -> LogScreen(contentPadding = contentPadding)
                MainTab.Setting -> SettingScreen(contentPadding = contentPadding)
            }
        }
    }
}

private fun mainContentPadding(scaffoldPadding: PaddingValues): PaddingValues = PaddingValues(
    start = 16.dp,
    top = 8.dp + scaffoldPadding.calculateTopPadding(),
    end = 16.dp,
    bottom = maxOf(110.dp, scaffoldPadding.calculateBottomPadding() + 16.dp),
)
