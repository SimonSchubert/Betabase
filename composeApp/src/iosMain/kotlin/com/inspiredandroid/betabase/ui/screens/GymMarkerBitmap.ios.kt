package com.inspiredandroid.betabase.ui.screens

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import coil3.Image
import coil3.PlatformContext
import coil3.compose.asPainter
import coil3.request.ImageRequest

internal actual fun ImageRequest.Builder.configureMarkerRequest(): ImageRequest.Builder = this

internal actual fun coilImageToMarkerBitmap(
    image: Image,
    sizePx: Int,
    platformContext: PlatformContext,
    density: Density,
): ImageBitmap? = runCatching {
    val bitmap = ImageBitmap(sizePx, sizePx)
    val painter = image.asPainter(platformContext)
    val size = Size(sizePx.toFloat(), sizePx.toFloat())
    CanvasDrawScope().draw(
        density = density,
        layoutDirection = LayoutDirection.Ltr,
        canvas = Canvas(bitmap),
        size = size,
    ) {
        with(painter) {
            draw(size)
        }
    }
    bitmap.takeIf { it.width > 0 && it.height > 0 }
}.getOrNull()
