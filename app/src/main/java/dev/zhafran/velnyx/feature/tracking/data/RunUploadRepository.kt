package dev.zhafran.velnyx.feature.tracking.data

import dev.zhafran.velnyx.feature.tracking.presentation.RunSummary

/**
 * Uploads a finished run from local Room storage to Supabase.
 *
 * Flow:
 *  1. Read all `ActiveRunPointEntity` rows for `runId` from Room.
 *  2. Apply Douglas-Peucker simplification (ε ≈ 5m) to the polyline.
 *  3. Insert the parent `runs` row, fetching back its server-side UUID.
 *  4. Insert the simplified `run_points` in batches (≤500 per request)
 *     to stay under PostgREST's payload limit.
 *  5. On success, delete the local active-run rows so the device doesn't
 *     keep retrying. On failure, leave the local rows intact so a future
 *     sync can retry.
 *
 * The entire operation is wrapped in try/catch and surfaced as a `Result`.
 * No exceptions escape this method on the failure path other than
 * `CancellationException`.
 */
interface RunUploadRepository {

    /**
     * Upload the finished run identified by [runId].
     *
     * @param runId  The local Room id of the finished active run.
     * @param summary  The aggregated stats already shown to the user on
     *                 the summary screen (single source of truth).
     * @param userId  The Supabase auth user id to attach the run to.
     *
     * @return `Result.success(uuid)` carrying the new `runs.id` UUID
     *         when the upload + cleanup succeed; `Result.failure(...)`
     *         on any error path (Room data left intact for retry).
     */
    suspend fun uploadRun(
        runId: Long,
        summary: RunSummary,
        userId: String,
    ): Result<String>
}
