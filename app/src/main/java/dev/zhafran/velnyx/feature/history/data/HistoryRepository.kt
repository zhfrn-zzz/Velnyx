package dev.zhafran.velnyx.feature.history.data

/**
 * Read-side repository for the run history feature.
 *
 * Both calls hit Supabase directly — there is no Room-side cache for
 * history at this point in the milestone (plan.md M3 lists "cached
 * locally in Room" as a goal, but it's not required for the demo
 * journey and would add complexity that doesn't pay off before the
 * deadline).
 *
 * RLS on the `runs` and `run_points` tables ensures the queries return
 * only the current user's data — no `auth.uid()` filter is needed in
 * the client.
 */
interface HistoryRepository {

    /**
     * Returns the 50 most-recent runs for the current user, newest first.
     */
    suspend fun getRunHistory(): Result<List<RunSummaryDto>>

    /**
     * Returns all stored points for [runId] in idx-ascending order.
     * The list may be empty (run was finished with zero GPS fixes).
     */
    suspend fun getRunPoints(runId: String): Result<List<RunPointDto>>
}
