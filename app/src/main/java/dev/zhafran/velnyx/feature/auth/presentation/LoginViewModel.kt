package dev.zhafran.velnyx.feature.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.zhafran.velnyx.feature.auth.data.AuthRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Login screen.
 * Manages form state, validation, and sign-in via [AuthRepository].
 * Emits one-shot [LoginEvent]s for navigation instead of relying on AuthState observation.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<LoginEvent>(
        replay = 0,
        extraBufferCapacity = 1,
    )
    val events: SharedFlow<LoginEvent> = _events.asSharedFlow()

    fun onEmailChange(email: String) {
        _uiState.update {
            it.copy(email = email, error = null).withValidation()
        }
    }

    fun onPasswordChange(password: String) {
        _uiState.update {
            it.copy(password = password, error = null).withValidation()
        }
    }

    fun onSubmit() {
        val state = _uiState.value
        if (!state.isSubmitEnabled || state.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = authRepository.signInWithEmail(state.email.trim(), state.password)
            _uiState.update { it.copy(isLoading = false) }
            result
                .onSuccess { _events.emit(LoginEvent.NavigateToHome) }
                .onFailure { e ->
                    _uiState.update { it.copy(error = mapLoginError(e)) }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

private fun mapLoginError(e: Throwable): String = when {
    e is io.github.jan.supabase.auth.exception.AuthRestException -> when (e.errorCode?.value) {
        "invalid_credentials" -> "Email or password is incorrect."
        "email_not_confirmed" -> "Please confirm your email first. Check your inbox."
        "over_request_rate_limit" -> "Too many attempts. Please wait a moment."
        else -> "Authentication failed. Please try again."
    }
    e is io.github.jan.supabase.exceptions.HttpRequestException -> "No internet connection. Please try again."
    e is java.net.UnknownHostException -> "No internet connection. Please try again."
    else -> "Something went wrong. Please try again."
}

// TODO: After AUTH_PROBE Logcat data is captured, replace mapLoginError body with:
// private fun mapLoginError(e: Throwable): String = when {
//     e is io.github.jan.supabase.auth.exception.AuthRestException && e.errorCode == "invalid_credentials" -> "Email or password is incorrect"
//     e is io.github.jan.supabase.exceptions.RestException && e.statusCode == 400 -> "Invalid request. Please check your inputs."
//     e is io.github.jan.supabase.exceptions.HttpRequestException -> "No internet connection. Please try again."
//     else -> "Something went wrong. Please try again."
// }

sealed interface LoginEvent {
    data object NavigateToHome : LoginEvent
}

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSubmitEnabled: Boolean = false,
) {
    fun withValidation(): LoginUiState = copy(
        isSubmitEnabled = email.contains("@") && password.length >= 6,
    )
}
