package com.inspiredandroid.betabase

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.inspiredandroid.betabase.ui.screens.CompetitionsScreen
import com.inspiredandroid.betabase.ui.theme.BetabaseTheme

@Composable
fun BetabaseApp() {
    BetabaseTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BetabaseTheme.colors.background),
        ) {
            CompetitionsScreen()
        }
    }
}
