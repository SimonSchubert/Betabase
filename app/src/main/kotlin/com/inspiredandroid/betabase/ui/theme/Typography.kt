package com.inspiredandroid.betabase.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

@Immutable
data class BetabaseTypography(
    val displayLarge: TextStyle,
    val displayMedium: TextStyle,
    val displaySmall: TextStyle,
    val titleLarge: TextStyle,
    val titleMedium: TextStyle,
    val titleSmall: TextStyle,
    val bodyLarge: TextStyle,
    val bodyMedium: TextStyle,
    val bodySmall: TextStyle,
    val label: TextStyle,
    val labelSmall: TextStyle,
    val dayNumber: TextStyle,
    val dayMonth: TextStyle,
)

private val sans = FontFamily.SansSerif

val DefaultBetabaseTypography = BetabaseTypography(
    displayLarge = TextStyle(
        fontFamily = sans,
        fontWeight = FontWeight.Black,
        fontSize = 56.sp,
        lineHeight = 56.sp,
        letterSpacing = (-0.04).em,
    ),
    displayMedium = TextStyle(
        fontFamily = sans,
        fontWeight = FontWeight.Black,
        fontSize = 40.sp,
        lineHeight = 42.sp,
        letterSpacing = (-0.03).em,
    ),
    displaySmall = TextStyle(
        fontFamily = sans,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 28.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.02).em,
    ),
    titleLarge = TextStyle(
        fontFamily = sans,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 22.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.01).em,
    ),
    titleMedium = TextStyle(
        fontFamily = sans,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 22.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = sans,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        lineHeight = 18.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = sans,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = sans,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = sans,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    label = TextStyle(
        fontFamily = sans,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 11.sp,
        lineHeight = 12.sp,
        letterSpacing = 0.12.em,
    ),
    labelSmall = TextStyle(
        fontFamily = sans,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 10.sp,
        lineHeight = 11.sp,
        letterSpacing = 0.14.em,
    ),
    dayNumber = TextStyle(
        fontFamily = sans,
        fontWeight = FontWeight.Black,
        fontSize = 48.sp,
        lineHeight = 48.sp,
        letterSpacing = (-0.05).em,
    ),
    dayMonth = TextStyle(
        fontFamily = sans,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 12.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.14.em,
    ),
)

val LocalBetabaseTypography = staticCompositionLocalOf { DefaultBetabaseTypography }
