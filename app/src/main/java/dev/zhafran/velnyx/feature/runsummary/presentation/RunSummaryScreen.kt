package dev.zhafran.velnyx.feature.runsummary.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.zhafran.velnyx.core.designsystem.component.VelnyxPrimaryButton
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxBlack
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxGray400
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxLime
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxOffBlack
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxTheme
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxWhite

@Composable
fun RunSummaryScreen(
    distanceM: Int,
    durationS: Int,
    avgPace: Int,
    calories: Int,
    onDone: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VelnyxBlack)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(64.dp))

        // Header
        Text(
            text = "Run Complete",
            color = VelnyxLime,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(40.dp))

        // 2 × 2 stats grid
        SummaryGrid(
            distanceM = distanceM,
            durationS = durationS,
            avgPace = avgPace,
            calories = calories,
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Sync status — fire-and-forget upload, assume success
        Text(
            text = "Synced to cloud ☁️",
            color = VelnyxGray400,
            style = MaterialTheme.typography.bodyMedium,
        )

        Spacer(modifier = Modifier.weight(1f))

        VelnyxPrimaryButton(text = "Done", onClick = onDone)

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun SummaryGrid(
    distanceM: Int,
    durationS: Int,
    avgPace: Int,
    calories: Int,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SummaryCell(
                label = "DISTANCE",
                value = "%.2f".format(distanceM / 1000.0),
                unit = "km",
                modifier = Modifier.weight(1f),
                emphasized = true,
            )
            SummaryCell(
                label = "DURATION",
                value = formatDuration(durationS),
                unit = null,
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SummaryCell(
                label = "AVG PACE",
                value = formatPace(avgPace),
                unit = null,
                modifier = Modifier.weight(1f),
            )
            SummaryCell(
                label = "CALORIES",
                value = calories.toString(),
                unit = "kcal",
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SummaryCell(
    label: String,
    value: String,
    unit: String?,
    modifier: Modifier = Modifier,
    emphasized: Boolean = false,
) {
    Column(
        modifier = modifier
            .background(VelnyxOffBlack, MaterialTheme.shapes.medium)
            .padding(16.dp),
    ) {
        Text(
            text = label,
            color = VelnyxGray400,
            style = MaterialTheme.typography.labelLarge,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                color = if (emphasized) VelnyxLime else VelnyxWhite,
                fontSize = if (emphasized) 36.sp else 28.sp,
                fontWeight = FontWeight.Bold,
            )
            if (unit != null) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = unit,
                    color = VelnyxGray400,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 6.dp),
                )
            }
        }
    }
}

// ── Formatting ──────────────────────────────────────────────────────────────

private fun formatDuration(durationS: Int): String {
    val totalSec = durationS.coerceAtLeast(0)
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}

private fun formatPace(secondsPerKm: Int): String {
    if (secondsPerKm <= 0) return "--:--"
    val min = secondsPerKm / 60
    val sec = secondsPerKm % 60
    return "%d:%02d /km".format(min, sec)
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun RunSummaryScreenPreview() {
    VelnyxTheme {
        RunSummaryScreen(
            distanceM = 5230,
            durationS = 1893,
            avgPace = 362,
            calories = 312,
            onDone = {},
        )
    }
}
