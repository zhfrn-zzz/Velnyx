package dev.zhafran.velnyx.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxGray100
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxSpacing
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxTheme

@Composable
fun VelnyxCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = VelnyxGray100),
        ) {
            Box(modifier = Modifier.padding(VelnyxSpacing.md)) {
                content()
            }
        }
    } else {
        Card(
            modifier = modifier,
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = VelnyxGray100),
        ) {
            Box(modifier = Modifier.padding(VelnyxSpacing.md)) {
                content()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun VelnyxCardPreview() {
    VelnyxTheme {
        VelnyxCard {
            Text("This week: 0.0 km")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun VelnyxCardClickablePreview() {
    VelnyxTheme {
        VelnyxCard(onClick = {}) {
            Text("Tap to view run detail")
        }
    }
}
