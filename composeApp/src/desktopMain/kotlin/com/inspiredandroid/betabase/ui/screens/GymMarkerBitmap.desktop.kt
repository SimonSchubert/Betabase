package com.inspiredandroid.betabase.ui.screens

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.Density
import coil3.Image
import coil3.PlatformContext
import coil3.request.ImageRequest

internal actual fun ImageRequest.Builder.configureMarkerRequest(): ImageRequest.Builder = this

internal actual fun coilImageToMarkerBitmap(
    image: Image,
    sizePx: Int,
    platformContext: PlatformContext,
    density: Density,
): ImageBitmap? = coilImageToMarkerBitmapViaPainter(image, sizePx, platformContext, density)
