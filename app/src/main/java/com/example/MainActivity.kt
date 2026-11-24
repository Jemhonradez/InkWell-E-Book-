package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.outlined.LibraryBooks
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Create
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.AppThemeMode
import com.example.ui.components.ProfileSettingsSheet
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.BrowseScreen
import com.example.ui.screens.ChapterEditorScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.ReaderScreen
import com.example.ui.screens.StoryDetailScreen
import com.example.ui.screens.WriteScreen
import com.example.ui.theme.InkwellTheme
import com.example.viewmodel.InkwellViewModel

object InkwellRoutes {
    const val HOME = "home"
    const val BROWSE = "browse"
    const val LIBRARY = "library"
    const val WRITE = "write"
    const val STORY_DETAIL = "story/{storyId}"
    const val READER = "reader/{storyId}/{chapterIndex}"
    const val EDITOR = "editor/{storyId}?chapterId={chapterId}"

    fun storyDetail(storyId: Long) = "story/$storyId"
    fun reader(storyId: Long, chapterIndex: Int) = "reader/$storyId/$chapterIndex"
    fun editor(storyId: Long, chapterId: Long = -1L) = "editor/$storyId?chapterId=$chapterId"
}

private data class TopLevelDestination(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

private val TOP_LEVEL_DESTINATIONS = listOf(
    TopLevelDestination(
        route = InkwellRoutes.HOME,
        labelRes = R.string.nav_home,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        testTag = "nav_home"
    ),
    TopLevelDestination(
        route = InkwellRoutes.BROWSE,
        labelRes = R.string.nav_browse,
        selectedIcon = Icons.Filled.Explore,
        unselectedIcon = Icons.Outlined.Explore,
        testTag = "nav_browse"
    ),
    TopLevelDestination(
        route = InkwellRoutes.LIBRARY,
        labelRes = R.string.nav_library,
        selectedIcon = Icons.AutoMirrored.Filled.LibraryBooks,
        unselectedIcon = Icons.AutoMirrored.Outlined.LibraryBooks,
        testTag = "nav_library"
    ),
    TopLevelDestination(
        route = InkwellRoutes.WRITE,
        labelRes = R.string.nav_write,
        selectedIcon = Icons.Filled.Create,
        unselectedIcon = Icons.Outlined.Create,
        testTag = "nav_write"
    )
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: InkwellViewModel = viewModel()
            val preferences by viewModel.userPreferences.collectAsStateWithLifecycle()
            val authState by viewModel.authUiState.collectAsStateWithLifecycle()
            val darkTheme = when (preferences.appThemeMode) {
                AppThemeMode.SYSTEM -> isSystemInDarkTheme()
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
            }

            InkwellTheme(darkTheme = darkTheme) {
                when {
                    authState.isCheckingSession -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.material3.CircularProgressIndicator()
                        }
                    }
                    authState.loggedInUser == null -> {
                        AuthScreen(
                            authState = authState,
                            onSignIn = viewModel::signIn,
                            onRegister = viewModel::register,
                            onClearError = viewModel::clearAuthError
                        )
                    }
                    else -> {
                        val activeUser = authState.loggedInUser!!
                        key(activeUser.id) {
                            InkwellApp(
                                viewModel = viewModel,
                                userEmail = activeUser.email
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InkwellApp(
    viewModel: InkwellViewModel,
    userEmail: String
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val homeState by viewModel.homeUiState.collectAsStateWithLifecycle()
    val browseState by viewModel.browseUiState.collectAsStateWithLifecycle()
    val libraryState by viewModel.libraryUiState.collectAsStateWithLifecycle()
    val writeState by viewModel.writeUiState.collectAsStateWithLifecycle()
    val preferences by viewModel.userPreferences.collectAsStateWithLifecycle()

    var showProfileSheet by remember { mutableStateOf(false) }

    val showTopLevelNav = currentRoute in setOf(
        InkwellRoutes.HOME,
        InkwellRoutes.BROWSE,
        InkwellRoutes.LIBRARY,
        InkwellRoutes.WRITE
    )

    fun navigateTopLevel(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavigationRail = maxWidth >= 600.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                if (showTopLevelNav && !useNavigationRail) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        TOP_LEVEL_DESTINATIONS.forEach { dest ->
                            val selected = currentRoute == dest.route
                            val label = stringResource(dest.labelRes)
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateTopLevel(dest.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = label
                                    )
                                },
                                label = { Text(label) },
                                modifier = Modifier.testTag(dest.testTag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(modifier = Modifier.fillMaxSize()) {
                if (showTopLevelNav && useNavigationRail) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxHeight()
                            .statusBarsPadding()
                    ) {
                        TOP_LEVEL_DESTINATIONS.forEach { dest ->
                            val selected = currentRoute == dest.route
                            val label = stringResource(dest.labelRes)
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateTopLevel(dest.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = label
                                    )
                                },
                                label = { Text(label) },
                                modifier = Modifier.testTag(dest.testTag)
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .then(
                            if (showTopLevelNav) Modifier.padding(innerPadding)
                            else Modifier
                        )
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = InkwellRoutes.HOME
                    ) {
                        composable(InkwellRoutes.HOME) {
                            HomeScreen(
                                uiState = homeState,
                                preferences = preferences,
                                onSelectGenre = viewModel::selectHomeGenre,
                                onOpenStory = { storyId ->
                                    navController.navigate(InkwellRoutes.storyDetail(storyId))
                                },
                                onStartReading = { storyId, chapterIdx ->
                                    navController.navigate(InkwellRoutes.reader(storyId, chapterIdx))
                                },
                                onToggleBookmark = { storyId, inLib ->
                                    viewModel.setLibraryStatus(storyId, inLib, "Currently Reading")
                                },
                                onNavigateToBrowse = { navigateTopLevel(InkwellRoutes.BROWSE) },
                                onOpenProfile = { showProfileSheet = true }
                            )
                        }

                        composable(InkwellRoutes.BROWSE) {
                            BrowseScreen(
                                uiState = browseState,
                                onQueryChange = viewModel::updateBrowseQuery,
                                onSelectGenre = viewModel::selectBrowseGenre,
                                onSelectStatus = viewModel::selectBrowseStatus,
                                onSelectSort = viewModel::selectBrowseSort,
                                onOpenStory = { storyId ->
                                    navController.navigate(InkwellRoutes.storyDetail(storyId))
                                },
                                onToggleBookmark = { storyId, inLib ->
                                    viewModel.setLibraryStatus(storyId, inLib, "Currently Reading")
                                }
                            )
                        }

                        composable(InkwellRoutes.LIBRARY) {
                            LibraryScreen(
                                uiState = libraryState,
                                onSelectShelf = viewModel::selectLibraryShelf,
                                onOpenStory = { storyId ->
                                    navController.navigate(InkwellRoutes.storyDetail(storyId))
                                },
                                onStartReading = { storyId, chapterIdx ->
                                    navController.navigate(InkwellRoutes.reader(storyId, chapterIdx))
                                },
                                onMoveToShelf = { storyId, shelf ->
                                    viewModel.setLibraryStatus(storyId, true, shelf)
                                },
                                onRemoveFromLibrary = { storyId ->
                                    viewModel.setLibraryStatus(storyId, false, "Currently Reading")
                                },
                                onExploreClick = { navigateTopLevel(InkwellRoutes.BROWSE) }
                            )
                        }

                        composable(InkwellRoutes.WRITE) {
                            WriteScreen(
                                uiState = writeState,
                                preferences = preferences,
                                onCreateStory = { title, synopsis, genre, tags, coverKey, status, publish ->
                                    viewModel.createStory(
                                        title = title,
                                        synopsis = synopsis,
                                        genre = genre,
                                        tagsCsv = tags,
                                        coverKey = coverKey,
                                        status = status,
                                        publishImmediately = publish
                                    ) { newStoryId ->
                                        navController.navigate(InkwellRoutes.editor(newStoryId))
                                    }
                                },
                                onUpdateStory = viewModel::updateStory,
                                onDeleteStory = { storyId -> viewModel.deleteStory(storyId) },
                                onImportCover = viewModel::importCoverFromUri,
                                onOpenStoryManage = { storyId ->
                                    navController.navigate(InkwellRoutes.storyDetail(storyId))
                                },
                                onWriteNewChapter = { storyId ->
                                    navController.navigate(InkwellRoutes.editor(storyId))
                                },
                                onOpenProfile = { showProfileSheet = true }
                            )
                        }

                        composable(
                            route = InkwellRoutes.STORY_DETAIL,
                            arguments = listOf(navArgument("storyId") { type = NavType.LongType })
                        ) { entry ->
                            val storyId = entry.arguments?.getLong("storyId") ?: return@composable
                            val storyWithStats by viewModel.observeStory(storyId)
                                .collectAsStateWithLifecycle(initialValue = null)
                            val includeDrafts = storyWithStats?.story?.isUserCreated == true
                            val chapters by viewModel.observeChapters(storyId, includeDrafts)
                                .collectAsStateWithLifecycle(initialValue = emptyList())

                            StoryDetailScreen(
                                storyWithStats = storyWithStats,
                                chapters = chapters,
                                onBack = { navController.popBackStack() },
                                onStartReading = { chIdx ->
                                    navController.navigate(InkwellRoutes.reader(storyId, chIdx))
                                },
                                onSetLibraryShelf = { inLib, shelf ->
                                    viewModel.setLibraryStatus(storyId, inLib, shelf)
                                },
                                onAddChapter = {
                                    navController.navigate(InkwellRoutes.editor(storyId))
                                },
                                onEditChapter = { chId ->
                                    navController.navigate(InkwellRoutes.editor(storyId, chId))
                                },
                                onDeleteChapter = viewModel::deleteChapter
                            )
                        }

                        composable(
                            route = InkwellRoutes.READER,
                            arguments = listOf(
                                navArgument("storyId") { type = NavType.LongType },
                                navArgument("chapterIndex") { type = NavType.IntType }
                            )
                        ) { entry ->
                            val storyId = entry.arguments?.getLong("storyId") ?: return@composable
                            val initialIdx = entry.arguments?.getInt("chapterIndex") ?: 0
                            var activeChapterIdx by remember(storyId, initialIdx) {
                                mutableIntStateOf(initialIdx)
                            }

                            val storyWithStats by viewModel.observeStory(storyId)
                                .collectAsStateWithLifecycle(initialValue = null)
                            val chapters by viewModel.observeChapters(storyId, includeDrafts = false)
                                .collectAsStateWithLifecycle(initialValue = emptyList())
                            val activeChapter = chapters.getOrNull(
                                activeChapterIdx.coerceIn(0, (chapters.size - 1).coerceAtLeast(0))
                            )
                            val commentsFlow = remember(activeChapter?.id) {
                                activeChapter?.let { viewModel.observeComments(it.id) }
                            }
                            val comments by (commentsFlow?.collectAsStateWithLifecycle(initialValue = emptyList())
                                ?: remember { mutableStateOf(emptyList()) })

                            ReaderScreen(
                                story = storyWithStats?.story,
                                chapters = chapters,
                                currentChapterIndex = activeChapterIdx,
                                comments = comments,
                                preferences = preferences,
                                onBack = { navController.popBackStack() },
                                onSelectChapterIndex = { newIdx -> activeChapterIdx = newIdx },
                                onToggleVote = viewModel::toggleChapterVote,
                                onPostComment = viewModel::postComment,
                                onToggleCommentLike = viewModel::toggleCommentLike,
                                onProgressChanged = { idx, progress ->
                                    viewModel.updateReadingProgress(storyId, idx, progress)
                                },
                                onRecordRead = viewModel::recordStoryRead,
                                onSetReaderTheme = viewModel::setReaderTheme,
                                onSetReaderFont = viewModel::setReaderFont,
                                onSetFontSize = viewModel::setFontSize,
                                onSetLineHeight = viewModel::setLineHeight
                            )
                        }

                        composable(
                            route = InkwellRoutes.EDITOR,
                            arguments = listOf(
                                navArgument("storyId") { type = NavType.LongType },
                                navArgument("chapterId") {
                                    type = NavType.LongType
                                    defaultValue = -1L
                                }
                            )
                        ) { entry ->
                            val storyId = entry.arguments?.getLong("storyId") ?: return@composable
                            val chapterId = entry.arguments?.getLong("chapterId") ?: -1L

                            val storyWithStats by viewModel.observeStory(storyId)
                                .collectAsStateWithLifecycle(initialValue = null)
                            val chapters by viewModel.observeChapters(storyId, includeDrafts = true)
                                .collectAsStateWithLifecycle(initialValue = emptyList())
                            val existingChapter = chapters.firstOrNull { it.id == chapterId }
                            val nextNumber = (chapters.maxOfOrNull { it.chapterNumber } ?: 0) + 1

                            ChapterEditorScreen(
                                story = storyWithStats?.story,
                                existingChapter = existingChapter,
                                nextChapterNumber = nextNumber,
                                onBack = { navController.popBackStack() },
                                onSave = { chId, chNumber, title, body, note, publish ->
                                    viewModel.saveChapter(
                                        storyId = storyId,
                                        chapterId = chId,
                                        chapterNumber = chNumber,
                                        title = title,
                                        body = body,
                                        authorNote = note,
                                        publish = publish
                                    ) {
                                        navController.popBackStack()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showProfileSheet) {
        ProfileSettingsSheet(
            preferences = preferences,
            userEmail = userEmail,
            onDismiss = { showProfileSheet = false },
            onSaveProfile = viewModel::updateProfile,
            onSelectAppTheme = viewModel::setAppThemeMode,
            onLogout = viewModel::logout
        )
    }
}
