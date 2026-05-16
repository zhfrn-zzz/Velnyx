package dev.zhafran.velnyx.feature.history.presentation

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.zhafran.velnyx.core.navigation.HistoryDetailRoute
import dev.zhafran.velnyx.feature.history.data.HistoryRepository
import dev.zhafran.velnyx.feature.history.data.RunPointDto
import dev.zhafran.velnyx.feature.history.data.RunSummaryDto
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the history detail screen.
 *
 * NOTE: There is no Empty state here. A run with zero stored points is
 * still a valid Success — the screen will show the stats grid and a
 * "no route recorded" placeholder where the map would be.
 */
sealed interface HistoryDetailUiState {
    data object Loading : HistoryDetailUiState
    data class Success(
        val run: RunSummaryDto,
        val points: List<RunPointDto>,
    ) : HistoryDetailUiState
    data class Error(val msg: String) : HistoryDetailUiState
}

@HiltViewModel
class HistoryDetailViewModel @Inject constructor(
    private val repository: HistoryRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    // Decoded from the type-safe nav route. Crashes loudly if the screen
    // is somehow shown without the runId arg — that would be a wiring
    // bug, not a runtime failure mode.
    private val runId: String = savedStateHandle.toRoute<HistoryDetailRoute>().runId

    private val _state = MutableStateFlow<HistoryDetailUiState>(HistoryDetailUiState.Loading)
    val state: StateFlow<HistoryDetailUiState> = _state.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            _state.value = HistoryDetailUiState.Loading
            try {
                // Fetch run summary + points in parallel.
                //
                // Limitation acknowledged: the repo only exposes
                // getRunHistory() (returns 50 most-recent rows), so for
                // older runs this won't resolve. For the demo journey
                // (sign in → run → save → list → detail), the just-saved
                // run will always be in the most-recent 50. Adding a
                // dedicated getRunById(runId) is the obvious follow-up
                // if/when this becomes a real problem.
                val (historyResult, pointsResult) = coroutineScope {
                    val historyDeferred = async { repository.getRunHistory() }
                    val pointsDeferred = async { repository.getRunPoints(runId) }
                    awaitAll(historyDeferred, pointsDeferred)
                }
                @Suppress("UNCHECKED_CAST")
                val historyRes = historyResult as Result<List<RunSummaryDto>>
                @Suppress("UNCHECKED_CAST")
                val pointsRes = pointsResult as Result<List<RunPointDto>>

                val history = historyRes.getOrElse { e ->
                    Log.e(TAG, "history fetch failed", e)
                    _state.value = HistoryDetailUiState.Error(
                        e.message ?: "Couldn't load run"
                    )
                    return@launch
                }
                val points = pointsRes.getOrElse { e ->
                    Log.e(TAG, "points fetch failed", e)
                    _state.value = HistoryDetailUiState.Error(
                        e.message ?: "Couldn't load route"
                    )
                    return@launch
                }

                val run = history.firstOrNull { it.id == runId }
                if (run == null) {
                    _state.value = HistoryDetailUiState.Error(
                        "Run not found in your most recent activity"
                    )
                } else {
                    _state.value = HistoryDetailUiState.Success(
                        run = run,
                        points = points,
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "load($runId) failed", e)
                _state.value = HistoryDetailUiState.Error(
                    e.message ?: "Couldn't load run"
                )
            }
        }
    }

    companion object {
        private const val TAG = "HistoryDetailViewModel"
    }
}
