package com.inspiredandroid.betabase.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.inspiredandroid.betabase.ui.theme.BetabaseTheme

@Composable
fun BetaSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search",
) {
    val colors = BetabaseTheme.colors
    val shape = BetabaseTheme.shapes.pill
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 44.dp)
            .clip(shape)
            .background(colors.surface)
            .border(1.5.dp, colors.hairline, shape)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SearchGlyph(tint = colors.inkMuted)
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 10.dp),
        ) {
            if (value.isEmpty()) {
                BetaText(
                    text = placeholder,
                    style = BetabaseTheme.typography.bodyMedium,
                    color = colors.inkMuted,
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                cursorBrush = SolidColor(colors.ink),
                textStyle = BetabaseTheme.typography.bodyMedium.copy(color = colors.ink),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (value.isNotEmpty()) {
            val interaction = remember { MutableInteractionSource() }
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(shape)
                    .clickable(
                        interactionSource = interaction,
                        indication = null,
                        onClick = { onValueChange("") },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                ClearGlyph(tint = colors.inkMuted)
            }
        }
    }
}

@Composable
private fun SearchGlyph(tint: androidx.compose.ui.graphics.Color) {
    Canvas(modifier = Modifier.size(18.dp)) {
        val w = size.width
        val cx = w * 0.42f
        val cy = w * 0.42f
        val r = w * 0.30f
        val stroke = w / 10f
        drawCircle(color = tint, center = Offset(cx, cy), radius = r, style = Stroke(width = stroke))
        // Handle running from circle edge down-right.
        val edgeX = cx + r * 0.707f
        val edgeY = cy + r * 0.707f
        drawLine(
            color = tint,
            start = Offset(edgeX, edgeY),
            end = Offset(w - stroke, w - stroke),
            strokeWidth = stroke,
        )
    }
}

@Composable
private fun ClearGlyph(tint: androidx.compose.ui.graphics.Color) {
    Canvas(modifier = Modifier.size(14.dp)) {
        val w = size.width
        val stroke = w / 7f
        val p = w * 0.18f
        drawLine(
            color = tint,
            start = Offset(p, p),
            end = Offset(w - p, w - p),
            strokeWidth = stroke,
        )
        drawLine(
            color = tint,
            start = Offset(w - p, p),
            end = Offset(p, w - p),
            strokeWidth = stroke,
        )
    }
}
