package dev.zhafran.velnyx.feature.auth.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zhafran.velnyx.feature.auth.data.AuthState
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onUnauthenticated: () -> Unit,
    onAuthenticated: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()

    // Wait at least 500ms so splash doesn't flash, then navigate based on auth state
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> {
                delay(500L)
                onAuthenticated()
            }
            is AuthState.Unauthenticated -> {
                delay(500L)
                onUnauthenticated()
            }
            is AuthState.Loading -> {
                // Wait for auth state to resolve
            }
        }
    }

    // Simple centered splash visual — real splash design comes later
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "VELNYX",
                style = MaterialTheme.typography.displayMedium,
            )
        }
    }
}
