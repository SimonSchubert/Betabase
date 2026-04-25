package com.inspiredandroid.betabase

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inspiredandroid.betabase.data.BundledJsonEventSource
import com.inspiredandroid.betabase.data.CompetitionsRepository
import com.inspiredandroid.betabase.data.IfscEventSource
import com.inspiredandroid.betabase.data.SourceTag
import com.inspiredandroid.betabase.ui.screens.CompetitionsScreen
import com.inspiredandroid.betabase.ui.screens.CompetitionsViewModel
import com.inspiredandroid.betabase.ui.theme.BetabaseTheme

@Composable
fun BetabaseApp() {
    BetabaseTheme {
        val httpClient = remember { createHttpClient() }
        val repository = remember(httpClient) {
            CompetitionsRepository(
                sources = listOf(
                    IfscEventSource(httpClient),
                    BundledJsonEventSource(
                        resourcePath = "files/nkbv_competitions.json",
                        tag = SourceTag.NKBV,
                    ),
                    BundledJsonEventSource(
                        resourcePath = "files/sca_competitions.json",
                        tag = SourceTag.SCA,
                    ),
                ),
            )
        }
        val viewModel = viewModel { CompetitionsViewModel(repository) }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BetabaseTheme.colors.background),
        ) {
            CompetitionsScreen(viewModel = viewModel)
        }
    }
}
