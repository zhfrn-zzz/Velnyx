package dev.zhafran.velnyx.feature.history.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * One row of the Supabase `runs` table as returned by [HistoryRepository.getRunHistory].
 *
 * Wire format only — keep this DTO confined to the data layer. UI code
 * should map it to a presentation model if behavior is ever added.
 *
 * Columns intentionally selected here (mirrors [HistoryRepositoryImpl]):
 *  - id, started_at, distance_m, duration_s, avg_pace_s_per_km, calories
 *
 * `ended_at`, `user_id`, and `created_at` are not requested because the
 * history list/detail UIs don't use them.
 */
@Serializable
data class RunSummaryDto(
    @SerialName("id") val id: String,
    @SerialName("started_at") val startedAt: String, // ISO-8601 timestamp string
    @SerialName("distance_m") val distanceM: Int,
    @SerialName("duration_s") val durationS: Int,
    @SerialName("avg_pace_s_per_km") val avgPaceSecondsPerKm: Int? = null,
    @SerialName("calories") val calories: Int? = null,
)
