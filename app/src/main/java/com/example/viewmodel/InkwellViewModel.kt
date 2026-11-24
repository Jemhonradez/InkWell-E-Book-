package com.example.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppThemeMode
import com.example.data.ChapterEntity
import com.example.data.CommentEntity
import com.example.data.CoverStorageHelper
import com.example.data.InkwellDatabase
import com.example.data.InkwellRepository
import com.example.data.ReaderFontStyle
import com.example.data.ReaderPreferencesRepository
import com.example.data.ReaderThemeMode
import com.example.data.StoryEntity
import com.example.data.StoryWithStats
import com.example.data.UserEntity
import com.example.data.UserPreferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AuthUiState(
    val isCheckingSession: Boolean = true,
    val loggedInUser: UserEntity? = null,
    val hasRegisteredUsers: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null
)

enum class BrowseStatusFilter(val label: String) {
    ALL("All"),
    COMPLETED("Completed"),
    ONGOING("Ongoing")
}

enum class BrowseSortOption(val label: String) {
    MOST_READ("Most Read"),
    TOP_VOTED("Top Voted"),
    RECENT("Latest")
}

data class HomeUiState(
    val isLoading: Boolean = true,
    val selectedGenre: String = "All",
    val featuredStories: List<StoryWithStats> = emptyList(),
    val continueReadingStory: StoryWithStats? = null,
    val trendingStories: List<StoryWithStats> = emptyList(),
    val feedStories: List<StoryWithStats> = emptyList()
)

data class BrowseUiState(
    val query: String = "",
    val selectedGenre: String? = null,
    val statusFilter: BrowseStatusFilter = BrowseStatusFilter.ALL,
    val sortOption: BrowseSortOption = BrowseSortOption.MOST_READ,
    val results: List<StoryWithStats> = emptyList(),
    val totalPublishedCount: Int = 0
)

data class LibraryUiState(
    val selectedShelf: String = "Currently Reading",
    val allSavedStories: List<StoryWithStats> = emptyList(),
    val shelfStories: List<StoryWithStats> = emptyList(),
    val shelfCounts: Map<String, Int> = emptyMap()
)

data class WriteUiState(
    val myStories: List<StoryWithStats> = emptyList(),
    val totalReads: Int = 0,
    val totalVotes: Int = 0,
    val totalChapters: Int = 0
)

class InkwellViewModel(application: Application) : AndroidViewModel(application) {

    private val database = InkwellDatabase.getInstance(application)
    private val repository = InkwellRepository(database.inkwellDao())
    private val prefsRepo = ReaderPreferencesRepository(application)

    private val selectedHomeGenre = MutableStateFlow("All")
    private val browseQuery = MutableStateFlow("")
    private val browseGenreFilter = MutableStateFlow<String?>(null)
    private val browseStatus = MutableStateFlow(BrowseStatusFilter.ALL)
    private val browseSort = MutableStateFlow(BrowseSortOption.MOST_READ)
    private val selectedLibraryShelf = MutableStateFlow("Currently Reading")
    private val authSubmitting = MutableStateFlow(false)
    private val authError = MutableStateFlow<String?>(null)

    val userPreferences: StateFlow<UserPreferences> = prefsRepo.preferences
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    private val activeUserFlow: Flow<UserEntity?> = prefsRepo.preferences
        .map { it.loggedInUserId }
        .flatMapLatest { userId ->
            if (userId == null) flowOf(null) else repository.observeUser(userId)
        }

    val authUiState: StateFlow<AuthUiState> = combine(
        activeUserFlow,
        repository.registeredUserCount,
        authSubmitting,
        authError
    ) { user, userCount, submitting, error ->
        AuthUiState(
            isCheckingSession = false,
            loggedInUser = user,
            hasRegisteredUsers = userCount > 0,
            isSubmitting = submitting,
            errorMessage = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AuthUiState(isCheckingSession = true)
    )

    val homeUiState: StateFlow<HomeUiState> = combine(
        repository.publishedStories,
        repository.libraryStories,
        selectedHomeGenre
    ) { published, library, genre ->
        val featured = published.filter { it.story.isFeatured }.ifEmpty { published.take(3) }
        val continueStory = library.firstOrNull {
            it.story.readingListName == "Currently Reading" && it.publishedChapterCount > 0
        } ?: library.firstOrNull { it.publishedChapterCount > 0 }
        val trending = published.sortedByDescending { it.story.readsCount }.take(8)
        val feed = if (genre == "All") {
            published
        } else {
            published.filter { it.story.genre.equals(genre, ignoreCase = true) }
        }
        HomeUiState(
            isLoading = false,
            selectedGenre = genre,
            featuredStories = featured,
            continueReadingStory = continueStory,
            trendingStories = trending,
            feedStories = feed
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    val browseUiState: StateFlow<BrowseUiState> = combine(
        repository.publishedStories,
        browseQuery,
        browseGenreFilter,
        browseStatus,
        browseSort
    ) { published, query, genre, status, sort ->
        val q = query.trim().lowercase()
        val filtered = published.filter { item ->
            val matchesQuery = if (q.isEmpty()) {
                true
            } else {
                item.story.title.lowercase().contains(q) ||
                    item.story.authorName.lowercase().contains(q) ||
                    item.story.genre.lowercase().contains(q) ||
                    item.story.tagsCsv.lowercase().contains(q) ||
                    item.story.synopsis.lowercase().contains(q)
            }
            val matchesGenre = genre == null || item.story.genre.equals(genre, ignoreCase = true)
            val matchesStatus = when (status) {
                BrowseStatusFilter.ALL -> true
                BrowseStatusFilter.COMPLETED -> item.story.status.equals("Completed", ignoreCase = true)
                BrowseStatusFilter.ONGOING -> item.story.status.equals("Ongoing", ignoreCase = true)
            }
            matchesQuery && matchesGenre && matchesStatus
        }
        val sorted = when (sort) {
            BrowseSortOption.MOST_READ -> filtered.sortedByDescending { it.story.readsCount }
            BrowseSortOption.TOP_VOTED -> filtered.sortedByDescending { it.story.votesCount }
            BrowseSortOption.RECENT -> filtered.sortedByDescending { it.story.updatedAt }
        }
        BrowseUiState(
            query = query,
            selectedGenre = genre,
            statusFilter = status,
            sortOption = sort,
            results = sorted,
            totalPublishedCount = published.size
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BrowseUiState()
    )

    val libraryUiState: StateFlow<LibraryUiState> = combine(
        repository.libraryStories,
        selectedLibraryShelf
    ) { saved, shelf ->
        val counts = SHELF_NAMES.associateWith { name ->
            saved.count { it.story.readingListName == name }
        }
        LibraryUiState(
            selectedShelf = shelf,
            allSavedStories = saved,
            shelfStories = saved.filter { it.story.readingListName == shelf },
            shelfCounts = counts
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LibraryUiState()
    )

    val writeUiState: StateFlow<WriteUiState> = repository.myStories
        .map { stories ->
            WriteUiState(
                myStories = stories,
                totalReads = stories.sumOf { it.story.readsCount },
                totalVotes = stories.sumOf { it.story.votesCount },
                totalChapters = stories.sumOf { it.chapterCount }
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = WriteUiState()
        )

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
        }
    }

    fun selectHomeGenre(genre: String) {
        selectedHomeGenre.value = genre
    }

    fun updateBrowseQuery(query: String) {
        browseQuery.value = query
    }

    fun selectBrowseGenre(genre: String?) {
        browseGenreFilter.value = genre
    }

    fun selectBrowseStatus(status: BrowseStatusFilter) {
        browseStatus.value = status
    }

    fun selectBrowseSort(sort: BrowseSortOption) {
        browseSort.value = sort
    }

    fun selectLibraryShelf(shelf: String) {
        selectedLibraryShelf.value = shelf
    }

    fun observeStory(storyId: Long): Flow<StoryWithStats?> = repository.observeStory(storyId)

    fun observeChapters(storyId: Long, includeDrafts: Boolean): Flow<List<ChapterEntity>> =
        repository.observeChapters(storyId, includeDrafts)

    fun observeComments(chapterId: Long): Flow<List<CommentEntity>> =
        repository.observeComments(chapterId)

    fun setLibraryStatus(storyId: Long, inLibrary: Boolean, listName: String) {
        viewModelScope.launch {
            repository.setLibraryStatus(storyId, inLibrary, listName)
        }
    }

    fun updateReadingProgress(storyId: Long, chapterIndex: Int, progress: Float) {
        viewModelScope.launch {
            repository.updateReadingProgress(storyId, chapterIndex, progress)
        }
    }

    fun recordStoryRead(storyId: Long) {
        viewModelScope.launch {
            repository.recordStoryRead(storyId)
        }
    }

    fun toggleChapterVote(chapterId: Long, storyId: Long) {
        viewModelScope.launch {
            repository.toggleChapterVote(chapterId, storyId)
        }
    }

    fun postComment(chapterId: Long, storyId: Long, text: String, paragraphIndex: Int) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val authorName = userPreferences.value.displayName
            repository.postComment(
                chapterId = chapterId,
                storyId = storyId,
                authorName = authorName,
                text = text,
                paragraphIndex = paragraphIndex
            )
        }
    }

    fun toggleCommentLike(commentId: Long) {
        viewModelScope.launch {
            repository.toggleCommentLike(commentId)
        }
    }

    fun createStory(
        title: String,
        synopsis: String,
        genre: String,
        tagsCsv: String,
        coverKey: String,
        status: String,
        publishImmediately: Boolean,
        onCreated: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val prefs = userPreferences.value
            val id = repository.createUserStory(
                title = title,
                authorName = prefs.displayName,
                authorHandle = prefs.handle,
                synopsis = synopsis,
                genre = genre,
                tagsCsv = tagsCsv,
                coverKey = coverKey,
                status = status,
                publishImmediately = publishImmediately
            )
            onCreated(id)
        }
    }

    fun updateStory(story: StoryEntity) {
        viewModelScope.launch {
            repository.updateUserStory(story)
        }
    }

    fun deleteStory(storyId: Long, onDeleted: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteStory(storyId)
            onDeleted()
        }
    }

    fun saveChapter(
        storyId: Long,
        chapterId: Long?,
        chapterNumber: Int,
        title: String,
        body: String,
        authorNote: String,
        publish: Boolean,
        onSaved: (Long) -> Unit = {}
    ) {
        viewModelScope.launch {
            val id = repository.saveChapter(
                storyId = storyId,
                chapterId = chapterId,
                chapterNumber = chapterNumber,
                title = title,
                body = body,
                authorNote = authorNote,
                publish = publish
            )
            onSaved(id)
        }
    }

    fun deleteChapter(chapterId: Long) {
        viewModelScope.launch {
            repository.deleteChapter(chapterId)
        }
    }

    fun importCoverFromUri(uri: Uri, onImported: (String?) -> Unit) {
        viewModelScope.launch {
            val stored = CoverStorageHelper.copyCoverToAppStorage(getApplication(), uri)
            onImported(stored)
        }
    }

    fun setReaderTheme(mode: ReaderThemeMode) {
        viewModelScope.launch { prefsRepo.setReaderTheme(mode) }
    }

    fun setReaderFont(font: ReaderFontStyle) {
        viewModelScope.launch { prefsRepo.setReaderFont(font) }
    }

    fun setFontSize(sizeSp: Float) {
        viewModelScope.launch { prefsRepo.setFontSize(sizeSp) }
    }

    fun setLineHeight(multiplier: Float) {
        viewModelScope.launch { prefsRepo.setLineHeight(multiplier) }
    }

    fun setAppThemeMode(mode: AppThemeMode) {
        viewModelScope.launch { prefsRepo.setAppThemeMode(mode) }
    }

    fun updateProfile(displayName: String, handle: String, bio: String) {
        viewModelScope.launch {
            prefsRepo.updateProfile(displayName, handle, bio)
            authUiState.value.loggedInUser?.id?.let { userId ->
                repository.updateUserProfile(userId, displayName, handle, bio)
            }
        }
    }

    fun clearAuthError() {
        authError.value = null
    }

    fun signIn(email: String, password: String) {
        if (authSubmitting.value) return
        viewModelScope.launch {
            authSubmitting.value = true
            authError.value = null
            val result = repository.authenticateUser(email, password)
            result.fold(
                onSuccess = { user ->
                    prefsRepo.setLoggedInUser(user)
                },
                onFailure = { err ->
                    authError.value = err.message ?: "Unable to sign in."
                }
            )
            authSubmitting.value = false
        }
    }

    fun register(displayName: String, handle: String, email: String, password: String) {
        if (authSubmitting.value) return
        viewModelScope.launch {
            authSubmitting.value = true
            authError.value = null
            val result = repository.registerUser(displayName, handle, email, password)
            result.fold(
                onSuccess = { user ->
                    prefsRepo.setLoggedInUser(user)
                },
                onFailure = { err ->
                    authError.value = err.message ?: "Unable to create account."
                }
            )
            authSubmitting.value = false
        }
    }

    fun logout() {
        viewModelScope.launch {
            authError.value = null
            prefsRepo.clearSession()
        }
    }

    companion object {
        val GENRES = listOf("All", "Fantasy", "Sci-Fi", "Romance", "Mystery", "Drama")
        val WRITE_GENRES = listOf("Fantasy", "Sci-Fi", "Romance", "Mystery", "Drama", "Poetry", "Thriller")
        val SHELF_NAMES = listOf("Currently Reading", "Favorites", "Read Later")
    }
}
