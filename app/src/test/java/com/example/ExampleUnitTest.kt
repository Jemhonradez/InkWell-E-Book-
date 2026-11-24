package com.example

import com.example.data.PasswordHasher
import com.example.data.StoryEntity
import com.example.data.StoryWithStats
import com.example.data.countWords
import com.example.data.formatCompactCount
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun compactCountFormatting_isAccurate() {
        assertEquals("42", formatCompactCount(42))
        assertEquals("1.5K", formatCompactCount(1500))
        assertEquals("2.4M", formatCompactCount(2_400_000))
    }

    @Test
    fun wordCount_handlesWhitespaceAndBlank() {
        assertEquals(0, countWords("   \n  "))
        assertEquals(5, countWords("The twin moons rose tonight."))
    }

    @Test
    fun storyWithStats_parsesTagsCorrectly() {
        val story = StoryEntity(
            id = 1L,
            title = "Test",
            authorName = "Alex",
            authorHandle = "@alex",
            synopsis = "Synopsis",
            genre = "Fantasy",
            tagsCsv = "magic, slowburn , , portal",
            coverKey = "gradient:amber"
        )
        val stats = StoryWithStats(story = story, chapterCount = 2, publishedChapterCount = 1)
        assertEquals(listOf("magic", "slowburn", "portal"), stats.tags)
    }

    @Test
    fun passwordHasher_verifiesMatchingAndRejectsWrongPassword() = runTest {
        val hashed = PasswordHasher.hashPassword("secret123")
        assertTrue(PasswordHasher.verifyPassword("secret123", hashed.saltHex, hashed.hashHex))
        assertFalse(PasswordHasher.verifyPassword("wrongpass", hashed.saltHex, hashed.hashHex))
    }
}
