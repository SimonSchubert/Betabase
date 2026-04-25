package com.inspiredandroid.betabase.screenshots

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.resources.Density
import com.android.resources.ScreenOrientation
import org.junit.Rule
import org.junit.Test

private val brandRed = Color(0xFFE63946)
private val brandWhite = Color.White

class StoreIconTest {

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5.copy(
            softButtons = false,
            screenWidth = 512,
            screenHeight = 512,
            xdpi = 160,
            ydpi = 160,
            density = Density.MEDIUM,
        ),
        showSystemUi = false,
    )

    @Test
    fun render() {
        paparazzi.snapshot {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(brandRed),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = "B",
                        style = TextStyle(
                            color = brandWhite,
                            fontWeight = FontWeight.Black,
                            fontSize = 380.sp,
                            fontFamily = FontFamily.SansSerif,
                            letterSpacing = (-0.06).em,
                        ),
                    )
                }
            }
        }
    }
}

class StoreFeatureGraphicTest {

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5.copy(
            softButtons = false,
            screenWidth = 1024,
            screenHeight = 500,
            xdpi = 160,
            ydpi = 160,
            density = Density.MEDIUM,
            orientation = ScreenOrientation.LANDSCAPE,
        ),
        showSystemUi = false,
    )

    @Test
    fun render() {
        paparazzi.snapshot {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(brandRed),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Spacer(Modifier.width(64.dp))
                    Column(verticalArrangement = Arrangement.Center) {
                        BasicText(
                            text = "BETABASE",
                            style = TextStyle(
                                color = brandWhite,
                                fontWeight = FontWeight.Black,
                                fontSize = 124.sp,
                                fontFamily = FontFamily.SansSerif,
                                letterSpacing = (-0.04).em,
                            ),
                        )
                        Spacer(Modifier.height(8.dp))
                        BasicText(
                            text = "CLIMBING COMPETITIONS",
                            style = TextStyle(
                                color = brandWhite,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 28.sp,
                                fontFamily = FontFamily.SansSerif,
                                letterSpacing = 0.18.em,
                            ),
                        )
                    }
                    Spacer(Modifier.width(40.dp))
                    Box(
                        modifier = Modifier
                            .padding(end = 80.dp)
                            .background(brandWhite)
                            .padding(horizontal = 36.dp, vertical = 20.dp),
                    ) {
                        BasicText(
                            text = "B",
                            style = TextStyle(
                                color = brandRed,
                                fontWeight = FontWeight.Black,
                                fontSize = 240.sp,
                                fontFamily = FontFamily.SansSerif,
                                letterSpacing = (-0.06).em,
                            ),
                        )
                    }
                }
            }
        }
    }
}
