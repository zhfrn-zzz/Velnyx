package dev.zhafran.velnyx.feature.tracking.data

import android.util.Log
import dev.zhafran.velnyx.core.data.db.ActiveRunDao
import dev.zhafran.velnyx.core.data.db.ActiveRunEntity
import dev.zhafran.velnyx.core.data.db.ActiveRunPointEntity
import dev.zhafran.velnyx.core.data.db.ActiveRunSegmentEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActiveRunRepositoryImpl @Inject constructor(
    private val dao: ActiveRunDao,
) : ActiveRunRepository {

    override suspend fun startRun(): Result<Long> {
        return try {
            // Guarantee a clean slate: any orphaned rows from a prior crashed run
            // would otherwise be picked up by observeActiveRun() and inflate the timer.
            dao.deleteAllActivePoints()
            dao.deleteAllActiveSegments()
            dao.deleteAllActiveRuns()

            val now = System.currentTimeMillis()
            val runId = dao.insertRun(
                ActiveRunEntity(
                    startedAt = now,
                    state = "RUNNING",
                    distanceM = 0,
                    durationS = 0,
                    totalPausedMs = 0L,
                    pausedSince = null,
                )
            )
            dao.insertSegment(
                ActiveRunSegmentEntity(
                    runId = runId,
                    segmentIdx = 0,
                    type = "RUNNING",
                    startedAt = now,
                )
            )
            Result.success(runId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("ActiveRunRepository", "startRun failed", e)
            Result.failure(e)
        }
    }

    override suspend fun pause(runId: Long) {
        val now = System.currentTimeMillis()
        val run = dao.getRunById(runId) ?: return
        dao.updateRun(run.copy(state = "PAUSED"))
        // Persist pause start time to Room — crash-safe
        dao.updatePausedSince(runId, now)
        val segIdx = dao.getSegmentCount(runId)
        dao.insertSegment(
            ActiveRunSegmentEntity(
                runId = runId,
                segmentIdx = segIdx,
                type = "PAUSED",
                startedAt = now,
            )
        )
    }

    override suspend fun resume(runId: Long) {
        val now = System.currentTimeMillis()
        val run = dao.getRunById(runId) ?: return
        // Transaction: compute paused duration, add to total, clear pausedSince
        val pausedSince = run.pausedSince
        if (pausedSince != null) {
            val elapsed = now - pausedSince
            dao.addPausedDuration(runId, elapsed)
        }
        dao.updatePausedSince(runId, null)
        dao.updateRun(run.copy(state = "RUNNING", pausedSince = null))
        val segIdx = dao.getSegmentCount(runId)
        dao.insertSegment(
            ActiveRunSegmentEntity(
                runId = runId,
                segmentIdx = segIdx,
                type = "RUNNING",
                startedAt = now,
            )
        )
    }

    override suspend fun finish(runId: Long): Result<Unit> {
        return try {
            val now = System.currentTimeMillis()
            val run = dao.getRunById(runId)
            if (run == null) {
                // Nothing to finalize (e.g. 0-GPS-point run that was never inserted,
                // or runId == -1). Treat as success with zero-valued summary upstream.
                return Result.success(Unit)
            }
            // If finished while paused, account for final pause duration.
            var totalPaused = run.totalPausedMs
            val pausedSince = run.pausedSince
            if (pausedSince != null) {
                totalPaused += now - pausedSince
            }
            val durationS = ((now - run.startedAt - totalPaused) / 1000)
                .toInt()
                .coerceAtLeast(0)
            dao.updateRun(
                run.copy(
                    state = "FINISHED",
                    durationS = durationS,
                    totalPausedMs = totalPaused,
                    pausedSince = null,
                )
            )
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("ActiveRunRepository", "finish($runId) failed", e)
            Result.failure(e)
        }
    }

    override suspend fun checkForOrphanedRun(): ActiveRunEntity? = dao.getActiveRun()

    override suspend fun deleteAllRunData() {
        dao.deleteAllActivePoints()
        dao.deleteAllActiveSegments()
        dao.deleteAllActiveRuns()
    }

    override fun observeActiveRun(): Flow<ActiveRunEntity?> = dao.observeActiveRun()

    override fun observeRunById(runId: Long): Flow<ActiveRunEntity?> = dao.observeRunById(runId)

    override suspend fun getRunById(runId: Long): ActiveRunEntity? = dao.getRunById(runId)

    override fun observePoints(runId: Long): Flow<List<ActiveRunPointEntity>> =
        dao.observePoints(runId)
}
