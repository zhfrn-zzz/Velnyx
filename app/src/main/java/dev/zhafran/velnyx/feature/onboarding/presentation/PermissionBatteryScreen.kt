package dev.zhafran.velnyx.feature.onboarding.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxTheme
import dev.zhafran.velnyx.core.util.PermissionHelper

@Composable
fun PermissionBatteryScreen(
    onContinue: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isIgnored by remember { mutableStateOf(PermissionHelper.isBatteryOptimizationIgnored(context)) }

    LaunchedEffect(isIgnored) {
        if (isIgnored) onContinue()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isIgnored = PermissionHelper.isBatteryOptimizationIgnored(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    PermissionStepLayout(
        progress = 0.8f,
        title = "Don't let Android stop your run",
        description = "Allow VELNYX to run without battery restrictions. Tap 'Allow' on the next dialog.",
        primaryButtonText = "Continue",
        onPrimaryClick = { PermissionHelper.requestIgnoreBatteryOptimization(context) },
        secondaryButtonText = "Skip for now",
        onSecondaryClick = onContinue,
        icon = Icons.Default.BatteryChargingFull,
    )
}

@Preview(showBackground = true)
@Composable
private fun PermissionBatteryScreenPreview() {
    VelnyxTheme { PermissionBatteryScreen(onContinue = {}) }
}