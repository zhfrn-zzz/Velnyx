package dev.zhafran.velnyx.feature.auth.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MarkEmailRead
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.zhafran.velnyx.core.designsystem.component.VelnyxPrimaryButton
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxSpacing
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxTheme

@Composable
fun ConfirmEmailScreen(
    email: String,
    onNavigateBack: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(VelnyxSpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(120.dp))

            Icon(
                imageVector = Icons.Outlined.MarkEmailRead,
                contentDescription = null,
                modifier = Modifier.height(64.dp),
                tint = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(VelnyxSpacing.xl))

            Text(
                text = "Check your email",
                style = MaterialTheme.typography.headlineMedium,
            )

            Spacer(modifier = Modifier.height(VelnyxSpacing.md))

            Text(
                text = "We sent a confirmation link to\n$email\n\nTap the link to activate your account, then come back to log in.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(VelnyxSpacing.xxl))

            VelnyxPrimaryButton(
                text = "Back to Login",
                onClick = onNavigateBack,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ConfirmEmailScreenPreview() {
    VelnyxTheme {
        ConfirmEmailScreen(
            email = "runner@example.com",
            onNavigateBack = {},
        )
    }
}
