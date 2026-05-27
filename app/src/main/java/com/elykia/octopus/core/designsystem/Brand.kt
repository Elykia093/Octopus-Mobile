package com.elykia.octopus.core.designsystem

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.elykia.octopus.R

@Composable
fun OctopusBrandMark(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
) {
    Image(
        painter = painterResource(id = R.drawable.octopus_logo),
        contentDescription = null,
        modifier = modifier
            .size(size)
            .aspectRatio(1f),
    )
}
