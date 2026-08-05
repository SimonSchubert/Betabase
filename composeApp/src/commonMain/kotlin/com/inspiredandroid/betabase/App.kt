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
import com.inspiredandroid.betabase.data.AthleteFeedRepository
import com.inspiredandroid.betabase.data.AthleteVideosFeedSource
import com.inspiredandroid.betabase.data.AthleteVideosRepository
import com.inspiredandroid.betabase.data.AthletesRepository
import com.inspiredandroid.betabase.data.BundledJsonEventSource
import com.inspiredandroid.betabase.data.CompetitionsRepository
import com.inspiredandroid.betabase.data.IfscEventSource
import com.inspiredandroid.betabase.data.IfscVideosSource
import com.inspiredandroid.betabase.data.SourceTag
import com.inspiredandroid.betabase.data.VideosRepository
import com.inspiredandroid.betabase.data.YoutubeChannelSource
import com.inspiredandroid.betabase.data.createFilterStorage
import com.inspiredandroid.betabase.data.createJsonCache
import com.inspiredandroid.betabase.data.setupImageLoader
import com.inspiredandroid.betabase.ui.components.BetabaseBottomNav
import com.inspiredandroid.betabase.ui.components.Tab
import com.inspiredandroid.betabase.ui.screens.AthletesScreen
import com.inspiredandroid.betabase.ui.screens.CompetitionsScreen
import com.inspiredandroid.betabase.ui.screens.CompetitionsViewModel
import com.inspiredandroid.betabase.ui.screens.GradesScreen
import com.inspiredandroid.betabase.ui.screens.GymsScreen
import com.inspiredandroid.betabase.ui.theme.BetabaseTheme
import kotlinx.datetime.TimeZone

private val TabSaver = Saver<Tab, String>(
    save = { it.name },
    restore = { runCatching { Tab.valueOf(it) }.getOrDefault(Tab.Comps) },
)

@Composable
fun BetabaseApp() {
    BetabaseTheme {
        remember {
            setupImageLoader()
            Unit
        }
        val httpClient = remember { createHttpClient() }
        val jsonCache = remember { createJsonCache() }
        val repository = remember(httpClient, jsonCache) {
            CompetitionsRepository(
                sources = listOf(
                    IfscEventSource(httpClient, cache = jsonCache),
                    BundledJsonEventSource(
                        resourcePath = "files/europe_competitions.json",
                        tag = SourceTag.EUROPE,
                    ),
                    BundledJsonEventSource(
                        resourcePath = "files/americas_competitions.json",
                        tag = SourceTag.AMERICAS,
                    ),
                    BundledJsonEventSource(
                        resourcePath = "files/nkbv_competitions.json",
                        tag = SourceTag.NKBV,
                    ),
                    BundledJsonEventSource(
                        resourcePath = "files/sca_competitions.json",
                        tag = SourceTag.SCA,
                    ),
                    BundledJsonEventSource(
                        resourcePath = "files/dav_competitions.json",
                        tag = SourceTag.DAV,
                        zone = TimeZone.of("Europe/Berlin"),
                    ),
                ),
            )
        }
        val videosRepository = remember(httpClient, jsonCache) {
            VideosRepository(IfscVideosSource(httpClient, cache = jsonCache))
        }
        val athletesRepository = remember { AthletesRepository() }
        val athleteVideosRepository = remember(httpClient, jsonCache) {
            AthleteVideosRepository(YoutubeChannelSource(httpClient, cache = jsonCache))
        }
        val athleteFeedRepository = remember(httpClient, jsonCache, athletesRepository) {
            AthleteFeedRepository(
                athletesRepository = athletesRepository,
                videosSource = AthleteVideosFeedSource(httpClient, cache = jsonCache),
            )
        }
        val filterStorage = remember { createFilterStorage() }
        val viewModel = viewModel {
            CompetitionsViewModel(
                repository = repository,
                videosRepository = videosRepository,
                athleteFeedRepository = athleteFeedRepository,
                filterStorage = filterStorage,
            )
        }

        var selectedTab by rememberSaveable(stateSaver = TabSaver) { mutableStateOf(Tab.Comps) }
        var showAthletes by rememberSaveable { mutableStateOf(false) }
        var selectedAthleteId by rememberSaveable { mutableStateOf<String?>(null) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BetabaseTheme.colors.background),
        ) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                var gymsEverVisible by rememberSaveable { mutableStateOf(false) }
                var gradesEverVisible by rememberSaveable { mutableStateOf(false) }
                if (selectedTab == Tab.Gyms) gymsEverVisible = true
                if (selectedTab == Tab.Grades) gradesEverVisible = true

                TabLayer(visible = selectedTab == Tab.Comps && !showAthletes) {
                    CompetitionsScreen(
                        viewModel = viewModel,
                        onOpenAthletes = {
                            selectedAthleteId = null
                            showAthletes = true
                        },
                        onOpenAthlete = { id ->
                            selectedAthleteId = id
                            showAthletes = true
                        },
                    )
                }
                if (gymsEverVisible) {
                    TabLayer(visible = selectedTab == Tab.Gyms && !showAthletes) {
                        GymsScreen(filterStorage = filterStorage)
                    }
                }
                if (gradesEverVisible) {
                    TabLayer(visible = selectedTab == Tab.Grades && !showAthletes) {
                        GradesScreen()
                    }
                }
                if (showAthletes) {
                    TabLayer(visible = true) {
                        AthletesScreen(
                            videosRepository = athleteVideosRepository,
                            selectedAthleteId = selectedAthleteId,
                            onSelectAthlete = { selectedAthleteId = it },
                            onBack = {
                                selectedAthleteId = null
                                showAthletes = false
                            },
                        )
                    }
                }
            }
            if (!showAthletes) {
                BetabaseBottomNav(
                    selected = selectedTab,
                    onSelect = { selectedTab = it },
                )
            }
        }
    }
}

@Composable
private fun TabLayer(visible: Boolean, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(if (visible) 1f else 0f)
            .alpha(if (visible) 1f else 0f),
    ) {
        content()
    }
}
