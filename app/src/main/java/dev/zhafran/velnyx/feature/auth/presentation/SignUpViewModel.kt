package dev.zhafran.velnyx.feature.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.zhafran.velnyx.feature.auth.data.AuthRepository
import dev.zhafran.velnyx.feature.auth.data.AuthState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Sign Up screen.
 * Manages form state, validation, and sign-up via [AuthRepository].
 * Emits one-shot [SignUpEvent]s for navigation instead of relying on AuthState observation.
 * Handles both email-confirmation-ON and email-confirmation-OFF Supabase configurations.
 */
@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SignUpEvent>(
        replay = 0,
        extraBufferCapacity = 1,
    )
    val events: SharedFlow<SignUpEvent> = _events.asSharedFlow()

    fun onDisplayNameChange(displayName: String) {
        _uiState.update {
            it.copy(displayName = displayName, error = null).withValidation()
        }
    }

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

    fun onConfirmPasswordChange(confirmPassword: String) {
        _uiState.update {
            it.copy(confirmPassword = confirmPassword, error = null).withValidation()
        }
    }

    fun onSubmit() {
        val state = _uiState.value
        if (!state.isSubmitEnabled || state.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = authRepository.signUpWithEmail(
                email = state.email.trim(),
                password = state.password,
                displayName = state.displayName.trim(),
            )
            _uiState.update { it.copy(isLoading = false) }
            result
                .onSuccess {
                    // Check if user is immediately authenticated (email confirmation OFF)
                    // or pending confirmation (email confirmation ON)
                    val isAuthenticated =
                        authRepository.authState.first() is AuthState.Authenticated
                    if (isAuthenticated) {
                        _events.emit(SignUpEvent.NavigateToProfileSetup)
                    } else {
                        _events.emit(SignUpEvent.NavigateToConfirmEmail)
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = mapSignUpError(e)) }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

private fun mapSignUpError(e: Throwable): String = when {
    e is io.github.jan.supabase.auth.exception.AuthRestException -> when (e.errorCode?.value) {
        "user_already_exists" -> "An account with this email already exists. Try logging in."
        "weak_password" -> "Password is too weak. Use at least 6 characters."
        "over_request_rate_limit" -> "Too many attempts. Please wait a moment."
        "validation_failed" -> "Please check your email format."
        else -> "Sign up failed. Please try again."
    }
    e is io.github.jan.supabase.exceptions.HttpRequestException -> "No internet connection. Please try again."
    e is java.net.UnknownHostException -> "No internet connection. Please try again."
    else -> "Something went wrong. Please try again."
}
// TODO: After AUTH_PROBE Logcat data is captured, replace mapSignUpError body with:
// private fun mapSignUpError(e: Throwable): String = when {
//     e is io.github.jan.supabase.auth.exception.AuthRestException && e.errorCode == "user_already_exists" -> "An account with this email already exists. Try logging in."
//     e is io.github.jan.supabase.exceptions.RestException && e.statusCode == 422 -> "Password is too weak. Use at least 6 characters."
//     e is io.github.jan.supabase.exceptions.HttpRequestException -> "No internet connection. Please try again."
//     else -> "Something went wrong. Please try again."
// }

sealed interface SignUpEvent {
    data object NavigateToProfileSetup : SignUpEvent
    data object NavigateToConfirmEmail : SignUpEvent
}

data class SignUpUiState(
    val displayName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSubmitEnabled: Boolean = false,
) {
    fun withValidation(): SignUpUiState = copy(
        isSubmitEnabled = displayName.isNotBlank() &&
            email.contains("@") &&
            password.length >= 6 &&
            password == confirmPassword,
    )
}
