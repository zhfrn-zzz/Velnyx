package dev.zhafran.velnyx.core.designsystem.component

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import dev.zhafran.velnyx.BuildConfig
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory.circleColor
import org.maplibre.android.style.layers.PropertyFactory.circleRadius
import org.maplibre.android.style.layers.PropertyFactory.circleStrokeColor
import org.maplibre.android.style.layers.PropertyFactory.circleStrokeWidth
import org.maplibre.android.style.layers.PropertyFactory.lineColor
import org.maplibre.android.style.layers.PropertyFactory.lineWidth
import org.maplibre.android.style.layers.PropertyFactory.lineCap
import org.maplibre.android.style.layers.PropertyFactory.lineJoin
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point

private const val STYLE_URL =
    "https://api.maptiler.com/maps/streets-v2/style.json?key=${BuildConfig.MAPTILER_API_KEY}"

@Composable
fun VelnyxMapView(
    modifier: Modifier = Modifier,
    currentLatLng: LatLng?,
    routePoints: List<LatLng>,
    onMapReady: (MapLibreMap) -> Unit = {},
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    Box(modifier = modifier) {
        var mapViewRef: MapView? = null

        AndroidView(
            factory = { context ->
                MapView(context).apply {
                    onCreate(null)
                    onStart()
                    onResume()
                    Log.d("VelnyxMapView", "Style URL: $STYLE_URL")
                    getMapAsync { map ->
                        onMapReady(map)
                        map.setStyle(STYLE_URL) { style ->
                            // Current position source + layer
                            style.addSource(GeoJsonSource("current-position-source"))
                            style.addLayer(
                                CircleLayer("current-position-layer", "current-position-source")
                                    .withProperties(
                                        circleRadius(8f),
                                        circleColor("rgba(186, 255, 41, 1)"),
                                        circleStrokeWidth(2f),
                                        circleStrokeColor("rgba(255, 255, 255, 1)"),
                                    ),
                            )
                            // Route source + layer
                            style.addSource(GeoJsonSource("route-source"))
                            style.addLayer(
                                LineLayer("route-layer", "route-source")
                                    .withProperties(
                                        lineColor("rgba(186, 255, 41, 1)"),
                                        lineWidth(4f),
                                        lineCap("round"),
                                        lineJoin("round"),
                                    ),
                            )
                        }
                    }
                    mapViewRef = this
                }
            },
            update = { mapView ->
                mapView.getMapAsync { map ->
                    val style = map.style ?: return@getMapAsync
                    // Update current position
                    if (currentLatLng != null) {
                        val point = Point.fromLngLat(currentLatLng.longitude, currentLatLng.latitude)
                        (style.getSource("current-position-source") as? GeoJsonSource)
                            ?.setGeoJson(Feature.fromGeometry(point))
                    }
                    // Update route
                    if (routePoints.size >= 2) {
                        val points = routePoints.map { Point.fromLngLat(it.longitude, it.latitude) }
                        (style.getSource("route-source") as? GeoJsonSource)
                            ?.setGeoJson(Feature.fromGeometry(LineString.fromLngLats(points)))
                    }
                    // Animate camera every 10 points
                    if (currentLatLng != null && routePoints.size % 10 == 0) {
                        val pos = CameraPosition.Builder()
                            .target(currentLatLng)
                            .zoom(16.0)
                            .build()
                        map.animateCamera(CameraUpdateFactory.newCameraPosition(pos), 300)
                    }
                }
            },
        )

        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_START -> mapViewRef?.onStart()
                    Lifecycle.Event.ON_RESUME -> mapViewRef?.onResume()
                    Lifecycle.Event.ON_PAUSE -> mapViewRef?.onPause()
                    Lifecycle.Event.ON_STOP -> mapViewRef?.onStop()
                    Lifecycle.Event.ON_DESTROY -> mapViewRef?.onDestroy()
                    else -> {}
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
                mapViewRef?.onDestroy()
            }
        }

        // Attribution overlay
        Text(
            text = "© OpenStreetMap contributors / © MapTiler",
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(4.dp),
            fontSize = 9.sp,
            color = Color.White.copy(alpha = 0.7f),
        )
    }
}
