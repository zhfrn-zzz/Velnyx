package dev.zhafran.velnyx.feature.auth.data

import kotlinx.coroutines.flow.Flow

/**
 * Repository contract for authentication operations.
 * All auth access goes through this interface, never the implementation directly.
 */
interface AuthRepository {

    /** Observable stream of the current authentication state. */
    val authState: Flow<AuthState>

    /** Sign up a new user with email, password, and display name metadata. */
    suspend fun signUpWithEmail(email: String, password: String, displayName: String): Result<Unit>

    /** Sign in an existing user with email and password. */
    suspend fun signInWithEmail(email: String, password: String): Result<Unit>

    /** Sign in with Google OAuth. Not implemented in this milestone. */
    suspend fun signInWithGoogle(idToken: String): Result<Unit>

    /** Sign out the current user. */
    suspend fun signOut()
}
