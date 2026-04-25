package com.inspiredandroid.betabase.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
fun BetaButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    background: Color = BetabaseTheme.colors.ink,
    onBackground: Color = BetabaseTheme.colors.inkInverse,
    shape: RoundedCornerShape = BetabaseTheme.shapes.button,
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(LocalContentColor provides onBackground) {
            BetaText(
                text = label.uppercase(),
                style = BetabaseTheme.typography.label,
            )
        }
    }
}

@Composable
fun BetaOutlineButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    border: Color = BetabaseTheme.colors.ink,
    contentColor: Color = BetabaseTheme.colors.ink,
    shape: RoundedCornerShape = BetabaseTheme.shapes.button,
) {
    Box(
        modifier = modifier
            .clip(shape)
            .border(2.dp, border, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            BetaText(
                text = label.uppercase(),
                style = BetabaseTheme.typography.label,
            )
        }
    }
}
