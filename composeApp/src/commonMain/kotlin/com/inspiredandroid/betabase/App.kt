package com.inspiredandroid.betabase

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inspiredandroid.betabase.data.BundledJsonEventSource
import com.inspiredandroid.betabase.data.CompetitionsRepository
import com.inspiredandroid.betabase.data.IfscEventSource
import com.inspiredandroid.betabase.data.IfscVideosSource
import com.inspiredandroid.betabase.data.SourceTag
import com.inspiredandroid.betabase.data.VideosRepository
import com.inspiredandroid.betabase.data.createFilterStorage
import com.inspiredandroid.betabase.ui.components.BetabaseBottomNav
import com.inspiredandroid.betabase.ui.components.Tab
import com.inspiredandroid.betabase.ui.screens.CompetitionsScreen
import com.inspiredandroid.betabase.ui.screens.CompetitionsViewModel
import com.inspiredandroid.betabase.ui.screens.GymsScreen
import com.inspiredandroid.betabase.ui.theme.BetabaseTheme

private val TabSaver = Saver<Tab, String>(save = { it.name }, restore = { Tab.valueOf(it) })

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
        val videosRepository = remember(httpClient) {
            VideosRepository(IfscVideosSource(httpClient))
        }
        val filterStorage = remember { createFilterStorage() }
        val viewModel = viewModel { CompetitionsViewModel(repository, videosRepository, filterStorage) }

        var selectedTab by rememberSaveable(stateSaver = TabSaver) { mutableStateOf(Tab.Comps) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BetabaseTheme.colors.background),
        ) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                var gymsEverVisible by rememberSaveable { mutableStateOf(false) }
                if (selectedTab == Tab.Gyms) gymsEverVisible = true
                val compsOnTop = selectedTab == Tab.Comps

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(if (compsOnTop) 1f else 0f)
                        .alpha(if (compsOnTop) 1f else 0f),
                ) {
                    CompetitionsScreen(viewModel = viewModel)
                }
                if (gymsEverVisible) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .zIndex(if (!compsOnTop) 1f else 0f)
                            .alpha(if (!compsOnTop) 1f else 0f),
                    ) {
                        GymsScreen(filterStorage = filterStorage)
                    }
                }
            }
            BetabaseBottomNav(
                selected = selectedTab,
                onSelect = { selectedTab = it },
            )
        }
    }
}
