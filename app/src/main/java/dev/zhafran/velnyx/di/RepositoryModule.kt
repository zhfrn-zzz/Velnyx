package dev.zhafran.velnyx.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.zhafran.velnyx.feature.auth.data.AuthRepository
import dev.zhafran.velnyx.feature.auth.data.AuthRepositoryImpl
import dev.zhafran.velnyx.feature.profile.data.ProfileRepository
import dev.zhafran.velnyx.feature.profile.data.ProfileRepositoryImpl
import dev.zhafran.velnyx.feature.tracking.data.ActiveRunRepository
import dev.zhafran.velnyx.feature.tracking.data.ActiveRunRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    abstract fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository

    @Binds
    abstract fun bindActiveRunRepository(impl: ActiveRunRepositoryImpl): ActiveRunRepository
}
