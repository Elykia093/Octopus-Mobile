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
    val onAccent: Color,
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
    accent = Color(0xFF4F8F66),
    onAccent = Color(0xFFFFFFFF),
    canvas = Color(0xFFF5F6F1),
    card = Color(0xFFFFFFFF),
    muted = Color(0xFFECEFE8),
    border = Color(0xFFD8DED5),
    textPrimary = Color(0xFF252B27),
    textSecondary = Color(0xFF626B64),
    primarySoft = Color(0xFFDFEEE4),
    selectedNav = Color(0xFFE5F0E7),
    navMuted = Color(0xFF6F776F),
)

private val DarkOctopusPalette = OctopusPalette(
    accent = Color(0xFF7BCB93),
    onAccent = Color(0xFF102217),
    canvas = Color(0xFF111512),
    card = Color(0xFF1B211D),
    muted = Color(0xFF252B26),
    border = Color(0xFF344036),
    textPrimary = Color(0xFFF4F6F0),
    textSecondary = Color(0xFFA9B3AA),
    primarySoft = Color(0xFF23382B),
    selectedNav = Color(0xFF294332),
    navMuted = Color(0xFF8F9A91),
)

private val LocalOctopusPalette = staticCompositionLocalOf { LightOctopusPalette }

object OctopusTokens {
    val SeedColor = Color(0xFF559A62)
    val Accent: Color
        @Composable get() = LocalOctopusPalette.current.accent
    val OnAccent: Color
        @Composable get() = LocalOctopusPalette.current.onAccent
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
    MiuixTheme(controller) {
        CompositionLocalProvider(LocalOctopusPalette provides palette) {
            content()
        }
    }
}
