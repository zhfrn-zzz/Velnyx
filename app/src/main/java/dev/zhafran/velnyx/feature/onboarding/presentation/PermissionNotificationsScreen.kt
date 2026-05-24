package dev.zhafran.velnyx.feature.onboarding.presentation

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxGray100
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxSpacing
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxTheme
import dev.zhafran.velnyx.core.util.PermissionHelper

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionNotificationsScreen(
    onContinue: () -> Unit,
    onSkipAll: () -> Unit = {},
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        LaunchedEffect(Unit) { onContinue() }
        return
    }

    val context = LocalContext.current
    val permissionState = rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    val hasBeenRequested = rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(permissionState.status) {
        if (permissionState.status.isGranted) onContinue()
    }

    val (buttonText, buttonAction) = when {
        permissionState.status.isGranted -> "Continue" to { onContinue() }
        permissionState.status.shouldShowRationale -> "Allow Notifications" to {
            hasBeenRequested.value = true
            permissionState.launchPermissionRequest()
        }
        !hasBeenRequested.value -> "Allow Notifications" to {
            hasBeenRequested.value = true
            permissionState.launchPermissionRequest()
        }
        else -> "Open Settings" to { PermissionHelper.openAppSettings(context) }
    }

    PermissionStepLayout(
        progress = 0.2f,
        title = "Stay in the loop",
        description = "VELNYX shows your active run in a notification while tracking. Allow notifications to enable this.",
        primaryButtonText = buttonText,
        onPrimaryClick = buttonAction,
        secondaryButtonText = "Skip",
        onSecondaryClick = onContinue,
        icon = Icons.Default.Notifications,
        // TODO: Remove before demo
        belowButtonContent = {
            TextButton(
                onClick = onSkipAll,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = VelnyxSpacing.xl),
            ) {
                Text(
                    text = "Skip all (dev)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = VelnyxGray100,
                )
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun PermissionNotificationsScreenPreview() {
    VelnyxTheme { PermissionNotificationsScreen(onContinue = {}) }
}
