package com.inspiredandroid.betabase.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import coil3.compose.AsyncImage
import com.inspiredandroid.betabase.data.AthleteFeedItem
import com.inspiredandroid.betabase.ui.components.BetaCard
import com.inspiredandroid.betabase.ui.components.BetaPill
import com.inspiredandroid.betabase.ui.components.BetaText
import com.inspiredandroid.betabase.ui.theme.BetabaseTheme
import com.inspiredandroid.betabase.ui.util.FixedInspectionNow
import com.inspiredandroid.betabase.ui.util.formatRelativePast
import com.inspiredandroid.betabase.ui.util.rememberNow
import kotlin.time.Instant

@Composable
fun AthleteVideosCarousel(
    items: List<AthleteFeedItem>,
    onOpenAthlete: (String) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    headerModifier: Modifier = Modifier,
) {
    val inInspection = LocalInspectionMode.current
    val tickedNow by rememberNow()
    val now = if (inInspection) FixedInspectionNow else tickedNow

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        BetaText(
            text = "ATHLETE CHANNELS",
            style = BetabaseTheme.typography.displayMedium,
            color = BetabaseTheme.colors.ink,
            modifier = headerModifier.fillMaxWidth(),
        )
        LazyRow(
            contentPadding = contentPadding,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(items, key = { "${it.athlete.id}:${it.video.id}" }) { item ->
                AthleteFeedCard(item = item, now = now, onOpenAthlete = onOpenAthlete)
            }
        }
    }
}

@Composable
private fun AthleteFeedCard(item: AthleteFeedItem, now: Instant, onOpenAthlete: (String) -> Unit) {
    val uriHandler = LocalUriHandler.current
    val onClick = remember(item.video.watchUrl, uriHandler) {
        val url = item.video.watchUrl
        val handler: () -> Unit = { runCatching { uriHandler.openUri(url) } }
        handler
    }

    BetaCard(
        modifier = Modifier.width(260.dp),
        background = BetabaseTheme.colors.ink,
        shape = BetabaseTheme.shapes.card,
        onClick = onClick,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(146.dp),
        ) {
            // 16:9 thumbnail; the ink-colored card shows through while loading/erroring.
            AsyncImage(
                model = item.video.thumbnailUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(146.dp),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(146.dp)
                    .background(
                        Brush.verticalGradient(
                            0.0f to Color.Black.copy(alpha = 0.5f),
                            0.4f to Color.Transparent,
                            0.55f to Color.Transparent,
                            1.0f to Color.Black.copy(alpha = 0.8f),
                        ),
                    ),
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(146.dp)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onOpenAthlete(item.athlete.id) },
                    ) {
                        AthleteAvatar(photoUrl = item.athlete.photoUrl, initials = item.athlete.initials())
                        BetaText(
                            text = item.athlete.fullName,
                            style = BetabaseTheme.typography.labelSmall,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    BetaPill(
                        label = formatRelativePast(item.video.publishedAt, now),
                        background = Color.Black.copy(alpha = 0.55f),
                        onColor = Color.White,
                    )
                }
                BetaText(
                    text = item.video.title,
                    style = BetabaseTheme.typography.titleMedium,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun AthleteAvatar(photoUrl: String?, initials: String) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center,
    ) {
        BetaText(
            text = initials,
            style = BetabaseTheme.typography.labelSmall,
            color = Color.White,
        )
        if (photoUrl != null) {
            AsyncImage(
                model = photoUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(28.dp).clip(CircleShape),
            )
        }
    }
}

private fun com.inspiredandroid.betabase.data.Athlete.initials(): String = listOf(firstName, lastName).mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("")
