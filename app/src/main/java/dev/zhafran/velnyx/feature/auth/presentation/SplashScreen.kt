package dev.zhafran.velnyx.feature.auth.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxBlack
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxLime

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VelnyxBlack),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "VELNYX",
            style = MaterialTheme.typography.displayMedium,
            color = VelnyxLime,
        )
    }
}
