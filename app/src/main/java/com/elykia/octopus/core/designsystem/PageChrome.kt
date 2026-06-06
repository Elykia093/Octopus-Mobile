package com.elykia.octopus.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import androidx.compose.runtime.Composable

@Composable
fun AppPageScaffold(
    title: String,
    actions: @Composable RowScope.() -> Unit = {},
    contentPadding: PaddingValues,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PageHeader(title = title, actions = actions)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item { content() }
        }
    }
}

@Composable
fun AppLazyPageScaffold(
    title: String,
    actions: @Composable RowScope.() -> Unit = {},
    contentPadding: PaddingValues,
    content: LazyListScope.() -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PageHeader(title = title, actions = actions)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content,
        )
    }
}

@Composable
fun PageHeader(
    title: String,
    actions: @Composable RowScope.() -> Unit = {},
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val compactHeader = maxWidth < 360.dp
        val showBrand = maxWidth >= 340.dp
        val horizontalGap = if (compactHeader) 8.dp else 10.dp
        val titleShape = RoundedCornerShape(if (compactHeader) 18.dp else 20.dp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(
                    start = if (compactHeader) 16.dp else 24.dp,
                    top = 18.dp,
                    end = if (compactHeader) 12.dp else 18.dp,
                    bottom = 6.dp,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(horizontalGap),
        ) {
            if (showBrand) {
                HeaderBrandSurface(compactHeader = compactHeader)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(if (compactHeader) 38.dp else 40.dp)
                    .clip(titleShape)
                    .background(OctopusTokens.Card.copy(alpha = 0.96f))
                    .border(1.dp, OctopusTokens.Border.copy(alpha = 0.68f), titleShape)
                    .padding(horizontal = if (compactHeader) 12.dp else 14.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Text(
                    text = title,
                    style = MiuixTheme.textStyles.title3,
                    fontWeight = FontWeight.SemiBold,
                    color = OctopusTokens.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                content = actions,
            )
        }
    }
}

@Composable
private fun HeaderBrandSurface(
    compactHeader: Boolean,
) {
    val brandSize = if (compactHeader) 36.dp else 40.dp

    Box(
        modifier = Modifier
            .size(brandSize)
            .clip(CircleShape)
            .background(OctopusTokens.PrimarySoft.copy(alpha = 0.82f))
            .border(1.dp, OctopusTokens.Accent.copy(alpha = 0.18f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        OctopusBrandMark(size = if (compactHeader) 24.dp else 27.dp)
    }
}

@Composable
fun PageActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        val shape = CircleShape
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(shape)
                .background(OctopusTokens.Card.copy(alpha = if (enabled) 0.92f else 0.52f))
                .border(1.dp, OctopusTokens.Border.copy(alpha = 0.66f), shape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = OctopusTokens.TextPrimary.copy(alpha = if (enabled) 0.7f else 0.28f),
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
fun SoftIconTile(
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = OctopusTokens.Accent,
) {
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(tint.copy(alpha = 0.11f))
            .border(1.dp, tint.copy(alpha = 0.16f), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
fun AppListCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    padding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(18.dp)
    val clickableModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 1.dp,
                shape = shape,
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.018f),
                spotColor = Color.Black.copy(alpha = 0.035f),
            )
            .clip(shape)
            .background(OctopusTokens.Card.copy(alpha = 0.99f))
            .border(1.dp, OctopusTokens.Border.copy(alpha = 0.64f), shape)
            .then(clickableModifier)
            .padding(padding),
    ) {
        content()
    }
}

@Composable
fun AppInfoChip(
    text: String,
    icon: ImageVector? = null,
    tint: Color = OctopusTokens.TextSecondary,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(OctopusTokens.Muted.copy(alpha = 0.66f))
            .border(1.dp, OctopusTokens.Border.copy(alpha = 0.48f), RoundedCornerShape(999.dp))
            .padding(horizontal = 9.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = tint,
                modifier = Modifier.size(12.dp),
            )
        }
        Text(
            text = text,
            style = MiuixTheme.textStyles.body2,
            color = tint,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = 220.dp),
        )
    }
}

@Composable
fun AppMetricRow(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    accentColor: Color = OctopusTokens.Accent,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(OctopusTokens.Muted.copy(alpha = 0.68f))
            .border(1.dp, OctopusTokens.Border.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(horizontal = 11.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        SoftIconTile(
            icon = icon,
            contentDescription = label,
            tint = accentColor,
            modifier = Modifier.size(34.dp),
        )
        Text(
            text = label,
            style = MiuixTheme.textStyles.main,
            color = OctopusTokens.TextSecondary,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = value,
            style = MiuixTheme.textStyles.main,
            color = OctopusTokens.TextPrimary,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = 136.dp),
        )
    }
}

@Composable
fun AppTypePill(
    text: String,
    color: Color,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.14f), RoundedCornerShape(999.dp))
            .padding(horizontal = 9.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            color = color,
            style = MiuixTheme.textStyles.body2,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
fun RankRow(
    rank: Int,
    title: String,
    subtitle: String,
    value: String,
    accent: Color,
    progress: Float? = null,
) {
    AppListCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                RankBadge(rank = rank)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(text = title, style = MiuixTheme.textStyles.main, fontWeight = FontWeight.Medium)
                    Text(
                        text = subtitle,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        style = MiuixTheme.textStyles.body2,
                    )
                }
                Text(
                    text = value,
                    color = accent,
                    style = MiuixTheme.textStyles.main,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            if (progress != null) {
                ProgressToneBar(progress = progress, color = accent)
            }
        }
    }
}

@Composable
fun SectionSpacer(height: Int = 4) {
    Spacer(modifier = Modifier.height(height.dp))
}
