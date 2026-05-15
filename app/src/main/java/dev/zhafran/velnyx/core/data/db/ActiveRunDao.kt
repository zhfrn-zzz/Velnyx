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

    @Query("SELECT * FROM active_runs WHERE state != 'FINISHED' LIMIT 1")
    fun observeActiveRun(): Flow<ActiveRunEntity?>

    @Query("SELECT * FROM active_runs WHERE state != 'FINISHED' LIMIT 1")
    suspend fun getActiveRun(): ActiveRunEntity?

    @Query("SELECT * FROM active_run_points WHERE runId = :runId ORDER BY idx")
    fun observePoints(runId: Long): Flow<List<ActiveRunPointEntity>>

    @Query("SELECT COUNT(*) FROM active_run_points WHERE runId = :runId")
    suspend fun getPointCount(runId: Long): Int

    @Query("SELECT * FROM active_run_segments WHERE runId = :runId ORDER BY segmentIdx")
    suspend fun getSegments(runId: Long): List<ActiveRunSegmentEntity>

    @Query("SELECT COUNT(*) FROM active_run_segments WHERE runId = :runId")
    suspend fun getSegmentCount(runId: Long): Int
}
