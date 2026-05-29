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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.inspiredandroid.betabase.data.Athlete
import com.inspiredandroid.betabase.data.AthleteGender
import com.inspiredandroid.betabase.data.AthleteVideosRepository
import com.inspiredandroid.betabase.data.Discipline
import com.inspiredandroid.betabase.data.MedalCount
import com.inspiredandroid.betabase.data.YoutubeVideo
import com.inspiredandroid.betabase.data.today
import com.inspiredandroid.betabase.ui.components.BetaCard
import com.inspiredandroid.betabase.ui.components.BetaChip
import com.inspiredandroid.betabase.ui.components.BetaPill
import com.inspiredandroid.betabase.ui.components.BetaSearchField
import com.inspiredandroid.betabase.ui.components.BetaText
import com.inspiredandroid.betabase.ui.theme.BetabaseTheme
import com.inspiredandroid.betabase.ui.util.FixedInspectionNow
import com.inspiredandroid.betabase.ui.util.formatRelativePast
import com.inspiredandroid.betabase.ui.util.rememberNow
import kotlin.time.Instant

private val ScreenSidePadding = 20.dp
private val Gold = Color(0xFFD4AF37)
private val Silver = Color(0xFFB8B8B8)
private val Bronze = Color(0xFFB87333)

@Composable
fun AthletesScreen(
    modifier: Modifier = Modifier,
    videosRepository: AthleteVideosRepository? = null,
) {
    val viewModel = viewModel { AthletesViewModel(videosRepository = videosRepository) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val sidePadding = Modifier.padding(horizontal = ScreenSidePadding)

    Box(modifier = modifier.fillMaxSize()) {
        ImageBackground()
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars),
            contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp + bottomInset),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item("header") {
                Header(
                    total = state.athletes.size,
                    visible = state.filtered.size,
                    modifier = sidePadding,
                )
            }
            item("search") {
                BetaSearchField(
                    value = state.filters.query,
                    onValueChange = viewModel::setQuery,
                    placeholder = "Search by name or country",
                    modifier = sidePadding,
                )
            }
            item("filters") {
                FilterRow(
                    selectedGenders = state.filters.genders,
                    selectedDisciplines = state.filters.disciplines,
                    onToggleGender = viewModel::toggle,
                    onToggleDiscipline = viewModel::toggle,
                    modifier = sidePadding,
                )
            }

            if (state.filtered.isEmpty() && !state.loading) {
                item("empty") {
                    BetaText(
                        text = if (state.athletes.isEmpty()) {
                            "No athletes loaded."
                        } else {
                            "No athletes match the current filters."
                        },
                        style = BetabaseTheme.typography.bodySmall,
                        color = BetabaseTheme.colors.inkMuted,
                        modifier = sidePadding,
                    )
                }
                return@LazyColumn
            }

            items(state.filtered, key = { it.id }) { athlete ->
                AthleteCard(
                    athlete = athlete,
                    videos = athlete.youtubeChannelId?.let { state.videosByChannel[it] },
                    onRequestVideos = { athlete.youtubeChannelId?.let(viewModel::ensureVideos) },
                    modifier = sidePadding,
                )
            }
        }
    }
}

@Composable
private fun Header(total: Int, visible: Int, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        BetaText(
            text = "ATHLETES",
            style = BetabaseTheme.typography.displayMedium,
            color = BetabaseTheme.colors.ink,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(6.dp))
        val countLabel = when {
            total == 0 -> "Loading…"
            visible == total -> "$total IFSC career-title holders"
            else -> "$visible of $total title holders"
        }
        BetaText(
            text = countLabel,
            style = BetabaseTheme.typography.label,
            color = BetabaseTheme.colors.inkMuted,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(4.dp))
        BetaText(
            text = "Gold medals across Olympics, Worlds, World Cup events (2004+) and Euros. World Cup events before 2004 aren't on Wikipedia.",
            style = BetabaseTheme.typography.bodySmall,
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterRow(
    selectedGenders: Set<AthleteGender>,
    selectedDisciplines: Set<Discipline>,
    onToggleGender: (AthleteGender) -> Unit,
    onToggleDiscipline: (Discipline) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = BetabaseTheme.colors
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            BetaChip(
                label = "Women",
                selected = AthleteGender.WOMEN in selectedGenders,
                activeColor = colors.women,
                onClick = { onToggleGender(AthleteGender.WOMEN) },
            )
            BetaChip(
                label = "Men",
                selected = AthleteGender.MEN in selectedGenders,
                activeColor = colors.men,
                onClick = { onToggleGender(AthleteGender.MEN) },
            )
        }
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            BetaChip(
                label = "Boulder",
                selected = Discipline.BOULDER in selectedDisciplines,
                activeColor = colors.boulder,
                onClick = { onToggleDiscipline(Discipline.BOULDER) },
            )
            BetaChip(
                label = "Lead",
                selected = Discipline.LEAD in selectedDisciplines,
                activeColor = colors.lead,
                onClick = { onToggleDiscipline(Discipline.LEAD) },
            )
            BetaChip(
                label = "Speed",
                selected = Discipline.SPEED in selectedDisciplines,
                activeColor = colors.speed,
                activeOnColor = colors.ink,
                onClick = { onToggleDiscipline(Discipline.SPEED) },
            )
            BetaChip(
                label = "Combined",
                selected = Discipline.COMBINED in selectedDisciplines,
                activeColor = colors.combined,
                onClick = { onToggleDiscipline(Discipline.COMBINED) },
            )
        }
    }
}

@Composable
private fun AthleteCard(
    athlete: Athlete,
    videos: List<YoutubeVideo>?,
    onRequestVideos: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current
    val colors = BetabaseTheme.colors
    val age = athlete.ageOn(today())
    val channelId = athlete.youtubeChannelId
    if (channelId != null) {
        LaunchedEffect(channelId) { onRequestVideos() }
    }
    BetaCard(
        modifier = modifier.fillMaxWidth(),
        shape = BetabaseTheme.shapes.card,
        onClick = { runCatching { uriHandler.openUri(athlete.wikiUrl) } },
    ) {
        Column(
            modifier = Modifier.padding(14.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AthletePhoto(photoUrl = athlete.photoUrl, initials = athlete.initials())
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    BetaText(
                        text = athlete.fullName,
                        style = BetabaseTheme.typography.titleMedium,
                        color = colors.ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val genderLabel = if (athlete.gender == AthleteGender.WOMEN) "W" else "M"
                        val genderColor = if (athlete.gender == AthleteGender.WOMEN) colors.women else colors.men
                        BetaPill(
                            label = genderLabel,
                            background = genderColor,
                            onColor = colors.inkInverse,
                        )
                        val subtitle = listOfNotNull(
                            athlete.country?.takeIf { it.isNotBlank() },
                            age?.let { "$it y/o" },
                        ).joinToString(" · ")
                        if (subtitle.isNotEmpty()) {
                            BetaText(
                                text = subtitle,
                                style = BetabaseTheme.typography.bodySmall,
                                color = colors.inkMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
                GoldsBadge(total = athlete.totalGolds)
            }

            athlete.lastGold?.let { gold ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BetaText(
                        text = "LAST GOLD",
                        style = BetabaseTheme.typography.labelSmall,
                        color = colors.inkMuted,
                    )
                    BetaText(
                        text = gold.label(),
                        style = BetabaseTheme.typography.bodySmall,
                        color = colors.ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            MedalsRow(athlete = athlete)

            if (!videos.isNullOrEmpty()) {
                AthleteVideosRow(videos = videos)
            }
        }
    }
}

@Composable
private fun AthleteVideosRow(videos: List<YoutubeVideo>) {
    val inInspection = LocalInspectionMode.current
    val tickedNow by rememberNow()
    val now = if (inInspection) FixedInspectionNow else tickedNow
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        BetaText(
            text = "LATEST VIDEOS",
            style = BetabaseTheme.typography.labelSmall,
            color = BetabaseTheme.colors.inkMuted,
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(videos, key = { it.id }) { video ->
                AthleteVideoCard(video = video, now = now)
            }
        }
    }
}

@Composable
private fun AthleteVideoCard(video: YoutubeVideo, now: Instant) {
    val uriHandler = LocalUriHandler.current
    BetaCard(
        modifier = Modifier.width(180.dp),
        shape = BetabaseTheme.shapes.card,
        onClick = { runCatching { uriHandler.openUri(video.watchUrl) } },
    ) {
        // mqdefault is 320×180 (16:9); 180.dp wide → 101.dp tall keeps the ratio.
        Box(modifier = Modifier.fillMaxWidth().height(101.dp)) {
            AsyncImage(
                model = video.thumbnailUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(101.dp),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(101.dp)
                    .background(
                        Brush.verticalGradient(
                            0.0f to Color.Black.copy(alpha = 0.35f),
                            0.45f to Color.Transparent,
                            0.6f to Color.Transparent,
                            1.0f to Color.Black.copy(alpha = 0.8f),
                        ),
                    ),
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(101.dp)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Row {
                    BetaPill(
                        label = formatRelativePast(video.publishedAt, now),
                        background = Color.Black.copy(alpha = 0.55f),
                        onColor = Color.White,
                    )
                }
                BetaText(
                    text = video.title,
                    style = BetabaseTheme.typography.titleSmall,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun AthletePhoto(photoUrl: String?, initials: String) {
    val colors = BetabaseTheme.colors
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(colors.surfaceMuted),
        contentAlignment = Alignment.Center,
    ) {
        BetaText(
            text = initials,
            style = BetabaseTheme.typography.titleMedium,
            color = colors.inkMuted,
        )
        if (photoUrl != null) {
            AsyncImage(
                model = photoUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(64.dp).clip(CircleShape),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MedalsRow(athlete: Athlete) {
    val sections = buildList {
        add("Olympics" to athlete.olympic)
        add("Worlds" to athlete.worldChampionships)
        add("WC · Boulder" to athlete.worldCupBoulder)
        add("WC · Lead" to athlete.worldCupLead)
        add("WC · Speed" to athlete.worldCupSpeed)
        add("World Games" to athlete.worldGames)
        add("Euros" to athlete.europeanChampionships)
    }.filter { it.second.any }
    if (sections.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        BetaText(
            text = "MEDAL RECORD",
            style = BetabaseTheme.typography.labelSmall,
            color = BetabaseTheme.colors.inkMuted,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            sections.forEach { (label, count) -> MedalChip(label = label, medals = count) }
        }
    }
}

@Composable
private fun MedalChip(label: String, medals: MedalCount) {
    val colors = BetabaseTheme.colors
    Row(
        modifier = Modifier
            .clip(BetabaseTheme.shapes.pill)
            .background(colors.surfaceMuted)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        BetaText(
            text = label.uppercase(),
            style = BetabaseTheme.typography.labelSmall,
            color = colors.ink,
        )
        if (medals.gold > 0) MedalDot(medals.gold, Gold)
        if (medals.silver > 0) MedalDot(medals.silver, Silver)
        if (medals.bronze > 0) MedalDot(medals.bronze, Bronze)
    }
}

@Composable
private fun MedalDot(count: Int, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color),
        )
        BetaText(
            text = count.toString(),
            style = BetabaseTheme.typography.labelSmall,
            color = BetabaseTheme.colors.ink,
        )
    }
}

@Composable
private fun GoldsBadge(total: Int) {
    val colors = BetabaseTheme.colors
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Gold),
            contentAlignment = Alignment.Center,
        ) {
            BetaText(
                text = total.toString(),
                style = BetabaseTheme.typography.titleMedium,
                color = colors.ink,
            )
        }
        BetaText(
            text = "GOLDS",
            style = BetabaseTheme.typography.labelSmall,
            color = colors.inkMuted,
        )
    }
}

private fun Athlete.initials(): String = listOf(firstName, lastName).mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("")
