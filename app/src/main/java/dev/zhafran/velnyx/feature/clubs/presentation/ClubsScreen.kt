package dev.zhafran.velnyx.feature.clubs.presentation

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Schedule
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
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// ── Data models ──────────────────────────────────────────────────────────────

@Serializable
data class Club(
    val id: String,
    val name: String,
    val city: String,
    val members: Int,
    val description: String,
    val schedule: String,
)

private val clubsJson = Json { ignoreUnknownKeys = true }

private fun formatMembers(members: Int): String = when {
    members >= 1000 -> {
        val k = members / 1000.0
        val rounded = (k * 10).toInt() / 10.0
        if (rounded == rounded.toInt().toDouble()) "${rounded.toInt()}K members"
        else "${"%.1f".format(rounded)}K members"
    }
    else -> "$members members"
}

// ── Screen ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClubsScreen(
    onNavigateBack: () -> Unit = {},
) {
    val context = LocalContext.current
    var clubs by remember { mutableStateOf<List<Club>>(emptyList()) }
    var selected by remember { mutableStateOf<Club?>(null) }

    LaunchedEffect(Unit) {
        clubs = withContext(Dispatchers.IO) {
            runCatching {
                context.assets.open("clubs.json").bufferedReader().use { reader ->
                    clubsJson.decodeFromString<List<Club>>(reader.readText())
                }
            }.getOrElse { emptyList() }
        }
    }

    val current = selected
    if (current != null) {
        ClubDetailScreen(
            club = current,
            onNavigateBack = { selected = null },
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Running Clubs") },
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
            items(clubs, key = { it.id }) { club ->
                ClubCard(club = club, onClick = { selected = club })
            }
        }
    }
}

@Composable
private fun ClubCard(
    club: Club,
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
                    imageVector = Icons.Outlined.Groups,
                    contentDescription = null,
                    tint = VelnyxBlack,
                    modifier = Modifier.size(32.dp),
                )
            }
            Spacer(modifier = Modifier.width(VelnyxSpacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = club.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = VelnyxWhite,
                )
                Spacer(modifier = Modifier.height(2.dp))
                MetaRow(icon = Icons.Outlined.LocationOn, text = club.city)
                Spacer(modifier = Modifier.height(2.dp))
                MetaRow(icon = Icons.Outlined.Groups, text = formatMembers(club.members))
                Spacer(modifier = Modifier.height(2.dp))
                MetaRow(icon = Icons.Outlined.Schedule, text = club.schedule)
            }
        }
    }
}

@Composable
private fun MetaRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = VelnyxGray400,
            modifier = Modifier.size(14.dp),
        )
        Spacer(modifier = Modifier.width(VelnyxSpacing.xs))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = VelnyxGray400,
        )
    }
}

// ── Detail ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClubDetailScreen(
    club: Club,
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = club.name,
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
                            imageVector = Icons.Outlined.Groups,
                            contentDescription = null,
                            tint = VelnyxBlack,
                            modifier = Modifier.size(36.dp),
                        )
                    }
                    Spacer(modifier = Modifier.width(VelnyxSpacing.md))
                    Column {
                        MetaRow(icon = Icons.Outlined.LocationOn, text = club.city)
                        Spacer(modifier = Modifier.height(4.dp))
                        MetaRow(icon = Icons.Outlined.Groups, text = formatMembers(club.members))
                    }
                }
            }
            item {
                Text(
                    text = club.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = VelnyxGray100,
                )
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = VelnyxOffBlack),
                ) {
                    Column(modifier = Modifier.padding(VelnyxSpacing.md)) {
                        Text(
                            text = "Schedule",
                            style = MaterialTheme.typography.titleLarge,
                            color = VelnyxWhite,
                        )
                        Spacer(modifier = Modifier.height(VelnyxSpacing.xs))
                        Text(
                            text = club.schedule,
                            style = MaterialTheme.typography.bodyMedium,
                            color = VelnyxGray400,
                        )
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(VelnyxSpacing.sm))
                Text(
                    text = "Static Posts",
                    style = MaterialTheme.typography.titleLarge,
                    color = VelnyxWhite,
                )
            }
            item {
                StaticPostCard(
                    author = "Rama K.",
                    timeAgo = "2h ago",
                    body = "Great run this morning! 5K easy with the squad, perfect cool weather.",
                )
            }
            item {
                StaticPostCard(
                    author = "Sari W.",
                    timeAgo = "Yesterday",
                    body = "PR'd my 10K loop today — thanks to whoever paced me through km 7-9.",
                )
            }
        }
    }
}

@Composable
private fun StaticPostCard(
    author: String,
    timeAgo: String,
    body: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = VelnyxOffBlack),
    ) {
        Column(modifier = Modifier.padding(VelnyxSpacing.md)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(VelnyxLime, RoundedCornerShape(percent = 50)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = author.firstOrNull()?.uppercase() ?: "•",
                        style = MaterialTheme.typography.labelLarge,
                        color = VelnyxBlack,
                    )
                }
                Spacer(modifier = Modifier.width(VelnyxSpacing.sm))
                Column {
                    Text(
                        text = author,
                        style = MaterialTheme.typography.labelLarge,
                        color = VelnyxWhite,
                    )
                    Text(
                        text = timeAgo,
                        style = MaterialTheme.typography.bodyMedium,
                        color = VelnyxGray400,
                    )
                }
            }
            Spacer(modifier = Modifier.height(VelnyxSpacing.sm))
            Text(
                text = body,
                style = MaterialTheme.typography.bodyLarge,
                color = VelnyxGray100,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun ClubsScreenPreview() {
    VelnyxTheme { ClubsScreen() }
}
