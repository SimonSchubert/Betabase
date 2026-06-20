package com.inspiredandroid.betabase.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.inspiredandroid.betabase.data.Gym
import com.inspiredandroid.betabase.data.GymMarkerCategory
import com.inspiredandroid.betabase.data.markerCategory
import com.inspiredandroid.betabase.ui.components.rememberGymMarkerBitmaps
import com.inspiredandroid.betabase.ui.theme.BetabaseTheme
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.asNumber
import org.maplibre.compose.expressions.dsl.asString
import org.maplibre.compose.expressions.dsl.case
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.feature
import org.maplibre.compose.expressions.dsl.format
import org.maplibre.compose.expressions.dsl.image
import org.maplibre.compose.expressions.dsl.not
import org.maplibre.compose.expressions.dsl.offset
import org.maplibre.compose.expressions.dsl.span
import org.maplibre.compose.expressions.dsl.step
import org.maplibre.compose.expressions.dsl.switch
import org.maplibre.compose.expressions.value.SymbolAnchor
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.layers.SymbolLayer
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.OrnamentOptions
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.GeoJsonOptions
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

// Above this zoom every gym is shown as its own pin; at/below it nearby gyms collapse into
// count badges. Kept low so a focused city ungroups into pins without zooming in far.
private const val CLUSTER_MAX_ZOOM = 9

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

    val allFeatures = remember(gyms) {
        FeatureCollection(
            gyms.map { gym ->
                Feature<Point, JsonObject>(
                    geometry = Point(Position(latitude = gym.latitude, longitude = gym.longitude)),
                    properties = JsonObject(
                        mapOf(
                            "gym_id" to JsonPrimitive(gym.id),
                            "name" to JsonPrimitive(gym.name),
                            "category" to JsonPrimitive(gym.markerCategory().name.lowercase()),
                        ),
                    ),
                    id = JsonPrimitive(gym.id),
                )
            },
        )
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

    var pendingClusterMove by remember { mutableStateOf<CameraPosition?>(null) }
    LaunchedEffect(pendingClusterMove) {
        val move = pendingClusterMove ?: return@LaunchedEffect
        cameraState.animateTo(finalPosition = move, duration = 400.milliseconds)
        pendingClusterMove = null
    }

    val fallbackMarkerBitmaps = rememberGymMarkerBitmaps()
    val gymMarkerBitmaps = rememberGymPhotoMarkerBitmaps(gyms)
    val accentColor = BetabaseTheme.colors.accent
    val onAccentColor = BetabaseTheme.colors.onAccent

    val categoryFallback = remember(fallbackMarkerBitmaps) {
        switch(
            input = feature["category"].asString(),
            case("boulder", image(fallbackMarkerBitmaps.getValue(GymMarkerCategory.BOULDER))),
            case("lead", image(fallbackMarkerBitmaps.getValue(GymMarkerCategory.LEAD))),
            case("combined", image(fallbackMarkerBitmaps.getValue(GymMarkerCategory.COMBINED))),
            fallback = image(fallbackMarkerBitmaps.getValue(GymMarkerCategory.BOULDER)),
        )
    }

    val gymPhotoCases = remember(gymMarkerBitmaps) {
        gymMarkerBitmaps.map { (gymId, markerBitmap) ->
            case(
                label = gymId,
                output = image(markerBitmap),
            )
        }.toTypedArray()
    }

    val markerIconImage = remember(gymPhotoCases, categoryFallback) {
        if (gymPhotoCases.isNotEmpty()) {
            switch(
                input = feature["gym_id"].asString(),
                cases = gymPhotoCases,
                fallback = categoryFallback,
            )
        } else {
            categoryFallback
        }
    }

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
        if (!supportsMaplibreProgrammaticLayers) return@MaplibreMap

        val source = rememberGeoJsonSource(
            data = GeoJsonData.Features(allFeatures),
            options = GeoJsonOptions(
                cluster = true,
                clusterRadius = 35,
                clusterMaxZoom = CLUSTER_MAX_ZOOM,
                clusterMinPoints = 2,
            ),
        )

        CircleLayer(
            id = "gym-clusters",
            source = source,
            filter = feature.has("point_count"),
            color = const(accentColor),
            radius = step(
                input = feature["point_count"].asNumber(),
                fallback = const(15.dp),
                10 to const(19.dp),
                30 to const(24.dp),
            ),
            strokeColor = const(onAccentColor),
            strokeWidth = const(2.dp),
            onClick = { clicked ->
                val cluster = clicked.firstOrNull()
                val center = cluster?.geometry as? Point
                if (cluster != null && center != null) {
                    pendingClusterMove = CameraPosition(
                        target = center.coordinates,
                        zoom = source.getClusterExpansionZoom(cluster),
                    )
                    ClickResult.Consume
                } else {
                    ClickResult.Pass
                }
            },
        )

        SymbolLayer(
            id = "gym-cluster-counts",
            source = source,
            filter = feature.has("point_count"),
            textField = format(span(feature["point_count_abbreviated"].asString())),
            textFont = const(listOf("Noto Sans Bold")),
            textSize = const(0.9.em),
            textColor = const(onAccentColor),
            textAllowOverlap = const(true),
        )

        SymbolLayer(
            id = "gym-markers",
            source = source,
            filter = feature.has("point_count").not(),
            iconImage = markerIconImage,
            iconAnchor = const(SymbolAnchor.Center),
            iconAllowOverlap = const(true),
            textField = format(span(feature["name"].asString())),
            textFont = const(listOf("Noto Sans Bold")),
            textSize = const(0.85.em),
            textColor = const(Color(0xFF111111)),
            textHaloColor = const(Color.White),
            textHaloWidth = const(2.dp),
            textAnchor = const(SymbolAnchor.Top),
            textOffset = offset(0.em, 1.2.em),
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
