package dev.zhafran.velnyx.feature.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.zhafran.velnyx.feature.profile.data.ProfileRepository
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException
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

@HiltViewModel
class ProfileSetupViewModel @Inject constructor(
    private val profileRepo: ProfileRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileSetupUiState())
    val state: StateFlow<ProfileSetupUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<ProfileSetupEvent>(
        replay = 0,
        extraBufferCapacity = 1,
    )
    val events: SharedFlow<ProfileSetupEvent> = _events.asSharedFlow()

    private var hasAttemptedSubmit = false

    fun onDisplayNameChange(value: String) {
        _state.update { it.copy(displayName = value) }
        revalidate()
    }

    fun onWeightChange(value: String) {
        _state.update { it.copy(weightKgText = value) }
        revalidate()
    }

    fun onSubmit() {
        hasAttemptedSubmit = true
        revalidate()

        val current = _state.value
        if (current.displayNameError != null || current.weightError != null) return
        if (current.displayName.isBlank() || current.weightKgText.isBlank()) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, generalError = null) }
            val result = profileRepo.upsertProfile(
                displayName = current.displayName.trim(),
                weightKg = current.weightKgText.toDouble(),
            )
            _state.update { it.copy(isLoading = false) }
            result.onSuccess {
                _events.tryEmit(ProfileSetupEvent.NavigateToPermissions)
            }
            result.onFailure { e ->
                _state.update { it.copy(generalError = mapProfileError(e)) }
            }
        }
    }

    private fun revalidate() {
        val name = _state.value.displayName
        val weightText = _state.value.weightKgText

        val nameError = if (!hasAttemptedSubmit) {
            null
        } else if (name.isBlank() || name.trim().length !in 1..50) {
            "Name must be 1–50 characters"
        } else {
            null
        }

        val weightKg = weightText.toDoubleOrNull()
        val weightError = if (!hasAttemptedSubmit) {
            null
        } else if (weightKg == null) {
            "Enter a valid number"
        } else if (weightKg !in 20.0..300.0) {
            "Enter weight between 20–300 kg"
        } else {
            null
        }

        val isValid = name.trim().length in 1..50 &&
            weightKg != null &&
            weightKg in 20.0..300.0

        _state.update {
            it.copy(
                displayNameError = nameError,
                weightError = weightError,
                isSubmitEnabled = isValid && !it.isLoading,
            )
        }
    }

    private fun mapProfileError(e: Throwable): String = when {
        e is IllegalStateException && e.message?.contains("Not authenticated") == true ->
            "Session expired. Please sign in again."
        e is RestException ->
            "Invalid profile data."
        e is HttpRequestException || e is UnknownHostException ->
            "No internet connection. Please try again."
        else ->
            "Could not save profile. Please try again."
    }
}

data class ProfileSetupUiState(
    val displayName: String = "",
    val weightKgText: String = "",
    val isLoading: Boolean = false,
    val displayNameError: String? = null,
    val weightError: String? = null,
    val generalError: String? = null,
    val isSubmitEnabled: Boolean = false,
)

sealed interface ProfileSetupEvent {
    data object NavigateToPermissions : ProfileSetupEvent
}
