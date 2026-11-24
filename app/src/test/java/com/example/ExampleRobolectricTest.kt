package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.InkwellDatabase
import com.example.data.InkwellRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var database: InkwellDatabase
    private lateinit var repository: InkwellRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, InkwellDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = InkwellRepository(database.inkwellDao())
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Inkwell", appName)
    }

    @Test
    fun `seed catalog and user story publish flow works`() = runTest {
        repository.ensureSeeded()
        val initialStories = repository.publishedStories.first()
        assertEquals(6, initialStories.size)

        val newStoryId = repository.createUserStory(
            title = "Echoes of Dusk",
            authorName = "Alex Rivera",
            authorHandle = "@alexwrites",
            synopsis = "A wandering bard discovers a song that stops time.",
            genre = "Fantasy",
            tagsCsv = "fantasy,music",
            coverKey = "gradient:amber",
            status = "Ongoing",
            publishImmediately = false
        )

        val chapterId = repository.saveChapter(
            storyId = newStoryId,
            chapterId = null,
            chapterNumber = 1,
            title = "Chapter 1: First Chord",
            body = "First paragraph.\n\nSecond paragraph.",
            authorNote = "Enjoy!",
            publish = true
        )

        val publishedAfter = repository.publishedStories.first()
        assertTrue(publishedAfter.any { it.story.id == newStoryId && it.publishedChapterCount == 1 })

        repository.toggleChapterVote(chapterId, newStoryId)
        val chapters = repository.observeChapters(newStoryId, includeDrafts = false).first()
        assertEquals(1, chapters.first().votesCount)
        assertTrue(chapters.first().isVoted)

        repository.postComment(chapterId, newStoryId, "Alex Rivera", "Loved this line!", paragraphIndex = 1)
        val comments = repository.observeComments(chapterId).first()
        assertEquals(1, comments.size)
        assertEquals(1, comments.first().paragraphIndex)
    }
}
