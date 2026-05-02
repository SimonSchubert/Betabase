package com.inspiredandroid.betabase.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.inspiredandroid.betabase.data.CompetitionsFilters
import com.inspiredandroid.betabase.data.Discipline
import com.inspiredandroid.betabase.data.Gender
import com.inspiredandroid.betabase.data.Round
import com.inspiredandroid.betabase.data.SourceTag
import com.inspiredandroid.betabase.ui.components.BetaButton
import com.inspiredandroid.betabase.ui.components.BetaCard
import com.inspiredandroid.betabase.ui.components.BetaChip
import com.inspiredandroid.betabase.ui.components.BetaText
import com.inspiredandroid.betabase.ui.components.CompetitionCard
import com.inspiredandroid.betabase.ui.theme.BetabaseTheme
import com.inspiredandroid.betabase.ui.util.FixedInspectionNow
import com.inspiredandroid.betabase.ui.util.rememberNow
import com.inspiredandroid.betabase.ui.util.startIn
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

@Composable
fun CompetitionsScreen(
    viewModel: CompetitionsViewModel,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    CompetitionsScreenContent(
        state = state,
        onRefresh = viewModel::refresh,
        onToggleSource = viewModel::toggle,
        onToggleDiscipline = viewModel::toggle,
        onToggleRound = viewModel::toggle,
        onToggleGender = viewModel::toggle,
    )
}

@Composable
fun CompetitionsScreenContent(
    state: CompetitionsUiState,
    onRefresh: () -> Unit,
    onToggleSource: (SourceTag) -> Unit,
    onToggleDiscipline: (Discipline) -> Unit,
    onToggleRound: (Round) -> Unit,
    onToggleGender: (Gender) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BetabaseTheme.colors.background),
    ) {
        when {
            state.showInitialLoading -> LoadingState()
            state.showError -> ErrorState(message = state.errorMessage.orEmpty(), onRetry = onRefresh)
            else -> ReadyState(
                state = state,
                onRefresh = onRefresh,
                onToggleSource = onToggleSource,
                onToggleDiscipline = onToggleDiscipline,
                onToggleRound = onToggleRound,
                onToggleGender = onToggleGender,
            )
        }
    }
}

@Composable
private fun ReadyState(
    state: CompetitionsUiState,
    onRefresh: () -> Unit,
    onToggleSource: (SourceTag) -> Unit,
    onToggleDiscipline: (Discipline) -> Unit,
    onToggleRound: (Round) -> Unit,
    onToggleGender: (Gender) -> Unit,
) {
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val visible = state.filteredEvents
    val zone = remember { TimeZone.currentSystemDefault() }
    val inInspection = LocalInspectionMode.current
    val tickedNow by rememberNow()
    val now = if (inInspection) FixedInspectionNow else tickedNow
    val grouped = remember(visible, zone) { visible.groupBy { it.startIn(zone).date } }
    val sortedDays = remember(grouped) { grouped.keys.sorted() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 12.dp,
            bottom = 32.dp + bottomInset,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item("header") {
            Header(refreshing = state.refreshing, onRefresh = onRefresh, total = state.events.size, visible = visible.size)
        }
        item("filters") {
            FilterChips(
                filters = state.filters,
                onToggleSource = onToggleSource,
                onToggleDiscipline = onToggleDiscipline,
                onToggleRound = onToggleRound,
                onToggleGender = onToggleGender,
            )
        }

        if (visible.isEmpty()) {
            item("empty") { EmptyState(filtered = state.events.isNotEmpty()) }
            return@LazyColumn
        }

        sortedDays.forEach { day ->
            item("day-$day") {
                Spacer(Modifier.height(8.dp))
                DayHeader(day)
            }
            items(grouped.getValue(day), key = { it.id }) { event ->
                CompetitionCard(event = event, now = now, zone = zone)
            }
        }
    }
}

@Composable
private fun Header(refreshing: Boolean, onRefresh: () -> Unit, total: Int, visible: Int) {
    Column(modifier = Modifier.fillMaxWidth()) {
        BetaText(
            text = "BETABASE",
            style = BetabaseTheme.typography.displayLarge,
            color = BetabaseTheme.colors.ink,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(6.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            val countLabel = if (visible == total) {
                if (total == 1) "1 upcoming event" else "$total upcoming events"
            } else {
                "$visible of $total events"
            }
            BetaText(
                text = countLabel,
                style = BetabaseTheme.typography.label,
                color = BetabaseTheme.colors.inkMuted,
                modifier = Modifier.weight(1f),
            )
            RefreshChip(refreshing = refreshing, onRefresh = onRefresh)
        }
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(BetabaseTheme.shapes.pill)
                .background(BetabaseTheme.colors.ink),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterChips(
    filters: CompetitionsFilters,
    onToggleSource: (SourceTag) -> Unit,
    onToggleDiscipline: (Discipline) -> Unit,
    onToggleRound: (Round) -> Unit,
    onToggleGender: (Gender) -> Unit,
) {
    val colors = BetabaseTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SourceTag.entries.forEach { source ->
                BetaChip(
                    label = source.regionLabel,
                    selected = source in filters.sources,
                    activeColor = sourceColor(source),
                    activeOnColor = sourceOnColor(source),
                    onClick = { onToggleSource(source) },
                )
            }
        }
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
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
                label = "Speed",
                selected = Discipline.SPEED in filters.disciplines,
                activeColor = colors.speed,
                activeOnColor = colors.ink,
                onClick = { onToggleDiscipline(Discipline.SPEED) },
            )
            BetaChip(
                label = "Combined",
                selected = Discipline.COMBINED in filters.disciplines,
                activeColor = colors.combined,
                onClick = { onToggleDiscipline(Discipline.COMBINED) },
            )
        }
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            BetaChip(
                label = "Qualifier",
                selected = Round.QUALIFICATION in filters.rounds,
                activeColor = colors.ink,
                onClick = { onToggleRound(Round.QUALIFICATION) },
            )
            BetaChip(
                label = "Semi",
                selected = Round.SEMIFINAL in filters.rounds,
                activeColor = colors.ink,
                onClick = { onToggleRound(Round.SEMIFINAL) },
            )
            BetaChip(
                label = "Final",
                selected = Round.FINAL in filters.rounds,
                activeColor = colors.ink,
                onClick = { onToggleRound(Round.FINAL) },
            )
            DotDivider()
            BetaChip(
                label = "Women",
                selected = Gender.WOMEN in filters.genders,
                activeColor = colors.women,
                onClick = { onToggleGender(Gender.WOMEN) },
            )
            BetaChip(
                label = "Men",
                selected = Gender.MEN in filters.genders,
                activeColor = colors.men,
                onClick = { onToggleGender(Gender.MEN) },
            )
            BetaChip(
                label = "Youth",
                selected = Gender.YOUTH in filters.genders,
                activeColor = colors.youth,
                onClick = { onToggleGender(Gender.YOUTH) },
            )
        }
    }
}

@Composable
private fun sourceColor(source: SourceTag) = when (source) {
    SourceTag.IFSC -> BetabaseTheme.colors.accent
    SourceTag.NKBV -> BetabaseTheme.colors.ink
    SourceTag.SCA -> Color(0xFF16A34A)
}

@Composable
private fun sourceOnColor(source: SourceTag) = when (source) {
    SourceTag.IFSC -> BetabaseTheme.colors.onAccent
    SourceTag.NKBV -> BetabaseTheme.colors.inkInverse
    SourceTag.SCA -> BetabaseTheme.colors.inkInverse
}

@Composable
private fun DotDivider() {
    Box(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .height(36.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .clip(BetabaseTheme.shapes.pill)
                .background(BetabaseTheme.colors.hairline)
                .padding(3.dp),
        )
    }
}

@Composable
private fun RefreshChip(refreshing: Boolean, onRefresh: () -> Unit) {
    val label = if (refreshing) "Refreshing…" else "Refresh"
    BetaCard(
        background = BetabaseTheme.colors.surface,
        bordered = true,
        shape = BetabaseTheme.shapes.pill,
        onClick = if (refreshing) null else onRefresh,
    ) {
        Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            BetaText(
                text = label.uppercase(),
                style = BetabaseTheme.typography.labelSmall,
                color = BetabaseTheme.colors.ink,
            )
        }
    }
}

@Composable
private fun DayHeader(day: LocalDate) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .clip(BetabaseTheme.shapes.tile)
                .background(BetabaseTheme.colors.ink)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BetaText(
                    text = day.day.toString().padStart(2, '0'),
                    style = BetabaseTheme.typography.dayNumber,
                    color = BetabaseTheme.colors.inkInverse,
                )
                Column {
                    BetaText(
                        text = day.month.name.take(3),
                        style = BetabaseTheme.typography.dayMonth,
                        color = BetabaseTheme.colors.inkInverse,
                    )
                    BetaText(
                        text = day.dayOfWeek.name,
                        style = BetabaseTheme.typography.labelSmall,
                        color = BetabaseTheme.colors.inkInverse,
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .height(2.dp)
                .clip(BetabaseTheme.shapes.pill)
                .background(BetabaseTheme.colors.hairline),
        )
    }
}

@Composable
private fun LoadingState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start,
    ) {
        BetaText(
            text = "BETABASE",
            style = BetabaseTheme.typography.displayLarge,
            color = BetabaseTheme.colors.ink,
        )
        Spacer(Modifier.height(8.dp))
        BetaText(
            text = "Loading competitions…",
            style = BetabaseTheme.typography.label,
            color = BetabaseTheme.colors.inkMuted,
        )
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start,
    ) {
        BetaText(
            text = "OFFLINE",
            style = BetabaseTheme.typography.label,
            color = BetabaseTheme.colors.accent,
        )
        Spacer(Modifier.height(6.dp))
        BetaText(
            text = "Couldn't reach the\ncalendar feed.",
            style = BetabaseTheme.typography.displayMedium,
            color = BetabaseTheme.colors.ink,
        )
        Spacer(Modifier.height(12.dp))
        BetaText(
            text = message,
            style = BetabaseTheme.typography.bodyMedium,
            color = BetabaseTheme.colors.inkMuted,
        )
        Spacer(Modifier.height(24.dp))
        BetaButton(
            label = "Try again",
            onClick = onRetry,
        )
    }
}

@Composable
private fun EmptyState(filtered: Boolean) {
    BetaCard(
        modifier = Modifier.fillMaxWidth(),
        background = BetabaseTheme.colors.surfaceMuted,
        shape = BetabaseTheme.shapes.cardLarge,
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            BetaText(
                text = if (filtered) "No matches" else "Nothing scheduled",
                style = BetabaseTheme.typography.titleLarge,
                color = BetabaseTheme.colors.ink,
            )
            BetaText(
                text = if (filtered) {
                    "Tap a chip above to widen the filter."
                } else {
                    "No upcoming competitions in the IFSC feed right now. Check back soon."
                },
                style = BetabaseTheme.typography.bodyMedium,
                color = BetabaseTheme.colors.inkMuted,
            )
        }
    }
}
