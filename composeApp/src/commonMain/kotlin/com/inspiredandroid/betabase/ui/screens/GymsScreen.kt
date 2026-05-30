package com.inspiredandroid.betabase.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inspiredandroid.betabase.data.DayKey
import com.inspiredandroid.betabase.data.Discipline
import com.inspiredandroid.betabase.data.FilterStorage
import com.inspiredandroid.betabase.data.Gym
import com.inspiredandroid.betabase.data.GymsFilters
import com.inspiredandroid.betabase.data.GymsRepository
import com.inspiredandroid.betabase.ui.components.BetaButton
import com.inspiredandroid.betabase.ui.components.BetaCard
import com.inspiredandroid.betabase.ui.components.BetaChip
import com.inspiredandroid.betabase.ui.components.BetaPill
import com.inspiredandroid.betabase.ui.components.BetaText
import com.inspiredandroid.betabase.ui.components.PlatformVerticalScrollbar
import com.inspiredandroid.betabase.ui.theme.BetabaseTheme

@Composable
fun GymsScreen(
    filterStorage: FilterStorage,
    modifier: Modifier = Modifier,
) {
    val viewModel = viewModel {
        GymsViewModel(
            repository = GymsRepository(),
            filterStorage = filterStorage,
        )
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    var selectedGym by remember { mutableStateOf<Gym?>(null) }

    Box(modifier = modifier.fillMaxSize().background(BetabaseTheme.colors.background)) {
        GymMap(
            gyms = state.filteredGyms,
            selectedGym = selectedGym,
            onGymSelected = { selectedGym = it },
            modifier = Modifier.fillMaxSize(),
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChips(
                filters = state.filters,
                onToggleDiscipline = viewModel::toggle,
                onToggleBoards = viewModel::toggleBoards,
                modifier = Modifier.fillMaxWidth(),
            )
            CityCoverageNotice(modifier = Modifier.fillMaxWidth())
        }

        selectedGym?.let { gym ->
            GymDetailsSheet(
                gym = gym,
                onDismiss = { selectedGym = null },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
            )
        }
    }
}

@Composable
expect fun GymMap(
    gyms: List<Gym>,
    selectedGym: Gym?,
    onGymSelected: (Gym?) -> Unit,
    modifier: Modifier = Modifier,
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterChips(
    filters: GymsFilters,
    onToggleDiscipline: (Discipline) -> Unit,
    onToggleBoards: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = BetabaseTheme.colors
    BetaCard(
        modifier = modifier,
        background = colors.surface,
        shape = BetabaseTheme.shapes.card,
        bordered = true,
    ) {
        FlowRow(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            BetaChip(
                label = "Boulder",
                selected = Discipline.BOULDER in filters.disciplines,
                activeColor = colors.boulder,
                onClick = { onToggleDiscipline(Discipline.BOULDER) },
            )
            BetaChip(
                label = "Lead",
                selected = Discipline.LEAD in filters.disciplines,
                activeColor = colors.lead,
                onClick = { onToggleDiscipline(Discipline.LEAD) },
            )
            BetaChip(
                label = "Boards",
                selected = filters.requireBoards,
                activeColor = colors.ink,
                onClick = onToggleBoards,
            )
        }
    }
}

@Composable
private fun CityCoverageNotice(modifier: Modifier = Modifier) {
    val colors = BetabaseTheme.colors
    BetaCard(
        modifier = modifier,
        background = colors.surface,
        shape = BetabaseTheme.shapes.card,
        bordered = true,
    ) {
        BetaText(
            text = "Map coverage is limited to a few cities for now and will expand over time.",
            style = BetabaseTheme.typography.bodySmall,
            color = colors.inkMuted,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun GymDetailsSheet(
    gym: Gym,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current
    val scrollState = rememberScrollState()
    BetaCard(
        modifier = modifier.padding(12.dp),
        shape = RoundedCornerShape(20.dp),
        bordered = true,
    ) {
        Box {
            Column(
                modifier = Modifier
                    .heightIn(max = 360.dp)
                    .verticalScroll(scrollState)
                    .padding(20.dp),
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Column(modifier = Modifier.weight(1f)) {
                        BetaText(
                            text = gym.name,
                            style = BetabaseTheme.typography.titleMedium,
                        )
                        Spacer(Modifier.height(4.dp))
                        BetaText(
                            text = gym.address,
                            style = BetabaseTheme.typography.bodySmall,
                            color = BetabaseTheme.colors.inkMuted,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(BetabaseTheme.shapes.pill)
                            .clickable(onClick = onDismiss)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        BetaText(
                            text = "CLOSE",
                            style = BetabaseTheme.typography.labelSmall,
                            color = BetabaseTheme.colors.inkMuted,
                        )
                    }
                }

                if (gym.disciplines.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        gym.disciplines.forEach { discipline ->
                            BetaPill(
                                label = discipline.displayName(),
                                background = discipline.color(),
                                onColor = BetabaseTheme.colors.inkInverse,
                            )
                        }
                    }
                }

                if (gym.boards.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    BetaText(
                        text = "TRAINING BOARDS",
                        style = BetabaseTheme.typography.labelSmall,
                        color = BetabaseTheme.colors.inkMuted,
                    )
                    Spacer(Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        gym.boards.forEach { board ->
                            BetaPill(
                                label = board.label,
                                background = BetabaseTheme.colors.ink,
                                onColor = BetabaseTheme.colors.inkInverse,
                            )
                        }
                    }
                }

                gym.sizeSqm?.let { size ->
                    Spacer(Modifier.height(12.dp))
                    BetaText(
                        text = "≈ $size m² climbing surface",
                        style = BetabaseTheme.typography.bodySmall,
                        color = BetabaseTheme.colors.inkMuted,
                    )
                }

                if (gym.openingHours.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    BetaText(
                        text = "OPENING HOURS",
                        style = BetabaseTheme.typography.labelSmall,
                        color = BetabaseTheme.colors.inkMuted,
                    )
                    Spacer(Modifier.height(6.dp))
                    DayKey.ordered.forEach { day ->
                        val hours = gym.openingHours[day] ?: "closed"
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            BetaText(
                                text = day.label,
                                style = BetabaseTheme.typography.bodySmall,
                            )
                            BetaText(
                                text = hours,
                                style = BetabaseTheme.typography.bodySmall,
                                color = BetabaseTheme.colors.inkMuted,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                BetaButton(
                    label = "Open Website",
                    onClick = { runCatching { uriHandler.openUri(gym.url) } },
                    shape = BetabaseTheme.shapes.pill,
                )
            }
            PlatformVerticalScrollbar(
                scrollState,
                Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .padding(vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun Discipline.displayName(): String = when (this) {
    Discipline.BOULDER -> "Boulder"
    Discipline.LEAD -> "Lead"
    Discipline.SPEED -> "Speed"
    Discipline.COMBINED -> "Combined"
    Discipline.OTHER -> "Other"
}

@Composable
private fun Discipline.color() = when (this) {
    Discipline.BOULDER -> BetabaseTheme.colors.boulder
    Discipline.LEAD -> BetabaseTheme.colors.lead
    Discipline.SPEED -> BetabaseTheme.colors.speed
    Discipline.COMBINED -> BetabaseTheme.colors.combined
    Discipline.OTHER -> BetabaseTheme.colors.inkMuted
}
