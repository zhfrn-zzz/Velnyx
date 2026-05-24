package dev.zhafran.velnyx.feature.auth.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.zhafran.velnyx.core.designsystem.component.VelnyxPrimaryButton
import dev.zhafran.velnyx.core.designsystem.component.VelnyxSecondaryButton
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxBlack
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxLime
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxSpacing
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxTheme
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxWhite

@Composable
fun GetStartedScreen(
    onNavigateToSignUp: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VelnyxBlack)
            .padding(VelnyxSpacing.lg),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "VELNYX",
            style = MaterialTheme.typography.displayMedium,
            color = VelnyxLime,
        )
        Spacer(modifier = Modifier.height(VelnyxSpacing.sm))
        Text(
            text = "Track your runs",
            style = MaterialTheme.typography.bodyMedium,
            color = VelnyxWhite,
        )
        Spacer(modifier = Modifier.height(VelnyxSpacing.xl))
        VelnyxPrimaryButton(text = "Join Us", onClick = onNavigateToSignUp)
        Spacer(modifier = Modifier.height(VelnyxSpacing.md))
        VelnyxSecondaryButton(text = "Log In", onClick = onNavigateToLogin)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
fun GetStartedScreenPreview() {
    VelnyxTheme { GetStartedScreen() }
}
