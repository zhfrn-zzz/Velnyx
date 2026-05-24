package dev.zhafran.velnyx.core.designsystem.component

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxError
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxGray100
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxLime
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxTheme
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxWhite

@Composable
fun velnyxDarkTextFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = VelnyxLime,
    unfocusedBorderColor = VelnyxGray100.copy(alpha = 0.5f),
    errorBorderColor = VelnyxError,
    focusedLabelColor = VelnyxLime,
    unfocusedLabelColor = VelnyxGray100,
    cursorColor = VelnyxLime,
    focusedTextColor = VelnyxWhite,
    unfocusedTextColor = VelnyxWhite,
    focusedLeadingIconColor = VelnyxGray100,
    unfocusedLeadingIconColor = VelnyxGray100,
    focusedTrailingIconColor = VelnyxGray100,
    unfocusedTrailingIconColor = VelnyxGray100,
    focusedSupportingTextColor = VelnyxGray100,
    unfocusedSupportingTextColor = VelnyxGray100,
    errorSupportingTextColor = VelnyxError,
    focusedPlaceholderColor = Color.White.copy(alpha = 0.5f),
    unfocusedPlaceholderColor = Color.White.copy(alpha = 0.5f),
)

@Composable
fun VelnyxTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true,
    colors: TextFieldColors = velnyxDarkTextFieldColors(),
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier,
        isError = isError,
        supportingText = supportingText,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        singleLine = singleLine,
        shape = MaterialTheme.shapes.medium,
        colors = colors,
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun VelnyxTextFieldPreview() {
    VelnyxTheme {
        VelnyxTextField(
            value = "runner@example.com",
            onValueChange = {},
            label = "Email",
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun VelnyxTextFieldErrorPreview() {
    VelnyxTheme {
        VelnyxTextField(
            value = "bad-email",
            onValueChange = {},
            label = "Email",
            isError = true,
            supportingText = { Text("Enter a valid email address") },
        )
    }
}
