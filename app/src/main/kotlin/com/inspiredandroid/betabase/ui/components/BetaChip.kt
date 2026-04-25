package com.inspiredandroid.betabase.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.inspiredandroid.betabase.ui.theme.BetabaseTheme
import com.inspiredandroid.betabase.ui.theme.LocalContentColor

@Composable
fun BetaChip(
    label: String,
    selected: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    activeOnColor: Color = BetabaseTheme.colors.inkInverse,
) {
    val shape = BetabaseTheme.shapes.pill
    val background = if (selected) activeColor else Color.Transparent
    val contentColor = when {
        selected -> activeOnColor
        else -> BetabaseTheme.colors.inkMuted
    }
    Box(
        modifier = modifier
            .clip(shape)
            .background(background)
            .border(
                width = 1.5.dp,
                color = if (selected) activeColor else BetabaseTheme.colors.hairline,
                shape = shape,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            BetaText(
                text = label.uppercase(),
                style = BetabaseTheme.typography.labelSmall,
            )
        }
    }
}
