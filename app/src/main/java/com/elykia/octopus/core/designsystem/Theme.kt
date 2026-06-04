package com.elykia.octopus.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController

private data class OctopusPalette(
    val accent: Color,
    val canvas: Color,
    val card: Color,
    val muted: Color,
    val border: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val primarySoft: Color,
    val selectedNav: Color,
    val navMuted: Color,
)

private val LightOctopusPalette = OctopusPalette(
    accent = Color(0xFF5BA567),
    canvas = Color(0xFFEDEBE4),
    card = Color(0xFFFBFBFC),
    muted = Color(0xFFEFEDE8),
    border = Color(0xFFDCD8CF),
    textPrimary = Color(0xFF3B352B),
    textSecondary = Color(0xFF7A766D),
    primarySoft = Color(0xFFE3EDE3),
    selectedNav = Color(0xFFE8D8B7),
    navMuted = Color(0xFF8B877E),
)

private val DarkOctopusPalette = OctopusPalette(
    accent = Color(0xFF82C98A),
    canvas = Color(0xFF171A16),
    card = Color(0xFF23261F),
    muted = Color(0xFF2B2E27),
    border = Color(0xFF3B4036),
    textPrimary = Color(0xFFF2EFE7),
    textSecondary = Color(0xFFB8B2A7),
    primarySoft = Color(0xFF283A2C),
    selectedNav = Color(0xFF4A3E28),
    navMuted = Color(0xFFA7A094),
)

private val LocalOctopusPalette = staticCompositionLocalOf { LightOctopusPalette }

object OctopusTokens {
    val SeedColor = Color(0xFF559A62)
    val Accent: Color
        @Composable get() = LocalOctopusPalette.current.accent
    val Canvas: Color
        @Composable get() = LocalOctopusPalette.current.canvas
    val Card: Color
        @Composable get() = LocalOctopusPalette.current.card
    val Muted: Color
        @Composable get() = LocalOctopusPalette.current.muted
    val Border: Color
        @Composable get() = LocalOctopusPalette.current.border
    val TextPrimary: Color
        @Composable get() = LocalOctopusPalette.current.textPrimary
    val TextSecondary: Color
        @Composable get() = LocalOctopusPalette.current.textSecondary
    val PrimarySoft: Color
        @Composable get() = LocalOctopusPalette.current.primarySoft
    val SelectedNav: Color
        @Composable get() = LocalOctopusPalette.current.selectedNav
    val NavMuted: Color
        @Composable get() = LocalOctopusPalette.current.navMuted
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
    val palette = remember(effectiveDark) {
        if (effectiveDark) DarkOctopusPalette else LightOctopusPalette
    }
    MiuixTheme(controller = controller, smoothRounding = true) {
        CompositionLocalProvider(LocalOctopusPalette provides palette) {
            content()
        }
    }
}
