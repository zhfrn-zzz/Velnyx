package dev.zhafran.velnyx.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxBlack
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxGray200
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxGray400
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxLime
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxTheme

@Composable
fun VelnyxPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = enabled,
        shape = MaterialTheme.shapes.extraLarge,
        colors = ButtonDefaults.buttonColors(
            containerColor = VelnyxLime,
            contentColor = VelnyxBlack,
            disabledContainerColor = VelnyxGray200,
            disabledContentColor = VelnyxGray400,
        ),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
fun VelnyxSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = enabled,
        shape = MaterialTheme.shapes.extraLarge,
        border = BorderStroke(1.5.dp, if (enabled) VelnyxLime else VelnyxGray200),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = VelnyxBlack,
            disabledContentColor = VelnyxGray400,
        ),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun VelnyxPrimaryButtonPreview() {
    VelnyxTheme {
        VelnyxPrimaryButton(text = "START RUN", onClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun VelnyxPrimaryButtonDisabledPreview() {
    VelnyxTheme {
        VelnyxPrimaryButton(text = "START RUN", onClick = {}, enabled = false)
    }
}

@Preview(showBackground = true)
@Composable
private fun VelnyxSecondaryButtonPreview() {
    VelnyxTheme {
        VelnyxSecondaryButton(text = "VIEW HISTORY", onClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun VelnyxSecondaryButtonDisabledPreview() {
    VelnyxTheme {
        VelnyxSecondaryButton(text = "VIEW HISTORY", onClick = {}, enabled = false)
    }
}
