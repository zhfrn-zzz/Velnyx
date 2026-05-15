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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.zhafran.velnyx.core.designsystem.component.VelnyxPrimaryButton
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxBlack
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxGray400
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxLime
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxWhite

@Composable
fun RunSummaryScreen(
    distanceM: Int = 0,
    durationMs: Long = 0L,
    avgPace: Int = 0,
    calories: Int = 0,
    onNavigateToHome: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VelnyxBlack)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Run Complete 🎉",
            color = VelnyxLime,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Distance
        Text(
            text = "%.2f".format(distanceM / 1000.0),
            color = VelnyxWhite,
            fontSize = 64.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(text = "KM", color = VelnyxGray400, style = MaterialTheme.typography.labelMedium)

        Spacer(modifier = Modifier.height(32.dp))

        // Stats row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            SummaryMetric("DURATION", formatDuration(durationMs))
            SummaryMetric("AVG PACE", formatPace(avgPace))
            SummaryMetric("CALORIES", "$calories kcal")
        }

        Spacer(modifier = Modifier.weight(1f))

        VelnyxPrimaryButton(text = "Done", onClick = onNavigateToHome)

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun SummaryMetric(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, color = VelnyxWhite, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
        Text(text = label, color = VelnyxGray400, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
    }
}

private fun formatDuration(durationMs: Long): String {
    val totalSec = (durationMs / 1000).toInt()
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
