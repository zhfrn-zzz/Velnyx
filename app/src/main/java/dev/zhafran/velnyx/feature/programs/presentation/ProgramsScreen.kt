package dev.zhafran.velnyx.feature.programs.presentation

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.material3.Text
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxBlack
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxGray100
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxGray400
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxLime
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxOffBlack
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxSpacing
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxTheme
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxWhite
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// ── Data models ──────────────────────────────────────────────────────────────

@Serializable
data class Program(
    val id: String,
    val title: String,
    val subtitle: String,
    val description: String,
    @SerialName("image_emoji") val imageEmoji: String,
    val weeks: List<ProgramWeek>,
)

@Serializable
data class ProgramWeek(
    val week: Int,
    val days: List<ProgramDay>,
)

@Serializable
data class ProgramDay(
    val day: String,
    val workout: String,
)

private val programsJson = Json { ignoreUnknownKeys = true }

// ── Screen ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgramsScreen(
    onNavigateBack: () -> Unit = {},
) {
    val context = LocalContext.current
    var programs by remember { mutableStateOf<List<Program>>(emptyList()) }
    var selected by remember { mutableStateOf<Program?>(null) }

    LaunchedEffect(Unit) {
        programs = withContext(Dispatchers.IO) {
            runCatching {
                context.assets.open("programs.json").bufferedReader().use { reader ->
                    programsJson.decodeFromString<List<Program>>(reader.readText())
                }
            }.getOrElse { emptyList() }
        }
    }

    val current = selected
    if (current != null) {
        ProgramDetailScreen(
            program = current,
            onNavigateBack = { selected = null },
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Running Programs") },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(VelnyxSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(VelnyxSpacing.md),
        ) {
            items(programs, key = { it.id }) { program ->
                ProgramCard(program = program, onClick = { selected = program })
            }
        }
    }
}

@Composable
private fun ProgramCard(
    program: Program,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = VelnyxOffBlack),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(VelnyxSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(VelnyxLime, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.DirectionsRun,
                    contentDescription = null,
                    tint = VelnyxBlack,
                    modifier = Modifier.size(32.dp),
                )
            }
            Spacer(modifier = Modifier.width(VelnyxSpacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = program.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = VelnyxWhite,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = program.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = VelnyxGray400,
                )
            }
        }
    }
}

// ── Detail ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProgramDetailScreen(
    program: Program,
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = program.title,
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(VelnyxSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(VelnyxSpacing.md),
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(VelnyxLime, RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.DirectionsRun,
                            contentDescription = null,
                            tint = VelnyxBlack,
                            modifier = Modifier.size(36.dp),
                        )
                    }
                    Spacer(modifier = Modifier.width(VelnyxSpacing.md))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = program.subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = VelnyxGray400,
                        )
                    }
                }
            }
            item {
                Text(
                    text = program.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = VelnyxGray100,
                )
            }
            item {
                Spacer(modifier = Modifier.height(VelnyxSpacing.sm))
                Text(
                    text = "Weekly Schedule",
                    style = MaterialTheme.typography.titleLarge,
                    color = VelnyxWhite,
                )
            }
            val firstWeek = program.weeks.firstOrNull()
            if (firstWeek != null) {
                item {
                    WeekPills(week = firstWeek)
                }
            } else {
                item {
                    Text(
                        text = "Schedule coming soon.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = VelnyxGray400,
                    )
                }
            }
        }
    }
}

@Composable
private fun WeekPills(week: ProgramWeek) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(VelnyxSpacing.sm),
    ) {
        Text(
            text = "Week ${week.week}",
            style = MaterialTheme.typography.titleLarge,
            color = VelnyxWhite,
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(VelnyxSpacing.sm),
        ) {
            items(week.days) { day ->
                DayPill(day = day)
            }
        }
    }
}

@Composable
private fun DayPill(day: ProgramDay) {
    Card(
        modifier = Modifier.width(120.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = VelnyxOffBlack),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(VelnyxSpacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(VelnyxSpacing.xs),
        ) {
            Box(
                modifier = Modifier
                    .background(VelnyxLime, RoundedCornerShape(percent = 50))
                    .padding(horizontal = VelnyxSpacing.md, vertical = 4.dp),
            ) {
                Text(
                    text = day.day,
                    style = MaterialTheme.typography.labelLarge,
                    color = VelnyxBlack,
                )
            }
            Text(
                text = day.workout,
                style = MaterialTheme.typography.bodyMedium,
                color = VelnyxGray100,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun ProgramsScreenPreview() {
    VelnyxTheme { ProgramsScreen() }
}
