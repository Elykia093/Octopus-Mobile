package com.elykia.octopus.core.designsystem

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun AppListCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        cornerRadius = 16.dp,
        insideMargin = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        colors = CardDefaults.defaultColors(
            color = MiuixTheme.colorScheme.surface,
        )
    ) {
        Box(modifier = Modifier.fillMaxWidth(), content = content)
    }
}

@Composable
fun SectionLabel(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MiuixTheme.textStyles.title4,
        fontWeight = FontWeight.SemiBold,
        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        modifier = modifier.padding(horizontal = 12.dp, vertical = 8.dp)
    )
}

@Composable
fun LoadingPane(title: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        InfiniteProgressIndicator(modifier = Modifier.padding(bottom = 16.dp))
        Text(text = "正在加载 $title...", color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
    }
}

@Composable
fun ErrorPane(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message, 
            color = MiuixTheme.colorScheme.error, 
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .padding(bottom = 24.dp)
        )
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                color = MiuixTheme.colorScheme.surfaceContainerHigh
            )
        ) {
            Text("重试", color = MiuixTheme.colorScheme.onSurface)
        }
    }
}
