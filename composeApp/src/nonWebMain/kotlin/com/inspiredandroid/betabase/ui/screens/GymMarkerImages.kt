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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit

private const val MARKER_DECODE_PX = 96
private const val MAX_CONCURRENT_FETCHES = 8

@Composable
fun rememberGymPhotoMarkerBitmaps(gyms: List<Gym>): Map<String, ImageBitmap> {
    val context = LocalPlatformContext.current
    val density = LocalDensity.current
    var bitmaps by remember { mutableStateOf<Map<String, ImageBitmap>>(emptyMap()) }

    LaunchedEffect(gyms.map { it.id to it.imageUrl }) {
        val targets = gyms.mapNotNull { gym -> gym.imageUrl?.let { gym.id to it } }
        bitmaps = emptyMap()
        if (targets.isEmpty()) return@LaunchedEffect

        val loader = SingletonImageLoader.get(context)
        val gate = Semaphore(MAX_CONCURRENT_FETCHES)
        val mutex = Mutex()

        coroutineScope {
            targets.map { (gymId, url) ->
                async(Dispatchers.Default) {
                    gate.withPermit {
                        val result = runCatching {
                            loader.execute(
                                ImageRequest.Builder(context)
                                    .data(url)
                                    .size(MARKER_DECODE_PX)
                                    .configureMarkerRequest()
                                    .build(),
                            )
                        }.getOrNull()
                        if (result is SuccessResult) {
                            val markerBitmap = coilImageToMarkerBitmap(
                                image = result.image,
                                sizePx = MARKER_DECODE_PX,
                                platformContext = context,
                                density = density,
                            )
                            if (markerBitmap != null && isValidMarkerBitmap(markerBitmap)) {
                                mutex.withLock {
                                    bitmaps = bitmaps + (gymId to markerBitmap)
                                }
                            }
                        }
                    }
                }
            }.awaitAll()
        }
    }

    return bitmaps
}