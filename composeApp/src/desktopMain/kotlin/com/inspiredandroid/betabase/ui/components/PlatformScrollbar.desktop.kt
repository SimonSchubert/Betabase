package com.inspiredandroid.betabase.ui.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.ScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.inspiredandroid.betabase.ui.theme.BetabaseTheme

@Composable
private fun betabaseScrollbarStyle(): ScrollbarStyle {
    val ink = BetabaseTheme.colors.ink
    return remember(ink) {
        ScrollbarStyle(
            minimalHeight = 24.dp,
            thickness = 8.dp,
            shape = RoundedCornerShape(4.dp),
            hoverDurationMillis = 300,
            unhoverColor = ink.copy(alpha = 0.20f),
            hoverColor = ink.copy(alpha = 0.45f),
        )
    }
}

@Composable
actual fun PlatformVerticalScrollbar(state: LazyListState, modifier: Modifier) {
    VerticalScrollbar(
        adapter = rememberScrollbarAdapter(state),
        modifier = modifier,
        style = betabaseScrollbarStyle(),
    )
}

@Composable
actual fun PlatformVerticalScrollbar(state: LazyGridState, modifier: Modifier) {
    VerticalScrollbar(
        adapter = rememberScrollbarAdapter(state),
        modifier = modifier,
        style = betabaseScrollbarStyle(),
    )
}

@Composable
actual fun PlatformVerticalScrollbar(state: ScrollState, modifier: Modifier) {
    VerticalScrollbar(
        adapter = rememberScrollbarAdapter(state),
        modifier = modifier,
        style = betabaseScrollbarStyle(),
    )
}
