package dev.zhafran.velnyx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dagger.hilt.android.AndroidEntryPoint
import dev.zhafran.velnyx.core.designsystem.component.VelnyxCard
import dev.zhafran.velnyx.core.designsystem.component.VelnyxPrimaryButton
import dev.zhafran.velnyx.core.designsystem.component.VelnyxSecondaryButton
import dev.zhafran.velnyx.core.designsystem.component.VelnyxTextField
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxSpacing
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VelnyxTheme {
                DesignSystemDemoScreen()
            }
        }
    }
}

// Temporary M1 day-1 demo — replace with VelnyxNavGraph() once auth is wired up.
@Composable
private fun DesignSystemDemoScreen() {
    var emailValue by rememberSaveable { mutableStateOf("") }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(VelnyxSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(VelnyxSpacing.md),
        ) {
            Text(
                text = "Design System",
                style = MaterialTheme.typography.headlineLarge,
            )
            Text(
                text = "00,00",
                style = MaterialTheme.typography.displayLarge,
            )
            Text(
                text = "Display Medium",
                style = MaterialTheme.typography.displayMedium,
            )
            Text(
                text = "Typography, spacing, shape and colour tokens from plan.md §4.",
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = "Inter body medium — 14sp regular",
                style = MaterialTheme.typography.bodyMedium,
            )
            VelnyxPrimaryButton(text = "START RUN", onClick = {})
            VelnyxPrimaryButton(text = "DISABLED PRIMARY", onClick = {}, enabled = false)
            VelnyxSecondaryButton(text = "VIEW HISTORY", onClick = {})
            VelnyxSecondaryButton(text = "DISABLED SECONDARY", onClick = {}, enabled = false)
            VelnyxTextField(
                value = emailValue,
                onValueChange = { emailValue = it },
                label = "Email",
                modifier = Modifier.fillMaxWidth(),
            )
            VelnyxCard {
                Text(
                    text = "This week: 0.0 km",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            VelnyxCard(onClick = {}) {
                Text(
                    text = "Clickable card — tap for ripple",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DesignSystemDemoScreenPreview() {
    VelnyxTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(VelnyxSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(VelnyxSpacing.md),
        ) {
            Text("Design System", style = MaterialTheme.typography.headlineLarge)
            Text("00,00", style = MaterialTheme.typography.displayLarge)
            Text(
                "Typography and colour tokens from plan.md §4.",
                style = MaterialTheme.typography.bodyLarge,
            )
            VelnyxPrimaryButton(text = "START RUN", onClick = {})
            VelnyxSecondaryButton(text = "VIEW HISTORY", onClick = {})
            VelnyxTextField(
                value = "runner@example.com",
                onValueChange = {},
                label = "Email",
                modifier = Modifier.fillMaxWidth(),
            )
            VelnyxCard { Text("This week: 0.0 km") }
        }
    }
}
