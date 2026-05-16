package dev.zhafran.velnyx.core.location

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.maplibre.android.geometry.LatLng
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Process-wide holder for the *latest raw* location fix from
 * FusedLocationProviderClient — published from [RunTrackingService]
 * BEFORE any accuracy / speed filtering is applied.
 *
 * Why this exists:
 *
 * FusedLocationProviderClient already fuses GPS + WiFi + cell-tower
 * signals into a single stream of fixes. Indoor or low-sky-view fixes
 * routinely come back with `accuracy > 30m` (because they are derived
 * from WiFi/cell, not satellites). Those fixes are still useful for
 * showing the user their current position on the map — they just
 * aren't trustworthy enough to feed into distance / pace / calorie
 * computation.
 *
 * The previous pipeline conflated these two concerns: a single filter
 * (`accuracy <= 30m && speed >= 1.0 m/s`) gated *both* the Room write
 * and the map marker, so the marker disappeared whenever the user was
 * indoors or standing still.
 *
 *  - This flow ([rawLocationFlow]) drives the map's current-position
 *    marker only. No filtering.
 *  - The filtered Room `active_run_points` table continues to drive
 *    distance, pace, calories, and the route polyline. See
 *    [RunTrackingService.processLocation] for the filter logic.
 */
@Singleton
class LocationRepository @Inject constructor() {

    private val _rawLocationFlow = MutableStateFlow<LatLng?>(null)

    /** Latest raw fix from the fused provider, or null if none yet. */
    val rawLocationFlow: StateFlow<LatLng?> = _rawLocationFlow.asStateFlow()

    /** Called from [RunTrackingService] on every location update. */
    fun publishRaw(latLng: LatLng) {
        _rawLocationFlow.value = latLng
    }

    /** Reset between runs / when tracking stops, so a stale marker
     *  doesn't bleed into the next run. */
    fun clear() {
        _rawLocationFlow.value = null
    }
}
