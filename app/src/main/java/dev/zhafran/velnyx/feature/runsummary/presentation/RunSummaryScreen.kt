package dev.zhafran.velnyx.feature.runsummary.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.zhafran.velnyx.core.designsystem.component.VelnyxPrimaryButton
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxSpacing
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxTheme

@Composable
fun RunSummaryScreen(
    runId: String = "",
    onNavigateToHome: () -> Unit = {},
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(VelnyxSpacing.lg),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("RunSummaryScreen", style = MaterialTheme.typography.headlineLarge)
            Text("(placeholder)", style = MaterialTheme.typography.bodyMedium)
            Text("Run ID: $runId", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(VelnyxSpacing.xl))
            VelnyxPrimaryButton(text = "Done", onClick = onNavigateToHome)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RunSummaryScreenPreview() {
    VelnyxTheme { RunSummaryScreen(runId = "demo-run-id") }
}
