package dev.zhafran.velnyx.core.designsystem.component

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxBlack
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxError
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxGray200
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxGray400
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxTheme

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
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = VelnyxBlack,
            unfocusedBorderColor = VelnyxGray200,
            errorBorderColor = VelnyxError,
            focusedLabelColor = VelnyxBlack,
            unfocusedLabelColor = VelnyxGray400,
        ),
    )
}

@Preview(showBackground = true)
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

@Preview(showBackground = true)
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
