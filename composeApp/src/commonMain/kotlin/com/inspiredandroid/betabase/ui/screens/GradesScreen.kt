package com.inspiredandroid.betabase.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.inspiredandroid.betabase.data.ClimbingHoldTypes
import com.inspiredandroid.betabase.data.GradeComparisonTable
import com.inspiredandroid.betabase.data.GradeDiscipline
import com.inspiredandroid.betabase.data.GradeSystem
import com.inspiredandroid.betabase.data.HoldType
import com.inspiredandroid.betabase.data.gradeTableFor
import com.inspiredandroid.betabase.ui.components.BetaCard
import com.inspiredandroid.betabase.ui.components.BetaChip
import com.inspiredandroid.betabase.ui.components.BetaText
import com.inspiredandroid.betabase.ui.components.PlatformVerticalScrollbar
import com.inspiredandroid.betabase.ui.theme.BetabaseTheme

private val ScreenSidePadding = 20.dp

private val GradeDisciplineSaver = Saver<GradeDiscipline, String>(
    save = { it.name },
    restore = { GradeDiscipline.valueOf(it) },
)

@Composable
fun GradesScreen(modifier: Modifier = Modifier) {
    var discipline by rememberSaveable(stateSaver = GradeDisciplineSaver) {
        mutableStateOf(GradeDiscipline.BOULDER)
    }
    val table = gradeTableFor(discipline)
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val listState = rememberLazyListState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BetabaseTheme.colors.background),
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars),
            contentPadding = PaddingValues(
                start = ScreenSidePadding,
                end = ScreenSidePadding,
                top = 12.dp,
                bottom = 32.dp + bottomInset,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    BetaText(
                        text = "GRADES",
                        style = BetabaseTheme.typography.displayMedium,
                        color = BetabaseTheme.colors.ink,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(6.dp))
                    BetaText(
                        text = "Approximate conversions between common grading systems. Grades are subjective — treat these as a guide, not exact science.",
                        style = BetabaseTheme.typography.label,
                        color = BetabaseTheme.colors.inkMuted,
                        modifier = Modifier.fillMaxWidth(),
                    )
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

            item {
                DisciplineChips(
                    selected = discipline,
                    onSelect = { discipline = it },
                )
            }

            item {
                GradeTableCard(table = table)
            }

            item {
                BetaText(
                    text = "About these scales",
                    style = BetabaseTheme.typography.titleMedium,
                    color = BetabaseTheme.colors.ink,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            itemsIndexed(
                items = table.systems,
                key = { _, system -> system.id },
            ) { _, system ->
                SystemBlurbCard(system = system)
            }

            item {
                BetaText(
                    text = "Hold types",
                    style = BetabaseTheme.typography.titleMedium,
                    color = BetabaseTheme.colors.ink,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            item {
                BetaText(
                    text = "The shape of a hold often matters as much as the grade. Here’s a quick guide to common gym and outdoor holds.",
                    style = BetabaseTheme.typography.label,
                    color = BetabaseTheme.colors.inkMuted,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            itemsIndexed(
                items = ClimbingHoldTypes,
                key = { _, hold -> hold.id },
            ) { _, hold ->
                HoldTypeCard(hold = hold)
            }
        }

        PlatformVerticalScrollbar(
            state = listState,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .padding(end = 2.dp),
        )
    }
}

@Composable
private fun DisciplineChips(
    selected: GradeDiscipline,
    onSelect: (GradeDiscipline) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        BetaChip(
            label = "Boulder",
            selected = selected == GradeDiscipline.BOULDER,
            activeColor = BetabaseTheme.colors.boulder,
            onClick = { onSelect(GradeDiscipline.BOULDER) },
        )
        BetaChip(
            label = "Lead",
            selected = selected == GradeDiscipline.LEAD,
            activeColor = BetabaseTheme.colors.lead,
            activeOnColor = BetabaseTheme.colors.inkInverse,
            onClick = { onSelect(GradeDiscipline.LEAD) },
        )
    }
}

@Composable
private fun GradeTableCard(
    table: GradeComparisonTable,
    modifier: Modifier = Modifier,
) {
    BetaCard(
        modifier = modifier.fillMaxWidth(),
        background = BetabaseTheme.colors.surface,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            GradeTableHeader(systems = table.systems)
            table.rows.forEachIndexed { index, row ->
                val bg = if (index % 2 == 0) {
                    BetabaseTheme.colors.surface
                } else {
                    BetabaseTheme.colors.surfaceMuted
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bg)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    table.systems.forEach { system ->
                        BetaText(
                            text = row.labels[system.id].orEmpty(),
                            style = BetabaseTheme.typography.bodyMedium,
                            color = BetabaseTheme.colors.ink,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GradeTableHeader(
    systems: List<GradeSystem>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(BetabaseTheme.colors.ink)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        systems.forEach { system ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                BetaText(
                    text = system.shortName.uppercase(),
                    style = BetabaseTheme.typography.labelSmall,
                    color = BetabaseTheme.colors.inkInverse,
                    textAlign = TextAlign.Center,
                )
                BetaText(
                    text = system.regionHint,
                    style = BetabaseTheme.typography.bodySmall,
                    color = BetabaseTheme.colors.inkInverse.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun SystemBlurbCard(
    system: GradeSystem,
    modifier: Modifier = Modifier,
) {
    BetaCard(
        modifier = modifier.fillMaxWidth(),
        background = BetabaseTheme.colors.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(BetabaseTheme.colors.surfaceMuted)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    BetaText(
                        text = system.shortName.uppercase(),
                        style = BetabaseTheme.typography.labelSmall,
                        color = BetabaseTheme.colors.ink,
                    )
                }
                BetaText(
                    text = system.fullName,
                    style = BetabaseTheme.typography.titleSmall,
                    color = BetabaseTheme.colors.ink,
                    modifier = Modifier.weight(1f),
                )
                BetaText(
                    text = system.regionHint,
                    style = BetabaseTheme.typography.bodySmall,
                    color = BetabaseTheme.colors.inkMuted,
                )
            }
            BetaText(
                text = system.description,
                style = BetabaseTheme.typography.bodyMedium,
                color = BetabaseTheme.colors.inkMuted,
            )
        }
    }
}

@Composable
private fun HoldTypeCard(
    hold: HoldType,
    modifier: Modifier = Modifier,
) {
    BetaCard(
        modifier = modifier.fillMaxWidth(),
        background = BetabaseTheme.colors.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            BetaText(
                text = hold.name,
                style = BetabaseTheme.typography.titleSmall,
                color = BetabaseTheme.colors.ink,
            )
            BetaText(
                text = hold.description,
                style = BetabaseTheme.typography.bodyMedium,
                color = BetabaseTheme.colors.inkMuted,
            )
        }
    }
}
