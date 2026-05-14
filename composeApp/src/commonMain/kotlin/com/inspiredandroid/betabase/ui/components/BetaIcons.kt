package com.inspiredandroid.betabase.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.inspiredandroid.betabase.ui.theme.LocalContentColor

@Composable
fun CompsIcon(
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    size: Dp = 22.dp,
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val bar = w * 0.22f
        val gap = (w - bar * 3f) / 2f
        val corner = CornerRadius(2f, 2f)
        drawRoundRect(
            color = tint,
            topLeft = Offset(0f, h * 0.40f),
            size = Size(bar, h * 0.60f),
            cornerRadius = corner,
        )
        drawRoundRect(
            color = tint,
            topLeft = Offset(bar + gap, h * 0.10f),
            size = Size(bar, h * 0.90f),
            cornerRadius = corner,
        )
        drawRoundRect(
            color = tint,
            topLeft = Offset((bar + gap) * 2f, h * 0.60f),
            size = Size(bar, h * 0.40f),
            cornerRadius = corner,
        )
    }
}

@Composable
fun rememberGymMarkerBitmap(
    width: Dp = 32.dp,
    height: Dp = 40.dp,
): ImageBitmap {
    val density = LocalDensity.current
    return remember(density, width, height) {
        drawGymMarker(density, width, height)
    }
}

private fun drawGymMarker(density: Density, width: Dp, height: Dp): ImageBitmap {
    val pxW: Int
    val pxH: Int
    with(density) {
        pxW = width.toPx().toInt()
        pxH = height.toPx().toInt()
    }
    val bitmap = ImageBitmap(pxW, pxH)
    val size = Size(pxW.toFloat(), pxH.toFloat())
    CanvasDrawScope().draw(
        density = density,
        layoutDirection = LayoutDirection.Ltr,
        canvas = androidx.compose.ui.graphics.Canvas(bitmap),
        size = size,
    ) {
        val cx = size.width / 2f
        val r = size.width * 0.45f
        val cy = r
        val tipY = size.height - 1f
        val outline = Path().apply {
            moveTo(cx - r, cy)
            arcTo(
                rect = Rect(center = Offset(cx, cy), radius = r),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 180f,
                forceMoveTo = false,
            )
            lineTo(cx, tipY)
            close()
        }
        drawPath(outline, color = Color(0xFF111111))
        val inset = size.width * 0.07f
        val innerR = r - inset
        val innerTipY = tipY - inset
        val innerFill = Path().apply {
            moveTo(cx - innerR, cy)
            arcTo(
                rect = Rect(center = Offset(cx, cy), radius = innerR),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 180f,
                forceMoveTo = false,
            )
            lineTo(cx, innerTipY)
            close()
        }
        drawPath(innerFill, color = Color(0xFFE63946))
        drawCircle(
            color = Color.White,
            center = Offset(cx, cy),
            radius = size.width * 0.16f,
        )
    }
    return bitmap
}

@Composable
fun GymsIcon(
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    size: Dp = 22.dp,
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val cx = w / 2f
        val cy = h * 0.40f
        val r = w * 0.32f
        val tipY = h * 0.96f
        val stroke = w / 10f
        val path = Path().apply {
            moveTo(cx - r, cy)
            arcTo(
                rect = Rect(center = Offset(cx, cy), radius = r),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 180f,
                forceMoveTo = false,
            )
            lineTo(cx, tipY)
            close()
        }
        drawPath(path = path, color = tint, style = Stroke(width = stroke))
        drawCircle(color = tint, center = Offset(cx, cy), radius = r * 0.32f)
    }
}
