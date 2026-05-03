package dev.zhafran.velnyx.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

// Dark mode is cut per plan.md §3 — one fixed light scheme only.
private val VelnyxColorScheme = lightColorScheme(
    primary = VelnyxLime,
    onPrimary = VelnyxBlack,
    primaryContainer = VelnyxLime,
    onPrimaryContainer = VelnyxBlack,
    secondary = VelnyxBlack,
    onSecondary = VelnyxWhite,
    secondaryContainer = VelnyxGray100,
    onSecondaryContainer = VelnyxBlack,
    background = VelnyxWhite,
    onBackground = VelnyxBlack,
    surface = VelnyxWhite,
    onSurface = VelnyxBlack,
    surfaceVariant = VelnyxGray100,
    onSurfaceVariant = VelnyxGray600,
    outline = VelnyxGray200,
    outlineVariant = VelnyxGray100,
    error = VelnyxError,
    onError = VelnyxWhite,
)

@Composable
fun VelnyxTheme(content: @Composable () -> Unit) {
    val typography = remember { velnyxTypography() }
    MaterialTheme(
        colorScheme = VelnyxColorScheme,
        typography = typography,
        shapes = VelnyxShapes,
        content = content,
    )
}
