package com.inspiredandroid.betabase.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.inspiredandroid.betabase.data.Gym
import com.inspiredandroid.betabase.data.GymMarkerCategory
import com.inspiredandroid.betabase.data.markerCategory
import com.inspiredandroid.betabase.ui.components.rememberGymMarkerBitmaps
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.asString
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.feature
import org.maplibre.compose.expressions.dsl.format
import org.maplibre.compose.expressions.dsl.image
import org.maplibre.compose.expressions.dsl.offset
import org.maplibre.compose.expressions.dsl.span
import org.maplibre.compose.expressions.value.SymbolAnchor
import org.maplibre.compose.layers.SymbolLayer
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.OrnamentOptions
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.compose.util.ClickResult
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position
import kotlin.time.Duration.Companion.milliseconds

private const val MAP_STYLE_URI = "https://tiles.openfreemap.org/styles/liberty"
private val BERLIN = Position(latitude = 52.5200, longitude = 13.4050)

@Composable
actual fun GymMap(
    gyms: List<Gym>,
    selectedGym: Gym?,
    onGymSelected: (Gym?) -> Unit,
    modifier: Modifier,
) {
    val cameraState = rememberCameraState(
        firstPosition = CameraPosition(
            target = BERLIN,
            zoom = 10.5,
        ),
    )

    val featuresByCategory = remember(gyms) {
        GymMarkerCategory.entries.associateWith { category ->
            FeatureCollection(
                gyms.filter { it.markerCategory() == category }.map { gym ->
                    Feature<Point, JsonObject>(
                        geometry = Point(Position(latitude = gym.latitude, longitude = gym.longitude)),
                        properties = JsonObject(mapOf("name" to JsonPrimitive(gym.name))),
                        id = JsonPrimitive(gym.id),
                    )
                },
            )
        }
    }

    LaunchedEffect(selectedGym) {
        val target = selectedGym ?: return@LaunchedEffect
        cameraState.animateTo(
            finalPosition = cameraState.position.copy(
                target = Position(latitude = target.latitude, longitude = target.longitude),
                zoom = maxOf(cameraState.position.zoom, 13.5),
            ),
            duration = 400.milliseconds,
        )
    }

    val markerBitmaps = rememberGymMarkerBitmaps()

    MaplibreMap(
        modifier = modifier.fillMaxSize(),
        baseStyle = BaseStyle.Uri(MAP_STYLE_URI),
        cameraState = cameraState,
        options = MapOptions(ornamentOptions = OrnamentOptions.OnlyLogo),
        onMapClick = { _, _ ->
            onGymSelected(null)
            ClickResult.Pass
        },
    ) {
        GymMarkerCategory.entries.forEach { category ->
            val source = rememberGeoJsonSource(
                data = GeoJsonData.Features(featuresByCategory.getValue(category)),
            )
            SymbolLayer(
                id = "gym-markers-${category.name.lowercase()}",
                source = source,
                iconImage = image(value = markerBitmaps.getValue(category)),
                iconAnchor = const(SymbolAnchor.Bottom),
                iconAllowOverlap = const(true),
                textField = format(span(feature["name"].asString())),
                textFont = const(listOf("Noto Sans Bold")),
                textSize = const(0.85.em),
                textColor = const(Color(0xFF111111)),
                textHaloColor = const(Color.White),
                textHaloWidth = const(2.dp),
                textAnchor = const(SymbolAnchor.Top),
                textOffset = offset(0.em, 0.4.em),
                textOptional = const(true),
                onClick = { clicked ->
                    val featureId = clicked.firstOrNull()?.id?.content
                    val gym = gyms.firstOrNull { it.id == featureId }
                    if (gym != null) {
                        onGymSelected(gym)
                        ClickResult.Consume
                    } else {
                        ClickResult.Pass
                    }
                },
            )
        }
    }
}
