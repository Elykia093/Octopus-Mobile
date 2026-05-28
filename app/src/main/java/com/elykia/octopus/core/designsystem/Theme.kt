package com.elykia.octopus.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController

object OctopusTokens {
    val SeedColor = Color(0xFF559A62)
    val Accent = Color(0xFF5BA567)
    val Canvas = Color(0xFFEDEBE4)
    val Card = Color(0xFFFBFBFC)
    val Muted = Color(0xFFEFEDE8)
    val Border = Color(0xFFDCD8CF)
    val TextPrimary = Color(0xFF3B352B)
    val TextSecondary = Color(0xFF7A766D)
    val PrimarySoft = Color(0xFFE3EDE3)
}

@Composable
fun OctopusTheme(
    themeMode: Int = 0,
    content: @Composable () -> Unit,
) {
    val effectiveDark = when (themeMode) {
        1 -> false
        2 -> true
        else -> isSystemInDarkTheme()
    }
    val controller = remember(themeMode, effectiveDark) {
        when (themeMode) {
            1 -> ThemeController(ColorSchemeMode.Light, keyColor = OctopusTokens.SeedColor)
            2 -> ThemeController(ColorSchemeMode.Dark, keyColor = OctopusTokens.SeedColor)
            else -> ThemeController(ColorSchemeMode.System, keyColor = OctopusTokens.SeedColor)
        }
    }
    MiuixTheme(controller = controller, smoothRounding = true, content = content)
}
