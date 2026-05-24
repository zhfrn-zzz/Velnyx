package dev.zhafran.velnyx.feature.history.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zhafran.velnyx.core.designsystem.component.VelnyxPrimaryButton
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxBlack
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxGray100
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxGray400
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxLime
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxOffBlack
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxSpacing
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxTheme
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxWhite
import dev.zhafran.velnyx.feature.history.data.RunSummaryDto
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryListScreen(
    onNavigateToDetail: (String) -> Unit = {},
    onNavigateBack: () -> Unit = {},
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.runs.collectAsStateWithLifecycle()
    val isRefreshing = uiState is HistoryUiState.Loading
    val pullState = rememberPullToRefreshState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Activity history") },
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
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = VelnyxBlack,
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = viewModel::refresh,
            state = pullState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (val state = uiState) {
                is HistoryUiState.Loading -> LoadingState()
                is HistoryUiState.Empty -> EmptyState()
                is HistoryUiState.Error -> ErrorState(
                    message = state.msg,
                    onRetry = {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                "Couldn't load runs. Check your connection."
                            )
                        }
                        viewModel.refresh()
                    },
                )
                is HistoryUiState.Success -> SuccessContent(
                    runs = state.runs,
                    onRunClick = onNavigateToDetail,
                )
            }
        }
    }
}

// ─── Success ─────────────────────────────────────────────────────────────────

@Composable
private fun SuccessContent(
    runs: List<RunSummaryDto>,
    onRunClick: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = VelnyxSpacing.md,
            vertical = VelnyxSpacing.md,
        ),
        verticalArrangement = Arrangement.spacedBy(VelnyxSpacing.sm),
    ) {
        item { TotalDistanceHero(runs = runs) }
        items(items = runs, key = { it.id }) { run ->
            RunHistoryCard(run = run, onClick = { onRunClick(run.id) })
        }
    }
}

@Composable
private fun TotalDistanceHero(runs: List<RunSummaryDto>) {
    val totalKm = runs.sumOf { it.distanceM }.toDouble() / 1000.0
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        color = VelnyxOffBlack,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(VelnyxSpacing.lg),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Total distance",
                color = VelnyxGray400,
                style = MaterialTheme.typography.labelLarge,
            )
            Spacer(modifier = Modifier.height(VelnyxSpacing.xs))
            Text(
                text = "%s KM total".format(Locale.ENGLISH, formatKm(totalKm)),
                color = VelnyxLime,
                fontWeight = FontWeight.Bold,
                fontSize = 40.sp,
            )
            Spacer(modifier = Modifier.height(VelnyxSpacing.xs))
            Text(
                text = "Across ${runs.size} run${if (runs.size == 1) "" else "s"}",
                color = VelnyxWhite.copy(alpha = 0.6f),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun RunHistoryCard(run: RunSummaryDto, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = VelnyxOffBlack),
    ) {
        Column(modifier = Modifier.padding(VelnyxSpacing.md)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = formatDate(run.startedAt),
                    style = MaterialTheme.typography.titleLarge,
                    color = VelnyxWhite,
                )
                Text(
                    text = "%.2f km".format(Locale.ENGLISH, run.distanceM / 1000.0),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = VelnyxLime,
                )
            }
            Spacer(modifier = Modifier.height(VelnyxSpacing.sm))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(VelnyxSpacing.lg),
            ) {
                MetricInline(label = "Time", value = formatDuration(run.durationS))
                MetricInline(
                    label = "Pace",
                    value = formatPace(run.avgPaceSecondsPerKm),
                )
            }
        }
    }
}

@Composable
private fun MetricInline(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = VelnyxGray400,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = VelnyxGray100,
            fontWeight = FontWeight.Medium,
        )
    }
}

// ─── Loading / Empty / Error states ──────────────────────────────────────────

@Composable
private fun LoadingState() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = VelnyxSpacing.md,
            vertical = VelnyxSpacing.md,
        ),
        verticalArrangement = Arrangement.spacedBy(VelnyxSpacing.sm),
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(
                        color = VelnyxOffBlack,
                        shape = MaterialTheme.shapes.medium,
                    ),
            )
        }
        items(count = 3) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .background(
                        color = VelnyxOffBlack,
                        shape = MaterialTheme.shapes.medium,
                    ),
            )
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(VelnyxSpacing.xl),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.DirectionsRun,
            contentDescription = null,
            tint = VelnyxGray400,
            modifier = Modifier.size(72.dp),
        )
        Spacer(modifier = Modifier.height(VelnyxSpacing.md))
        Text(
            text = "No runs yet. Time to move!",
            style = MaterialTheme.typography.titleLarge,
            color = VelnyxWhite,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(VelnyxSpacing.xl),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Couldn't load your runs",
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
        Spacer(modifier = Modifier.height(VelnyxSpacing.lg))
        VelnyxPrimaryButton(
            text = "Try again",
            onClick = onRetry,
        )
    }
}

// ─── Formatting helpers ──────────────────────────────────────────────────────

private val DATE_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEE, d MMM", Locale.ENGLISH)

internal fun formatDate(iso: String): String = try {
    Instant.parse(iso)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .format(DATE_FORMATTER)
} catch (_: Exception) {
    iso
}

internal fun formatKm(km: Double): String = "%.1f".format(Locale.ENGLISH, km)

internal fun formatDuration(durationS: Int): String {
    val total = durationS.coerceAtLeast(0)
    val h = total / 3600
    val m = (total % 3600) / 60
    val s = total % 60
    return if (h > 0) {
        "%d:%02d:%02d".format(Locale.ENGLISH, h, m, s)
    } else {
        "%02d:%02d".format(Locale.ENGLISH, m, s)
    }
}

internal fun formatPace(secondsPerKm: Int?): String {
    if (secondsPerKm == null || secondsPerKm <= 0) return "--:--"
    val min = secondsPerKm / 60
    val sec = secondsPerKm % 60
    return "%d:%02d /km".format(Locale.ENGLISH, min, sec)
}

// ─── Previews ────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun HistoryListScreenEmptyPreview() {
    VelnyxTheme {
        Surface(color = VelnyxBlack) { EmptyState() }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun HistoryListScreenLoadingPreview() {
    VelnyxTheme {
        Surface(color = VelnyxBlack) { LoadingState() }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun HistoryListScreenErrorPreview() {
    VelnyxTheme {
        Surface(color = VelnyxBlack) { ErrorState(message = "Network unreachable", onRetry = {}) }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun HistoryListSuccessPreview() {
    VelnyxTheme {
        Surface(color = VelnyxBlack) {
            SuccessContent(
                runs = listOf(
                    RunSummaryDto(
                        id = "1",
                        startedAt = "2026-05-16T07:42:11Z",
                        distanceM = 5230,
                        durationS = 1893,
                        avgPaceSecondsPerKm = 362,
                        calories = 312,
                    ),
                    RunSummaryDto(
                        id = "2",
                        startedAt = "2026-05-15T18:05:00Z",
                        distanceM = 2410,
                        durationS = 845,
                        avgPaceSecondsPerKm = null,
                        calories = null,
                    ),
                ),
                onRunClick = {},
            )
        }
    }
}
