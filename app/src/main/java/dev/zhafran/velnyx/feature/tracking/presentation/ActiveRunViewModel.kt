package dev.zhafran.velnyx.feature.tracking.presentation

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.zhafran.velnyx.core.data.db.ActiveRunEntity
import dev.zhafran.velnyx.core.location.LocationRepository
import dev.zhafran.velnyx.core.location.RunTrackingService
import dev.zhafran.velnyx.core.util.MetCalculator
import dev.zhafran.velnyx.feature.profile.data.ProfileRepository
import dev.zhafran.velnyx.feature.tracking.data.ActiveRunRepository
import org.maplibre.android.geometry.LatLng
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RunStats(
    val distanceM: Int = 0,
    val durationMs: Long = 0L,
    val avgPaceSecondsPerKm: Int = 0,
    val calories: Int = 0,
    val lastAccuracyM: Float = 99f,
    val lastLatLng: LatLng? = null,
)

data class RunSummary(
    val distanceM: Int = 0,
    val durationMs: Long = 0L,
    val avgPaceSecondsPerKm: Int = 0,
    val calories: Int = 0,
)

sealed interface RunState {
    data object Idle : RunState
    data class Countdown(val secLeft: Int) : RunState
    data class Running(val stats: RunStats, val routePoints: List<LatLng> = emptyList()) : RunState
    data class Paused(val stats: RunStats, val routePoints: List<LatLng> = emptyList()) : RunState
    data class Finishing(val stats: RunStats) : RunState
    data class Finished(val summary: RunSummary) : RunState
}

@HiltViewModel
class ActiveRunViewModel @Inject constructor(
    private val app: Application,
    private val repository: ActiveRunRepository,
    private val profileRepository: ProfileRepository,
    private val locationRepository: LocationRepository,
) : AndroidViewModel(app) {

    private val _state = MutableStateFlow<RunState>(RunState.Idle)
    val state: StateFlow<RunState> = _state

    private val _errors = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val errors: SharedFlow<String> = _errors

    private var runId: Long = -1L
    private var weightKg: Float = 70f
    private var displayTimerJob: Job? = null

    init {
        viewModelScope.launch {
            val profile = profileRepository.getProfileOnce().getOrNull()
            if (profile != null) weightKg = profile.weightKg.toFloat()
        }
    }

    fun onStartTapped() {
        if (_state.value !is RunState.Idle) return
        viewModelScope.launch {
            try {
                for (i in 3 downTo 1) {
                    _state.value = RunState.Countdown(i)
                    delay(1000)
                }
                val result = repository.startRun()
                val newId = result.getOrElse { e ->
                    Log.e("ActiveRunViewModel", "startRun failed", e)
                    _state.value = RunState.Idle
                    _errors.tryEmit("Could not start run: ${e.message}")
                    return@launch
                }
                runId = newId
                app.startForegroundService(RunTrackingService.startIntent(app, runId))
                _state.value = RunState.Running(RunStats())
                observeRun(runId)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("ActiveRunViewModel", "onStartTapped failed", e)
                _state.value = RunState.Idle
                _errors.tryEmit("Could not start run: ${e.message}")
            }
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
        val routePoints = when (current) {
            is RunState.Running -> current.routePoints
            is RunState.Paused -> current.routePoints
            else -> emptyList()
        }
        stopDisplayTimer()
        viewModelScope.launch {
            val stats = currentStats()
            try {
                _state.value = RunState.Finishing(stats)
                repository.finish(runId)
                    .onFailure { e -> Log.e("ActiveRunViewModel", "repository.finish failed", e) }
                runCatching { app.startService(RunTrackingService.stopIntent(app)) }
                    .onFailure { e -> Log.e("ActiveRunViewModel", "stopService failed", e) }
                delay(100)
                _state.value = RunState.Finished(
                    RunSummary(
                        distanceM = stats.distanceM,
                        durationMs = stats.durationMs,
                        avgPaceSecondsPerKm = stats.avgPaceSecondsPerKm,
                        calories = stats.calories,
                    )
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("ActiveRunViewModel", "onFinishTapped failed", e)
                _errors.tryEmit("Could not finish run: ${e.message}")
                _state.value = RunState.Paused(stats, routePoints)
            }
        }
    }

    private fun observeRun(runId: Long) {
        repository.observeRunById(runId)
            .catch { e ->
                if (e is CancellationException) throw e
                Log.e("ActiveRunViewModel", "observeRunById($runId) failed", e)
            }
            .onEach { entity ->
                try {
                    if (entity == null || entity.state == "FINISHED") return@onEach
                    val stats = buildStats(entity)
                    when (entity.state) {
                        "PAUSED" -> {
                            stopDisplayTimer()
                            _state.value = RunState.Paused(stats)
                        }
                        else -> {
                            _state.value = RunState.Running(stats)
                            startDisplayTimer(runId)
                        }
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e("ActiveRunViewModel", "observeRun collect failed", e)
                }
            }
            .launchIn(viewModelScope)

        // Observe points for accuracy + route polyline. NOTE: this
        // stream is filtered (accuracy <= 30m AND speed >= 1 m/s) by
        // RunTrackingService before the points hit Room, so it is the
        // right source for distance/pace/calories and for the route
        // polyline — but it must NOT drive the map marker, otherwise
        // the marker disappears whenever the user is indoors or
        // standing still. The marker is driven by `rawLocationFlow`
        // below.
        repository.observePoints(runId)
            .catch { e ->
                if (e is CancellationException) throw e
                Log.e("ActiveRunViewModel", "observePoints($runId) failed", e)
            }
            .onEach { points ->
                val lastAccuracy = points.lastOrNull()?.accuracy ?: 99f
                val route = points.map { LatLng(it.lat, it.lon) }
                val current = _state.value
                _state.value = when (current) {
                    is RunState.Running -> current.copy(
                        stats = current.stats.copy(lastAccuracyM = lastAccuracy),
                        routePoints = route,
                    )
                    is RunState.Paused -> current.copy(
                        stats = current.stats.copy(lastAccuracyM = lastAccuracy),
                        routePoints = route,
                    )
                    else -> current
                }
            }
            .launchIn(viewModelScope)

        // Observe the raw (unfiltered) location stream from the fused
        // provider. This drives the map marker only — it includes
        // indoor WiFi/cell-tower fixes that would otherwise be
        // discarded by the metrics filter.
        locationRepository.rawLocationFlow
            .onEach { latLng ->
                try {
                    if (latLng == null) return@onEach
                    val current = _state.value
                    _state.value = when (current) {
                        is RunState.Running -> current.copy(
                            stats = current.stats.copy(lastLatLng = latLng),
                        )
                        is RunState.Paused -> current.copy(
                            stats = current.stats.copy(lastLatLng = latLng),
                        )
                        else -> current
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e("ActiveRunViewModel", "rawLocationFlow collect failed", e)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun startDisplayTimer(runId: Long) {
        if (displayTimerJob?.isActive == true) return
        displayTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val entity = repository.getRunById(runId) ?: continue
                if (entity.state != "RUNNING") break
                val now = System.currentTimeMillis()
                val durationMs = (now - entity.startedAt - entity.totalPausedMs).coerceAtLeast(0)
                val current = _state.value
                if (current is RunState.Running) {
                    _state.value = current.copy(stats = current.stats.copy(durationMs = durationMs))
                }
            }
        }
    }

    private fun stopDisplayTimer() {
        displayTimerJob?.cancel()
        displayTimerJob = null
    }

    private fun buildStats(entity: ActiveRunEntity): RunStats {
        val now = System.currentTimeMillis()
        val pausedMs = if (entity.pausedSince != null) {
            entity.totalPausedMs + (now - entity.pausedSince)
        } else {
            entity.totalPausedMs
        }
        val durationMs = (now - entity.startedAt - pausedMs).coerceAtLeast(0)
        val distanceM = entity.distanceM
        val pace = if (distanceM >= 10) ((durationMs / 1000).toInt() * 1000) / distanceM else 0
        val durationHours = durationMs / 3_600_000f
        val calories = MetCalculator.calculate(pace, weightKg, durationHours)
        // Preserve fields that aren't derived from the entity:
        //  - lastAccuracyM is set by the points observer
        //  - lastLatLng is set by the raw-location observer
        // Pulling them from currentStats() avoids resetting them on
        // every entity emission.
        val carry = currentStats()
        return RunStats(
            distanceM = distanceM,
            durationMs = durationMs,
            avgPaceSecondsPerKm = pace,
            calories = calories,
            lastAccuracyM = carry.lastAccuracyM,
            lastLatLng = carry.lastLatLng,
        )
    }

    private fun currentStats(): RunStats = when (val s = _state.value) {
        is RunState.Running -> s.stats
        is RunState.Paused -> s.stats
        is RunState.Finishing -> s.stats
        else -> RunStats()
    }
}
