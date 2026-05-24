package dev.zhafran.velnyx.feature.auth.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zhafran.velnyx.core.designsystem.component.VelnyxPrimaryButton
import dev.zhafran.velnyx.core.designsystem.component.VelnyxTextField
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxBlack
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxError
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxGray100
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxLime
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxSpacing
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxWhite

@Composable
fun SignUpScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateBack: () -> Unit,
    onSignUpSuccess: () -> Unit,
    onConfirmEmailRequired: (email: String) -> Unit,
    viewModel: SignUpViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                SignUpEvent.NavigateToProfileSetup -> onSignUpSuccess()
                SignUpEvent.NavigateToConfirmEmail -> onConfirmEmailRequired(uiState.email)
            }
        }
    }

    var showPassword by rememberSaveable { mutableStateOf(false) }
    var showConfirmPassword by rememberSaveable { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = VelnyxBlack,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(VelnyxSpacing.lg),
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Go back",
                    tint = VelnyxWhite,
                )
            }

            Spacer(modifier = Modifier.height(VelnyxSpacing.xl))

            Text(
                text = "Create an account",
                style = MaterialTheme.typography.headlineLarge,
                color = VelnyxWhite,
            )

            Spacer(modifier = Modifier.height(VelnyxSpacing.sm))

            Text(
                text = "Start your running journey today",
                style = MaterialTheme.typography.bodyMedium,
                color = VelnyxGray100,
            )

            Spacer(modifier = Modifier.height(VelnyxSpacing.xl))

            VelnyxTextField(
                value = uiState.displayName,
                onValueChange = viewModel::onDisplayNameChange,
                label = "Display Name",
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null,
                    )
                },
            )

            Spacer(modifier = Modifier.height(VelnyxSpacing.md))

            VelnyxTextField(
                value = uiState.email,
                onValueChange = viewModel::onEmailChange,
                label = "Email",
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Email,
                        contentDescription = null,
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            )

            Spacer(modifier = Modifier.height(VelnyxSpacing.md))

            VelnyxTextField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChange,
                label = "Password",
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (showPassword) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            imageVector = if (showPassword) {
                                Icons.Outlined.VisibilityOff
                            } else {
                                Icons.Outlined.Visibility
                            },
                            contentDescription = if (showPassword) {
                                "Hide password"
                            } else {
                                "Show password"
                            },
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            )

            Spacer(modifier = Modifier.height(VelnyxSpacing.md))

            VelnyxTextField(
                value = uiState.confirmPassword,
                onValueChange = viewModel::onConfirmPasswordChange,
                label = "Confirm Password",
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.confirmPassword.isNotEmpty() &&
                    uiState.password != uiState.confirmPassword,
                supportingText = if (
                    uiState.confirmPassword.isNotEmpty() &&
                    uiState.password != uiState.confirmPassword
                ) {
                    { Text("Passwords do not match") }
                } else {
                    null
                },
                visualTransformation = if (showConfirmPassword) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                        Icon(
                            imageVector = if (showConfirmPassword) {
                                Icons.Outlined.VisibilityOff
                            } else {
                                Icons.Outlined.Visibility
                            },
                            contentDescription = if (showConfirmPassword) {
                                "Hide password"
                            } else {
                                "Show password"
                            },
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            )

            if (uiState.error != null) {
                Spacer(modifier = Modifier.height(VelnyxSpacing.sm))
                Text(
                    text = uiState.error!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = VelnyxError,
                )
            }

            Spacer(modifier = Modifier.height(VelnyxSpacing.xl))

            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.CenterHorizontally),
                    color = VelnyxLime,
                )
            } else {
                VelnyxPrimaryButton(
                    text = "Join Us",
                    onClick = viewModel::onSubmit,
                    enabled = uiState.isSubmitEnabled,
                )
            }

            Spacer(modifier = Modifier.height(VelnyxSpacing.md))

            TextButton(
                onClick = onNavigateToLogin,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            ) {
                Text(
                    text = "Already have an account? Log in",
                    color = VelnyxLime,
                )
            }
        }
    }
}
