package com.inspiredandroid.betabase.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.inspiredandroid.betabase.data.Discipline
import com.inspiredandroid.betabase.data.IfscVideo
import com.inspiredandroid.betabase.ui.components.BetaCard
import com.inspiredandroid.betabase.ui.components.BetaPill
import com.inspiredandroid.betabase.ui.components.BetaText
import com.inspiredandroid.betabase.ui.theme.BetabaseTheme
import com.inspiredandroid.betabase.ui.util.FixedInspectionNow
import com.inspiredandroid.betabase.ui.util.formatRelativePast
import com.inspiredandroid.betabase.ui.util.formatRelativeStart
import com.inspiredandroid.betabase.ui.util.rememberNow
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

@Composable
fun StreamsCarousel(
    streams: List<IfscVideo>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    headerModifier: Modifier = Modifier,
) {
    val inInspection = LocalInspectionMode.current
    val tickedNow by rememberNow()
    val now = if (inInspection) FixedInspectionNow else tickedNow

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionHeader(count = streams.size, modifier = headerModifier)
        LazyRow(
            contentPadding = contentPadding,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(streams, key = { it.id }) { video ->
                StreamCard(video = video, now = now)
            }
        }
    }
}

@Composable
private fun SectionHeader(count: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        BetaText(
            text = "STREAMS",
            style = BetabaseTheme.typography.label,
            color = BetabaseTheme.colors.ink,
        )
        BetaText(
            text = "$count from IFSC",
            style = BetabaseTheme.typography.labelSmall,
            color = BetabaseTheme.colors.inkMuted,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StreamCard(
    video: IfscVideo,
    now: Instant,
) {
    val accent = disciplineColor(video.discipline)
    val uriHandler = LocalUriHandler.current
    val onClick = remember(video.watchUrl, uriHandler) {
        val url = video.watchUrl
        val handler: () -> Unit = { runCatching { uriHandler.openUri(url) } }
        handler
    }
    val (label, subtitle) = remember(video.title) { splitTitle(video.title) }
    val status = remember(video, now) { videoStatus(video, now) }

    BetaCard(
        modifier = Modifier.width(260.dp),
        background = accent,
        shape = BetabaseTheme.shapes.card,
        onClick = onClick,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(146.dp),
        ) {
            // mqdefault is 320×180 (16:9). Card is also 16:9 so Crop fits cleanly.
            // Loading/error: nothing renders, so the discipline-colored card shows through.
            AsyncImage(
                model = video.thumbnailUrl,
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
                            0.0f to Color.Black.copy(alpha = 0.45f),
                            0.4f to Color.Transparent,
                            0.6f to Color.Transparent,
                            1.0f to Color.Black.copy(alpha = 0.75f),
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
                val publishedLabel = remember(video.publishedAt, now) {
                    if (status is VideoStatus.Replay) formatRelativePast(video.publishedAt, now) else null
                }
                StatusRow(status = status, publishedLabel = publishedLabel)

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    BetaText(
                        text = label,
                        style = BetabaseTheme.typography.titleMedium,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (subtitle != null) {
                        BetaText(
                            text = subtitle,
                            style = BetabaseTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.85f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusRow(status: VideoStatus, publishedLabel: String?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        StatusPill(status = status)
        if (publishedLabel != null) {
            BetaPill(
                label = publishedLabel,
                background = Color.Black.copy(alpha = 0.55f),
                onColor = Color.White,
            )
        }
    }
}

@Composable
private fun StatusPill(status: VideoStatus) {
    val (label, isLive) = when (status) {
        is VideoStatus.Live -> "Live now" to true
        is VideoStatus.Upcoming -> (status.relative ?: "Upcoming") to false
        is VideoStatus.Replay -> "Replay · ${status.duration}" to false
    }
    val background = if (isLive) BetabaseTheme.colors.accent else Color.Black.copy(alpha = 0.65f)
    val onBackground = if (isLive) BetabaseTheme.colors.onAccent else Color.White
    BetaPill(label = label, background = background, onColor = onBackground)
}

private sealed class VideoStatus {
    data object Live : VideoStatus()
    data class Upcoming(val relative: String?) : VideoStatus()
    data class Replay(val duration: String) : VideoStatus()
}

private fun videoStatus(video: IfscVideo, now: Instant): VideoStatus {
    val scheduled = video.scheduledStartTime
    if (video.durationSeconds == 0 && scheduled != null) {
        val delta = scheduled - now
        return if (delta < 0.hours && delta > (-3).hours) {
            VideoStatus.Live
        } else {
            VideoStatus.Upcoming(formatRelativeStart(scheduled, now))
        }
    }
    return VideoStatus.Replay(formatDuration(video.durationSeconds))
}

private fun formatDuration(seconds: Int): String {
    if (seconds <= 0) return "—"
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return if (hours > 0) {
        "${hours}h ${minutes.toString().padStart(2, '0')}m"
    } else {
        "$minutes:${secs.toString().padStart(2, '0')}"
    }
}

private fun splitTitle(title: String): Pair<String, String?> {
    val idx = title.indexOf('|')
    if (idx <= 0) return title to null
    val head = title.substring(0, idx).trim()
    val tail = title.substring(idx + 1).trim().takeIf { it.isNotEmpty() }
    return head to tail
}

@Composable
private fun disciplineColor(discipline: Discipline): Color = when (discipline) {
    Discipline.BOULDER -> BetabaseTheme.colors.boulder
    Discipline.LEAD -> BetabaseTheme.colors.lead
    Discipline.SPEED -> BetabaseTheme.colors.speed
    Discipline.COMBINED -> BetabaseTheme.colors.combined
    Discipline.OTHER -> BetabaseTheme.colors.ink
}
