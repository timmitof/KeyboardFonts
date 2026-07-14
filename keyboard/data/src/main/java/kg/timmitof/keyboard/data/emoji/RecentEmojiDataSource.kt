package kg.timmitof.keyboard.data.emoji

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

private val Context.emojiPreferences by preferencesDataStore(name = "emoji_preferences")

/**
 * Хранилище недавно использованных эмодзи поверх DataStore Preferences.
 *
 * Список хранится одной строкой с несимвольным разделителем —
 * свежие эмодзи в начале, размер ограничен [MAX_RECENT].
 */
@Singleton
class RecentEmojiDataSource @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    suspend fun getRecent(): List<String> =
        context.emojiPreferences.data.first()[RECENT_KEY].toEmojiList()

    suspend fun add(emoji: String): List<String> {
        var updated: List<String> = emptyList()
        context.emojiPreferences.edit { prefs ->
            val current = prefs[RECENT_KEY].toEmojiList()
            updated = (listOf(emoji) + (current - emoji)).take(MAX_RECENT)
            prefs[RECENT_KEY] = updated.joinToString(SEPARATOR)
        }
        return updated
    }

    private fun String?.toEmojiList(): List<String> =
        this?.split(SEPARATOR)?.filter { it.isNotEmpty() }.orEmpty()

    companion object {
        private val RECENT_KEY = stringPreferencesKey("recent_emojis")
        private const val SEPARATOR = ""
        private const val MAX_RECENT = 40
    }
}