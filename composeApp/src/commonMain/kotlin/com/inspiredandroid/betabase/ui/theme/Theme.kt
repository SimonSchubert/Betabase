package com.inspiredandroid.betabase.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

object BetabaseTheme {
    val colors: BetabaseColors
        @Composable
        @ReadOnlyComposable
        get() = LocalBetabaseColors.current

    val typography: BetabaseTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalBetabaseTypography.current

    val shapes: BetabaseShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalBetabaseShapes.current
}

val LocalContentColor = staticCompositionLocalOf { Color.Black }
val LocalTextStyle = staticCompositionLocalOf { TextStyle.Default }

@Composable
fun BetabaseTheme(
    colors: BetabaseColors = LightBetabaseColors,
    typography: BetabaseTypography = DefaultBetabaseTypography,
    shapes: BetabaseShapes = DefaultBetabaseShapes,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalBetabaseColors provides colors,
        LocalBetabaseTypography provides typography,
        LocalBetabaseShapes provides shapes,
        LocalContentColor provides colors.ink,
        LocalTextStyle provides typography.bodyMedium,
        content = content,
    )
}
