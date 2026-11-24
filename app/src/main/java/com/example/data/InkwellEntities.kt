package com.example.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "stories")
data class StoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val authorName: String,
    val authorHandle: String,
    val synopsis: String,
    val genre: String,
    val tagsCsv: String,
    val coverKey: String,
    val status: String = "Ongoing",
    val isMature: Boolean = false,
    val readsCount: Int = 0,
    val votesCount: Int = 0,
    val inLibrary: Boolean = false,
    val readingListName: String = "Currently Reading",
    val lastReadChapterIndex: Int = 0,
    val lastReadScrollProgress: Float = 0f,
    val isUserCreated: Boolean = false,
    val isDraft: Boolean = false,
    val isFeatured: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "chapters",
    foreignKeys = [
        ForeignKey(
            entity = StoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["storyId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("storyId")]
)
data class ChapterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val storyId: Long,
    val chapterNumber: Int,
    val title: String,
    val body: String,
    val authorNote: String = "",
    val isPublished: Boolean = true,
    val votesCount: Int = 0,
    val isVoted: Boolean = false,
    val wordCount: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "comments",
    foreignKeys = [
        ForeignKey(
            entity = ChapterEntity::class,
            parentColumns = ["id"],
            childColumns = ["chapterId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("chapterId"), Index("storyId")]
)
data class CommentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val chapterId: Long,
    val storyId: Long,
    val authorName: String,
    val authorInitials: String,
    val text: String,
    val paragraphIndex: Int = -1,
    val likesCount: Int = 0,
    val isLiked: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val email: String,
    val displayName: String,
    val handle: String,
    val bio: String = "Late-night reader & daydreaming storyteller.",
    val passwordSalt: String,
    val passwordHash: String,
    val createdAt: Long = System.currentTimeMillis()
)

data class StoryWithStats(
    @Embedded val story: StoryEntity,
    val chapterCount: Int = 0,
    val publishedChapterCount: Int = 0
) {
    val tags: List<String>
        get() = story.tagsCsv
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
}

fun formatCompactCount(count: Int): String {
    return when {
        count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000f)
        count >= 1_000 -> String.format("%.1fK", count / 1_000f)
        else -> count.toString()
    }
}

fun countWords(text: String): Int {
    val trimmed = text.trim()
    if (trimmed.isEmpty()) return 0
    return trimmed.split(Regex("\\s+")).size
}
