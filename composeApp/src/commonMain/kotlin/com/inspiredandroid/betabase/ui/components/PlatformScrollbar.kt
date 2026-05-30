package com.inspiredandroid.betabase.ui.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Draws a draggable vertical scrollbar bound to [state] on platforms that expect one
 * (desktop). On touch platforms and web it renders nothing, so callers can wrap any
 * scrollable in a [androidx.compose.foundation.layout.Box] and overlay this unconditionally.
 */
@Composable
expect fun PlatformVerticalScrollbar(state: LazyListState, modifier: Modifier = Modifier)

@Composable
expect fun PlatformVerticalScrollbar(state: LazyGridState, modifier: Modifier = Modifier)

@Composable
expect fun PlatformVerticalScrollbar(state: ScrollState, modifier: Modifier = Modifier)
