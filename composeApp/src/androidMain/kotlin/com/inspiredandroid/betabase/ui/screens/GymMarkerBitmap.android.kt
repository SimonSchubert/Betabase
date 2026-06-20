package com.inspiredandroid.betabase.ui.screens

import android.graphics.Bitmap
import android.util.DisplayMetrics
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.Density
import coil3.Image
import coil3.PlatformContext
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.toBitmap

internal actual fun ImageRequest.Builder.configureMarkerRequest(): ImageRequest.Builder =
    allowHardware(false)

internal actual fun coilImageToMarkerBitmap(
    image: Image,
    sizePx: Int,
    platformContext: PlatformContext,
    density: Density,
): ImageBitmap? =
    runCatching {
        val bitmap = image.toBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        if (bitmap.width <= 0 || bitmap.height <= 0) return null
        // MapLibre derives pixelRatio from bitmap.density; Coil leaves it at 0 which crashes nativeAddImages.
        if (bitmap.density <= 0) {
            bitmap.density = platformContext.resources.displayMetrics.densityDpi
                .takeIf { it > 0 }
                ?: DisplayMetrics.DENSITY_DEFAULT
        }
        bitmap.asImageBitmap()
    }.getOrNull()