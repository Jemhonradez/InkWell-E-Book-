package com.example.data

import kotlinx.coroutines.flow.Flow

class InkwellRepository(private val dao: InkwellDao) {

    val publishedStories: Flow<List<StoryWithStats>> = dao.observePublishedStories()
    val libraryStories: Flow<List<StoryWithStats>> = dao.observeLibraryStories()
    val myStories: Flow<List<StoryWithStats>> = dao.observeMyStories()

    suspend fun ensureSeeded() {
        SeedCatalog.populateIfEmpty(dao)
    }

    fun observeStory(storyId: Long): Flow<StoryWithStats?> = dao.observeStoryWithStats(storyId)

    fun observeChapters(storyId: Long, includeDrafts: Boolean): Flow<List<ChapterEntity>> {
        return if (includeDrafts) {
            dao.observeChaptersForStory(storyId)
        } else {
            dao.observePublishedChaptersForStory(storyId)
        }
    }

    fun observeComments(chapterId: Long): Flow<List<CommentEntity>> =
        dao.observeCommentsForChapter(chapterId)

    suspend fun createUserStory(
        title: String,
        authorName: String,
        authorHandle: String,
        synopsis: String,
        genre: String,
        tagsCsv: String,
        coverKey: String,
        status: String,
        publishImmediately: Boolean
    ): Long {
        val storyId = dao.insertStory(
            StoryEntity(
                title = title.trim(),
                authorName = authorName.trim().ifEmpty { "Inkwell Writer" },
                authorHandle = authorHandle.trim().ifEmpty { "@writer" },
                synopsis = synopsis.trim(),
                genre = genre,
                tagsCsv = tagsCsv,
                coverKey = coverKey,
                status = status,
                isUserCreated = true,
                isDraft = !publishImmediately,
                inLibrary = true,
                readingListName = "Currently Reading",
                updatedAt = System.currentTimeMillis()
            )
        )
        return storyId
    }

    suspend fun updateUserStory(story: StoryEntity) {
        dao.updateStory(story.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteStory(storyId: Long) {
        dao.deleteStoryById(storyId)
    }

    suspend fun saveChapter(
        storyId: Long,
        chapterId: Long?,
        chapterNumber: Int,
        title: String,
        body: String,
        authorNote: String,
        publish: Boolean
    ): Long {
        val now = System.currentTimeMillis()
        val words = countWords(body)
        val savedId = if (chapterId != null && chapterId > 0) {
            val existing = dao.getChapterById(chapterId)
            if (existing != null) {
                dao.updateChapter(
                    existing.copy(
                        chapterNumber = chapterNumber,
                        title = title.trim(),
                        body = body.trim(),
                        authorNote = authorNote.trim(),
                        isPublished = publish,
                        wordCount = words,
                        updatedAt = now
                    )
                )
                existing.id
            } else {
                dao.insertChapter(
                    ChapterEntity(
                        storyId = storyId,
                        chapterNumber = chapterNumber,
                        title = title.trim(),
                        body = body.trim(),
                        authorNote = authorNote.trim(),
                        isPublished = publish,
                        wordCount = words,
                        updatedAt = now
                    )
                )
            }
        } else {
            dao.insertChapter(
                ChapterEntity(
                    storyId = storyId,
                    chapterNumber = chapterNumber,
                    title = title.trim(),
                    body = body.trim(),
                    authorNote = authorNote.trim(),
                    isPublished = publish,
                    wordCount = words,
                    updatedAt = now
                )
            )
        }

        val story = dao.getStoryById(storyId)
        if (story != null) {
            dao.updateStory(
                story.copy(
                    isDraft = if (publish) false else story.isDraft,
                    updatedAt = now
                )
            )
        }
        return savedId
    }

    suspend fun deleteChapter(chapterId: Long) {
        dao.deleteChapterById(chapterId)
    }

    suspend fun setLibraryStatus(storyId: Long, inLibrary: Boolean, listName: String) {
        dao.updateLibraryStatus(storyId, inLibrary, listName, System.currentTimeMillis())
    }

    suspend fun updateReadingProgress(storyId: Long, chapterIndex: Int, progress: Float) {
        dao.updateReadingProgress(storyId, chapterIndex, progress, System.currentTimeMillis())
    }

    suspend fun recordStoryRead(storyId: Long) {
        dao.incrementStoryReads(storyId)
    }

    suspend fun toggleChapterVote(chapterId: Long, storyId: Long) {
        dao.toggleChapterVote(chapterId, storyId)
    }

    suspend fun postComment(
        chapterId: Long,
        storyId: Long,
        authorName: String,
        text: String,
        paragraphIndex: Int
    ) {
        val cleanName = authorName.trim().ifEmpty { "Reader" }
        val initials = cleanName
            .split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
            .ifEmpty { "R" }

        dao.insertComment(
            CommentEntity(
                chapterId = chapterId,
                storyId = storyId,
                authorName = cleanName,
                authorInitials = initials,
                text = text.trim(),
                paragraphIndex = paragraphIndex,
                likesCount = 1,
                isLiked = true,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun toggleCommentLike(commentId: Long) {
        dao.toggleCommentLike(commentId)
    }

    val registeredUserCount: Flow<Int> = dao.observeUserCount()

    fun observeUser(userId: Long): Flow<UserEntity?> = dao.observeUserById(userId)

    suspend fun getUserById(userId: Long): UserEntity? = dao.getUserById(userId)

    suspend fun registerUser(
        displayName: String,
        handle: String,
        email: String,
        password: String
    ): Result<UserEntity> {
        val cleanEmail = email.trim().lowercase()
        val cleanName = displayName.trim()
        val cleanHandle = handle.trim().let {
            val base = it.ifEmpty { cleanName.replace(" ", "").lowercase() }
            if (base.startsWith("@")) base else "@$base"
        }

        if (cleanName.isEmpty()) {
            return Result.failure(IllegalArgumentException("Please enter your pen name."))
        }
        if (!cleanEmail.contains("@") || !cleanEmail.contains(".")) {
            return Result.failure(IllegalArgumentException("Please enter a valid email address."))
        }
        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("Password must be at least 6 characters."))
        }
        if (dao.getUserByEmail(cleanEmail) != null) {
            return Result.failure(IllegalStateException("An account with this email already exists."))
        }

        val hashed = PasswordHasher.hashPassword(password)
        val newUser = UserEntity(
            email = cleanEmail,
            displayName = cleanName,
            handle = cleanHandle,
            passwordSalt = hashed.saltHex,
            passwordHash = hashed.hashHex
        )
        val userId = dao.insertUser(newUser)
        return Result.success(newUser.copy(id = userId))
    }

    suspend fun authenticateUser(email: String, password: String): Result<UserEntity> {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isEmpty() || password.isEmpty()) {
            return Result.failure(IllegalArgumentException("Please enter both email and password."))
        }
        val user = dao.getUserByEmail(cleanEmail)
            ?: return Result.failure(IllegalArgumentException("Incorrect email or password."))

        val valid = PasswordHasher.verifyPassword(password, user.passwordSalt, user.passwordHash)
        return if (valid) {
            Result.success(user)
        } else {
            Result.failure(IllegalArgumentException("Incorrect email or password."))
        }
    }

    suspend fun updateUserProfile(
        userId: Long,
        displayName: String,
        handle: String,
        bio: String
    ) {
        val formattedHandle = handle.trim().let {
            if (it.isEmpty()) "@writer" else if (it.startsWith("@")) it else "@$it"
        }
        dao.updateUserProfile(
            userId = userId,
            displayName = displayName.trim().ifEmpty { "Inkwell Reader" },
            handle = formattedHandle,
            bio = bio.trim()
        )
    }
}
