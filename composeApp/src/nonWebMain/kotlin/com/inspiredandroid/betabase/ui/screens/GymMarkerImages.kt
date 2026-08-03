package com.inspiredandroid.betabase.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalDensity
import coil3.SingletonImageLoader
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import com.inspiredandroid.betabase.data.Gym
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

private const val MARKER_DECODE_PX = 96
private const val MAX_CONCURRENT_FETCHES = 8

/**
 * Process-scoped photo markers so leaving/re-entering the map tab does not drop
 * bitmaps and force MapLibre to rebuild the icon expression (which flashes pins).
 *
 * Only written from the composition main thread inside [rememberGymPhotoMarkerBitmaps].
 */
private data class MarkerCacheKey(val gymId: String, val imageUrl: String)

private val processMarkerBitmaps = mutableMapOf<MarkerCacheKey, ImageBitmap>()

@Composable
fun rememberGymPhotoMarkerBitmaps(gyms: List<Gym>): Map<String, ImageBitmap> {
    val context = LocalPlatformContext.current
    val density = LocalDensity.current

    val targets = remember(gyms) {
        gyms.mapNotNull { gym ->
            val url = gym.imageUrl?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            MarkerCacheKey(gym.id, url)
        }
    }

    // Seed from process cache so remounts paint photos on the first frame.
    // Map is accumulated across filter changes so the SymbolLayer icon expression
    // stays content-stable when the visible gym set shrinks/grows.
    var bitmaps by remember {
        mutableStateOf(snapshotMarkerBitmaps(targets))
    }

    LaunchedEffect(targets) {
        val staleIds = dropStaleUrls(targets)
        var next = bitmaps
        if (staleIds.isNotEmpty()) {
            next = next - staleIds
        }

        val cached = snapshotMarkerBitmaps(targets)
        if (cached.isNotEmpty()) {
            next = next + cached
        }
        if (next != bitmaps) {
            bitmaps = next
        }

        val missing = targets.filter { it !in processMarkerBitmaps }
        if (missing.isEmpty()) return@LaunchedEffect

        val loader = SingletonImageLoader.get(context)
        val gate = Semaphore(MAX_CONCURRENT_FETCHES)

        // Decode off the main thread. Do not publish Compose state per image —
        // each publish rebuilds the MapLibre icon switch and flashes every pin.
        val loaded = coroutineScope {
            missing.map { key ->
                async(Dispatchers.Default) {
                    gate.withPermit {
                        fetchMarkerBitmap(
                            key = key,
                            loader = loader,
                            context = context,
                            density = density,
                        )
                    }
                }
            }.awaitAll()
        }.filterNotNull()

        if (loaded.isEmpty()) return@LaunchedEffect

        for ((key, bitmap) in loaded) {
            processMarkerBitmaps[key] = bitmap
        }
        next = bitmaps + loaded.associate { (key, bitmap) -> key.gymId to bitmap }
        if (next != bitmaps) {
            bitmaps = next
        }
    }

    return bitmaps
}

private suspend fun fetchMarkerBitmap(
    key: MarkerCacheKey,
    loader: coil3.ImageLoader,
    context: coil3.PlatformContext,
    density: androidx.compose.ui.unit.Density,
): Pair<MarkerCacheKey, ImageBitmap>? {
    val result = runCatching {
        loader.execute(
            ImageRequest.Builder(context)
                .data(key.imageUrl)
                .size(MARKER_DECODE_PX)
                .configureMarkerRequest()
                .build(),
        )
    }.getOrNull()
    if (result !is SuccessResult) return null
    val markerBitmap = coilImageToMarkerBitmap(
        image = result.image,
        sizePx = MARKER_DECODE_PX,
        platformContext = context,
        density = density,
    )
    return if (markerBitmap != null && isValidMarkerBitmap(markerBitmap)) {
        key to markerBitmap
    } else {
        null
    }
}

/** gymId → bitmap for keys that are present in the process cache. */
private fun snapshotMarkerBitmaps(targets: List<MarkerCacheKey>): Map<String, ImageBitmap> {
    if (targets.isEmpty()) return emptyMap()
    return buildMap(targets.size) {
        for (key in targets) {
            processMarkerBitmaps[key]?.let { put(key.gymId, it) }
        }
    }
}

/**
 * If a gym's imageUrl changed, drop the old cache entry so we do not keep showing
 * the previous photo. Returns gym ids that lost their cached bitmap.
 */
private fun dropStaleUrls(targets: List<MarkerCacheKey>): Set<String> {
    if (targets.isEmpty() || processMarkerBitmaps.isEmpty()) return emptySet()
    val desired = targets.associate { it.gymId to it.imageUrl }
    val stale = processMarkerBitmaps.keys.filter { key ->
        val url = desired[key.gymId]
        url != null && url != key.imageUrl
    }
    if (stale.isEmpty()) return emptySet()
    stale.forEach { processMarkerBitmaps.remove(it) }
    return stale.map { it.gymId }.toSet()
}
