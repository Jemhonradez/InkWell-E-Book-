package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TheaterComedy
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.StoryListCard
import com.example.viewmodel.BrowseSortOption
import com.example.viewmodel.BrowseStatusFilter
import com.example.viewmodel.BrowseUiState

private data class GenreCardSpec(
    val name: String,
    val subtitle: String,
    val colors: List<Color>,
    val icon: ImageVector
)

private val GENRE_CARDS = listOf(
    GenreCardSpec("Fantasy", "Magic & realms", listOf(Color(0xFF28204D), Color(0xFF563A7D)), Icons.Filled.AutoStories),
    GenreCardSpec("Sci-Fi", "Neon & futures", listOf(Color(0xFF0E2B3E), Color(0xFF1F637A)), Icons.Filled.RocketLaunch),
    GenreCardSpec("Romance", "Slow burns & coffee", listOf(Color(0xFF75263C), Color(0xFFC44D58)), Icons.Filled.Favorite),
    GenreCardSpec("Mystery", "Clues & whispers", listOf(Color(0xFF2B241D), Color(0xFF695036)), Icons.Filled.Psychology),
    GenreCardSpec("Drama", "Heart & life", listOf(Color(0xFF193C32), Color(0xFF2F725D)), Icons.Filled.TheaterComedy),
    GenreCardSpec("Thriller", "Pulse-pounding", listOf(Color(0xFF3E1812), Color(0xFF9C351E)), Icons.Filled.Explore)
)

@Composable
fun BrowseScreen(
    uiState: BrowseUiState,
    onQueryChange: (String) -> Unit,
    onSelectGenre: (String?) -> Unit,
    onSelectStatus: (BrowseStatusFilter) -> Unit,
    onSelectSort: (BrowseSortOption) -> Unit,
    onOpenStory: (Long) -> Unit,
    onToggleBookmark: (Long, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Browse Stories",
                    style = MaterialTheme.typography.headlineLarge
                )

                OutlinedTextField(
                    value = uiState.query,
                    onValueChange = onQueryChange,
                    placeholder = { Text("Search titles, authors, #tags...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null
                        )
                    },
                    trailingIcon = {
                        if (uiState.query.isNotEmpty()) {
                            IconButton(
                                onClick = { onQueryChange("") },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Clear,
                                    contentDescription = "Clear search"
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("browse_search_input")
                )
            }
        }

        // Status & Sort Filter Row
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (uiState.selectedGenre != null) {
                    item {
                        FilterChip(
                            selected = true,
                            onClick = { onSelectGenre(null) },
                            label = { Text("${uiState.selectedGenre} ×") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
                items(BrowseStatusFilter.entries) { status ->
                    FilterChip(
                        selected = uiState.statusFilter == status,
                        onClick = { onSelectStatus(status) },
                        label = { Text(status.label) }
                    )
                }
                items(BrowseSortOption.entries) { sort ->
                    FilterChip(
                        selected = uiState.sortOption == sort,
                        onClick = { onSelectSort(sort) },
                        label = { Text(sort.label) }
                    )
                }
            }
        }

        // Show Genre Explore Tiles when no search query & no genre filter active
        if (uiState.query.isBlank() && uiState.selectedGenre == null) {
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Explore by Genre",
                        style = MaterialTheme.typography.titleLarge
                    )

                    GENRE_CARDS.chunked(2).forEach { rowGenres ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowGenres.forEach { card ->
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(84.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .clickable { onSelectGenre(card.name) }
                                        .testTag("browse_genre_${card.name.lowercase()}"),
                                    shape = RoundedCornerShape(18.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Brush.linearGradient(card.colors))
                                            .padding(14.dp)
                                    ) {
                                        Icon(
                                            imageVector = card.icon,
                                            contentDescription = null,
                                            tint = Color.White.copy(alpha = 0.22f),
                                            modifier = Modifier
                                                .size(46.dp)
                                                .align(Alignment.BottomEnd)
                                        )
                                        Column(modifier = Modifier.align(Alignment.TopStart)) {
                                            Text(
                                                text = card.name,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = card.subtitle,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.White.copy(alpha = 0.8f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Results Section Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (uiState.selectedGenre != null) {
                        "${uiState.selectedGenre} Stories"
                    } else if (uiState.query.isNotBlank()) {
                        "Search Results"
                    } else {
                        "All Stories"
                    },
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "${uiState.results.size} found",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (uiState.results.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(36.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(44.dp)
                    )
                    Text(
                        text = "No matching stories",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Try a different keyword, tag, or genre filter.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(
                items = uiState.results,
                key = { "browse_${it.story.id}" }
            ) { item ->
                StoryListCard(
                    item = item,
                    onClick = { onOpenStory(item.story.id) },
                    onBookmarkToggle = {
                        onToggleBookmark(item.story.id, !item.story.inLibrary)
                    },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
        }
    }
}
