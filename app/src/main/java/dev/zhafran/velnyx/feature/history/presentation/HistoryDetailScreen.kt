package dev.zhafran.velnyx.feature.history.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zhafran.velnyx.core.designsystem.component.VelnyxMapView
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxBlack
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxGray400
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxLime
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxOffBlack
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxSpacing
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxWhite
import dev.zhafran.velnyx.feature.history.data.RunPointDto
import dev.zhafran.velnyx.feature.history.data.RunSummaryDto
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryDetailScreen(
    runId: String = "",
    onNavigateBack: () -> Unit = {},
    viewModel: HistoryDetailViewModel = hiltViewModel(),
) {
    @Suppress("UNUSED_PARAMETER") val ignored = runId

    val uiState by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Run detail") },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        colors = IconButtonDefaults.iconButtonColors(contentColor = VelnyxWhite),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VelnyxBlack,
                    titleContentColor = VelnyxWhite,
                ),
            )
        },
        containerColor = VelnyxBlack,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (val state = uiState) {
                is HistoryDetailUiState.Loading -> LoadingState()
                is HistoryDetailUiState.Error -> ErrorState(message = state.msg)
                is HistoryDetailUiState.Success -> SuccessContent(
                    run = state.run,
                    points = state.points,
                )
            }
        }
    }
}

// ─── Success ─────────────────────────────────────────────────────────────────

@Composable
private fun SuccessContent(
    run: RunSummaryDto,
    points: List<RunPointDto>,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(VelnyxSpacing.md),
        verticalArrangement = Arrangement.spacedBy(VelnyxSpacing.md),
    ) {
        DateHeader(run = run)
        StatsGrid(run = run)
        if (points.isNotEmpty()) {
            RouteMap(points = points)
        } else {
            NoRouteCard()
        }
        Spacer(modifier = Modifier.height(VelnyxSpacing.md))
    }
}

@Composable
private fun DateHeader(run: RunSummaryDto) {
    Text(
        text = formatDate(run.startedAt),
        style = MaterialTheme.typography.headlineLarge,
        color = VelnyxWhite,
    )
}

@Composable
private fun StatsGrid(run: RunSummaryDto) {
    val distanceKm = "%.2f".format(Locale.ENGLISH, run.distanceM / 1000.0)
    val duration = formatDuration(run.durationS)
    val pace = formatPace(run.avgPaceSecondsPerKm)
    val calories = run.calories?.let { "$it kcal" } ?: "—"

    Column(
        verticalArrangement = Arrangement.spacedBy(VelnyxSpacing.sm),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(VelnyxSpacing.sm)) {
            StatCell(
                label = "Distance",
                value = distanceKm,
                unit = "km",
                modifier = Modifier.weight(1f),
            )
            StatCell(
                label = "Duration",
                value = duration,
                unit = null,
                modifier = Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(VelnyxSpacing.sm)) {
            StatCell(
                label = "Avg Pace",
                value = pace,
                unit = null,
                modifier = Modifier.weight(1f),
            )
            StatCell(
                label = "Calories",
                value = calories,
                unit = null,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun StatCell(
    label: String,
    value: String,
    unit: String?,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = VelnyxOffBlack),
    ) {
        Column(modifier = Modifier.padding(VelnyxSpacing.md)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = VelnyxGray400,
            )
            Spacer(modifier = Modifier.height(VelnyxSpacing.xs))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = VelnyxLime,
                )
                if (unit != null) {
                    Spacer(modifier = Modifier.padding(start = 4.dp))
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.bodyMedium,
                        color = VelnyxGray400,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun RouteMap(points: List<RunPointDto>) {
    val density = LocalDensity.current
    val paddingPx = remember(density) { with(density) { 50.dp.toPx().toInt() } }
    val latLngs = remember(points) { points.map { LatLng(it.lat, it.lon) } }
    val firstPoint = latLngs.firstOrNull()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .clip(RoundedCornerShape(16.dp)),
    ) {
        VelnyxMapView(
            modifier = Modifier.fillMaxSize(),
            currentLatLng = firstPoint,
            routePoints = latLngs,
            autoFollow = false,
            onMapReady = { map ->
                if (latLngs.size >= 2) {
                    val boundsBuilder = LatLngBounds.Builder()
                    latLngs.forEach { boundsBuilder.include(it) }
                    val bounds = boundsBuilder.build()
                    map.animateCamera(
                        CameraUpdateFactory.newLatLngBounds(bounds, paddingPx),
                    )
                } else if (firstPoint != null) {
                    map.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(firstPoint, 16.0),
                    )
                }
            },
        )
    }
}

@Composable
private fun NoRouteCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = VelnyxOffBlack),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(VelnyxSpacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "No route recorded",
                style = MaterialTheme.typography.titleLarge,
                color = VelnyxWhite,
            )
            Spacer(modifier = Modifier.height(VelnyxSpacing.xs))
            Text(
                text = "This run finished without any GPS fixes.",
                style = MaterialTheme.typography.bodyMedium,
                color = VelnyxGray400,
                textAlign = TextAlign.Center,
            )
        }
    }
}

// ─── Loading / Error ─────────────────────────────────────────────────────────

@Composable
private fun LoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = VelnyxLime)
    }
}

@Composable
private fun ErrorState(message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(VelnyxSpacing.xl),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Couldn't load this run",
            style = MaterialTheme.typography.titleLarge,
            color = VelnyxWhite,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(VelnyxSpacing.sm))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = VelnyxGray400,
            textAlign = TextAlign.Center,
        )
    }
}
