package com.openstream.app.ui.screen.search

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.openstream.app.core.plugins.SearchResponse
import com.openstream.app.ui.components.GlassBackground
import com.openstream.app.ui.components.GlassCard
import com.openstream.app.ui.components.GlassChip

private val TRENDING = listOf("Naruto", "Breaking Bad", "One Piece", "Avengers", "Attack on Titan", "Stranger Things")
private val FILTERS  = listOf("All", "Movies", "TV", "Anime", "Live")

@Composable
fun SearchScreen(
    navController : NavController,
    viewModel     : SearchViewModel = hiltViewModel()
) {
    val uiState     by viewModel.uiState.collectAsState()
    var query       by remember { mutableStateOf("") }
    var activeFilter by remember { mutableStateOf("All") }
    val focusReq   = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        runCatching { focusReq.requestFocus() }
    }

    GlassBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            // â”€â”€ Search bar â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                }
                GlassCard(
                    modifier       = Modifier.weight(1f),
                    cornerRadius   = 14.dp,
                    contentPadding = 0.dp
                ) {
                    Row(
                        modifier          = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Search, null, tint = Color.White.copy(0.50f), modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        BasicTextField(
                            value         = query,
                            onValueChange = { query = it; if (it.isNotBlank()) viewModel.search(it) else viewModel.clear() },
                            modifier      = Modifier.weight(1f).focusRequester(focusReq),
                            textStyle     = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                            cursorBrush   = SolidColor(Color.White),
                            singleLine    = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { viewModel.search(query) }),
                            decorationBox = { inner ->
                                if (query.isEmpty()) Text("Search movies, shows, anime...",
                                    style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(0.35f))
                                inner()
                            }
                        )
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = ""; viewModel.clear() }, modifier = Modifier.size(20.dp)) {
                                Icon(Icons.Default.Close, null, tint = Color.White.copy(0.60f), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // â”€â”€ Filter chips â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
            Row(
                modifier              = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FILTERS.forEach { f ->
                    FilterChip(
                        selected = activeFilter == f,
                        onClick  = { activeFilter = f; if (query.isNotBlank()) viewModel.search(query) },
                        label    = { Text(f) },
                        colors   = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(0.38f),
                            selectedLabelColor     = Color.White,
                            containerColor         = Color.White.copy(0.08f),
                            labelColor             = Color.White.copy(0.65f)
                        )
                    )
                }
            }

            // â”€â”€ Content area â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
            when (val state = uiState) {
                is SearchUiState.Idle    -> TrendingSection()
                is SearchUiState.Loading -> SearchSkeletonGrid()
                is SearchUiState.Empty   -> EmptySection()
                is SearchUiState.Error   -> ErrorSection(state.message) { viewModel.search(query) }
                is SearchUiState.Success -> {
                    val filtered = if (activeFilter == "All") state.results
                    else state.results.filter { it.type.equals(activeFilter, ignoreCase = true) }
                    ResultsGrid(filtered)
                }
            }
        }
    }
}

// â”€â”€ Trending â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

@Composable
private fun TrendingSection() {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
        Text("Trending Searches", style = MaterialTheme.typography.titleSmall,
            color = Color.White.copy(0.60f), fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))
        TRENDING.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { term ->
                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(0.10f), RoundedCornerShape(50))
                            .clickable {}
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(term, style = MaterialTheme.typography.labelMedium, color = Color.White.copy(0.80f))
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

// â”€â”€ Results grid â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

@Composable
private fun ResultsGrid(items: List<SearchResponse>) {
    LazyVerticalGrid(
        columns             = GridCells.Fixed(3),
        contentPadding      = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement   = Arrangement.spacedBy(8.dp),
        modifier            = Modifier.fillMaxSize()
    ) {
        items(items, key = { it.url.ifBlank { it.name } }) { item ->
            SearchPosterCard(item)
        }
    }
}

@Composable
private fun SearchPosterCard(item: SearchResponse) {
    Box(
        modifier = Modifier
            .aspectRatio(2f / 3f)
            .clip(RoundedCornerShape(10.dp))
            .clickable {}
    ) {
        if (item.posterUrl.isNotBlank()) {
            AsyncImage(item.posterUrl, item.name, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        } else {
            Box(
                modifier = Modifier.fillMaxSize().background(
                    Brush.linearGradient(listOf(Color(0xFF7C6FFF).copy(0.60f), Color(0xFF03DAC6).copy(0.40f)))
                ),
                contentAlignment = Alignment.Center
            ) {
                Text(item.name.take(1).uppercase(), style = MaterialTheme.typography.titleMedium,
                    color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth().align(Alignment.BottomCenter)
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.85f))))
                .padding(6.dp)
        ) {
            Text(item.name, style = MaterialTheme.typography.labelSmall, color = Color.White,
                maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

// â”€â”€ Skeleton â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

@Composable
private fun SearchSkeletonGrid() {
    val alpha by rememberInfiniteTransition(label = "sk").animateFloat(
        0.3f, 0.7f,
        infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "alpha"
    )
    LazyVerticalGrid(
        columns             = GridCells.Fixed(3),
        contentPadding      = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement   = Arrangement.spacedBy(8.dp)
    ) {
        items(12) {
            Box(modifier = Modifier.aspectRatio(2f / 3f).clip(RoundedCornerShape(10.dp)).background(Color.White.copy(alpha)))
        }
    }
}

@Composable
private fun EmptySection() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Nothing found", style = MaterialTheme.typography.titleMedium, color = Color.White.copy(0.55f), fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Text("Try a different search term", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.35f))
        }
    }
}

@Composable
private fun ErrorSection(msg: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Search failed", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Text(msg, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.55f))
                Spacer(Modifier.height(16.dp))
                Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) { Text("Retry") }
            }
        }
    }
}
