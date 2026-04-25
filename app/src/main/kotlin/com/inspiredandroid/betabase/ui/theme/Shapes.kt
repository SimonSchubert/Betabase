package com.inspiredandroid.betabase.ui.theme

import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

@Immutable
data class BetabaseShapes(
    val card: RoundedCornerShape,
    val cardLarge: RoundedCornerShape,
    val pill: RoundedCornerShape,
    val button: RoundedCornerShape,
    val tile: RoundedCornerShape,
)

val DefaultBetabaseShapes = BetabaseShapes(
    card = RoundedCornerShape(20.dp),
    cardLarge = RoundedCornerShape(28.dp),
    pill = RoundedCornerShape(CornerSize(50)),
    button = RoundedCornerShape(16.dp),
    tile = RoundedCornerShape(24.dp),
)

val LocalBetabaseShapes = staticCompositionLocalOf { DefaultBetabaseShapes }
