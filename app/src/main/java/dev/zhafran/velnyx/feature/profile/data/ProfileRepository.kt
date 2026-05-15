package dev.zhafran.velnyx.feature.profile.data

import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun observeProfile(): Flow<Profile?>
    suspend fun upsertProfile(displayName: String, weightKg: Double): Result<Unit>
    suspend fun getProfileOnce(): Result<Profile?>
}
