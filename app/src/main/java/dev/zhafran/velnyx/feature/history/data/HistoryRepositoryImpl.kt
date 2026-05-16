package dev.zhafran.velnyx.feature.history.data

import android.util.Log
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.CancellationException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
) : HistoryRepository {

    override suspend fun getRunHistory(): Result<List<RunSummaryDto>> {
        return try {
            val rows = postgrest.from(TABLE_RUNS)
                .select(
                    columns = Columns.list(
                        "id",
                        "started_at",
                        "distance_m",
                        "duration_s",
                        "avg_pace_s_per_km",
                        "calories",
                    ),
                ) {
                    order(column = "started_at", order = Order.DESCENDING)
                    limit(MAX_HISTORY_ROWS)
                }
                .decodeList<RunSummaryDto>()
            Result.success(rows)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "getRunHistory failed", e)
            Result.failure(e)
        }
    }

    override suspend fun getRunPoints(runId: String): Result<List<RunPointDto>> {
        return try {
            val rows = postgrest.from(TABLE_RUN_POINTS)
                .select(
                    columns = Columns.list("idx", "lat", "lon"),
                ) {
                    filter { eq("run_id", runId) }
                    order(column = "idx", order = Order.ASCENDING)
                }
                .decodeList<RunPointDto>()
            Result.success(rows)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "getRunPoints($runId) failed", e)
            Result.failure(e)
        }
    }

    companion object {
        private const val TAG = "HistoryRepository"
        private const val TABLE_RUNS = "runs"
        private const val TABLE_RUN_POINTS = "run_points"
        // 50 covers >>1 month of daily runs for our demo profile, and one
        // request stays well under PostgREST's default 1000-row cap.
        private const val MAX_HISTORY_ROWS = 50L
    }
}
