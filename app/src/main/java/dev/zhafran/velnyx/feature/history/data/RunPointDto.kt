package dev.zhafran.velnyx.feature.history.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * One row of the Supabase `run_points` table, trimmed to the columns the
 * detail-map view actually needs.
 *
 * `recorded_at` and `accuracy` are intentionally omitted from the wire —
 * once the polyline is being rendered for display only, neither column
 * adds anything. Selecting fewer columns means smaller payloads on
 * older Indonesian mobile networks.
 */
@Serializable
data class RunPointDto(
    @SerialName("idx") val idx: Int,
    @SerialName("lat") val lat: Double,
    @SerialName("lon") val lon: Double,
)
