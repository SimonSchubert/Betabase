package com.inspiredandroid.betabase.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import betabase.composeapp.generated.resources.Res
import betabase.composeapp.generated.resources.arrow_up
import betabase.composeapp.generated.resources.boulder
import betabase.composeapp.generated.resources.menu_book
import com.inspiredandroid.betabase.data.GymMarkerCategory
import com.inspiredandroid.betabase.ui.theme.BetabaseTheme
import com.inspiredandroid.betabase.ui.theme.LocalContentColor
import org.jetbrains.compose.resources.painterResource

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
fun rememberGymMarkerBitmaps(
    width: Dp = 32.dp,
    height: Dp = 40.dp,
): Map<GymMarkerCategory, ImageBitmap> {
    val density = LocalDensity.current
    val boulderColor = BetabaseTheme.colors.boulder
    val leadColor = BetabaseTheme.colors.lead
    val combinedColor = BetabaseTheme.colors.combined
    val boulderPainter = painterResource(Res.drawable.boulder)
    val arrowUpPainter = painterResource(Res.drawable.arrow_up)
    return remember(
        density,
        width,
        height,
        boulderColor,
        leadColor,
        combinedColor,
        boulderPainter,
        arrowUpPainter,
    ) {
        mapOf(
            GymMarkerCategory.BOULDER to drawGymMarker(density, width, height, boulderColor) { cx, cy, r ->
                drawPainterCentered(boulderPainter, cx, cy + r * 0.05f, r * 1.35f, r * 1.2f)
            },
            GymMarkerCategory.LEAD to drawGymMarker(density, width, height, leadColor) { cx, cy, r ->
                drawPainterCentered(arrowUpPainter, cx, cy, r * 1.15f, r * 1.35f)
            },
            GymMarkerCategory.COMBINED to drawGymMarker(density, width, height, combinedColor) { cx, cy, r ->
                drawPainterCentered(arrowUpPainter, cx, cy - r * 0.4f, r * 0.8f, r * 0.85f)
                drawPainterCentered(boulderPainter, cx, cy + r * 0.4f, r * 1.05f, r * 0.85f)
            },
        )
    }
}

private fun DrawScope.drawPainterCentered(
    painter: Painter,
    cx: Float,
    cy: Float,
    width: Float,
    height: Float,
) {
    translate(left = cx - width / 2f, top = cy - height / 2f) {
        with(painter) {
            draw(size = Size(width, height))
        }
    }
}

private fun drawGymMarker(
    density: Density,
    width: Dp,
    height: Dp,
    fill: Color,
    drawGlyph: DrawScope.(cx: Float, cy: Float, r: Float) -> Unit,
): ImageBitmap {
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
        drawPath(innerFill, color = fill)
        drawGlyph(cx, cy, innerR)
    }
    return bitmap
}

@Composable
fun AthletesIcon(
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    size: Dp = 22.dp,
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val cx = w / 2f
        val cy = h * 0.58f
        val r = w * 0.30f
        val ribbonWidth = w * 0.12f
        // Two ribbon strips fanning up from the medal.
        val ribbonTop = h * 0.02f
        val left = Path().apply {
            moveTo(cx - r * 0.6f, cy - r * 0.55f)
            lineTo(cx - r * 0.15f, ribbonTop)
            lineTo(cx - r * 0.15f + ribbonWidth, ribbonTop)
            lineTo(cx - r * 0.6f + ribbonWidth, cy - r * 0.55f)
            close()
        }
        val right = Path().apply {
            moveTo(cx + r * 0.6f, cy - r * 0.55f)
            lineTo(cx + r * 0.15f, ribbonTop)
            lineTo(cx + r * 0.15f - ribbonWidth, ribbonTop)
            lineTo(cx + r * 0.6f - ribbonWidth, cy - r * 0.55f)
            close()
        }
        drawPath(left, color = tint)
        drawPath(right, color = tint)
        // Medal circle.
        drawCircle(color = tint, center = Offset(cx, cy), radius = r, style = Stroke(width = w / 10f))
        drawCircle(color = tint, center = Offset(cx, cy), radius = r * 0.35f)
    }
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

/** Material Symbols outlined menu_book — glossary / grade systems. */
@Composable
fun GradesIcon(
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    size: Dp = 22.dp,
) {
    Icon(
        painter = painterResource(Res.drawable.menu_book),
        contentDescription = null,
        modifier = modifier.size(size),
        tint = tint,
    )
}

/** Pull-up bar with hanging figure — training / hang sessions. */
@Composable
fun TrainIcon(
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    size: Dp = 22.dp,
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = w * 0.11f
        // Bar
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.08f, h * 0.12f),
            size = Size(w * 0.84f, stroke),
            cornerRadius = CornerRadius(stroke / 2f, stroke / 2f),
        )
        // Hands gripping bar
        val handY = h * 0.12f + stroke
        drawCircle(color = tint, center = Offset(w * 0.32f, handY + w * 0.06f), radius = w * 0.07f)
        drawCircle(color = tint, center = Offset(w * 0.68f, handY + w * 0.06f), radius = w * 0.07f)
        // Arms
        val armTop = handY + w * 0.12f
        val shoulderY = h * 0.48f
        drawLine(
            color = tint,
            start = Offset(w * 0.32f, armTop),
            end = Offset(w * 0.42f, shoulderY),
            strokeWidth = stroke,
        )
        drawLine(
            color = tint,
            start = Offset(w * 0.68f, armTop),
            end = Offset(w * 0.58f, shoulderY),
            strokeWidth = stroke,
        )
        // Head
        drawCircle(color = tint, center = Offset(w * 0.50f, shoulderY + w * 0.02f), radius = w * 0.10f)
        // Torso
        drawLine(
            color = tint,
            start = Offset(w * 0.50f, shoulderY + w * 0.14f),
            end = Offset(w * 0.50f, h * 0.78f),
            strokeWidth = stroke,
        )
        // Legs slightly bent
        drawLine(
            color = tint,
            start = Offset(w * 0.50f, h * 0.78f),
            end = Offset(w * 0.38f, h * 0.96f),
            strokeWidth = stroke,
        )
        drawLine(
            color = tint,
            start = Offset(w * 0.50f, h * 0.78f),
            end = Offset(w * 0.62f, h * 0.96f),
            strokeWidth = stroke,
        )
    }
}

/** Bell for competition start reminders — outline when off, filled when armed. */
@Composable
fun ReminderBellIcon(
    filled: Boolean,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    size: Dp = 20.dp,
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = w * 0.10f
        val cx = w / 2f
        // Dome
        val domeTop = h * 0.12f
        val domeBottom = h * 0.62f
        val domeWidth = w * 0.72f
        val left = cx - domeWidth / 2f
        val right = cx + domeWidth / 2f
        val path = Path().apply {
            moveTo(left, domeBottom)
            quadraticTo(left, domeTop, cx, domeTop)
            quadraticTo(right, domeTop, right, domeBottom)
            lineTo(right + w * 0.06f, domeBottom + h * 0.08f)
            lineTo(left - w * 0.06f, domeBottom + h * 0.08f)
            close()
        }
        if (filled) {
            drawPath(path, color = tint)
        } else {
            drawPath(path, color = tint, style = Stroke(width = stroke))
        }
        // Clapper
        drawCircle(
            color = tint,
            center = Offset(cx, h * 0.88f),
            radius = w * 0.10f,
        )
        // Crown nub
        drawCircle(
            color = tint,
            center = Offset(cx, domeTop),
            radius = w * 0.07f,
        )
    }
}
