package dev.zhafran.velnyx.core.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "active_runs")
data class ActiveRunEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startedAt: Long,
    val state: String, // RUNNING, PAUSED, FINISHED
    val distanceM: Int,
    val durationS: Int,
    val totalPausedMs: Long = 0L,
    val pausedSince: Long? = null,
)
