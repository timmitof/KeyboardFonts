package kg.timmitof.keyboard.data.emoji

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first

class PreferredEmojiVariantDataSource(
    private val emojiPreferences: EmojiPreferences
) {

    suspend fun getAll(): Map<String, String> =
        emojiPreferences.data.first()[VARIANTS_KEY].toVariantMap()

    /** Выбор базового эмодзи сбрасывает предпочтение. */
    suspend fun setPreferred(base: String, variant: String): Map<String, String> {
        var updated: Map<String, String> = emptyMap()
        emojiPreferences.edit { prefs ->
            val current = prefs[VARIANTS_KEY].toVariantMap()
            updated = if (variant == base) current - base else current + (base to variant)
            prefs[VARIANTS_KEY] = updated.entries
                .joinToString(ENTRY_SEPARATOR) { (key, value) -> "$key$PAIR_SEPARATOR$value" }
        }
        return updated
    }

    private fun String?.toVariantMap(): Map<String, String> =
        this?.split(ENTRY_SEPARATOR)
            ?.mapNotNull { entry ->
                val parts = entry.split(PAIR_SEPARATOR)
                if (parts.size == 2) parts[0] to parts[1] else null
            }
            ?.toMap()
            .orEmpty()

    companion object {
        private val VARIANTS_KEY = stringPreferencesKey("preferred_emoji_variants")

        private const val ENTRY_SEPARATOR = "\u0001"

        private const val PAIR_SEPARATOR = "\u0002"
    }
}
