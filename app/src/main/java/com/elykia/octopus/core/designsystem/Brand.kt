package com.elykia.octopus.core.designsystem

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun OctopusBrandMark(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
) {
    val brandColor = if (MiuixTheme.colorScheme.background.luminance() < 0.5f) {
        Color(0xFFF3F0E7)
    } else {
        OctopusTokens.SeedColor
    }

    Box(
        modifier = modifier
            .size(size)
            .aspectRatio(1f),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = this.size.minDimension * 0.072f

            fun point(x: Float, y: Float) = Offset(this.size.width * x / 100f, this.size.height * y / 100f)

            drawPath(
                path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(point(50f, 15f).x, point(50f, 15f).y)
                    cubicTo(point(70f, 15f).x, point(70f, 15f).y, point(85f, 30f).x, point(85f, 30f).y, point(85f, 50f).x, point(85f, 50f).y)
                    cubicTo(point(85f, 65f).x, point(85f, 65f).y, point(75f, 75f).x, point(75f, 75f).y, point(70f, 80f).x, point(70f, 80f).y)
                    moveTo(point(50f, 15f).x, point(50f, 15f).y)
                    cubicTo(point(30f, 15f).x, point(30f, 15f).y, point(15f, 30f).x, point(15f, 30f).y, point(15f, 50f).x, point(15f, 50f).y)
                    cubicTo(point(15f, 65f).x, point(15f, 65f).y, point(25f, 75f).x, point(25f, 75f).y, point(30f, 80f).x, point(30f, 80f).y)
                },
                color = brandColor,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )

            drawPath(
                path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(point(30f, 80f).x, point(30f, 80f).y)
                    quadraticTo(point(30f, 90f).x, point(30f, 90f).y, point(20f, 90f).x, point(20f, 90f).y)
                    moveTo(point(43f, 77f).x, point(43f, 77f).y)
                    quadraticTo(point(43f, 90f).x, point(43f, 90f).y, point(38f, 90f).x, point(38f, 90f).y)
                    moveTo(point(57f, 77f).x, point(57f, 77f).y)
                    quadraticTo(point(57f, 90f).x, point(57f, 90f).y, point(62f, 90f).x, point(62f, 90f).y)
                    moveTo(point(70f, 80f).x, point(70f, 80f).y)
                    quadraticTo(point(70f, 90f).x, point(70f, 90f).y, point(80f, 90f).x, point(80f, 90f).y)
                },
                color = brandColor,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
        }
    }
}
