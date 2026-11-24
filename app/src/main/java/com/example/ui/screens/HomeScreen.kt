package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.UserPreferences
import com.example.ui.components.ContinueReadingCard
import com.example.ui.components.FeaturedHeroCard
import com.example.ui.components.StoryListCard
import com.example.ui.components.TrendingBookTile
import com.example.viewmodel.HomeUiState
import com.example.viewmodel.InkwellViewModel

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    preferences: UserPreferences,
    onSelectGenre: (String) -> Unit,
    onOpenStory: (Long) -> Unit,
    onStartReading: (Long, Int) -> Unit,
    onToggleBookmark: (Long, Boolean) -> Unit,
    onNavigateToBrowse: () -> Unit,
    onOpenProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Editorial Top Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Inkwell",
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Stories made for late nights",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onNavigateToBrowse,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("home_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search stories"
                        )
                    }

                    val initials = preferences.displayName
                        .split(" ")
                        .filter { it.isNotBlank() }
                        .take(2)
                        .joinToString("") { it.first().uppercase() }
                        .ifEmpty { "IR" }

                    Surface(
                        onClick = onOpenProfile,
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("home_profile_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = initials,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Continue Reading Banner
        uiState.continueReadingStory?.let { continueItem ->
            item {
                ContinueReadingCard(
                    item = continueItem,
                    onContinueClick = {
                        onStartReading(
                            continueItem.story.id,
                            continueItem.story.lastReadChapterIndex
                        )
                    },
                    onOpenDetail = { onOpenStory(continueItem.story.id) },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
        }

        // Editor's Spotlight Hero Carousel
        if (uiState.featuredStories.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Editor's Picks",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(
                            items = uiState.featuredStories,
                            key = { "feat_${it.story.id}" }
                        ) { featured ->
                            FeaturedHeroCard(
                                item = featured,
                                onClick = { onOpenStory(featured.story.id) },
                                onReadClick = {
                                    onStartReading(
                                        featured.story.id,
                                        featured.story.lastReadChapterIndex
                                    )
                                },
                                modifier = Modifier.width(326.dp)
                            )
                        }
                    }
                }
            }
        }

        // Trending Now Shelf
        if (uiState.trendingStories.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Trending on Inkwell",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "Explore all",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(onClick = onNavigateToBrowse)
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(
                            items = uiState.trendingStories,
                            key = { "trend_${it.story.id}" }
                        ) { trending ->
                            TrendingBookTile(
                                item = trending,
                                onClick = { onOpenStory(trending.story.id) }
                            )
                        }
                    }
                }
            }
        }

        // Genre Chips & Community Feed Header
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "For Your Reading Mood",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(InkwellViewModel.GENRES) { genre ->
                        val selected = uiState.selectedGenre == genre
                        FilterChip(
                            selected = selected,
                            onClick = { onSelectGenre(genre) },
                            label = { Text(genre) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("home_genre_${genre.lowercase()}")
                        )
                    }
                }
            }
        }

        // Story Feed
        if (uiState.feedStories.isEmpty() && !uiState.isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No stories in ${uiState.selectedGenre} yet. Why not write the first one?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(
                items = uiState.feedStories,
                key = { "feed_${it.story.id}" }
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
