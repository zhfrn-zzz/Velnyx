package dev.zhafran.velnyx.feature.profile.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zhafran.velnyx.core.designsystem.component.VelnyxPrimaryButton
import dev.zhafran.velnyx.core.designsystem.component.VelnyxTextField
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxGray600
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxSpacing
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxTheme

@Composable
fun ProfileSetupScreen(
    onProfileSaved: () -> Unit,
    viewModel: ProfileSetupViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                ProfileSetupEvent.NavigateToPermissions -> onProfileSaved()
            }
        }
    }

    ProfileSetupContent(
        state = state,
        onDisplayNameChange = viewModel::onDisplayNameChange,
        onWeightChange = viewModel::onWeightChange,
        onSubmit = viewModel::onSubmit,
    )
}

@Composable
private fun ProfileSetupContent(
    state: ProfileSetupUiState,
    onDisplayNameChange: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(VelnyxSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(VelnyxSpacing.md),
        ) {
            Spacer(Modifier.height(VelnyxSpacing.xl))

            Text(
                text = "Tell us about you",
                style = MaterialTheme.typography.headlineLarge,
            )

            Text(
                text = "We'll use this to calculate your calories burned during runs.",
                style = MaterialTheme.typography.bodyLarge,
                color = VelnyxGray600,
            )

            Spacer(Modifier.height(VelnyxSpacing.sm))

            VelnyxTextField(
                value = state.displayName,
                onValueChange = onDisplayNameChange,
                label = "Display name",
                modifier = Modifier.fillMaxWidth(),
                isError = state.displayNameError != null,
                supportingText = state.displayNameError?.let { error ->
                    { Text(error) }
                },
                singleLine = true,
            )

            VelnyxTextField(
                value = state.weightKgText,
                onValueChange = onWeightChange,
                label = "Weight (kg)",
                modifier = Modifier.fillMaxWidth(),
                isError = state.weightError != null,
                supportingText = {
                    Text(state.weightError ?: "20 – 300 kg")
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
            )

            if (state.generalError != null) {
                Text(
                    text = state.generalError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Spacer(Modifier.weight(1f))

            VelnyxPrimaryButton(
                text = if (state.isLoading) "Saving..." else "Continue",
                onClick = onSubmit,
                enabled = state.isSubmitEnabled && !state.isLoading,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileSetupScreenPreview() {
    VelnyxTheme {
        ProfileSetupContent(
            state = ProfileSetupUiState(
                displayName = "Zhafran",
                weightKgText = "70",
                isSubmitEnabled = true,
            ),
            onDisplayNameChange = {},
            onWeightChange = {},
            onSubmit = {},
        )
    }
}
