package com.openstream.app.ui.screen.extensions

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.openstream.app.data.local.ExtensionEntity
import com.openstream.app.data.local.ExtensionStatus
import com.openstream.app.ui.components.GlassBackground
import com.openstream.app.ui.components.GlassCard
import com.openstream.app.ui.components.GlassChip
import com.openstream.app.ui.components.GlassTopBar
import kotlinx.coroutines.delay

enum class ExtensionFilter { ALL, INSTALLED, NOT_INSTALLED }

@Composable
fun ExtensionsScreen(
    navController : NavController,
    viewModel     : ExtensionsViewModel = hiltViewModel()
) {
    val uiState          by viewModel.uiState.collectAsState()
    var searchQuery      by remember { mutableStateOf("") }
    var activeFilter     by remember { mutableStateOf(ExtensionFilter.ALL) }
    var selectedLanguage by remember { mutableStateOf<String?>(null) }
    var detailExtension  by remember { mutableStateOf<ExtensionEntity?>(null) }
    var showBanner       by remember { mutableStateOf(false) }
    var bannerCount      by remember { mutableIntStateOf(0) }

    // Show banner when updatedCount > 0, auto-dismiss after 4s
    LaunchedEffect(uiState) {
        val s = uiState
        if (s is ExtensionsUiState.Success && s.updatedCount > 0) {
            bannerCount = s.updatedCount
            showBanner  = true
            delay(4000)
            showBanner  = false
        }
    }

    val availableLanguages = remember(uiState) {
        (uiState as? ExtensionsUiState.Success)
            ?.extensions?.map { it.language }?.distinct()?.sorted() ?: emptyList()
    }

    GlassBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Column {
                    GlassTopBar(
                        title = {
                            Text("Extensions", style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold, color = Color.White)
                        },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                            }
                        },
                        actions = {
                            IconButton(onClick = { viewModel.refresh() }) {
                                Icon(Icons.Default.Refresh, null, tint = Color.White)
                            }
                        }
                    )
                    // Update banner
                    AnimatedVisibility(visible = showBanner, exit = fadeOut()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF4CAF50).copy(alpha = 0.85f))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                "$bannerCount extension${if (bannerCount > 1) "s" else ""} updated âœ“",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White, fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                SearchBarSection(query = searchQuery, onChange = { searchQuery = it })
                FilterRow(
                    activeFilter = activeFilter, onFilterChange = { activeFilter = it },
                    selectedLanguage = selectedLanguage, onLanguageChange = { selectedLanguage = it },
                    languages = availableLanguages
                )
                when (val state = uiState) {
                    is ExtensionsUiState.Loading -> LoadingSection()
                    is ExtensionsUiState.Error   -> ErrorSection(state.message) { viewModel.refresh() }
                    is ExtensionsUiState.Success -> {
                        val filtered = state.extensions.filter { ext ->
                            val mSearch = searchQuery.isBlank() ||
                                ext.name.contains(searchQuery, true) ||
                                ext.repoName.contains(searchQuery, true)
                            val mFilter = when (activeFilter) {
                                ExtensionFilter.ALL           -> true
                                ExtensionFilter.INSTALLED     -> ext.isInstalled
                                ExtensionFilter.NOT_INSTALLED -> !ext.isInstalled
                            }
                            val mLang = selectedLanguage == null || ext.language == selectedLanguage
                            mSearch && mFilter && mLang
                        }
                        if (filtered.isEmpty()) EmptySection()
                        else GroupedList(
                            grouped = filtered.groupBy { it.language },
                            onTap   = { detailExtension = it }
                        )
                    }
                }
            }
        }
    }

    detailExtension?.let { ext ->
        ExtensionDetailSheet(extension = ext, onDismiss = { detailExtension = null })
    }
}

// â”€â”€ Search â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

@Composable
private fun SearchBarSection(query: String, onChange: (String) -> Unit) {
    GlassCard(
        modifier       = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        cornerRadius   = 14.dp,
        contentPadding = 0.dp
    ) {
        Row(
            modifier          = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Search, null, tint = Color.White.copy(0.50f), modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            BasicTextField(
                value = query, onValueChange = onChange,
                modifier    = Modifier.weight(1f),
                textStyle   = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                cursorBrush = SolidColor(Color.White),
                singleLine  = true,
                decorationBox = { inner ->
                    if (query.isEmpty())
                        Text("Search extensions...", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(0.35f))
                    inner()
                }
            )
        }
    }
}

// â”€â”€ Filters â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

@Composable
private fun FilterRow(
    activeFilter    : ExtensionFilter,
    onFilterChange  : (ExtensionFilter) -> Unit,
    selectedLanguage: String?,
    onLanguageChange: (String?) -> Unit,
    languages       : List<String>
) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        ExtensionFilter.values().forEach { f ->
            FilterChip(
                selected = activeFilter == f,
                onClick  = { onFilterChange(f) },
                label    = { Text(f.name.replace("_"," ").lowercase().replaceFirstChar { it.uppercase() }) },
                colors   = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(0.38f),
                    selectedLabelColor     = Color.White,
                    containerColor         = Color.White.copy(0.08f),
                    labelColor             = Color.White.copy(0.65f)
                )
            )
        }
        if (languages.isNotEmpty()) {
            Box(Modifier.height(20.dp).width(1.dp).background(Color.White.copy(0.20f)))
            FilterChip(
                selected = selectedLanguage == null,
                onClick  = { onLanguageChange(null) },
                label    = { Text("All Lang") },
                colors   = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.secondary.copy(0.38f),
                    selectedLabelColor     = Color.White,
                    containerColor         = Color.White.copy(0.08f),
                    labelColor             = Color.White.copy(0.65f)
                )
            )
            languages.forEach { lang ->
                val sel = selectedLanguage == lang
                FilterChip(
                    selected = sel,
                    onClick  = { onLanguageChange(if (sel) null else lang) },
                    label    = { Text(lang.uppercase()) },
                    colors   = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondary.copy(0.38f),
                        selectedLabelColor     = Color.White,
                        containerColor         = Color.White.copy(0.08f),
                        labelColor             = Color.White.copy(0.65f)
                    )
                )
            }
        }
    }
}

// â”€â”€ Grouped list â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

@Composable
private fun GroupedList(grouped: Map<String, List<ExtensionEntity>>, onTap: (ExtensionEntity) -> Unit) {
    val expanded = remember { mutableStateMapOf<String, Boolean>() }
    LazyColumn(
        modifier            = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        grouped.forEach { (lang, exts) ->
            val isExpanded = expanded[lang] ?: true
            item(key = "hdr_$lang") {
                LangHeader(lang, exts.size, isExpanded) { expanded[lang] = !isExpanded }
            }
            if (isExpanded) {
                items(exts, key = { it.id }) { ext ->
                    ExtensionCard(extension = ext, onTap = onTap)
                }
            }
        }
    }
}

@Composable
private fun LangHeader(language: String, count: Int, isExpanded: Boolean, onClick: () -> Unit) {
    Row(
        modifier          = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(28.dp).background(
                Brush.linearGradient(listOf(
                    MaterialTheme.colorScheme.primary.copy(0.60f),
                    MaterialTheme.colorScheme.secondary.copy(0.40f)
                )), CircleShape
            ),
            contentAlignment = Alignment.Center
        ) {
            Text(language.uppercase().take(2), style = MaterialTheme.typography.labelSmall,
                color = Color.White, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(10.dp))
        Text(language.uppercase(), style = MaterialTheme.typography.titleSmall,
            color = Color.White, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        GlassChip("$count")
        Spacer(Modifier.width(8.dp))
        Icon(
            if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            null, tint = Color.White.copy(0.65f), modifier = Modifier.size(20.dp)
        )
    }
}

// â”€â”€ Extension card â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

@Composable
private fun ExtensionCard(extension: ExtensionEntity, onTap: (ExtensionEntity) -> Unit) {
    GlassCard(
        modifier       = Modifier.fillMaxWidth().clickable { onTap(extension) },
        cornerRadius   = 16.dp,
        contentPadding = 12.dp
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ExtensionIconView(extension.iconUrl, extension.name, 44)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(extension.name, style = MaterialTheme.typography.titleSmall,
                    color = Color.White, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Text(extension.repoName, style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(0.50f), maxLines = 1)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    GlassChip(extension.language)
                    GlassChip(extension.type)
                }
            }
            Spacer(Modifier.width(8.dp))
            // Status column
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                when (extension.status) {
                    ExtensionStatus.UPDATING -> {
                        CircularProgressIndicator(
                            modifier  = Modifier.size(16.dp),
                            color     = Color.White,
                            strokeWidth = 2.dp
                        )
                    }
                    ExtensionStatus.UPDATE_AVAILABLE -> {
                        TextButton(
                            onClick        = {},
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            colors         = ButtonDefaults.textButtonColors(contentColor = Color(0xFFFFC107))
                        ) { Text("Update", style = MaterialTheme.typography.labelSmall) }
                    }
                    else -> {
                        Box(
                            modifier = Modifier.size(8.dp).background(
                                if (extension.isInstalled) Color(0xFF4CAF50) else Color(0xFF616161),
                                CircleShape
                            )
                        )
                        Spacer(Modifier.height(4.dp))
                        TextButton(
                            onClick        = {},
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            colors         = ButtonDefaults.textButtonColors(
                                contentColor = if (extension.isInstalled) Color(0xFFEF5350) else Color.White.copy(0.72f)
                            )
                        ) {
                            Text(
                                if (extension.isInstalled) "Remove" else "Install",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    }
}

// â”€â”€ Icon â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

@Composable
fun ExtensionIconView(iconUrl: String, name: String, size: Int) {
    if (iconUrl.isNotBlank()) {
        AsyncImage(
            model = iconUrl, contentDescription = name,
            modifier = Modifier.size(size.dp).clip(RoundedCornerShape(10.dp)),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = Modifier.size(size.dp).background(
                Brush.linearGradient(listOf(Color(0xFF7C6FFF).copy(0.60f), Color(0xFF03DAC6).copy(0.40f))),
                RoundedCornerShape(10.dp)
            ),
            contentAlignment = Alignment.Center
        ) {
            Text(name.take(1).uppercase(), style = MaterialTheme.typography.titleMedium,
                color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
    }
}

// â”€â”€ States â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

@Composable
private fun LoadingSection() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(16.dp))
            Text("Fetching extensions...", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(0.60f))
        }
    }
}

@Composable
private fun ErrorSection(message: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Could not load extensions", style = MaterialTheme.typography.titleMedium,
                    color = Color.White, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Text(message, style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(0.55f), textAlign = TextAlign.Center)
                Spacer(Modifier.height(16.dp))
                Button(onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                    Text("Retry")
                }
            }
        }
    }
}

@Composable
private fun EmptySection() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("No extensions found", style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(0.50f))
    }
}
