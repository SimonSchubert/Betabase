package com.inspiredandroid.betabase.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.inspiredandroid.betabase.data.Athlete
import com.inspiredandroid.betabase.data.AthleteGender
import com.inspiredandroid.betabase.data.AthleteVideosRepository
import com.inspiredandroid.betabase.data.MedalCount
import com.inspiredandroid.betabase.data.YoutubeVideo
import com.inspiredandroid.betabase.data.flagEmoji
import com.inspiredandroid.betabase.data.isActive
import com.inspiredandroid.betabase.data.today
import com.inspiredandroid.betabase.ui.components.BetaButton
import com.inspiredandroid.betabase.ui.components.BetaCard
import com.inspiredandroid.betabase.ui.components.BetaChip
import com.inspiredandroid.betabase.ui.components.BetaOutlineButton
import com.inspiredandroid.betabase.ui.components.BetaPill
import com.inspiredandroid.betabase.ui.components.BetaSearchField
import com.inspiredandroid.betabase.ui.components.BetaText
import com.inspiredandroid.betabase.ui.components.PlatformVerticalScrollbar
import com.inspiredandroid.betabase.ui.theme.BetabaseTheme
import com.inspiredandroid.betabase.ui.util.FixedInspectionNow
import com.inspiredandroid.betabase.ui.util.formatRelativePast
import com.inspiredandroid.betabase.ui.util.rememberNow
import kotlin.time.Instant
import androidx.compose.foundation.lazy.grid.items as gridItems

private val ScreenSidePadding = 20.dp
private val Gold = Color(0xFFD4AF37)
private val Silver = Color(0xFFB8B8B8)
private val Bronze = Color(0xFFB87333)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AthletesScreen(
    modifier: Modifier = Modifier,
    videosRepository: AthleteVideosRepository? = null,
    selectedAthleteId: String? = null,
    onSelectAthlete: (String?) -> Unit = {},
    onBack: (() -> Unit)? = null,
) {
    val viewModel = viewModel { AthletesViewModel(videosRepository = videosRepository) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val selected = remember(selectedAthleteId, state.athletes) {
        selectedAthleteId?.let { id -> state.athletes.firstOrNull { it.id == id } }
    }

    // System back leaves the athletes stack only when no detail is open.
    if (onBack != null && selected == null) {
        BackHandler(enabled = true, onBack = onBack)
    }

    Box(modifier = modifier.fillMaxSize()) {
        AthletesScreenContent(
            state = state,
            onToggleGender = viewModel::toggle,
            onSelectCountry = viewModel::selectCountry,
            onQueryChange = viewModel::setQuery,
            onToggleInactive = viewModel::toggleInactive,
            onOpen = { onSelectAthlete(it.id) },
            onBack = onBack,
        )
        if (selected != null) {
            AthleteDetail(
                athlete = selected,
                currentYear = state.currentYear,
                videos = selected.youtubeChannelId?.let { state.videosByChannel[it] },
                onRequestVideos = { selected.youtubeChannelId?.let(viewModel::ensureVideos) },
                onBack = { onSelectAthlete(null) },
            )
        }
    }
}

/** Stateless grid for tests / screenshots. Does not host the detail sheet. */
@Composable
fun AthletesScreenContent(
    state: AthletesUiState,
    onToggleGender: (AthleteGender) -> Unit = {},
    onSelectCountry: (String?) -> Unit = {},
    onQueryChange: (String) -> Unit = {},
    onToggleInactive: () -> Unit = {},
    onOpen: (Athlete) -> Unit = {},
    onBack: (() -> Unit)? = null,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        ImageBackground()
        AthletesGrid(
            state = state,
            onToggleGender = onToggleGender,
            onSelectCountry = onSelectCountry,
            onQueryChange = onQueryChange,
            onToggleInactive = onToggleInactive,
            onOpen = onOpen,
            onBack = onBack,
        )
    }
}

@Composable
private fun AthletesGrid(
    state: AthletesUiState,
    onToggleGender: (AthleteGender) -> Unit,
    onSelectCountry: (String?) -> Unit,
    onQueryChange: (String) -> Unit,
    onToggleInactive: () -> Unit,
    onOpen: (Athlete) -> Unit,
    onBack: (() -> Unit)? = null,
) {
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val sidePadding = Modifier.padding(horizontal = ScreenSidePadding)
    val gridState = rememberLazyGridState()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 108.dp),
            state = gridState,
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars),
            contentPadding = PaddingValues(
                top = 12.dp,
                bottom = 32.dp + bottomInset,
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp - ScreenSidePadding * 2),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val fullSpan: LazyGridItemSpanScope.() -> GridItemSpan = { GridItemSpan(maxLineSpan) }

            item(span = fullSpan, key = "header") {
                Header(
                    total = state.athletes.size,
                    visible = state.filtered.size,
                    onBack = onBack,
                    modifier = sidePadding,
                )
            }
            item(span = fullSpan, key = "search") {
                Box(modifier = sidePadding) {
                    BetaSearchField(
                        value = state.filters.query,
                        onValueChange = onQueryChange,
                        placeholder = "Search by name or country",
                    )
                }
            }
            item(span = fullSpan, key = "gender") {
                GenderRow(
                    selected = state.filters.genders,
                    showInactive = state.filters.showInactive,
                    onToggle = onToggleGender,
                    onToggleInactive = onToggleInactive,
                )
            }
            if (state.countries.isNotEmpty()) {
                item(span = fullSpan, key = "country") {
                    CountryRow(
                        countries = state.countries,
                        selected = state.filters.country,
                        onSelect = onSelectCountry,
                    )
                }
            }

            if (state.filtered.isEmpty() && !state.loading) {
                item(span = fullSpan, key = "empty") {
                    Box(modifier = sidePadding) {
                        BetaText(
                            text = if (state.athletes.isEmpty()) {
                                "No athletes loaded."
                            } else {
                                "No athletes match the current filters."
                            },
                            style = BetabaseTheme.typography.bodySmall,
                            color = BetabaseTheme.colors.inkMuted,
                        )
                    }
                }
                return@LazyVerticalGrid
            }

            gridItems(state.filtered, key = { it.id }) { athlete ->
                Box(modifier = sidePadding) {
                    AthleteTile(athlete = athlete, currentYear = state.currentYear, onClick = { onOpen(athlete) })
                }
            }
        }
        PlatformVerticalScrollbar(
            gridState,
            Modifier
                .align(Alignment.CenterEnd)
                .windowInsetsPadding(WindowInsets.statusBars)
                .fillMaxHeight(),
        )
    }
}

@Composable
private fun Header(
    total: Int,
    visible: Int,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (onBack != null) {
                BackButton(onBack = onBack)
            }
            BetaText(
                text = "ATHLETES",
                style = BetabaseTheme.typography.displayMedium,
                color = BetabaseTheme.colors.ink,
                modifier = Modifier.weight(1f),
            )
        }
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

@Composable
private fun GenderRow(
    selected: Set<AthleteGender>,
    showInactive: Boolean,
    onToggle: (AthleteGender) -> Unit,
    onToggleInactive: () -> Unit,
) {
    val colors = BetabaseTheme.colors
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = ScreenSidePadding),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "women") {
            BetaChip(
                label = "Women",
                selected = AthleteGender.WOMEN in selected,
                activeColor = colors.women,
                onClick = { onToggle(AthleteGender.WOMEN) },
            )
        }
        item(key = "men") {
            BetaChip(
                label = "Men",
                selected = AthleteGender.MEN in selected,
                activeColor = colors.men,
                onClick = { onToggle(AthleteGender.MEN) },
            )
        }
        item(key = "retired") {
            BetaChip(
                label = "Include retired",
                selected = showInactive,
                activeColor = colors.ink,
                onClick = onToggleInactive,
            )
        }
    }
}

@Composable
private fun CountryRow(
    countries: List<CountryFacet>,
    selected: String?,
    onSelect: (String?) -> Unit,
) {
    val colors = BetabaseTheme.colors
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = ScreenSidePadding),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "__all") {
            BetaChip(
                label = "🌍 All",
                selected = selected == null,
                activeColor = colors.ink,
                onClick = { onSelect(null) },
            )
        }
        items(countries, key = { it.name }) { facet ->
            val flag = facet.code?.let { flagEmoji(it) }.orEmpty()
            BetaChip(
                label = "$flag ${facet.name} ${facet.count}".trim(),
                selected = selected == facet.name,
                activeColor = colors.ink,
                onClick = { onSelect(facet.name) },
            )
        }
    }
}

@Composable
private fun AthleteTile(athlete: Athlete, currentYear: Int, onClick: () -> Unit) {
    val colors = BetabaseTheme.colors
    BetaCard(
        modifier = Modifier.fillMaxWidth(),
        shape = BetabaseTheme.shapes.card,
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier.padding(8.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f)) {
                TilePhoto(photoUrl = athlete.photoUrl, initials = athlete.initials())
                if (!athlete.isActive(currentYear)) {
                    Box(modifier = Modifier.align(Alignment.TopStart).padding(6.dp)) {
                        BetaPill(
                            label = "RETIRED",
                            background = colors.surfaceMuted,
                            onColor = colors.inkMuted,
                        )
                    }
                }
                if (athlete.totalGolds > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(6.dp)
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(Gold),
                        contentAlignment = Alignment.Center,
                    ) {
                        BetaText(
                            text = athlete.totalGolds.toString(),
                            style = BetabaseTheme.typography.labelSmall,
                            color = colors.ink,
                        )
                    }
                }
            }
            BetaText(
                text = athlete.fullName,
                style = BetabaseTheme.typography.titleSmall,
                color = colors.ink,
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
            athlete.countryCode?.let { code ->
                BetaText(
                    text = "${flagEmoji(code)} ${athlete.country.orEmpty()}".trim(),
                    style = BetabaseTheme.typography.bodySmall,
                    color = colors.inkMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun TilePhoto(photoUrl: String?, initials: String) {
    val colors = BetabaseTheme.colors
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(shape)
            .background(colors.surfaceMuted),
        contentAlignment = Alignment.Center,
    ) {
        BetaText(
            text = initials,
            style = BetabaseTheme.typography.titleLarge,
            color = colors.inkMuted,
        )
        if (photoUrl != null) {
            AsyncImage(
                model = photoUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(shape),
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun AthleteDetail(
    athlete: Athlete,
    currentYear: Int,
    videos: List<YoutubeVideo>?,
    onRequestVideos: () -> Unit,
    onBack: () -> Unit,
) {
    BackHandler(enabled = true, onBack = onBack)
    val uriHandler = LocalUriHandler.current
    val colors = BetabaseTheme.colors
    val sidePadding = Modifier.padding(horizontal = ScreenSidePadding)
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val channelId = athlete.youtubeChannelId
    if (channelId != null) {
        LaunchedEffect(channelId) { onRequestVideos() }
    }

    val detailListState = rememberLazyListState()
    Box(modifier = Modifier.fillMaxSize().background(colors.background)) {
        ImageBackground()
        LazyColumn(
            state = detailListState,
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars),
            contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp + bottomInset),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item("topbar") {
                Row(
                    modifier = sidePadding.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BackButton(onBack = onBack)
                }
            }
            item("hero") {
                AthleteHero(athlete = athlete, currentYear = currentYear, modifier = sidePadding)
            }
            athlete.lastGold?.let { gold ->
                item("lastgold") {
                    Row(
                        modifier = sidePadding,
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
                        )
                    }
                }
            }
            athlete.lastCompeted?.let { season ->
                item("lastseason") {
                    Row(
                        modifier = sidePadding,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        BetaText(
                            text = "LAST SEASON",
                            style = BetabaseTheme.typography.labelSmall,
                            color = colors.inkMuted,
                        )
                        BetaText(
                            text = season.toString(),
                            style = BetabaseTheme.typography.bodySmall,
                            color = colors.ink,
                        )
                    }
                }
            }
            item("titles") {
                CareerTitles(athlete = athlete, modifier = sidePadding)
            }
            item("medals") {
                MedalsRow(athlete = athlete, modifier = sidePadding)
            }
            if (!videos.isNullOrEmpty()) {
                item("videos") {
                    Column(modifier = sidePadding) {
                        AthleteVideosRow(videos = videos)
                    }
                }
            }
            val profileLink = athlete.wikiUrl ?: athlete.ifscProfileUrl
            if (profileLink != null) {
                item("profile") {
                    BetaButton(
                        label = if (athlete.wikiUrl != null) "View on Wikipedia" else "View IFSC profile",
                        onClick = { runCatching { uriHandler.openUri(profileLink) } },
                        modifier = sidePadding.fillMaxWidth(),
                    )
                }
            }
            if (athlete.xHandle != null) {
                val xUrl = "https://x.com/${athlete.xHandle}"
                item("x") {
                    BetaOutlineButton(
                        label = "View on 𝕏",
                        onClick = { runCatching { uriHandler.openUri(xUrl) } },
                        modifier = sidePadding.fillMaxWidth(),
                    )
                }
            }
        }
        PlatformVerticalScrollbar(
            detailListState,
            Modifier
                .align(Alignment.CenterEnd)
                .windowInsetsPadding(WindowInsets.statusBars)
                .fillMaxHeight(),
        )
    }
}

@Composable
private fun AthleteHero(athlete: Athlete, currentYear: Int, modifier: Modifier = Modifier) {
    val colors = BetabaseTheme.colors
    val age = athlete.ageOn(today())
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AthletePhoto(photoUrl = athlete.photoUrl, initials = athlete.initials(), size = 128.dp)
        BetaText(
            text = athlete.fullName,
            style = BetabaseTheme.typography.displaySmall,
            color = colors.ink,
            textAlign = TextAlign.Center,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val genderLabel = if (athlete.gender == AthleteGender.WOMEN) "W" else "M"
            val genderColor = if (athlete.gender == AthleteGender.WOMEN) colors.women else colors.men
            BetaPill(label = genderLabel, background = genderColor, onColor = colors.inkInverse)
            if (!athlete.isActive(currentYear)) {
                BetaPill(label = "RETIRED", background = colors.surfaceMuted, onColor = colors.inkMuted)
            }
            val country = athlete.country?.takeIf { it.isNotBlank() }?.let { name ->
                athlete.countryCode?.let { "${flagEmoji(it)} $name" } ?: name
            }
            val subtitle = listOfNotNull(country, age?.let { "$it y/o" }).joinToString(" · ")
            if (subtitle.isNotEmpty()) {
                BetaText(
                    text = subtitle,
                    style = BetabaseTheme.typography.bodyMedium,
                    color = colors.inkMuted,
                )
            }
        }
        if (athlete.totalGolds > 0) {
            GoldsBadge(total = athlete.totalGolds)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CareerTitles(athlete: Athlete, modifier: Modifier = Modifier) {
    val colors = BetabaseTheme.colors
    val titles = buildList {
        if (athlete.boulderTitles > 0) add(Triple("Boulder", athlete.boulderTitles, colors.boulder))
        if (athlete.leadTitles > 0) add(Triple("Lead", athlete.leadTitles, colors.lead))
        if (athlete.speedTitles > 0) add(Triple("Speed", athlete.speedTitles, colors.speed))
        if (athlete.combinedTitles > 0) add(Triple("Combined", athlete.combinedTitles, colors.combined))
    }
    if (titles.isEmpty()) return
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        BetaText(
            text = "CAREER TITLES",
            style = BetabaseTheme.typography.labelSmall,
            color = colors.inkMuted,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            titles.forEach { (label, count, color) -> TitleChip(label = label, count = count, color = color) }
        }
    }
}

@Composable
private fun TitleChip(label: String, count: Int, color: Color) {
    val colors = BetabaseTheme.colors
    Row(
        modifier = Modifier
            .clip(BetabaseTheme.shapes.pill)
            .background(colors.surfaceMuted)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
        BetaText(
            text = label.uppercase(),
            style = BetabaseTheme.typography.labelSmall,
            color = colors.ink,
        )
        BetaText(
            text = count.toString(),
            style = BetabaseTheme.typography.labelSmall,
            color = colors.ink,
        )
    }
}

@Composable
private fun BackButton(onBack: () -> Unit) {
    val colors = BetabaseTheme.colors
    BetaCard(
        modifier = Modifier.size(40.dp),
        shape = CircleShape,
        onClick = onBack,
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            BackGlyph(tint = colors.ink)
        }
    }
}

@Composable
private fun BackGlyph(tint: Color) {
    Canvas(modifier = Modifier.size(16.dp)) {
        val w = size.width
        val h = size.height
        val stroke = w / 8f
        drawLine(tint, Offset(w * 0.6f, h * 0.2f), Offset(w * 0.32f, h * 0.5f), stroke, cap = StrokeCap.Round)
        drawLine(tint, Offset(w * 0.32f, h * 0.5f), Offset(w * 0.6f, h * 0.8f), stroke, cap = StrokeCap.Round)
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
private fun AthletePhoto(photoUrl: String?, initials: String, size: androidx.compose.ui.unit.Dp = 64.dp) {
    val colors = BetabaseTheme.colors
    Box(
        modifier = Modifier
            .size(size)
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
                modifier = Modifier.size(size).clip(CircleShape),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MedalsRow(athlete: Athlete, modifier: Modifier = Modifier) {
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
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
