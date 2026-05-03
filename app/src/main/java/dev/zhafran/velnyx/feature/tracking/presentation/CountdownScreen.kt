package dev.zhafran.velnyx.feature.tracking.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxSpacing
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxTheme
import kotlinx.coroutines.delay

@Composable
fun CountdownScreen(
    onNavigateToLiveTracking: () -> Unit = {},
) {
    LaunchedEffect(Unit) {
        delay(3_000L)
        onNavigateToLiveTracking()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(VelnyxSpacing.lg),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("CountdownScreen", style = MaterialTheme.typography.headlineLarge)
            Text("(placeholder)", style = MaterialTheme.typography.bodyMedium)
            Text("Auto-navigates in 3 seconds…", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CountdownScreenPreview() {
    VelnyxTheme { CountdownScreen() }
}
