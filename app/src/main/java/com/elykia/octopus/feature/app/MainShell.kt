package com.elykia.octopus.feature.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
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
import com.elykia.octopus.core.designsystem.ActionIcon
import com.elykia.octopus.core.designsystem.BrandTopBar
import com.elykia.octopus.core.designsystem.DockItem
import com.elykia.octopus.core.designsystem.FloatingDockBar
import com.elykia.octopus.core.designsystem.PageContainer
import com.elykia.octopus.core.designsystem.icons.AppMiuixIcons
import com.elykia.octopus.navigation.MainTab
import top.yukonga.miuix.kmp.basic.Scaffold

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
            )
        },
    ) { padding ->
        PageContainer {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp)
                    .padding(top = 10.dp)
                    .padding(bottom = padding.calculateBottomPadding()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                BrandTopBar(
                    title = stringResource(currentTab.titleRes),
                    actions = listOf(
                        ActionIcon(
                            icon = AppMiuixIcons.Close,
                            contentDescription = stringResource(R.string.action_logout),
                            onClick = onLogout,
                        )
                    ),
                )
                when (currentTab) {
                    MainTab.Home -> HomeScreen(contentPadding = PaddingValues(bottom = 110.dp))
                    MainTab.Channel -> ChannelScreen(contentPadding = PaddingValues(bottom = 110.dp))
                    MainTab.Group -> GroupScreen(contentPadding = PaddingValues(bottom = 110.dp))
                    MainTab.Model -> ModelScreen(contentPadding = PaddingValues(bottom = 110.dp))
                    MainTab.Log -> LogScreen(contentPadding = PaddingValues(bottom = 110.dp))
                    MainTab.Setting -> SettingScreen(contentPadding = PaddingValues(bottom = 110.dp))
                }
            }
        }
    }
}
