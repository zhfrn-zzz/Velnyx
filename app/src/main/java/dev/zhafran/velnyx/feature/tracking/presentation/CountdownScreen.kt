package dev.zhafran.velnyx.feature.tracking.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxBlack
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxLime

@Composable
fun CountdownScreen(secLeft: Int) {
    val scale = remember(secLeft) { Animatable(0.5f) }

    LaunchedEffect(secLeft) {
        scale.animateTo(1f, animationSpec = tween(durationMillis = 400))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VelnyxBlack),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (secLeft == 0) "GO!" else secLeft.toString(),
            color = VelnyxLime,
            fontSize = 120.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.scale(scale.value),
        )
    }
}
