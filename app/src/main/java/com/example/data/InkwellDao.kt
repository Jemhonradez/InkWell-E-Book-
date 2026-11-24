package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface InkwellDao {

    @Query(
        """
        SELECT s.*,
            (SELECT COUNT(*) FROM chapters c WHERE c.storyId = s.id) AS chapterCount,
            (SELECT COUNT(*) FROM chapters c WHERE c.storyId = s.id AND c.isPublished = 1) AS publishedChapterCount
        FROM stories s
        WHERE s.isDraft = 0
        ORDER BY s.isFeatured DESC, s.readsCount DESC, s.updatedAt DESC
        """
    )
    fun observePublishedStories(): Flow<List<StoryWithStats>>

    @Query(
        """
        SELECT s.*,
            (SELECT COUNT(*) FROM chapters c WHERE c.storyId = s.id) AS chapterCount,
            (SELECT COUNT(*) FROM chapters c WHERE c.storyId = s.id AND c.isPublished = 1) AS publishedChapterCount
        FROM stories s
        WHERE s.inLibrary = 1
        ORDER BY s.updatedAt DESC
        """
    )
    fun observeLibraryStories(): Flow<List<StoryWithStats>>

    @Query(
        """
        SELECT s.*,
            (SELECT COUNT(*) FROM chapters c WHERE c.storyId = s.id) AS chapterCount,
            (SELECT COUNT(*) FROM chapters c WHERE c.storyId = s.id AND c.isPublished = 1) AS publishedChapterCount
        FROM stories s
        WHERE s.isUserCreated = 1
        ORDER BY s.updatedAt DESC
        """
    )
    fun observeMyStories(): Flow<List<StoryWithStats>>

    @Query(
        """
        SELECT s.*,
            (SELECT COUNT(*) FROM chapters c WHERE c.storyId = s.id) AS chapterCount,
            (SELECT COUNT(*) FROM chapters c WHERE c.storyId = s.id AND c.isPublished = 1) AS publishedChapterCount
        FROM stories s
        WHERE s.id = :storyId
        """
    )
    fun observeStoryWithStats(storyId: Long): Flow<StoryWithStats?>

    @Query("SELECT * FROM stories WHERE id = :storyId")
    suspend fun getStoryById(storyId: Long): StoryEntity?

    @Query("SELECT * FROM chapters WHERE storyId = :storyId ORDER BY chapterNumber ASC")
    fun observeChaptersForStory(storyId: Long): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters WHERE storyId = :storyId AND isPublished = 1 ORDER BY chapterNumber ASC")
    fun observePublishedChaptersForStory(storyId: Long): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters WHERE id = :chapterId")
    suspend fun getChapterById(chapterId: Long): ChapterEntity?

    @Query("SELECT * FROM comments WHERE chapterId = :chapterId ORDER BY createdAt DESC")
    fun observeCommentsForChapter(chapterId: Long): Flow<List<CommentEntity>>

    @Query("SELECT COUNT(*) FROM stories")
    suspend fun getStoryCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: StoryEntity): Long

    @Update
    suspend fun updateStory(story: StoryEntity)

    @Query("DELETE FROM stories WHERE id = :storyId")
    suspend fun deleteStoryById(storyId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: ChapterEntity): Long

    @Update
    suspend fun updateChapter(chapter: ChapterEntity)

    @Query("DELETE FROM chapters WHERE id = :chapterId")
    suspend fun deleteChapterById(chapterId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity): Long

    @Query("UPDATE stories SET inLibrary = :inLibrary, readingListName = :listName, updatedAt = :updatedAt WHERE id = :storyId")
    suspend fun updateLibraryStatus(storyId: Long, inLibrary: Boolean, listName: String, updatedAt: Long)

    @Query("UPDATE stories SET lastReadChapterIndex = :chapterIndex, lastReadScrollProgress = :progress, updatedAt = :updatedAt WHERE id = :storyId")
    suspend fun updateReadingProgress(storyId: Long, chapterIndex: Int, progress: Float, updatedAt: Long)

    @Query("UPDATE stories SET readsCount = readsCount + 1 WHERE id = :storyId")
    suspend fun incrementStoryReads(storyId: Long)

    @Transaction
    suspend fun toggleChapterVote(chapterId: Long, storyId: Long) {
        val chapter = getChapterById(chapterId) ?: return
        val story = getStoryById(storyId) ?: return
        val nowVoted = !chapter.isVoted
        val delta = if (nowVoted) 1 else -1
        updateChapter(
            chapter.copy(
                isVoted = nowVoted,
                votesCount = (chapter.votesCount + delta).coerceAtLeast(0)
            )
        )
        updateStory(
            story.copy(
                votesCount = (story.votesCount + delta).coerceAtLeast(0)
            )
        )
    }

    @Query("UPDATE comments SET isLiked = NOT isLiked, likesCount = CASE WHEN isLiked = 1 THEN MAX(0, likesCount - 1) ELSE likesCount + 1 END WHERE id = :commentId")
    suspend fun toggleCommentLike(commentId: Long)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity): Long

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: Long): UserEntity?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun observeUserById(userId: Long): Flow<UserEntity?>

    @Query("SELECT COUNT(*) FROM users")
    fun observeUserCount(): Flow<Int>

    @Query("UPDATE users SET displayName = :displayName, handle = :handle, bio = :bio WHERE id = :userId")
    suspend fun updateUserProfile(userId: Long, displayName: String, handle: String, bio: String)
}
