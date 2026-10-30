package kg.timmitof.keyboard.data.emoji

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first

/** Одна строка с несимвольным разделителем: свежие в начале, не больше [MAX_RECENT]. */
class RecentEmojiDataSource(
    private val context: Context
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
        private const val SEPARATOR = "\u0001"
        private const val MAX_RECENT = 40
    }
}