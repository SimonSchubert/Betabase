package com.inspiredandroid.betabase.screenshots

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.inspiredandroid.betabase.data.CompetitionsFilters
import com.inspiredandroid.betabase.data.Discipline
import com.inspiredandroid.betabase.data.Gender
import com.inspiredandroid.betabase.ui.components.BetabaseBottomNav
import com.inspiredandroid.betabase.ui.components.Tab
import com.inspiredandroid.betabase.ui.screens.AthletesScreenContent
import com.inspiredandroid.betabase.ui.screens.AthletesUiState
import com.inspiredandroid.betabase.ui.screens.CompetitionsScreenContent
import com.inspiredandroid.betabase.ui.screens.CompetitionsUiState
import com.inspiredandroid.betabase.ui.screens.GradesScreen
import com.inspiredandroid.betabase.ui.theme.BetabaseTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ScreenshotTest {

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5.copy(softButtons = false),
        showSystemUi = true,
        maxPercentDifference = 0.1,
    )

    @Before
    fun setUpImages() {
        // Live IFSC / YouTube images — downloaded into build/fixture-cache (not git).
        paparazzi.installLiveImageLoader(screenshotRemoteImageUrls())
    }

    private fun snap(selectedTab: Tab = Tab.Comps, content: @Composable () -> Unit) {
        paparazzi.unsafeUpdateConfig(theme = "android:Theme.Material.Light.NoActionBar")
        paparazzi.snapshot {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                BetabaseTheme {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(BetabaseTheme.colors.background),
                    ) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            content()
                        }
                        BetabaseBottomNav(
                            selected = selectedTab,
                            onSelect = {},
                        )
                    }
                }
            }
        }
    }

    @Test
    fun ready() {
        snap {
            CompetitionsScreenContent(
                state = CompetitionsUiState(
                    initialLoading = false,
                    events = sampleEvents,
                ),
                onRefresh = {},
                onToggleSource = {},
                onToggleDiscipline = {},
                onToggleRound = {},
                onToggleGender = {},
                onTogglePara = {},
            )
        }
    }

    /** Primary store/README shot: athlete channels + streams + competition list. */
    @Test
    fun readyWithMedia() {
        snap {
            CompetitionsScreenContent(
                state = CompetitionsUiState(
                    initialLoading = false,
                    events = sampleEvents,
                    streams = sampleStreams,
                    athleteVideos = sampleAthleteVideos,
                ),
                onRefresh = {},
                onToggleSource = {},
                onToggleDiscipline = {},
                onToggleRound = {},
                onToggleGender = {},
                onTogglePara = {},
            )
        }
    }

    @Test
    fun athletes() {
        snap(selectedTab = Tab.Athletes) {
            AthletesScreenContent(
                state = AthletesUiState(
                    athletes = sampleAthletes,
                    loading = false,
                    currentYear = 2026,
                ),
            )
        }
    }

    @Test
    fun grades() {
        snap(selectedTab = Tab.Grades) {
            GradesScreen()
        }
    }

    @Test
    fun loading() {
        snap {
            CompetitionsScreenContent(
                state = CompetitionsUiState(
                    initialLoading = true,
                    events = emptyList(),
                ),
                onRefresh = {},
                onToggleSource = {},
                onToggleDiscipline = {},
                onToggleRound = {},
                onToggleGender = {},
                onTogglePara = {},
            )
        }
    }

    @Test
    fun error() {
        snap {
            CompetitionsScreenContent(
                state = CompetitionsUiState(
                    initialLoading = false,
                    errorMessage = "Couldn't reach the calendar feed.",
                    events = emptyList(),
                ),
                onRefresh = {},
                onToggleSource = {},
                onToggleDiscipline = {},
                onToggleRound = {},
                onToggleGender = {},
                onTogglePara = {},
            )
        }
    }

    @Test
    fun filteredEmpty() {
        snap {
            CompetitionsScreenContent(
                state = CompetitionsUiState(
                    initialLoading = false,
                    events = sampleEvents,
                    filters = CompetitionsFilters.Default.copy(
                        disciplines = setOf(Discipline.SPEED),
                        genders = setOf(Gender.MEN),
                    ),
                ),
                onRefresh = {},
                onToggleSource = {},
                onToggleDiscipline = {},
                onToggleRound = {},
                onToggleGender = {},
                onTogglePara = {},
            )
        }
    }

    @Test
    fun youthFiltered() {
        snap {
            CompetitionsScreenContent(
                state = CompetitionsUiState(
                    initialLoading = false,
                    events = youthSampleEvents,
                    filters = CompetitionsFilters.Default.copy(
                        genders = setOf(Gender.YOUTH),
                        disciplines = setOf(Discipline.BOULDER, Discipline.LEAD, Discipline.SPEED, Discipline.COMBINED),
                    ),
                ),
                onRefresh = {},
                onToggleSource = {},
                onToggleDiscipline = {},
                onToggleRound = {},
                onToggleGender = {},
                onTogglePara = {},
            )
        }
    }
}
