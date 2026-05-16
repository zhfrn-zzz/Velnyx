package dev.zhafran.velnyx.core.util

import org.maplibre.android.geometry.LatLng
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Douglas-Peucker polyline simplification on the sphere.
 *
 * Reduces an ordered sequence of GPS fixes to a smaller subset that
 * preserves the polyline shape within [epsilonMeters].
 *
 * The "distance from a candidate point to the current segment" is
 * computed via the great-circle cross-track distance, with along-track
 * clamping so points that project outside the segment fall back to the
 * Haversine distance to the nearer endpoint.
 *
 * For a 5km run with ~1000 raw fixes, ε = 5m typically reduces the
 * polyline to ~50–150 points with no visible shape change. See
 * CLAUDE.md "Polyline simplification" in the gotchas section for the
 * rationale.
 */
object PolylineSimplifier {

    private const val EARTH_RADIUS_M = 6_371_000.0
    private const val DEFAULT_EPSILON_M = 5.0

    /**
     * Returns a simplified subset of [points] that preserves order and
     * both endpoints. The first and last points are always kept. The
     * return value contains the same `LatLng` references as [points]
     * (no copies), so callers can map kept-LatLngs back to richer
     * objects by reference identity.
     *
     * Edge cases:
     * - empty input → empty output
     * - input of size ≤ 2 → returned as-is
     */
    fun simplify(
        points: List<LatLng>,
        epsilonMeters: Double = DEFAULT_EPSILON_M,
    ): List<LatLng> {
        if (points.isEmpty()) return emptyList()
        if (points.size <= 2) return points

        val keep = BooleanArray(points.size)
        keep[0] = true
        keep[points.size - 1] = true
        dp(points, 0, points.size - 1, epsilonMeters, keep)
        return points.filterIndexed { i, _ -> keep[i] }
    }

    private fun dp(
        points: List<LatLng>,
        start: Int,
        end: Int,
        epsilon: Double,
        keep: BooleanArray,
    ) {
        // Nothing between start and end → recursion bottoms out.
        if (end <= start + 1) return

        val a = points[start]
        val b = points[end]
        var maxDist = 0.0
        var maxIdx = -1
        for (i in (start + 1) until end) {
            val d = distanceToSegmentMeters(points[i], a, b)
            if (d > maxDist) {
                maxDist = d
                maxIdx = i
            }
        }

        if (maxIdx != -1 && maxDist > epsilon) {
            keep[maxIdx] = true
            dp(points, start, maxIdx, epsilon, keep)
            dp(points, maxIdx, end, epsilon, keep)
        }
    }

    /**
     * Great-circle distance from point [p] to the segment [a]→[b], in meters.
     *
     * If [p] projects onto the segment, returns the absolute cross-track
     * distance (perpendicular from [p] to the great circle through [a]
     * and [b]). Otherwise returns the Haversine distance to the nearer
     * endpoint.
     */
    private fun distanceToSegmentMeters(p: LatLng, a: LatLng, b: LatLng): Double {
        // Degenerate segment: a and b coincide → just point-to-point.
        if (a.latitude == b.latitude && a.longitude == b.longitude) {
            return haversineMeters(p, a)
        }

        val d13 = haversineMeters(a, p)
        if (d13 == 0.0) return 0.0

        val theta12 = bearingRad(a, b)
        val theta13 = bearingRad(a, p)
        val d12 = haversineMeters(a, b)

        // Cross-track distance: perpendicular from p to great circle a-b.
        val crossTrack = asin(
            sin(d13 / EARTH_RADIUS_M) * sin(theta13 - theta12)
        ) * EARTH_RADIUS_M

        // Along-track distance: signed projection of p onto segment a-b.
        // Clamp the acos input — FP error can push it slightly outside
        // [-1, 1] when p is essentially on the great circle, which would
        // otherwise produce NaN.
        val ratio = (cos(d13 / EARTH_RADIUS_M) / cos(crossTrack / EARTH_RADIUS_M))
            .coerceIn(-1.0, 1.0)
        val alongTrack = acos(ratio) * EARTH_RADIUS_M

        return when {
            alongTrack > d12 -> haversineMeters(p, b) // projects past b
            alongTrack < 0.0 -> d13                    // projects before a
            else -> abs(crossTrack)
        }
    }

    private fun haversineMeters(a: LatLng, b: LatLng): Double {
        val lat1 = Math.toRadians(a.latitude)
        val lat2 = Math.toRadians(b.latitude)
        val dLat = lat2 - lat1
        val dLon = Math.toRadians(b.longitude - a.longitude)
        val sinDLat = sin(dLat / 2)
        val sinDLon = sin(dLon / 2)
        val h = sinDLat * sinDLat + cos(lat1) * cos(lat2) * sinDLon * sinDLon
        return 2 * EARTH_RADIUS_M * asin(sqrt(h.coerceIn(0.0, 1.0)))
    }

    private fun bearingRad(from: LatLng, to: LatLng): Double {
        val lat1 = Math.toRadians(from.latitude)
        val lat2 = Math.toRadians(to.latitude)
        val dLon = Math.toRadians(to.longitude - from.longitude)
        val y = sin(dLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)
        return atan2(y, x)
    }
}
