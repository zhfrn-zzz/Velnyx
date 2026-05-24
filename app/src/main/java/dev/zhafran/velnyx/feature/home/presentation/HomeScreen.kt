package dev.zhafran.velnyx.feature.home.presentation

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
import dev.zhafran.velnyx.feature.history.presentation.HistoryUiState
import dev.zhafran.velnyx.feature.history.presentation.HistoryViewModel
import dev.zhafran.velnyx.feature.history.presentation.formatDate
import dev.zhafran.velnyx.feature.history.presentation.formatDuration
import dev.zhafran.velnyx.feature.history.presentation.formatPace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.Locale

private fun formatTimestamp(millis: Long): String {
    val sdf = java.text.SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
    return sdf.format(java.util.Date(millis))
}

// ── Data model ──────────────────────────────────────────────────────────────

@Serializable
data class RunningInfo(
    val id: String,
    val title: String,
    val category: String,
    val emoji: String,
    val body: String,
)

private val runningInfoJson = Json { ignoreUnknownKeys = true }

// ── Screen ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToCountdown: () -> Unit = {},
    onResumeOrphanedRun: (runId: Long) -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToPrograms: () -> Unit = {},
    onNavigateToClubs: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToInfoDetail: (String) -> Unit = {},
    homeViewModel: HomeViewModel = hiltViewModel(),
    historyViewModel: HistoryViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    var infos by remember { mutableStateOf<List<RunningInfo>>(emptyList()) }
    val historyState by historyViewModel.runs.collectAsStateWithLifecycle()
    val orphanedRun by homeViewModel.orphanedRun.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        infos = withContext(Dispatchers.IO) {
            runCatching {
                context.assets.open("running_info.json").bufferedReader().use { reader ->
                    runningInfoJson.decodeFromString<List<RunningInfo>>(reader.readText())
                }
            }.getOrElse { emptyList() }
        }
    }

    // ── Orphaned run recovery dialog ────────────────────────────────────────
    orphanedRun?.let { orphan ->
        AlertDialog(
            shape = RoundedCornerShape(16.dp),
            containerColor = VelnyxOffBlack,
            titleContentColor = VelnyxWhite,
            textContentColor = VelnyxGray400,
            onDismissRequest = { /* force choice */ },
            title = { Text("Unfinished Run") },
            text = {
                Text(
                    "You have a paused run from ${formatTimestamp(orphan.startedAt)}. " +
                        "What would you like to do?"
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    homeViewModel.resumeOrphanedRun()
                    onResumeOrphanedRun(orphan.id)
                }) { Text("Resume", color = VelnyxLime) }
            },
            dismissButton = {
                TextButton(onClick = { homeViewModel.discardOrphanedRun() }) {
                    Text("Discard", color = VelnyxGray400)
                }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "VELNYX",
                        style = MaterialTheme.typography.titleLarge,
                        color = VelnyxWhite,
                    )
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToSettings,
                        colors = IconButtonDefaults.iconButtonColors(contentColor = VelnyxWhite),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Settings",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VelnyxBlack,
                    titleContentColor = VelnyxWhite,
                    actionIconContentColor = VelnyxWhite,
                ),
            )
        },
        containerColor = VelnyxBlack,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(VelnyxBlack)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = VelnyxSpacing.lg),
        ) {
            Spacer(modifier = Modifier.height(VelnyxSpacing.md))

            Text(
                text = "Ready to run?",
                style = MaterialTheme.typography.headlineLarge,
                color = VelnyxWhite,
            )

            Spacer(modifier = Modifier.height(VelnyxSpacing.lg))
            VelnyxPrimaryButton(text = "Start Run", onClick = onNavigateToCountdown)

            Spacer(modifier = Modifier.height(VelnyxSpacing.md))
            Row(horizontalArrangement = Arrangement.spacedBy(VelnyxSpacing.sm)) {
                DarkOutlinedButton(
                    text = "Programs",
                    onClick = onNavigateToPrograms,
                    modifier = Modifier.weight(1f),
                )
                DarkOutlinedButton(
                    text = "Clubs",
                    onClick = onNavigateToClubs,
                    modifier = Modifier.weight(1f),
                )
            }

            // ── Recent Activity ─────────────────────────────────────────────
            RecentActivitySection(
                state = historyState,
                onSeeAll = onNavigateToHistory,
                onNavigateToCountdown = onNavigateToCountdown,
            )

            // ── Running Information ─────────────────────────────────────────
            if (infos.isNotEmpty()) {
                Spacer(modifier = Modifier.height(VelnyxSpacing.xl))
                Text(
                    text = "Running Information",
                    style = MaterialTheme.typography.titleLarge,
                    color = VelnyxWhite,
                )
                Spacer(modifier = Modifier.height(VelnyxSpacing.sm))
                LazyRow(
                    contentPadding = PaddingValues(end = VelnyxSpacing.lg),
                    horizontalArrangement = Arrangement.spacedBy(VelnyxSpacing.md),
                ) {
                    items(infos, key = { it.id }) { info ->
                        InfoCard(
                            info = info,
                            onClick = { onNavigateToInfoDetail(info.id) },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(VelnyxSpacing.xl))
        }
    }
}

// ── Buttons ─────────────────────────────────────────────────────────────────

/**
 * Outlined button styled for the dark home surface. Local to HomeScreen so the
 * shared `VelnyxSecondaryButton` (which assumes a light surface) stays untouched
 * and other screens keep their existing appearance.
 */
@Composable
private fun DarkOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = MaterialTheme.shapes.extraLarge,
        border = BorderStroke(1.5.dp, VelnyxLime),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = VelnyxWhite),
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

// ── Recent Activity ──────────────────────────────────────────────────────────

@Composable
private fun RecentActivitySection(
    state: HistoryUiState,
    onSeeAll: () -> Unit,
    onNavigateToCountdown: () -> Unit,
) {
    // Per spec: silently hide on Error.
    if (state is HistoryUiState.Error) return

    Spacer(modifier = Modifier.height(VelnyxSpacing.xl))
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Recent Activity",
            style = MaterialTheme.typography.titleLarge,
            color = VelnyxWhite,
            modifier = Modifier.weight(1f),
        )
        if (state is HistoryUiState.Success) {
            TextButton(onClick = onSeeAll) {
                Text(
                    text = "See all",
                    color = VelnyxGray100,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(VelnyxSpacing.sm))

    when (state) {
        is HistoryUiState.Loading -> RecentSkeletonCard()
        is HistoryUiState.Empty -> EmptyRecentCard(onClick = onNavigateToCountdown)
        is HistoryUiState.Success -> {
            Column(verticalArrangement = Arrangement.spacedBy(VelnyxSpacing.sm)) {
                state.runs.take(3).forEach { run ->
                    RecentRunCard(run = run, onClick = onSeeAll)
                }
            }
        }
        is HistoryUiState.Error -> Unit // already handled above
    }
}

@Composable
private fun RecentSkeletonCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(
                color = VelnyxOffBlack,
                shape = MaterialTheme.shapes.medium,
            ),
    )
}

@Composable
private fun DarkRunCard(
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = VelnyxOffBlack),
    ) {
        Box(modifier = Modifier.padding(VelnyxSpacing.md)) {
            content()
        }
    }
}

@Composable
private fun EmptyRecentCard(onClick: () -> Unit) {
    DarkRunCard(onClick = onClick) {
        Column {
            Text(
                text = "Complete your first run!",
                style = MaterialTheme.typography.titleLarge,
                color = VelnyxWhite,
            )
            Spacer(modifier = Modifier.height(VelnyxSpacing.xs))
            Text(
                text = "Tap Start Run above and head outside.",
                style = MaterialTheme.typography.bodyMedium,
                color = VelnyxGray400,
            )
        }
    }
}

@Composable
private fun RecentRunCard(run: RunSummaryDto, onClick: () -> Unit) {
    DarkRunCard(onClick = onClick) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = formatDate(run.startedAt),
                    style = MaterialTheme.typography.bodyLarge,
                    color = VelnyxGray400,
                )
                Text(
                    text = "%.2f km".format(Locale.ENGLISH, run.distanceM / 1000.0),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = VelnyxLime,
                )
            }
            Spacer(modifier = Modifier.height(VelnyxSpacing.xs))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(VelnyxSpacing.lg),
            ) {
                Text(
                    text = formatDuration(run.durationS),
                    style = MaterialTheme.typography.bodyMedium,
                    color = VelnyxGray400,
                )
                Text(
                    text = formatPace(run.avgPaceSecondsPerKm),
                    style = MaterialTheme.typography.bodyMedium,
                    color = VelnyxGray400,
                )
            }
        }
    }
}

// ── Running Information ──────────────────────────────────────────────────────

@Composable
private fun InfoCard(
    info: RunningInfo,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(220.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = VelnyxOffBlack),
    ) {
        Column(modifier = Modifier.padding(VelnyxSpacing.md)) {
            Text(
                text = info.emoji,
                fontSize = 32.sp,
            )
            Spacer(modifier = Modifier.height(VelnyxSpacing.sm))
            Text(
                text = info.title,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp),
                color = VelnyxWhite,
                maxLines = 2,
            )
            Spacer(modifier = Modifier.height(VelnyxSpacing.sm))
            CategoryPill(category = info.category)
        }
    }
}

@Composable
private fun CategoryPill(category: String) {
    Box(
        modifier = Modifier
            .background(VelnyxLime, RoundedCornerShape(percent = 50))
            .padding(horizontal = VelnyxSpacing.md, vertical = 4.dp),
    ) {
        Text(
            text = category,
            style = MaterialTheme.typography.labelLarge,
            color = VelnyxBlack,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
fun HomeScreenPreview() {
    VelnyxTheme { HomeScreen() }
}
