package dev.zhafran.velnyx.feature.tracking.data

import android.util.Log
import dev.zhafran.velnyx.core.data.db.ActiveRunDao
import dev.zhafran.velnyx.core.data.db.ActiveRunPointEntity
import dev.zhafran.velnyx.core.util.PolylineSimplifier
import dev.zhafran.velnyx.feature.tracking.presentation.RunSummary
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.maplibre.android.geometry.LatLng
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RunUploadRepositoryImpl @Inject constructor(
    private val dao: ActiveRunDao,
    private val postgrest: Postgrest,
) : RunUploadRepository {

    override suspend fun uploadRun(
        runId: Long,
        summary: RunSummary,
        userId: String,
    ): Result<String> {
        return try {
            val run = dao.getRunById(runId)
                ?: return Result.failure(
                    IllegalStateException("Run $runId not found in Room — nothing to upload")
                )

            // 1. Read raw points and simplify.
            val rawPoints = dao.getPoints(runId)
            val keptPoints = applySimplification(rawPoints)
            Log.d(
                TAG,
                "uploadRun($runId): simplified ${rawPoints.size} → ${keptPoints.size} points"
            )

            // 2. Insert the parent `runs` row and fetch back its UUID.
            //    Wall-clock end time = start + active duration + total paused.
            val endedAtMs = run.startedAt + run.durationS * 1000L + run.totalPausedMs
            val runDto = RunInsertDto(
                userId = userId,
                startedAt = Instant.ofEpochMilli(run.startedAt).toString(),
                endedAt = Instant.ofEpochMilli(endedAtMs).toString(),
                distanceM = summary.distanceM,
                durationS = (summary.durationMs / 1000L).toInt(),
                avgPaceSPerKm = summary.avgPaceSecondsPerKm.takeIf { it > 0 },
                calories = summary.calories.takeIf { it > 0 },
            )
            val inserted = postgrest.from(TABLE_RUNS).insert(runDto) {
                select() // PostgREST: return the inserted row.
            }.decodeSingle<RunInsertedDto>()
            val supabaseRunId = inserted.id

            // 3. Batch-insert simplified points. We re-index 0..n-1 so the
            //    primary key (run_id, idx) is contiguous after simplification
            //    drops intermediate points.
            if (keptPoints.isNotEmpty()) {
                val pointDtos = keptPoints.mapIndexed { i, point ->
                    RunPointInsertDto(
                        runId = supabaseRunId,
                        idx = i,
                        lat = point.lat,
                        lon = point.lon,
                        recordedAt = Instant.ofEpochMilli(point.recordedAt).toString(),
                        accuracy = point.accuracy,
                    )
                }
                pointDtos.chunked(MAX_POINTS_PER_INSERT).forEach { chunk ->
                    postgrest.from(TABLE_RUN_POINTS).insert(chunk)
                }
            }

            // 4. Cleanup: cascade also deletes points + segments.
            dao.deleteActiveRunById(runId)

            Log.i(TAG, "uploadRun($runId): uploaded as $supabaseRunId")
            Result.success(supabaseRunId)
        } catch (e: CancellationException) {
            // Never swallow — let the coroutine machinery handle cancellation.
            throw e
        } catch (e: Exception) {
            // On failure we deliberately leave the local Room data untouched
            // so a future retry path (sync worker, manual re-upload) can pick
            // it up.
            Log.e(TAG, "uploadRun($runId) failed; leaving local data intact", e)
            Result.failure(e)
        }
    }

    /**
     * Simplifies the run's polyline while preserving the underlying
     * `ActiveRunPointEntity` payload (recordedAt, accuracy) for each
     * surviving point.
     *
     * Strategy:
     *  - Build a parallel `List<LatLng>` from [rawPoints].
     *  - Run [PolylineSimplifier] on it. The simplifier returns a
     *    subset of the same `LatLng` references (it uses
     *    `filterIndexed` internally), preserving order.
     *  - Walk both lists in lockstep using reference identity (`===`)
     *    to recover the matching entities.
     */
    private fun applySimplification(
        rawPoints: List<ActiveRunPointEntity>,
    ): List<ActiveRunPointEntity> {
        if (rawPoints.size <= 2) return rawPoints

        val latLngs = rawPoints.map { LatLng(it.lat, it.lon) }
        val simplified = PolylineSimplifier.simplify(latLngs, EPSILON_M)

        val kept = ArrayList<ActiveRunPointEntity>(simplified.size)
        var s = 0
        for (i in latLngs.indices) {
            if (s >= simplified.size) break
            if (latLngs[i] === simplified[s]) {
                kept += rawPoints[i]
                s++
            }
        }
        return kept
    }

    companion object {
        private const val TAG = "RunUploadRepository"
        private const val TABLE_RUNS = "runs"
        private const val TABLE_RUN_POINTS = "run_points"
        private const val EPSILON_M = 5.0
        private const val MAX_POINTS_PER_INSERT = 500
    }
}

// region DTOs
//
// PostgREST DTOs scoped to this file — they don't belong in a public
// model package because they describe wire-format only. Snake-case
// column names are explicit via @SerialName so future Kotlin renames
// don't silently break the wire contract.

@Serializable
private data class RunInsertDto(
    @SerialName("user_id") val userId: String,
    @SerialName("started_at") val startedAt: String,
    @SerialName("ended_at") val endedAt: String,
    @SerialName("distance_m") val distanceM: Int,
    @SerialName("duration_s") val durationS: Int,
    @SerialName("avg_pace_s_per_km") val avgPaceSPerKm: Int? = null,
    @SerialName("calories") val calories: Int? = null,
)

@Serializable
private data class RunInsertedDto(
    @SerialName("id") val id: String,
)

@Serializable
private data class RunPointInsertDto(
    @SerialName("run_id") val runId: String,
    @SerialName("idx") val idx: Int,
    @SerialName("lat") val lat: Double,
    @SerialName("lon") val lon: Double,
    @SerialName("recorded_at") val recordedAt: String,
    @SerialName("accuracy") val accuracy: Float? = null,
)

// endregion
