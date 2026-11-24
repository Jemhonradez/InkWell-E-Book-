package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.ChapterEntity
import com.example.data.StoryWithStats
import com.example.data.formatCompactCount
import com.example.ui.components.BookCover
import com.example.ui.theme.WarmGold
import com.example.viewmodel.InkwellViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StoryDetailScreen(
    storyWithStats: StoryWithStats?,
    chapters: List<ChapterEntity>,
    onBack: () -> Unit,
    onStartReading: (Int) -> Unit,
    onSetLibraryShelf: (Boolean, String) -> Unit,
    onAddChapter: () -> Unit,
    onEditChapter: (Long) -> Unit,
    onDeleteChapter: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    if (storyWithStats == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val story = storyWithStats.story
    val publishedChapters = chapters.filter { it.isPublished }
    var shelfMenuExpanded by remember { mutableStateOf(false) }
    var synopsisExpanded by remember { mutableStateOf(false) }

    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding()),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Atmospheric Header Backdrop + Floating Book Cover
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(310.dp)
                ) {
                    BookCover(
                        coverKey = story.coverKey,
                        title = story.title,
                        authorName = story.authorName,
                        modifier = Modifier
                            .fillMaxSize()
                            .blur(26.dp)
                            .alpha(0.55f),
                        elevation = 0.dp,
                        showTypographyOverlayOnGradient = false
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.Black.copy(alpha = 0.4f),
                                        MaterialTheme.colorScheme.background.copy(alpha = 0.65f),
                                        MaterialTheme.colorScheme.background
                                    )
                                )
                            )
                    )

                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .statusBarsPadding()
                            .padding(12.dp)
                            .size(48.dp)
                            .background(Color.Black.copy(alpha = 0.35f), CircleShape)
                            .testTag("story_detail_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    BookCover(
                        coverKey = story.coverKey,
                        title = story.title,
                        authorName = story.authorName,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp)
                            .width(142.dp)
                            .aspectRatio(0.7f),
                        elevation = 14.dp
                    )
                }
            }

            // Title, Author, Metrics
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = story.genre,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = story.status,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Text(
                        text = story.title,
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Text(
                        text = "${story.authorName} (${story.authorHandle})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Stats Bar
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 1.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            DetailStat(
                                icon = Icons.Filled.Visibility,
                                value = formatCompactCount(story.readsCount),
                                label = "Reads",
                                iconTint = MaterialTheme.colorScheme.onSurface
                            )
                            DetailStat(
                                icon = Icons.Filled.Star,
                                value = formatCompactCount(story.votesCount),
                                label = "Votes",
                                iconTint = WarmGold
                            )
                            DetailStat(
                                icon = Icons.AutoMirrored.Filled.MenuBook,
                                value = publishedChapters.size.toString(),
                                label = "Parts",
                                iconTint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Primary Reading & Library Buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val resumeIdx = story.lastReadChapterIndex
                            .coerceIn(0, (publishedChapters.size - 1).coerceAtLeast(0))
                        val hasProgress = story.lastReadChapterIndex > 0 || story.lastReadScrollProgress > 0.05f

                        Button(
                            onClick = { onStartReading(resumeIdx) },
                            enabled = publishedChapters.isNotEmpty(),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1.3f)
                                .height(50.dp)
                                .testTag("story_start_reading_btn")
                        ) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when {
                                    publishedChapters.isEmpty() -> "No Parts Yet"
                                    hasProgress -> "Continue Part ${resumeIdx + 1}"
                                    else -> "Start Reading"
                                }
                            )
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedButton(
                                onClick = { shelfMenuExpanded = true },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("story_library_btn")
                            ) {
                                Icon(
                                    imageVector = if (story.inLibrary) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                    contentDescription = null,
                                    tint = if (story.inLibrary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (story.inLibrary) "Saved" else "Save",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            DropdownMenu(
                                expanded = shelfMenuExpanded,
                                onDismissRequest = { shelfMenuExpanded = false }
                            ) {
                                InkwellViewModel.SHELF_NAMES.forEach { shelf ->
                                    DropdownMenuItem(
                                        text = {
                                            val active = story.inLibrary && story.readingListName == shelf
                                            Text(if (active) "✓ $shelf" else "Add to $shelf")
                                        },
                                        onClick = {
                                            shelfMenuExpanded = false
                                            onSetLibraryShelf(true, shelf)
                                        }
                                    )
                                }
                                if (story.inLibrary) {
                                    HorizontalDivider()
                                    DropdownMenuItem(
                                        text = { Text("Remove from Library") },
                                        onClick = {
                                            shelfMenuExpanded = false
                                            onSetLibraryShelf(false, story.readingListName)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Synopsis & Tags
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "About This Story",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = story.synopsis.ifBlank { "No synopsis added yet." },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = if (synopsisExpanded) Int.MAX_VALUE else 4,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.clickable { synopsisExpanded = !synopsisExpanded }
                    )

                    if (storyWithStats.tags.isNotEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            storyWithStats.tags.forEach { tag ->
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Text(
                                        text = "#$tag",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Table of Contents Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Table of Contents (${chapters.size})",
                        style = MaterialTheme.typography.titleLarge
                    )
                    if (story.isUserCreated) {
                        OutlinedButton(
                            onClick = onAddChapter,
                            shape = RoundedCornerShape(50),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier
                                .height(40.dp)
                                .testTag("detail_add_chapter_btn")
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Part")
                        }
                    }
                }
            }

            if (chapters.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "No chapters written yet",
                                style = MaterialTheme.typography.titleMedium
                            )
                            if (story.isUserCreated) {
                                Button(onClick = onAddChapter, shape = RoundedCornerShape(50)) {
                                    Text("Write Chapter 1")
                                }
                            }
                        }
                    }
                }
            } else {
                itemsIndexed(
                    items = chapters,
                    key = { _, ch -> "toc_${ch.id}" }
                ) { index, chapter ->
                    val publishedIndex = publishedChapters.indexOfFirst { it.id == chapter.id }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 4.dp)
                            .clickable(enabled = chapter.isPublished || story.isUserCreated) {
                                if (chapter.isPublished && publishedIndex >= 0) {
                                    onStartReading(publishedIndex)
                                } else if (story.isUserCreated) {
                                    onEditChapter(chapter.id)
                                }
                            }
                            .testTag("toc_chapter_${chapter.id}"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = chapter.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (!chapter.isPublished) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.secondaryContainer,
                                            shape = RoundedCornerShape(50)
                                        ) {
                                            Text(
                                                text = "DRAFT",
                                                style = MaterialTheme.typography.labelSmall,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "${chapter.wordCount} words • ${formatCompactCount(chapter.votesCount)} votes",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (story.isUserCreated) {
                                Row {
                                    IconButton(
                                        onClick = { onEditChapter(chapter.id) },
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Icon(Icons.Filled.Edit, contentDescription = "Edit chapter")
                                    }
                                    IconButton(
                                        onClick = { onDeleteChapter(chapter.id) },
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.DeleteOutline,
                                            contentDescription = "Delete chapter",
                                            tint = MaterialTheme.colorScheme.error
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
}

@Composable
private fun DetailStat(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    iconTint: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
