package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChapterEntity
import com.example.data.StoryEntity
import com.example.data.countWords
import com.example.ui.theme.MerriweatherFontFamily
import com.example.ui.theme.PlayfairDisplayFontFamily
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterEditorScreen(
    story: StoryEntity?,
    existingChapter: ChapterEntity?,
    nextChapterNumber: Int,
    onBack: () -> Unit,
    onSave: (
        chapterId: Long?,
        chapterNumber: Int,
        title: String,
        body: String,
        authorNote: String,
        publish: Boolean
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val defaultNumber = existingChapter?.chapterNumber ?: nextChapterNumber
    var title by remember(existingChapter?.id) {
        mutableStateOf(existingChapter?.title ?: "Chapter $defaultNumber")
    }
    var body by remember(existingChapter?.id) {
        mutableStateOf(existingChapter?.body ?: "")
    }
    var authorNote by remember(existingChapter?.id) {
        mutableStateOf(existingChapter?.authorNote ?: "")
    }

    val wordCount = remember(body) { countWords(body) }
    val readMinutes = remember(wordCount) { max(1, (wordCount + 199) / 200) }
    val canSave = title.isNotBlank() && body.isNotBlank()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("editor_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                title = {
                    Column {
                        Text(
                            text = story?.title ?: "Chapter Editor",
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1
                        )
                        Text(
                            text = "$wordCount words • ~$readMinutes min read",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 3.dp,
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            if (canSave) {
                                onSave(existingChapter?.id, defaultNumber, title, body, authorNote, false)
                            }
                        },
                        enabled = canSave,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("editor_save_draft_btn")
                    ) {
                        Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Draft")
                    }

                    Button(
                        onClick = {
                            if (canSave) {
                                onSave(existingChapter?.id, defaultNumber, title, body, authorNote, true)
                            }
                        },
                        enabled = canSave,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1.2f)
                            .height(48.dp)
                            .testTag("editor_publish_btn")
                    ) {
                        Icon(Icons.Filled.Publish, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Publish")
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("Chapter title") },
                textStyle = TextStyle(
                    fontFamily = PlayfairDisplayFontFamily,
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.onBackground
                ),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("editor_title_input")
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            OutlinedTextField(
                value = body,
                onValueChange = { body = it },
                placeholder = {
                    Text(
                        text = "Begin your chapter here...\n\nSeparate paragraphs with blank lines so readers can leave inline paragraph comments.",
                        fontFamily = MerriweatherFontFamily,
                        fontSize = 16.sp
                    )
                },
                textStyle = TextStyle(
                    fontFamily = MerriweatherFontFamily,
                    fontSize = 17.sp,
                    lineHeight = 28.sp,
                    color = MaterialTheme.colorScheme.onBackground
                ),
                minLines = 12,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("editor_body_input")
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            OutlinedTextField(
                value = authorNote,
                onValueChange = { authorNote = it },
                label = { Text("Author's Note (optional)") },
                placeholder = { Text("Share a thought with your readers at the end of this part...") },
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("editor_note_input")
            )
        }
    }
}
