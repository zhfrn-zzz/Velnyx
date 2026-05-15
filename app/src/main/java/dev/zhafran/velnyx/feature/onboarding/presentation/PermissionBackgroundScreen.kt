package dev.zhafran.velnyx.feature.onboarding.presentation

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxGray400
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxSpacing
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxTheme
import dev.zhafran.velnyx.core.util.PermissionHelper

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionBackgroundScreen(
    onContinue: () -> Unit,
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        LaunchedEffect(Unit) { onContinue() }
        return
    }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val permissionState = rememberPermissionState(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    var checkedOnResume by remember { mutableStateOf(false) }

    LaunchedEffect(permissionState.status) {
        if (permissionState.status.isGranted) onContinue()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                checkedOnResume = !checkedOnResume
                if (PermissionHelper.isBackgroundLocationGranted(context)) {
                    onContinue()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val useSettingsDeepLink = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

    PermissionStepLayout(
        progress = 0.6f,
        title = "Keep tracking when your phone is in your pocket",
        description = "Set location access to 'Allow all the time' in the next screen so VELNYX can record your run when the screen is off.",
        primaryButtonText = if (useSettingsDeepLink) "Open Settings" else "Allow Background Location",
        onPrimaryClick = {
            if (useSettingsDeepLink) {
                PermissionHelper.openAppSettings(context)
            } else {
                permissionState.launchPermissionRequest()
            }
        },
        icon = Icons.Default.MyLocation,
        belowButtonContent = if (useSettingsDeepLink) {
            {
                Spacer(modifier = Modifier.height(VelnyxSpacing.md))
                Text(
                    text = "Tap Permissions → Location → Allow all the time",
                    style = MaterialTheme.typography.bodyMedium,
                    color = VelnyxGray400,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = VelnyxSpacing.md),
                )
            }
        } else {
            null
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun PermissionBackgroundScreenPreview() {
    VelnyxTheme { PermissionBackgroundScreen(onContinue = {}) }
}
