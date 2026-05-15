package dev.zhafran.velnyx.feature.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.zhafran.velnyx.feature.auth.data.AuthRepository
import dev.zhafran.velnyx.feature.auth.data.AuthState
import dev.zhafran.velnyx.feature.profile.data.ProfileRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepo: AuthRepository,
    private val profileRepo: ProfileRepository,
) : ViewModel() {

    private val _splashState = MutableStateFlow<SplashState>(SplashState.Loading)
    val splashState: StateFlow<SplashState> = _splashState.asStateFlow()

    init {
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()

            val authState = authRepo.authState.first { it !is AuthState.Loading }

            when (authState) {
                is AuthState.Unauthenticated -> {
                    enforceMinDelay(startTime)
                    _splashState.value = SplashState.GoToGetStarted
                }
                is AuthState.Authenticated -> {
                    val profileResult = profileRepo.getProfileOnce()
                    val hasProfile = profileResult.getOrNull() != null
                    enforceMinDelay(startTime)
                    _splashState.value = if (hasProfile) {
                        SplashState.GoToHome
                    } else {
                        SplashState.GoToProfileSetup
                    }
                }
                is AuthState.Loading -> { /* unreachable after first { } */ }
            }
        }
    }

    private suspend fun enforceMinDelay(startTime: Long) {
        val elapsed = System.currentTimeMillis() - startTime
        if (elapsed < MIN_SPLASH_MS) {
            delay(MIN_SPLASH_MS - elapsed)
        }
    }

    companion object {
        private const val MIN_SPLASH_MS = 500L
    }
}

sealed interface SplashState {
    data object Loading : SplashState
    data object GoToGetStarted : SplashState
    data object GoToProfileSetup : SplashState
    data object GoToHome : SplashState
}
