package dev.zhafran.velnyx.feature.tracking.data

import dev.zhafran.velnyx.core.data.db.ActiveRunDao
import dev.zhafran.velnyx.core.data.db.ActiveRunEntity
import dev.zhafran.velnyx.core.data.db.ActiveRunPointEntity
import dev.zhafran.velnyx.core.data.db.ActiveRunSegmentEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActiveRunRepositoryImpl @Inject constructor(
    private val dao: ActiveRunDao,
) : ActiveRunRepository {

    override suspend fun startRun(): Long {
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
        return runId
    }

    override suspend fun pause(runId: Long) {
        val now = System.currentTimeMillis()
        val run = dao.getActiveRun() ?: return
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
        val run = dao.getActiveRun() ?: return
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

    override suspend fun finish(runId: Long) {
        val now = System.currentTimeMillis()
        val run = dao.getActiveRun() ?: return
        // If finished while paused, account for final pause duration
        var totalPaused = run.totalPausedMs
        val pausedSince = run.pausedSince
        if (pausedSince != null) {
            totalPaused += now - pausedSince
        }
        val durationS = ((now - run.startedAt - totalPaused) / 1000).toInt()
        dao.updateRun(
            run.copy(
                state = "FINISHED",
                durationS = durationS,
                totalPausedMs = totalPaused,
                pausedSince = null,
            )
        )
    }

    override fun observeActiveRun(): Flow<ActiveRunEntity?> = dao.observeActiveRun()

    override fun observePoints(runId: Long): Flow<List<ActiveRunPointEntity>> =
        dao.observePoints(runId)
}
