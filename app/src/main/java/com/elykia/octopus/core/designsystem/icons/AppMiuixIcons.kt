package com.elykia.octopus.core.designsystem.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathNode
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Add
import top.yukonga.miuix.kmp.icon.extended.Carrier
import top.yukonga.miuix.kmp.icon.extended.Close
import top.yukonga.miuix.kmp.icon.extended.Community
import top.yukonga.miuix.kmp.icon.extended.Create
import top.yukonga.miuix.kmp.icon.extended.Delete
import top.yukonga.miuix.kmp.icon.extended.Filter
import top.yukonga.miuix.kmp.icon.extended.Layers
import top.yukonga.miuix.kmp.icon.extended.More
import top.yukonga.miuix.kmp.icon.extended.Notes
import top.yukonga.miuix.kmp.icon.extended.Refresh
import top.yukonga.miuix.kmp.icon.extended.Search
import top.yukonga.miuix.kmp.icon.extended.Settings
import top.yukonga.miuix.kmp.icon.extended.Sort

object AppMiuixIcons {
    val Search = MiuixIcons.Search
    val Filter = MiuixIcons.Filter
    val Sort = MiuixIcons.Sort
    val Add = MiuixIcons.Add
    val Create = MiuixIcons.Create
    val Delete = MiuixIcons.Delete
    val Close = MiuixIcons.Close
    val Refresh = MiuixIcons.Refresh
    val More = MiuixIcons.More
    val Channel = MiuixIcons.Carrier
    val Group = MiuixIcons.Community
    val Model = MiuixIcons.Layers
    val Log = MiuixIcons.Notes
    val Setting = MiuixIcons.Settings

    val Home: ImageVector
        get() {
            if (_home != null) return _home!!
            _home = ImageVector.Builder(
                name = "Home.Custom",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            ).apply {
                addPath(
                    pathData = listOf(
                        PathNode.MoveTo(4.5f, 10.5f),
                        PathNode.LineTo(12f, 4.25f),
                        PathNode.LineTo(19.5f, 10.5f),
                        PathNode.MoveTo(6.5f, 8.9f),
                        PathNode.LineTo(6.5f, 19.25f),
                        PathNode.LineTo(10f, 19.25f),
                        PathNode.LineTo(10f, 13.75f),
                        PathNode.LineTo(14f, 13.75f),
                        PathNode.LineTo(14f, 19.25f),
                        PathNode.LineTo(17.5f, 19.25f),
                        PathNode.LineTo(17.5f, 8.9f),
                    ),
                    fill = SolidColor(Color.Transparent),
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 1.8f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                    pathFillType = PathFillType.NonZero,
                )
            }.build()
            return _home!!
        }

    private var _home: ImageVector? = null
}
