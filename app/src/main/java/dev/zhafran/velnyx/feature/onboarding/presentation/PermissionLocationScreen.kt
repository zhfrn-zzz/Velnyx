package dev.zhafran.velnyx.feature.onboarding.presentation

import android.Manifest
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxTheme
import dev.zhafran.velnyx.core.util.PermissionHelper

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionLocationScreen(
    onContinue: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val permissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val hasBeenRequested = rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(permissionState.status) {
        if (permissionState.status.isGranted) onContinue()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (PermissionHelper.isFineLocationGranted(context)) {
                    onContinue()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val (buttonText, buttonAction) = when {
        permissionState.status.isGranted -> "Continue" to { onContinue() }
        permissionState.status.shouldShowRationale -> "Allow Location" to {
            hasBeenRequested.value = true
            permissionState.launchPermissionRequest()
        }
        !hasBeenRequested.value -> "Allow Location" to {
            hasBeenRequested.value = true
            permissionState.launchPermissionRequest()
        }
        else -> "Open Settings" to { PermissionHelper.openAppSettings(context) }
    }

    PermissionStepLayout(
        progress = 0.4f,
        title = "Track your runs",
        description = "VELNYX needs location access to record your route, distance, and pace as you run.",
        primaryButtonText = buttonText,
        onPrimaryClick = buttonAction,
        icon = Icons.Default.LocationOn,
    )
}

@Preview(showBackground = true)
@Composable
private fun PermissionLocationScreenPreview() {
    VelnyxTheme { PermissionLocationScreen(onContinue = {}) }
}
