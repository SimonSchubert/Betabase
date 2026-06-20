package com.inspiredandroid.betabase.ui.screens

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.Density
import coil3.Image
import coil3.PlatformContext
import coil3.request.ImageRequest

internal expect fun ImageRequest.Builder.configureMarkerRequest(): ImageRequest.Builder

internal expect fun coilImageToMarkerBitmap(
    image: Image,
    sizePx: Int,
    platformContext: PlatformContext,
    density: Density,
): ImageBitmap?

internal fun isValidMarkerBitmap(bitmap: ImageBitmap): Boolean =
    bitmap.width > 0 && bitmap.height > 0