package dev.zhafran.velnyx.feature.onboarding.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.zhafran.velnyx.core.designsystem.component.VelnyxPrimaryButton
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxBlack
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxGray100
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxLime
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxOffBlack
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxSpacing
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxTheme
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxWhite

@Composable
fun PermissionStepLayout(
    progress: Float,
    title: String,
    description: String,
    primaryButtonText: String,
    onPrimaryClick: () -> Unit,
    modifier: Modifier = Modifier,
    secondaryButtonText: String? = null,
    onSecondaryClick: (() -> Unit)? = null,
    icon: ImageVector? = null,
    belowButtonContent: @Composable (() -> Unit)? = null,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = VelnyxBlack,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(VelnyxSpacing.lg),
        ) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = VelnyxLime,
                trackColor = VelnyxOffBlack,
                strokeCap = StrokeCap.Round,
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = VelnyxLime,
                    )
                    Spacer(modifier = Modifier.height(VelnyxSpacing.lg))
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineLarge,
                    color = VelnyxWhite,
                )
                Spacer(modifier = Modifier.height(VelnyxSpacing.md))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = VelnyxGray100,
                )
            }

            VelnyxPrimaryButton(
                text = primaryButtonText,
                onClick = onPrimaryClick,
            )
            belowButtonContent?.invoke()
            if (secondaryButtonText != null && onSecondaryClick != null) {
                Spacer(modifier = Modifier.height(VelnyxSpacing.sm))
                TextButton(
                    onClick = onSecondaryClick,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = secondaryButtonText,
                        style = MaterialTheme.typography.labelLarge,
                        color = VelnyxWhite,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun PermissionStepLayoutPreview() {
    VelnyxTheme {
        PermissionStepLayout(
            progress = 0.4f,
            title = "Track your runs",
            description = "VELNYX needs location access to record your route, distance, and pace.",
            primaryButtonText = "Allow Location",
            onPrimaryClick = {},
            secondaryButtonText = "Skip",
            onSecondaryClick = {},
            icon = Icons.Default.LocationOn,
        )
    }
}
