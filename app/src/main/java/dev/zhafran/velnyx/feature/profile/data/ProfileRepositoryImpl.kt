package dev.zhafran.velnyx.feature.profile.data

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val auth: Auth,
    private val postgrest: Postgrest,
) : ProfileRepository {

    override fun observeProfile(): Flow<Profile?> = auth.sessionStatus.map { status ->
        when (status) {
            is SessionStatus.Authenticated -> {
                val userId = status.session.user?.id ?: return@map null
                runCatching {
                    postgrest.from("profiles")
                        .select {
                            filter { eq("id", userId) }
                        }
                        .decodeSingleOrNull<ProfileDto>()
                        ?.toDomain()
                }.getOrNull()
            }
            else -> null
        }
    }

    override suspend fun upsertProfile(
        displayName: String,
        weightKg: Double,
    ): Result<Unit> {
        val userId = auth.currentSessionOrNull()?.user?.id
            ?: return Result.failure(IllegalStateException("Not authenticated"))
        return runCatching {
            postgrest.from("profiles").upsert(
                ProfileDto(
                    id = userId,
                    displayName = displayName,
                    weightKg = weightKg,
                ),
            )
            Unit
        }
    }

    override suspend fun getProfileOnce(): Result<Profile?> {
        val userId = auth.currentSessionOrNull()?.user?.id
            ?: return Result.failure(IllegalStateException("Not authenticated"))
        return runCatching {
            postgrest.from("profiles")
                .select {
                    filter { eq("id", userId) }
                }
                .decodeSingleOrNull<ProfileDto>()
                ?.toDomain()
        }
    }
}
