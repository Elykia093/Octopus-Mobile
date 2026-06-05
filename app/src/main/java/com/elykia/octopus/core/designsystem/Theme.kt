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
    val glowPrimary: Color,
    val glowSecondary: Color,
)

private val LightOctopusPalette = OctopusPalette(
    accent = Color(0xFF5BA567),
    canvas = Color(0xFFEEECE4),
    card = Color(0xFFFEFCFA),
    muted = Color(0xFFF1EFE8),
    border = Color(0xFFDED9CF),
    textPrimary = Color(0xFF352F27),
    textSecondary = Color(0xFF777168),
    primarySoft = Color(0xFFE3ECDF),
    selectedNav = Color(0xFFE9DBBE),
    navMuted = Color(0xFF89847B),
    glowPrimary = Color(0xFFC9DFC9),
    glowSecondary = Color(0xFFDDEAE8),
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
    glowPrimary = Color(0xFF24422A),
    glowSecondary = Color(0xFF21353A),
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
    val GlowPrimary: Color
        @Composable get() = LocalOctopusPalette.current.glowPrimary
    val GlowSecondary: Color
        @Composable get() = LocalOctopusPalette.current.glowSecondary
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
