package dev.zhafran.velnyx.feature.tracking.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.zhafran.velnyx.core.data.db.ActiveRunEntity
import dev.zhafran.velnyx.core.location.RunTrackingService
import dev.zhafran.velnyx.feature.tracking.data.ActiveRunRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RunStats(
    val distanceM: Int = 0,
    val durationS: Int = 0,
    val avgPaceSPerKm: Int = 0,
)

data class RunSummary(
    val distanceM: Int = 0,
    val durationS: Int = 0,
    val avgPaceSPerKm: Int = 0,
    val calories: Int = 0,
)

sealed interface RunState {
    data object Idle : RunState
    data class Countdown(val secLeft: Int) : RunState
    data class Running(val stats: RunStats) : RunState
    data class Paused(val stats: RunStats) : RunState
    data class Finishing(val stats: RunStats) : RunState
    data class Finished(val summary: RunSummary) : RunState
}

@HiltViewModel
class ActiveRunViewModel @Inject constructor(
    private val app: Application,
    private val repository: ActiveRunRepository,
) : AndroidViewModel(app) {

    private val _state = MutableStateFlow<RunState>(RunState.Idle)
    val state: StateFlow<RunState> = _state

    private var runId: Long = -1L

    fun onStartTapped() {
        if (_state.value !is RunState.Idle) return
        viewModelScope.launch {
            for (i in 3 downTo 1) {
                _state.value = RunState.Countdown(i)
                delay(1000)
            }
            runId = repository.startRun()
            app.startForegroundService(RunTrackingService.startIntent(app, runId))
            _state.value = RunState.Running(RunStats())
            observeRun()
        }
    }

    fun onPauseTapped() {
        if (_state.value !is RunState.Running) return
        viewModelScope.launch {
            repository.pause(runId)
            app.startService(RunTrackingService.pauseIntent(app))
        }
    }

    fun onResumeTapped() {
        if (_state.value !is RunState.Paused) return
        viewModelScope.launch {
            repository.resume(runId)
            app.startService(RunTrackingService.resumeIntent(app))
        }
    }

    fun onFinishTapped() {
        val current = _state.value
        if (current !is RunState.Running && current !is RunState.Paused) return
        viewModelScope.launch {
            val stats = statsFromEntity(null) // will use last known
            _state.value = RunState.Finishing(stats)
            repository.finish(runId)
            app.startService(RunTrackingService.stopIntent(app))
            // Re-read final state from Room
            // Small delay to let Room write complete
            delay(100)
            _state.value = RunState.Finished(
                RunSummary(
                    distanceM = stats.distanceM,
                    durationS = stats.durationS,
                    avgPaceSPerKm = stats.avgPaceSPerKm,
                    calories = 0,
                )
            )
        }
    }

    /**
     * Observes Room for the active run entity. Duration is derived purely
     * from persisted fields: (now - startedAt - totalPausedMs).
     * No in-memory timer — fully crash-safe.
     */
    private fun observeRun() {
        repository.observeActiveRun()
            .onEach { entity ->
                if (entity == null || entity.state == "FINISHED") return@onEach
                val stats = statsFromEntity(entity)
                _state.value = when (entity.state) {
                    "PAUSED" -> RunState.Paused(stats)
                    else -> RunState.Running(stats)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun statsFromEntity(entity: ActiveRunEntity?): RunStats {
        if (entity == null) {
            val current = _state.value
            return when (current) {
                is RunState.Running -> current.stats
                is RunState.Paused -> current.stats
                is RunState.Finishing -> current.stats
                else -> RunStats()
            }
        }
        val now = System.currentTimeMillis()
        val pausedMs = if (entity.pausedSince != null) {
            entity.totalPausedMs + (now - entity.pausedSince)
        } else {
            entity.totalPausedMs
        }
        val durationS = ((now - entity.startedAt - pausedMs) / 1000).toInt().coerceAtLeast(0)
        val distanceM = entity.distanceM
        val pace = if (distanceM >= 10) (durationS * 1000) / distanceM else 0
        return RunStats(distanceM = distanceM, durationS = durationS, avgPaceSPerKm = pace)
    }
}
