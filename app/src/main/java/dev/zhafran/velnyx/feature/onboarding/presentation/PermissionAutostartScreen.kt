package dev.zhafran.velnyx.feature.onboarding.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxTheme
import dev.zhafran.velnyx.core.util.PermissionHelper

@Composable
fun PermissionAutostartScreen(
    onContinue: () -> Unit,
) {
    val context = LocalContext.current

    PermissionStepLayout(
        progress = 1.0f,
        title = "Allow VELNYX to run in background",
        description = "Your phone may stop background apps to save battery. " +
            "To keep run tracking working reliably:\n\n" +
            "1. Tap 'Open VELNYX Settings' below\n" +
            "2. Find 'Auto-launch' or 'Background activity' and enable it\n" +
            "3. Make sure 'Allow notifications' is on\n" +
            "4. Come back and tap 'Done'",
        primaryButtonText = "Open VELNYX Settings",
        onPrimaryClick = { PermissionHelper.openAppSettings(context) },
        secondaryButtonText = "Done — I've enabled it",
        onSecondaryClick = onContinue,
        icon = Icons.Default.Settings,
    )
}

@Preview(showBackground = true)
@Composable
private fun PermissionAutostartScreenPreview() {
    VelnyxTheme { PermissionAutostartScreen(onContinue = {}) }
}
