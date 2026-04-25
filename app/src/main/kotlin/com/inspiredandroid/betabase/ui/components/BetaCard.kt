package com.inspiredandroid.betabase.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.inspiredandroid.betabase.ui.theme.BetabaseTheme

@Composable
fun BetaCard(
    modifier: Modifier = Modifier,
    background: Color = BetabaseTheme.colors.surface,
    shape: RoundedCornerShape = BetabaseTheme.shapes.card,
    bordered: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val base = modifier
        .clip(shape)
        .background(background)
        .let { if (bordered) it.border(1.5.dp, BetabaseTheme.colors.hairline, shape) else it }
        .let { if (onClick != null) it.clickable(onClick = onClick) else it }
    Box(modifier = base) {
        content()
    }
}
