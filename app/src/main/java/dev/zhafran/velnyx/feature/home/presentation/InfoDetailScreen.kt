package dev.zhafran.velnyx.feature.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxBlack
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxGray100
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxLime
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxOffBlack
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxSpacing
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxTheme
import dev.zhafran.velnyx.core.designsystem.theme.VelnyxWhite
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

private val infoDetailJson = Json { ignoreUnknownKeys = true }

/**
 * Reads `running_info.json` from assets and renders the article matching [articleId].
 *
 * Loads its own data instead of receiving the article via the route, because
 * type-safe Navigation Compose routes only carry primitive args. The JSON is
 * tiny (4 articles), so re-reading on detail navigation is essentially free.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoDetailScreen(
    articleId: String,
    onNavigateBack: () -> Unit = {},
) {
    val context = LocalContext.current
    var state by remember { mutableStateOf<InfoDetailState>(InfoDetailState.Loading) }

    LaunchedEffect(articleId) {
        state = withContext(Dispatchers.IO) {
            runCatching {
                val text = context.assets.open("running_info.json").bufferedReader().use { it.readText() }
                val all = infoDetailJson.decodeFromString<List<RunningInfo>>(text)
                val match = all.firstOrNull { it.id == articleId }
                if (match != null) InfoDetailState.Success(match) else InfoDetailState.NotFound
            }.getOrElse { InfoDetailState.NotFound }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = (state as? InfoDetailState.Success)?.article?.title.orEmpty(),
                        style = MaterialTheme.typography.titleLarge,
                        color = VelnyxWhite,
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
                    navigationIconContentColor = VelnyxWhite,
                ),
            )
        },
        containerColor = VelnyxBlack,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(VelnyxBlack),
        ) {
            when (val s = state) {
                InfoDetailState.Loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = VelnyxLime)
                }
                InfoDetailState.NotFound -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(VelnyxSpacing.xl),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Article not found",
                        style = MaterialTheme.typography.titleLarge,
                        color = VelnyxWhite,
                        textAlign = TextAlign.Center,
                    )
                }
                is InfoDetailState.Success -> ArticleBody(article = s.article)
            }
        }
    }
}

@Composable
private fun ArticleBody(article: RunningInfo) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = VelnyxSpacing.lg),
    ) {
        Spacer(modifier = Modifier.height(VelnyxSpacing.xl))

        // Large centered emoji
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(VelnyxOffBlack, RoundedCornerShape(28.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = article.emoji, fontSize = 56.sp)
            }
        }

        Spacer(modifier = Modifier.height(VelnyxSpacing.lg))

        // Category pill (centered)
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .background(VelnyxLime, RoundedCornerShape(percent = 50))
                    .padding(horizontal = VelnyxSpacing.md, vertical = 4.dp),
            ) {
                Text(
                    text = article.category,
                    style = MaterialTheme.typography.labelLarge,
                    color = VelnyxBlack,
                )
            }
        }

        Spacer(modifier = Modifier.height(VelnyxSpacing.md))

        // Title
        Text(
            text = article.title,
            style = MaterialTheme.typography.headlineLarge,
            color = VelnyxWhite,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(VelnyxSpacing.lg))

        // Body
        Text(
            text = article.body,
            style = MaterialTheme.typography.bodyLarge,
            color = VelnyxGray100,
            lineHeight = 24.sp,
        )

        Spacer(modifier = Modifier.height(VelnyxSpacing.xxl))
    }
}

private sealed interface InfoDetailState {
    data object Loading : InfoDetailState
    data object NotFound : InfoDetailState
    data class Success(val article: RunningInfo) : InfoDetailState
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun InfoDetailScreenPreview() {
    VelnyxTheme {
        InfoDetailScreen(articleId = "warmup")
    }
}
