package dev.zhafran.velnyx.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.zhafran.velnyx.core.data.db.ActiveRunEntity
import dev.zhafran.velnyx.feature.tracking.data.ActiveRunRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val activeRunRepository: ActiveRunRepository,
) : ViewModel() {

    private val _orphanedRun = MutableStateFlow<ActiveRunEntity?>(null)
    val orphanedRun: StateFlow<ActiveRunEntity?> = _orphanedRun

    init {
        viewModelScope.launch {
            _orphanedRun.value = activeRunRepository.checkForOrphanedRun()
        }
    }

    fun discardOrphanedRun() {
        viewModelScope.launch {
            activeRunRepository.deleteAllRunData()
            _orphanedRun.value = null
        }
    }

    fun resumeOrphanedRun() {
        _orphanedRun.value = null
    }
}
