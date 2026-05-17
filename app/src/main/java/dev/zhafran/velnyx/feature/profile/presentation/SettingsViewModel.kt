package dev.zhafran.velnyx.feature.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.zhafran.velnyx.feature.auth.data.AuthRepository
import io.github.jan.supabase.exceptions.HttpRequestException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.UnknownHostException
import javax.inject.Inject

/**
 * ViewModel backing [SettingsScreen]. Owns the sign-out action.
 * Emits one-shot [SettingsEvent] for navigation; transient errors surface via [SettingsUiState.error].
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<SettingsEvent>(
        replay = 0,
        extraBufferCapacity = 1,
    )
    val events: SharedFlow<SettingsEvent> = _events.asSharedFlow()

    fun onSignOutClicked() {
        if (_state.value.isSigningOut) return
        viewModelScope.launch {
            _state.update { it.copy(isSigningOut = true, error = null) }
            try {
                authRepository.signOut()
                _events.emit(SettingsEvent.NavigateToGetStarted)
            } catch (ce: CancellationException) {
                throw ce
            } catch (t: Throwable) {
                _state.update {
                    it.copy(isSigningOut = false, error = mapSignOutError(t))
                }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    private fun mapSignOutError(e: Throwable): String = when (e) {
        is HttpRequestException, is UnknownHostException ->
            "No internet connection. Please try again."
        else -> "Sign out failed. Please try again."
    }
}

data class SettingsUiState(
    val isSigningOut: Boolean = false,
    val error: String? = null,
)

sealed interface SettingsEvent {
    data object NavigateToGetStarted : SettingsEvent
}
