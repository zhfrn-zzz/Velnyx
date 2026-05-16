package dev.zhafran.velnyx.feature.history.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.zhafran.velnyx.feature.history.data.HistoryRepository
import dev.zhafran.velnyx.feature.history.data.RunSummaryDto
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the history list screen. Use a sealed interface so the
 * `when` expressions in the screen are exhaustive at compile time.
 */
sealed interface HistoryUiState {
    data object Loading : HistoryUiState
    data object Empty : HistoryUiState
    data class Success(val runs: List<RunSummaryDto>) : HistoryUiState
    data class Error(val msg: String) : HistoryUiState
}

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: HistoryRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<HistoryUiState>(HistoryUiState.Loading)

    /**
     * Property name kept as `runs` per the M3 spec, even though it
     * exposes the full UI state. The screen treats it as state-of-the-runs.
     */
    val runs: StateFlow<HistoryUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun refresh() {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            // Don't reset to Loading on refresh — the PullToRefreshBox
            // already shows its own indicator. Resetting would hide the
            // existing list during the network round-trip and feel janky.
            // Only show Loading on the very first load when state is
            // already Loading (its initial value) or Error.
            val previous = _state.value
            if (previous is HistoryUiState.Error) {
                _state.value = HistoryUiState.Loading
            }
            try {
                repository.getRunHistory()
                    .onSuccess { runs ->
                        _state.value = if (runs.isEmpty()) {
                            HistoryUiState.Empty
                        } else {
                            HistoryUiState.Success(runs)
                        }
                    }
                    .onFailure { e ->
                        Log.e(TAG, "getRunHistory failed", e)
                        _state.value = HistoryUiState.Error(
                            e.message ?: "Couldn't load your runs"
                        )
                    }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "load failed", e)
                _state.value = HistoryUiState.Error(
                    e.message ?: "Couldn't load your runs"
                )
            }
        }
    }

    companion object {
        private const val TAG = "HistoryViewModel"
    }
}
