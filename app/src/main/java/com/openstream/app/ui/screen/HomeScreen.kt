package com.openstream.app.ui.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.openstream.app.core.plugins.SearchResponse
import com.openstream.app.navigation.Screen
import com.openstream.app.ui.components.GlassBackground
import com.openstream.app.ui.components.GlassCard
import com.openstream.app.ui.components.GlassChip
import com.openstream.app.ui.components.GlassTopBar
import com.openstream.app.ui.screen.home.HomeUiState
import com.openstream.app.ui.screen.home.HomeViewModel
import com.openstream.app.ui.theme.ThemeViewModel
import com.openstream.app.ui.update.AppUpdateViewModel
import com.openstream.app.ui.update.UpdateBanner
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    navController   : NavController,
    themeViewModel  : ThemeViewModel     = hiltViewModel(),
    homeViewModel   : HomeViewModel      = hiltViewModel(),
    updateViewModel : AppUpdateViewModel = hiltViewModel()
) {
    val homeState   by homeViewModel.uiState.collectAsState()
    val updateState by updateViewModel.state.collectAsState()
    var longPressed by remember { mutableStateOf<SearchResponse?>(null) }

    GlassBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                GlassTopBar(
                    title = {
                        Text("OpenStream", style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold, color = Color.White)
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate(Screen.Search.route) }) {
                            Icon(Icons.Default.Search, null, tint = Color.White)
                        }
                    }
                )
            }
        ) { pad ->
            LazyColumn(
                modifier            = Modifier.fillMaxSize().padding(pad),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                item {
                    UpdateBanner(state = updateState, onDismiss = { updateViewModel.dismiss() })
                }

                when (val s = homeState) {
                    is HomeUiState.Loading -> item { HomeSkeletonSection() }
                    is HomeUiState.Error   -> item { HomeErrorSection(s.message) { homeViewModel.load() } }
                    is HomeUiState.Empty   -> item { HomeEmptySection() }
                    is HomeUiState.Success -> {
                        val allItems  = s.sections.flatMap { it.items }
                        val heroItems = allItems.take(6)

                        if (heroItems.isNotEmpty()) {
                            item { HeroBanner(items = heroItems) }
                        }

                        s.sections.forEach { section ->
                            item(key = "hdr_${section.title}") {
                                SectionHeader(title = section.title, onSeeAll = {})
                            }
                            item(key = "row_${section.title}") {
                                ContentCarousel(items = section.items, onLongPress = { longPressed = it })
                            }
                        }
                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }

    longPressed?.let { item ->
        ItemBottomSheet(item = item, onDismiss = { longPressed = null })
    }
}

// â”€â”€ Hero Banner â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HeroBanner(items: List<SearchResponse>) {
    if (items.isEmpty()) return
    val pagerState = rememberPagerState(pageCount = { items.size })

    LaunchedEffect(pagerState) {
        while (true) {
            delay(5_000L)
            val next = (pagerState.currentPage + 1) % items.size
            pagerState.animateScrollToPage(next)
        }
    }

    Box(modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f)) {
        HorizontalPager(state = pagerState) { page ->
            HeroBannerItem(item = items[page])
        }

        // Dot indicators
        Row(
            modifier              = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            repeat(items.size) { i ->
                Box(
                    modifier = Modifier
                        .size(if (pagerState.currentPage == i) 20.dp else 6.dp, 6.dp)
                        .clip(CircleShape)
                        .background(if (pagerState.currentPage == i) Color.White else Color.White.copy(0.45f))
                )
            }
        }
    }
}

@Composable
private fun HeroBannerItem(item: SearchResponse) {
    Box(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            model              = item.posterUrl.ifBlank { null },
            contentDescription = item.name,
            modifier           = Modifier.fillMaxSize(),
            contentScale       = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(0.85f)),
                        startY = 100f
                    )
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(item.name, style = MaterialTheme.typography.headlineSmall, color = Color.White,
                fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (item.type.isNotBlank())       GlassChip(item.type)
                if (item.sourceName.isNotBlank()) GlassChip(item.sourceName)
            }
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick        = {},
                    colors         = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape          = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, null, tint = Color.Black, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Watch Now", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                OutlinedButton(
                    onClick        = {},
                    shape          = RoundedCornerShape(10.dp),
                    border         = BorderStroke(1.dp, Color.White.copy(0.60f)),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Watchlist", color = Color.White, fontSize = 14.sp)
                }
            }
        }
    }
}

// â”€â”€ Section header â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

@Composable
private fun SectionHeader(title: String, onSeeAll: () -> Unit) {
    Row(
        modifier          = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = Color.White,
            fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        TextButton(onClick = onSeeAll, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)) {
            Text("See All", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.width(2.dp))
            Icon(Icons.Default.ArrowForward, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
        }
    }
}

// â”€â”€ Carousel â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

@Composable
private fun ContentCarousel(items: List<SearchResponse>, onLongPress: (SearchResponse) -> Unit) {
    LazyRow(
        contentPadding        = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(items, key = { it.url.ifBlank { it.name } }) { item ->
            PosterCard(item = item, onLongPress = onLongPress)
        }
    }
}

@Composable
private fun PosterCard(item: SearchResponse, onLongPress: (SearchResponse) -> Unit) {
    Box(
        modifier = Modifier
            .size(110.dp, 165.dp)
            .clip(RoundedCornerShape(10.dp))
            .pointerInput(item) {
                detectTapGestures(onLongPress = { onLongPress(item) })
            }
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
                Text(item.name.take(1).uppercase(), style = MaterialTheme.typography.titleLarge,
                    color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth().align(Alignment.BottomCenter)
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.80f))))
                .padding(6.dp)
        ) {
            Text(item.name, style = MaterialTheme.typography.labelSmall, color = Color.White,
                maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

// â”€â”€ Item Bottom Sheet â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItemBottomSheet(item: SearchResponse, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = Color(0xFF13131E)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Box(modifier = Modifier.size(90.dp, 135.dp).clip(RoundedCornerShape(10.dp))) {
                if (item.posterUrl.isNotBlank()) {
                    AsyncImage(item.posterUrl, item.name, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary.copy(0.40f)))
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                if (item.type.isNotBlank()) GlassChip(item.type)
                Spacer(Modifier.height(4.dp))
                if (item.sourceName.isNotBlank()) Text(item.sourceName, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(0.50f))
                Spacer(Modifier.height(16.dp))
                Button(onClick = {}, modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                    Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Watch Now")
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Add to Watchlist")
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

// â”€â”€ Skeleton â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

@Composable
private fun HomeSkeletonSection() {
    val alpha by rememberInfiniteTransition(label = "shimmer").animateFloat(
        initialValue  = 0.3f, targetValue = 0.7f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label         = "alpha"
    )
    Column {
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f).background(Color.White.copy(alpha)))
        Spacer(Modifier.height(16.dp))
        repeat(2) {
            Row(modifier = Modifier.padding(horizontal = 16.dp)) {
                repeat(3) {
                    Box(modifier = Modifier.size(110.dp, 165.dp).clip(RoundedCornerShape(10.dp)).background(Color.White.copy(alpha)))
                    Spacer(Modifier.width(10.dp))
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun HomeErrorSection(msg: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Couldn''t load content", style = MaterialTheme.typography.titleMedium,
                    color = Color.White, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                Text(msg, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.55f))
                Spacer(Modifier.height(16.dp))
                Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                    Text("Retry")
                }
            }
        }
    }
}

@Composable
private fun HomeEmptySection() {
    Box(Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("No content yet", style = MaterialTheme.typography.titleMedium, color = Color.White.copy(0.60f))
            Spacer(Modifier.height(8.dp))
            Text("Install extensions to load content", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.40f))
        }
    }
}
