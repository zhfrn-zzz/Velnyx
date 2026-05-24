package dev.zhafran.velnyx.core.navigation

import java.util.concurrent.atomic.AtomicLong

/**
 * Process-level holder for an orphaned run ID that should be resumed.
 * Set before navigating to TrackingGraphRoute; consumed once by CountdownRoute.
 */
object PendingResumeRun {
    private val runId = AtomicLong(-1L)

    fun set(id: Long) { runId.set(id) }

    fun consumeIfPresent(): Long {
        return runId.getAndSet(-1L)
    }
}
