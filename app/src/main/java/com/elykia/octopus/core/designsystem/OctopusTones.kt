package com.elykia.octopus.core.designsystem

import androidx.compose.ui.graphics.Color

object OctopusTones {
    val Blue = Color(0xFF5C8C72)
    val Sky = Color(0xFF6F9B8A)
    val Purple = Color(0xFF897C68)
    val Indigo = Color(0xFF6F7F8A)
    val Orange = Color(0xFFB98C53)
    val Green = Color(0xFF5BA567)
    val Red = Color(0xFFB45B53)
    val Gray = Color(0xFF8B877E)

    val Request = Blue
    val Cost = Green
    val Token = Orange
    val SuccessRate = Purple
    val Success = Green
    val Danger = Red

    fun channelType(type: Int): Color = when (type) {
        0 -> Blue
        1 -> Sky
        2 -> Indigo
        3 -> Orange
        4 -> Red
        5 -> Green
        else -> Gray
    }

    fun groupMode(mode: Int): Color = when (mode) {
        1 -> Blue
        2 -> Purple
        3 -> Orange
        4 -> Green
        else -> Gray
    }

    fun rank(rank: Int): Pair<Color, Color>? = when (rank) {
        1 -> Orange.copy(alpha = 0.16f) to Orange
        2 -> Gray.copy(alpha = 0.16f) to Gray
        3 -> Purple.copy(alpha = 0.16f) to Purple
        else -> null
    }
}
