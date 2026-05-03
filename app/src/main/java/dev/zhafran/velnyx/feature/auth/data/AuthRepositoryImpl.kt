package dev.zhafran.velnyx.feature.auth.data

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Supabase-backed implementation of [AuthRepository].
 * Maps Supabase [SessionStatus] to app-level [AuthState].
 * Auth operations return raw [Result] — error mapping is handled by ViewModels.
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: Auth,
) : AuthRepository {

    override val authState: Flow<AuthState> = auth.sessionStatus.map { status ->
        when (status) {
            is SessionStatus.Initializing -> AuthState.Loading
            is SessionStatus.Authenticated -> AuthState.Authenticated(
                userId = status.session.user?.id ?: "",
                email = status.session.user?.email,
            )
            is SessionStatus.NotAuthenticated -> AuthState.Unauthenticated
            is SessionStatus.RefreshFailure -> AuthState.Unauthenticated
        }
    }

    override suspend fun signUpWithEmail(
        email: String,
        password: String,
        displayName: String,
    ): Result<Unit> = runCatching {
        auth.signUpWith(Email) {
            this.email = email
            this.password = password
            this.data = buildJsonObject {
                put("display_name", displayName)
            }
        }
        Unit
    }

    override suspend fun signInWithEmail(
        email: String,
        password: String,
    ): Result<Unit> = runCatching {
        auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<Unit> {
        throw NotImplementedError("Google OAuth in next milestone task")
    }

    override suspend fun signOut() {
        auth.signOut()
    }
}
