package com.inspiredandroid.betabase.ui.components

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.inspiredandroid.betabase.ui.theme.LocalContentColor
import com.inspiredandroid.betabase.ui.theme.LocalTextStyle

@Composable
fun BetaText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    style: TextStyle = LocalTextStyle.current,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    overflow: TextOverflow = TextOverflow.Clip,
    textAlign: TextAlign? = null,
) {
    val resolvedColor = if (color.isSpecified()) color else LocalContentColor.current
    BasicText(
        text = text,
        modifier = modifier,
        style = style.merge(TextStyle(color = resolvedColor, textAlign = textAlign ?: TextAlign.Unspecified)),
        maxLines = maxLines,
        minLines = minLines,
        overflow = overflow,
    )
}

private fun Color.isSpecified(): Boolean = this != Color.Unspecified
