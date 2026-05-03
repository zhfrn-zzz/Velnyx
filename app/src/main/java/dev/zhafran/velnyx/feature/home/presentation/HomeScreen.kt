package dev.zhafran.velnyx.feature.home.presentation

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import dev.zhafran.velnyx.GpsSmokeTestService
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxBlack
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxGray100
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxLime
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxTheme

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen() {
    val context = LocalContext.current

    // Permission list: POST_NOTIFICATIONS only needed on Android 13+
    val permissionsState = rememberMultiplePermissionsState(
        permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(Manifest.permission.POST_NOTIFICATIONS, Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            listOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    )

    var isServiceRunning by rememberSaveable { mutableStateOf(false) }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(32.dp))

            // ── Header ─────────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Good morning,",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "Runner",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(color = VelnyxGray100, shape = CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = "V", fontWeight = FontWeight.ExtraBold, color = VelnyxBlack)
                }
            }

            Spacer(Modifier.height(32.dp))

            // ── GPS Smoke Test (M0 — remove after device test passes) ──────────
            Text(
                text = "M0 · GPS SMOKE TEST",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.Start),
            )
            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    when {
                        !permissionsState.allPermissionsGranted ->
                            permissionsState.launchMultiplePermissionRequest()

                        isServiceRunning -> {
                            context.stopService(Intent(context, GpsSmokeTestService::class.java))
                            isServiceRunning = false
                        }

                        else -> {
                            context.startForegroundService(
                                Intent(context, GpsSmokeTestService::class.java)
                            )
                            isServiceRunning = true
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = when {
                        isServiceRunning -> MaterialTheme.colorScheme.error
                        else -> VelnyxLime
                    },
                    contentColor = when {
                        isServiceRunning -> MaterialTheme.colorScheme.onError
                        else -> VelnyxBlack
                    },
                ),
                shape = MaterialTheme.shapes.large,
            ) {
                Text(
                    text = when {
                        !permissionsState.allPermissionsGranted -> "REQUEST PERMISSIONS"
                        isServiceRunning -> "■  STOP GPS TEST"
                        else -> "▶  START GPS TEST"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    letterSpacing = 1.sp,
                )
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = when {
                    !permissionsState.allPermissionsGranted ->
                        "Needs: FINE_LOCATION + POST_NOTIFICATIONS (Android 13+)"
                    isServiceRunning ->
                        "Running — adb logcat -s GPS_TEST to see fixes"
                    else ->
                        "Tap to start service — lock screen, walk outside 10 min"
                },
                style = MaterialTheme.typography.labelSmall,
                color = if (isServiceRunning)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.Start),
            )

            Spacer(Modifier.height(24.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(24.dp))

            // ── Stats row placeholder ──────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StatCard(label = "This week", value = "0.0 km", modifier = Modifier.weight(1f))
                StatCard(label = "Total runs", value = "0", modifier = Modifier.weight(1f))
                StatCard(label = "Best pace", value = "--:--", modifier = Modifier.weight(1f))
            }

            Spacer(Modifier.height(20.dp))

            // ── Branding card (placeholder for real home — M1) ────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = VelnyxBlack),
                shape = MaterialTheme.shapes.large,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "VELNYX",
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Black,
                        color = VelnyxLime,
                        letterSpacing = 6.sp,
                    )
                    Text(
                        text = "Your running companion",
                        style = MaterialTheme.typography.bodyMedium,
                        color = VelnyxLime.copy(alpha = 0.65f),
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = VelnyxGray100),
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = VelnyxBlack,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    VelnyxTheme { HomeScreen() }
}
