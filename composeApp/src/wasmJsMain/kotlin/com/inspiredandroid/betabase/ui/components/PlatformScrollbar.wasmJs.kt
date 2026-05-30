package com.inspiredandroid.betabase.ui.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun PlatformVerticalScrollbar(state: LazyListState, modifier: Modifier) = Unit

@Composable
actual fun PlatformVerticalScrollbar(state: LazyGridState, modifier: Modifier) = Unit

@Composable
actual fun PlatformVerticalScrollbar(state: ScrollState, modifier: Modifier) = Unit
