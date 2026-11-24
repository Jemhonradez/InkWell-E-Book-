package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.inkwellDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "inkwell_preferences"
)

enum class ReaderThemeMode(val label: String) {
    PAPER("Paper"),
    SEPIA("Sepia"),
    MIDNIGHT("Midnight"),
    OLED("OLED")
}

enum class ReaderFontStyle(val label: String) {
    SERIF("Merriweather"),
    SANS("Jakarta Sans")
}

enum class AppThemeMode(val label: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark")
}

data class UserPreferences(
    val loggedInUserId: Long? = null,
    val displayName: String = "Alex Rivera",
    val handle: String = "@alexwrites",
    val bio: String = "Late-night reader & daydreaming storyteller.",
    val readerTheme: ReaderThemeMode = ReaderThemeMode.PAPER,
    val readerFont: ReaderFontStyle = ReaderFontStyle.SERIF,
    val fontSizeSp: Float = 18f,
    val lineHeightMultiplier: Float = 1.65f,
    val appThemeMode: AppThemeMode = AppThemeMode.SYSTEM
)

class ReaderPreferencesRepository(private val context: Context) {

    private object Keys {
        val LOGGED_IN_USER_ID = longPreferencesKey("logged_in_user_id")
        val DISPLAY_NAME = stringPreferencesKey("display_name")
        val HANDLE = stringPreferencesKey("handle")
        val BIO = stringPreferencesKey("bio")
        val READER_THEME = stringPreferencesKey("reader_theme")
        val READER_FONT = stringPreferencesKey("reader_font")
        val FONT_SIZE_SP = floatPreferencesKey("font_size_sp")
        val LINE_HEIGHT = floatPreferencesKey("line_height_multiplier")
        val APP_THEME_MODE = stringPreferencesKey("app_theme_mode")
    }

    val preferences: Flow<UserPreferences> = context.inkwellDataStore.data.map { prefs ->
        UserPreferences(
            loggedInUserId = prefs[Keys.LOGGED_IN_USER_ID],
            displayName = prefs[Keys.DISPLAY_NAME] ?: "Alex Rivera",
            handle = prefs[Keys.HANDLE] ?: "@alexwrites",
            bio = prefs[Keys.BIO] ?: "Late-night reader & daydreaming storyteller.",
            readerTheme = prefs[Keys.READER_THEME]?.let { name ->
                ReaderThemeMode.entries.firstOrNull { it.name == name }
            } ?: ReaderThemeMode.PAPER,
            readerFont = prefs[Keys.READER_FONT]?.let { name ->
                ReaderFontStyle.entries.firstOrNull { it.name == name }
            } ?: ReaderFontStyle.SERIF,
            fontSizeSp = prefs[Keys.FONT_SIZE_SP] ?: 18f,
            lineHeightMultiplier = prefs[Keys.LINE_HEIGHT] ?: 1.65f,
            appThemeMode = prefs[Keys.APP_THEME_MODE]?.let { name ->
                AppThemeMode.entries.firstOrNull { it.name == name }
            } ?: AppThemeMode.SYSTEM
        )
    }

    suspend fun setLoggedInUser(user: UserEntity) {
        context.inkwellDataStore.edit { prefs ->
            prefs[Keys.LOGGED_IN_USER_ID] = user.id
            prefs[Keys.DISPLAY_NAME] = user.displayName
            prefs[Keys.HANDLE] = user.handle
            prefs[Keys.BIO] = user.bio
        }
    }

    suspend fun clearSession() {
        context.inkwellDataStore.edit { prefs ->
            prefs.remove(Keys.LOGGED_IN_USER_ID)
        }
    }

    suspend fun updateProfile(displayName: String, handle: String, bio: String) {
        val formattedHandle = handle.trim().let {
            if (it.isEmpty()) "@writer" else if (it.startsWith("@")) it else "@$it"
        }
        context.inkwellDataStore.edit { prefs ->
            prefs[Keys.DISPLAY_NAME] = displayName.trim().ifEmpty { "Inkwell Reader" }
            prefs[Keys.HANDLE] = formattedHandle
            prefs[Keys.BIO] = bio.trim()
        }
    }

    suspend fun setReaderTheme(mode: ReaderThemeMode) {
        context.inkwellDataStore.edit { it[Keys.READER_THEME] = mode.name }
    }

    suspend fun setReaderFont(font: ReaderFontStyle) {
        context.inkwellDataStore.edit { it[Keys.READER_FONT] = font.name }
    }

    suspend fun setFontSize(sizeSp: Float) {
        context.inkwellDataStore.edit { it[Keys.FONT_SIZE_SP] = sizeSp.coerceIn(14f, 24f) }
    }

    suspend fun setLineHeight(multiplier: Float) {
        context.inkwellDataStore.edit { it[Keys.LINE_HEIGHT] = multiplier.coerceIn(1.35f, 1.9f) }
    }

    suspend fun setAppThemeMode(mode: AppThemeMode) {
        context.inkwellDataStore.edit { it[Keys.APP_THEME_MODE] = mode.name }
    }
}
