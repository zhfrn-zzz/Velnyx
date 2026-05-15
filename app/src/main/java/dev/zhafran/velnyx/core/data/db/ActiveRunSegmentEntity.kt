package dev.zhafran.velnyx.core.data.db

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "active_run_segments",
    primaryKeys = ["runId", "segmentIdx"],
    foreignKeys = [
        ForeignKey(
            entity = ActiveRunEntity::class,
            parentColumns = ["id"],
            childColumns = ["runId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class ActiveRunSegmentEntity(
    val runId: Long,
    val segmentIdx: Int,
    val type: String, // RUNNING or PAUSED
    val startedAt: Long,
)
