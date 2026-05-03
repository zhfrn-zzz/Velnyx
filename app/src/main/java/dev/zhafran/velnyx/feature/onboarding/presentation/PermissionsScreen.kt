package dev.zhafran.velnyx.feature.onboarding.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.zhafran.velnyx.core.designsystem.component.VelnyxPrimaryButton
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxSpacing
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxTheme

@Composable
fun PermissionsScreen(
    onNavigateToHome: () -> Unit = {},
) {
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
            Text("PermissionsScreen", style = MaterialTheme.typography.headlineLarge)
            Text("(placeholder)", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(VelnyxSpacing.xl))
            VelnyxPrimaryButton(text = "Continue", onClick = onNavigateToHome)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PermissionsScreenPreview() {
    VelnyxTheme { PermissionsScreen() }
}
