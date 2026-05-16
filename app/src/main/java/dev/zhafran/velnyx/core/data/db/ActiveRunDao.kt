package dev.zhafran.velnyx.core.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ActiveRunDao {

    @Insert
    suspend fun insertRun(run: ActiveRunEntity): Long

    @Update
    suspend fun updateRun(run: ActiveRunEntity)

    @Query("UPDATE active_runs SET pausedSince = :pausedSince WHERE id = :id")
    suspend fun updatePausedSince(id: Long, pausedSince: Long?)

    @Query("UPDATE active_runs SET totalPausedMs = totalPausedMs + :additionalMs WHERE id = :id")
    suspend fun addPausedDuration(id: Long, additionalMs: Long)

    @Insert
    suspend fun insertPoint(point: ActiveRunPointEntity)

    @Insert
    suspend fun insertSegment(segment: ActiveRunSegmentEntity)

    @Query("SELECT * FROM active_runs WHERE state != 'FINISHED' ORDER BY startedAt DESC LIMIT 1")
    fun observeActiveRun(): Flow<ActiveRunEntity?>

    @Query("SELECT * FROM active_runs WHERE state != 'FINISHED' ORDER BY startedAt DESC LIMIT 1")
    suspend fun getActiveRun(): ActiveRunEntity?

    @Query("SELECT * FROM active_runs WHERE id = :runId LIMIT 1")
    fun observeRunById(runId: Long): Flow<ActiveRunEntity?>

    @Query("SELECT * FROM active_runs WHERE id = :runId LIMIT 1")
    suspend fun getRunById(runId: Long): ActiveRunEntity?

    @Query("DELETE FROM active_runs")
    suspend fun deleteAllActiveRuns()

    @Query("DELETE FROM active_run_points")
    suspend fun deleteAllActivePoints()

    @Query("DELETE FROM active_run_segments")
    suspend fun deleteAllActiveSegments()

    @Query("SELECT * FROM active_run_points WHERE runId = :runId ORDER BY idx")
    fun observePoints(runId: Long): Flow<List<ActiveRunPointEntity>>

    // One-shot snapshot of points, used by the upload pipeline (M3) so it
    // doesn't have to consume a Flow inside a non-collecting suspend path.
    @Query("SELECT * FROM active_run_points WHERE runId = :runId ORDER BY idx")
    suspend fun getPoints(runId: Long): List<ActiveRunPointEntity>

    @Query("SELECT COUNT(*) FROM active_run_points WHERE runId = :runId")
    suspend fun getPointCount(runId: Long): Int

    // Per-id delete used by the upload pipeline after a successful Supabase
    // sync. Foreign keys on active_run_points and active_run_segments cascade,
    // so this single statement cleans up all three tables for `runId`.
    @Query("DELETE FROM active_runs WHERE id = :runId")
    suspend fun deleteActiveRunById(runId: Long)

    @Query("SELECT * FROM active_run_segments WHERE runId = :runId ORDER BY segmentIdx")
    suspend fun getSegments(runId: Long): List<ActiveRunSegmentEntity>

    @Query("SELECT COUNT(*) FROM active_run_segments WHERE runId = :runId")
    suspend fun getSegmentCount(runId: Long): Int
}
