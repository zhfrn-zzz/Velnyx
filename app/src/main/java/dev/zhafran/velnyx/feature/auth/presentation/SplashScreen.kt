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

@Composable
fun SplashScreen(
    onGoToGetStarted: () -> Unit,
    onGoToProfileSetup: () -> Unit,
    onGoToHome: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel(),
) {
    val splashState by viewModel.splashState.collectAsStateWithLifecycle()

    LaunchedEffect(splashState) {
        when (splashState) {
            SplashState.GoToGetStarted -> onGoToGetStarted()
            SplashState.GoToProfileSetup -> onGoToProfileSetup()
            SplashState.GoToHome -> onGoToHome()
            SplashState.Loading -> {}
        }
    }

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
