package dev.zhafran.velnyx.core.data.db

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "active_run_points",
    primaryKeys = ["runId", "idx"],
    foreignKeys = [
        ForeignKey(
            entity = ActiveRunEntity::class,
            parentColumns = ["id"],
            childColumns = ["runId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class ActiveRunPointEntity(
    val runId: Long,
    val idx: Int,
    val lat: Double,
    val lon: Double,
    val recordedAt: Long,
    val accuracy: Float,
    val speed: Float,
)
