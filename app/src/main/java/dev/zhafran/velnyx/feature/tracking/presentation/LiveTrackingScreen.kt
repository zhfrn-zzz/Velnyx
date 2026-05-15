package dev.zhafran.velnyx.feature.tracking.presentation

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.zhafran.velnyx.core.designsystem.component.VelnyxMapView
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxBlack
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxError
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxGray400
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxLime
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxWhite

@Composable
fun LiveTrackingScreen(
    state: RunState,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onFinish: () -> Unit,
) {
    val stats = when (state) {
        is RunState.Running -> state.stats
        is RunState.Paused -> state.stats
        else -> RunStats()
    }
    val isRunning = state is RunState.Running
    var showFinishDialog by remember { mutableStateOf(false) }

    if (showFinishDialog) {
        AlertDialog(
            onDismissRequest = { showFinishDialog = false },
            title = { Text("End this run?") },
            confirmButton = {
                TextButton(onClick = {
                    showFinishDialog = false
                    onFinish()
                }) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = { showFinishDialog = false }) { Text("Cancel") }
            },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VelnyxBlack),
    ) {
        // GPS accuracy indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val accuracyColor = when {
                stats.lastAccuracyM < 10f -> VelnyxLime
                stats.lastAccuracyM < 20f -> androidx.compose.ui.graphics.Color.Yellow
                else -> VelnyxError
            }
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(accuracyColor),
            )
            Text(
                text = " GPS",
                color = VelnyxGray400,
                style = MaterialTheme.typography.labelSmall,
            )
        }

        // Hero metric — distance
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = formatDistance(stats.distanceM),
                color = VelnyxLime,
                fontSize = 72.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "KM",
                color = VelnyxGray400,
                style = MaterialTheme.typography.labelMedium,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Secondary metrics row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            MetricColumn("AVG PACE", formatPace(stats.avgPaceSecondsPerKm))
            MetricColumn("DURATION", formatDuration(stats.durationMs))
            MetricColumn("CALORIES", "${stats.calories} kcal")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Map
        VelnyxMapView(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp)),
            currentLatLng = stats.lastLatLng,
            routePoints = when (state) {
                is RunState.Running -> state.routePoints
                is RunState.Paused -> state.routePoints
                else -> emptyList()
            },
        )

        Spacer(modifier = Modifier.weight(1f))

        // Bottom controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Map toggle (no-op)
            IconButton(onClick = {}) {
                Icon(Icons.Default.Map, contentDescription = "Map", tint = VelnyxWhite)
            }

            // Pause / Resume
            if (isRunning) {
                OutlinedIconButton(
                    onClick = onPause,
                    modifier = Modifier.size(72.dp),
                    shape = CircleShape,
                    colors = IconButtonDefaults.outlinedIconButtonColors(
                        contentColor = VelnyxWhite,
                    ),
                ) {
                    Icon(Icons.Default.Pause, contentDescription = "Pause", modifier = Modifier.size(36.dp))
                }
            } else {
                IconButton(
                    onClick = onResume,
                    modifier = Modifier
                        .size(72.dp)
                        .background(VelnyxLime, CircleShape),
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Resume", tint = VelnyxBlack, modifier = Modifier.size(36.dp))
                }
            }

            // Finish
            OutlinedIconButton(
                onClick = { showFinishDialog = true },
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                colors = IconButtonDefaults.outlinedIconButtonColors(
                    contentColor = VelnyxError,
                ),
            ) {
                Icon(Icons.Default.Stop, contentDescription = "Finish")
            }
        }
    }
}

@Composable
private fun MetricColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, color = VelnyxWhite, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        Text(text = label, color = VelnyxGray400, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
    }
}

private fun formatDistance(meters: Int): String {
    val km = meters / 1000.0
    return "%.2f".format(km)
}

private fun formatPace(secondsPerKm: Int): String {
    if (secondsPerKm <= 0) return "--:--"
    val min = secondsPerKm / 60
    val sec = secondsPerKm % 60
    return "%d:%02d /km".format(min, sec)
}

private fun formatDuration(durationMs: Long): String {
    val totalSec = (durationMs / 1000).toInt()
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}
