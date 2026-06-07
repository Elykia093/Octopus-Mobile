package com.elykia.octopus.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.elykia.octopus.R
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun ScreenPane(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(14.dp),
    content: @Composable () -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = verticalArrangement,
    ) {
        item {
            content()
        }
    }
}

@Composable
fun LoadingPane(title: String) {
    StatePane(
        title = title,
        summary = stringResource(R.string.common_loading),
    )
}

@Composable
fun ErrorPane(
    message: String,
    onRetry: (() -> Unit)? = null,
) {
    StatePane(
        title = stringResource(R.string.error_title),
        summary = message,
        action = if (onRetry != null) {
            {
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColorsPrimary(),
                ) {
                    Text(text = stringResource(R.string.common_retry))
                }
            }
        } else {
            null
        },
    )
}

@Composable
fun EmptyPane(
    title: String,
    summary: String,
) {
    StatePane(title = title, summary = summary)
}

@Composable
fun LoadingStateCard(
    title: String,
    modifier: Modifier = Modifier,
) {
    InlineStateCard(
        title = title,
        summary = stringResource(R.string.common_loading),
        modifier = modifier,
    )
}

@Composable
fun ErrorStateCard(
    message: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
) {
    InlineStateCard(
        title = stringResource(R.string.error_title),
        summary = message,
        modifier = modifier,
        action = if (onRetry != null) {
            {
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColorsPrimary(),
                ) {
                    Text(text = stringResource(R.string.common_retry))
                }
            }
        } else {
            null
        },
    )
}

@Composable
fun EmptyStateCard(
    title: String,
    summary: String,
    modifier: Modifier = Modifier,
) {
    InlineStateCard(title = title, summary = summary, modifier = modifier)
}

@Composable
fun InlineStateCard(
    title: String,
    summary: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    AppListCard(
        modifier = modifier,
        padding = PaddingValues(horizontal = 22.dp, vertical = 24.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OctopusBrandMark(size = 42.dp)
            Text(
                text = title,
                style = MiuixTheme.textStyles.title3,
                fontWeight = FontWeight.Bold,
                color = OctopusTokens.TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = summary,
                color = OctopusTokens.TextSecondary,
                style = MiuixTheme.textStyles.body2,
            )
            action?.invoke()
        }
    }
}

@Composable
fun OperationErrorCard(
    message: String,
    modifier: Modifier = Modifier,
    onDismiss: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MiuixTheme.colorScheme.error.copy(alpha = 0.08f))
            .border(1.dp, MiuixTheme.colorScheme.error.copy(alpha = 0.24f), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = message,
            style = MiuixTheme.textStyles.body2,
            color = MiuixTheme.colorScheme.error,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (onDismiss != null) {
            TextButton(text = stringResource(R.string.action_close), onClick = onDismiss)
        }
    }
}

@Composable
fun StatePane(
    title: String,
    summary: String,
    action: (@Composable () -> Unit)? = null,
) {
    PageContainer {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            AppListCard(
                modifier = Modifier.padding(horizontal = 28.dp),
                padding = PaddingValues(horizontal = 22.dp, vertical = 24.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OctopusBrandMark(size = 44.dp)
                    Text(
                        text = title,
                        style = MiuixTheme.textStyles.title3,
                        fontWeight = FontWeight.Bold,
                        color = OctopusTokens.TextPrimary,
                    )
                    Text(
                        text = summary,
                        color = OctopusTokens.TextSecondary,
                        style = MiuixTheme.textStyles.body2,
                    )
                    action?.invoke()
                }
            }
        }
    }
}

@Composable
fun InlineEmptyCard(
    title: String,
    summary: String,
) {
    AppListCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = title, style = MiuixTheme.textStyles.main, fontWeight = FontWeight.SemiBold)
            Text(
                text = summary,
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        }
    }
}
