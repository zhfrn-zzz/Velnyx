package dev.zhafran.velnyx.feature.auth.data

/**
 * Represents the current authentication state of the user.
 * Observed by Splash, Login, and SignUp screens to drive navigation.
 */
sealed interface AuthState {

    /** Session status is being loaded from storage. */
    data object Loading : AuthState

    /** User is not authenticated. */
    data object Unauthenticated : AuthState

    /** User is authenticated with a valid session. */
    data class Authenticated(
        val userId: String,
        val email: String?,
    ) : AuthState
}
