package com.inspiredandroid.betabase.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.inspiredandroid.betabase.data.CompetitionEvent
import com.inspiredandroid.betabase.data.Discipline
import com.inspiredandroid.betabase.data.Gender
import com.inspiredandroid.betabase.data.Round
import com.inspiredandroid.betabase.ui.theme.BetabaseTheme

@Composable
fun CompetitionCard(
    event: CompetitionEvent,
    modifier: Modifier = Modifier,
) {
    val accent = disciplineColor(event.discipline)
    val uriHandler = LocalUriHandler.current
    val url = event.url
    val onClick: (() -> Unit)? = if (url != null) {
        remember(url, uriHandler) {
            val handler: () -> Unit = { runCatching { uriHandler.openUri(url) } }
            handler
        }
    } else {
        null
    }
    BetaCard(
        modifier = modifier.fillMaxWidth(),
        shape = BetabaseTheme.shapes.card,
        background = BetabaseTheme.colors.surface,
        onClick = onClick,
    ) {
        Row(modifier = Modifier.heightIn(min = 120.dp)) {
            Box(
                modifier = Modifier
                    .width(10.dp)
                    .fillMaxHeight()
                    .background(accent),
            )
            Column(
                modifier = Modifier
                    .padding(horizontal = 18.dp, vertical = 16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    BetaText(
                        text = if (event.allDay) "All day" else formatHourMinute(event.start.hour, event.start.minute),
                        style = BetabaseTheme.typography.titleMedium,
                        color = BetabaseTheme.colors.ink,
                    )
                    DotSeparator()
                    BetaText(
                        text = event.location.ifBlank { "—" },
                        style = BetabaseTheme.typography.bodySmall,
                        color = BetabaseTheme.colors.inkMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    BetaText(
                        text = event.source.regionLabel,
                        style = BetabaseTheme.typography.labelSmall,
                        color = BetabaseTheme.colors.inkMuted,
                    )
                }

                BetaText(
                    text = event.title,
                    style = BetabaseTheme.typography.titleLarge,
                    color = BetabaseTheme.colors.ink,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                if (!event.series.isNullOrBlank() && event.series != event.title) {
                    BetaText(
                        text = event.series,
                        style = BetabaseTheme.typography.bodySmall,
                        color = BetabaseTheme.colors.inkMuted,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Spacer(Modifier.height(2.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    BetaPill(
                        label = event.discipline.label(),
                        background = accent,
                        onColor = onDiscipline(event.discipline),
                    )
                    BetaPill(
                        label = event.round.label(),
                        background = roundBackground(event.round),
                        onColor = roundForeground(event.round),
                    )
                    BetaPill(
                        label = event.gender.label(),
                        background = BetabaseTheme.colors.ink,
                        onColor = BetabaseTheme.colors.inkInverse,
                    )
                }
            }
        }
    }
}

private fun formatHourMinute(hour: Int, minute: Int): String = "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"

@Composable
private fun DotSeparator() {
    Box(
        modifier = Modifier
            .width(4.dp)
            .height(4.dp)
            .clip(BetabaseTheme.shapes.pill)
            .background(BetabaseTheme.colors.hairline),
    )
}

@Composable
private fun disciplineColor(discipline: Discipline): Color = when (discipline) {
    Discipline.BOULDER -> BetabaseTheme.colors.boulder
    Discipline.LEAD -> BetabaseTheme.colors.lead
    Discipline.SPEED -> BetabaseTheme.colors.speed
    Discipline.COMBINED -> BetabaseTheme.colors.combined
    Discipline.OTHER -> BetabaseTheme.colors.inkMuted
}

@Composable
private fun onDiscipline(discipline: Discipline): Color = when (discipline) {
    Discipline.SPEED -> BetabaseTheme.colors.ink
    else -> BetabaseTheme.colors.inkInverse
}

@Composable
private fun roundBackground(round: Round): Color = when (round) {
    Round.FINAL -> BetabaseTheme.colors.finalRound
    Round.SEMIFINAL -> BetabaseTheme.colors.semiRound
    Round.QUALIFICATION -> BetabaseTheme.colors.qualRound
    Round.OTHER -> BetabaseTheme.colors.surfaceMuted
}

@Composable
private fun roundForeground(round: Round): Color = when (round) {
    Round.FINAL -> BetabaseTheme.colors.onFinalRound
    else -> BetabaseTheme.colors.ink
}

private fun Discipline.label() = when (this) {
    Discipline.BOULDER -> "Boulder"
    Discipline.LEAD -> "Lead"
    Discipline.SPEED -> "Speed"
    Discipline.COMBINED -> "Combined"
    Discipline.OTHER -> "Other"
}

private fun Round.label() = when (this) {
    Round.FINAL -> "Final"
    Round.SEMIFINAL -> "Semi"
    Round.QUALIFICATION -> "Qualifier"
    Round.OTHER -> "Round"
}

private fun Gender.label() = when (this) {
    Gender.WOMEN -> "Women"
    Gender.MEN -> "Men"
    Gender.YOUTH -> "Youth"
    Gender.MIXED -> "Open"
}
