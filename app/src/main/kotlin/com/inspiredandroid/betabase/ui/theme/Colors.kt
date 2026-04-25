package com.inspiredandroid.betabase.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class BetabaseColors(
    val background: Color,
    val surface: Color,
    val surfaceMuted: Color,
    val ink: Color,
    val inkMuted: Color,
    val inkInverse: Color,
    val hairline: Color,
    val accent: Color,
    val onAccent: Color,
    val boulder: Color,
    val lead: Color,
    val speed: Color,
    val combined: Color,
    val women: Color,
    val men: Color,
    val youth: Color,
    val finalRound: Color,
    val onFinalRound: Color,
    val semiRound: Color,
    val qualRound: Color,
)

val LightBetabaseColors = BetabaseColors(
    background = Color(0xFFF4EFE6),
    surface = Color(0xFFFFFFFF),
    surfaceMuted = Color(0xFFEDE6D7),
    ink = Color(0xFF111111),
    inkMuted = Color(0xFF6B665D),
    inkInverse = Color(0xFFFFFFFF),
    hairline = Color(0xFFE3DDCF),
    accent = Color(0xFFE63946),
    onAccent = Color(0xFFFFFFFF),
    boulder = Color(0xFFFF6B35),
    lead = Color(0xFF1D4ED8),
    speed = Color(0xFFFACC15),
    combined = Color(0xFF7C3AED),
    women = Color(0xFFEC4899),
    men = Color(0xFF0EA5E9),
    youth = Color(0xFF14B8A6),
    finalRound = Color(0xFF111111),
    onFinalRound = Color(0xFFFFFFFF),
    semiRound = Color(0xFFFCD5CE),
    qualRound = Color(0xFFE3DDCF),
)

val LocalBetabaseColors = staticCompositionLocalOf { LightBetabaseColors }
