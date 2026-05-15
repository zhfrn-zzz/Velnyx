package dev.zhafran.velnyx.feature.tracking.data

import dev.zhafran.velnyx.core.data.db.ActiveRunEntity
import dev.zhafran.velnyx.core.data.db.ActiveRunPointEntity
import kotlinx.coroutines.flow.Flow

interface ActiveRunRepository {
    suspend fun startRun(): Long
    suspend fun pause(runId: Long)
    suspend fun resume(runId: Long)
    suspend fun finish(runId: Long)
    fun observeActiveRun(): Flow<ActiveRunEntity?>
    fun observePoints(runId: Long): Flow<List<ActiveRunPointEntity>>
}
