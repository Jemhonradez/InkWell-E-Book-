package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.StoryEntity
import com.example.data.UserPreferences
import com.example.data.formatCompactCount
import com.example.ui.components.BookCover
import com.example.ui.components.COVER_PRESETS
import com.example.viewmodel.InkwellViewModel
import com.example.viewmodel.WriteUiState

@Composable
fun WriteScreen(
    uiState: WriteUiState,
    preferences: UserPreferences,
    onCreateStory: (
        title: String,
        synopsis: String,
        genre: String,
        tagsCsv: String,
        coverKey: String,
        status: String,
        publishImmediately: Boolean
    ) -> Unit,
    onUpdateStory: (StoryEntity) -> Unit,
    onDeleteStory: (Long) -> Unit,
    onImportCover: (Uri, (String?) -> Unit) -> Unit,
    onOpenStoryManage: (Long) -> Unit,
    onWriteNewChapter: (Long) -> Unit,
    onOpenProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showCreateSheet by remember { mutableStateOf(false) }
    var editingStory by remember { mutableStateOf<StoryEntity?>(null) }
    var pendingDeleteStory by remember { mutableStateOf<StoryEntity?>(null) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Author Studio",
                        style = MaterialTheme.typography.headlineLarge
                    )
                    Text(
                        text = "Writing as ${preferences.displayName} (${preferences.handle})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable(onClick = onOpenProfile)
                            .padding(vertical = 2.dp)
                    )
                }

                Button(
                    onClick = { showCreateSheet = true },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("create_story_button")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.action_create_story))
                }
            }
        }

        // Studio Stats Overview
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StudioMetric("Stories", uiState.myStories.size.toString())
                    StudioMetric("Chapters", uiState.totalChapters.toString())
                    StudioMetric("Reads", formatCompactCount(uiState.totalReads))
                    StudioMetric("Votes", formatCompactCount(uiState.totalVotes))
                }
            }
        }

        if (uiState.myStories.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Create,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = stringResource(R.string.empty_write_title),
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = stringResource(R.string.empty_write_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = { showCreateSheet = true },
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("empty_create_story_button")
                        ) {
                            Text("Start a Story")
                        }
                    }
                }
            }
        } else {
            items(
                items = uiState.myStories,
                key = { "my_story_${it.story.id}" }
            ) { item ->
                val story = item.story
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenStoryManage(story.id) }
                        .testTag("my_story_card_${story.id}"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BookCover(
                                coverKey = story.coverKey,
                                title = story.title,
                                authorName = story.authorName,
                                modifier = Modifier
                                    .width(72.dp)
                                    .aspectRatio(0.7f)
                            )

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Surface(
                                        color = if (story.isDraft) {
                                            MaterialTheme.colorScheme.secondaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.primaryContainer
                                        },
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Text(
                                            text = if (story.isDraft) "DRAFT" else "PUBLISHED",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (story.isDraft) {
                                                MaterialTheme.colorScheme.onSecondaryContainer
                                            } else {
                                                MaterialTheme.colorScheme.onPrimaryContainer
                                            },
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                    Text(
                                        text = story.genre,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Text(
                                    text = story.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${item.publishedChapterCount} published • ${item.chapterCount} total parts",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(
                                onClick = { editingStory = story },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(Icons.Filled.Edit, contentDescription = "Edit story details")
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { onWriteNewChapter(story.id) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("add_chapter_btn_${story.id}")
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Write Chapter")
                            }

                            OutlinedButton(
                                onClick = { onOpenStoryManage(story.id) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                            ) {
                                Text("Chapters")
                            }

                            IconButton(
                                onClick = { pendingDeleteStory = story },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.DeleteOutline,
                                    contentDescription = "Delete story",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateSheet || editingStory != null) {
        StoryMetadataSheet(
            initialStory = editingStory,
            onDismiss = {
                showCreateSheet = false
                editingStory = null
            },
            onImportCover = onImportCover,
            onSave = { title, synopsis, genre, tagsCsv, coverKey, status ->
                val existing = editingStory
                if (existing == null) {
                    onCreateStory(title, synopsis, genre, tagsCsv, coverKey, status, false)
                } else {
                    onUpdateStory(
                        existing.copy(
                            title = title,
                            synopsis = synopsis,
                            genre = genre,
                            tagsCsv = tagsCsv,
                            coverKey = coverKey,
                            status = status
                        )
                    )
                }
                showCreateSheet = false
                editingStory = null
            }
        )
    }

    pendingDeleteStory?.let { target ->
        AlertDialog(
            onDismissRequest = { pendingDeleteStory = null },
            title = { Text("Delete \"${target.title}\"?") },
            text = { Text("This removes the story and all of its chapters from your device.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteStory(target.id)
                        pendingDeleteStory = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteStory = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun StudioMetric(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StoryMetadataSheet(
    initialStory: StoryEntity?,
    onDismiss: () -> Unit,
    onImportCover: (Uri, (String?) -> Unit) -> Unit,
    onSave: (
        title: String,
        synopsis: String,
        genre: String,
        tagsCsv: String,
        coverKey: String,
        status: String
    ) -> Unit
) {
    var title by remember { mutableStateOf(initialStory?.title ?: "") }
    var synopsis by remember { mutableStateOf(initialStory?.synopsis ?: "") }
    var genre by remember { mutableStateOf(initialStory?.genre ?: "Fantasy") }
    var tags by remember { mutableStateOf(initialStory?.tagsCsv ?: "") }
    var coverKey by remember { mutableStateOf(initialStory?.coverKey ?: "gradient:amber") }
    var status by remember { mutableStateOf(initialStory?.status ?: "Ongoing") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                onImportCover(uri) { storedPath ->
                    if (storedPath != null) {
                        coverKey = storedPath
                    }
                }
            }
        }
    )

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = if (initialStory == null) "Create a New Story" else "Edit Story Details",
                style = MaterialTheme.typography.headlineSmall
            )

            // Cover Preview + Picker
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                BookCover(
                    coverKey = coverKey,
                    title = title.ifBlank { "Untitled Story" },
                    authorName = "You",
                    modifier = Modifier
                        .width(84.dp)
                        .aspectRatio(0.7f)
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Book Cover",
                        style = MaterialTheme.typography.titleSmall
                    )
                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(44.dp)
                    ) {
                        Icon(Icons.Filled.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pick from Photos")
                    }
                }
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(COVER_PRESETS) { preset ->
                    val selected = coverKey == preset.key
                    Box(
                        modifier = Modifier
                            .width(56.dp)
                            .aspectRatio(0.7f)
                            .clip(RoundedCornerShape(10.dp))
                            .then(
                                if (selected) {
                                    Modifier.border(
                                        2.5.dp,
                                        MaterialTheme.colorScheme.primary,
                                        RoundedCornerShape(10.dp)
                                    )
                                } else Modifier
                            )
                            .clickable { coverKey = preset.key }
                    ) {
                        BookCover(
                            coverKey = preset.key,
                            title = preset.label,
                            authorName = "",
                            modifier = Modifier.fillMaxSize(),
                            showTypographyOverlayOnGradient = false
                        )
                    }
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Story Title *") },
                placeholder = { Text("e.g., Echoes of the Glass Sea") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("story_title_input")
            )

            OutlinedTextField(
                value = synopsis,
                onValueChange = { synopsis = it },
                label = { Text("Blurb / Synopsis") },
                placeholder = { Text("Hook readers with your premise...") },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("story_synopsis_input")
            )

            Text(text = "Genre", style = MaterialTheme.typography.titleSmall)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(InkwellViewModel.WRITE_GENRES) { g ->
                    FilterChip(
                        selected = genre == g,
                        onClick = { genre = g },
                        label = { Text(g) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            OutlinedTextField(
                value = tags,
                onValueChange = { tags = it },
                label = { Text("Tags (comma separated)") },
                placeholder = { Text("magic, slowburn, adventure") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Ongoing", "Completed").forEach { option ->
                    FilterChip(
                        selected = status == option,
                        onClick = { status = option },
                        label = { Text(option) }
                    )
                }
            }

            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(title, synopsis, genre, tags, coverKey, status)
                    }
                },
                enabled = title.isNotBlank(),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("save_story_button")
            ) {
                Text(if (initialStory == null) "Create Story & Start Writing" else "Save Changes")
            }
        }
    }
}
