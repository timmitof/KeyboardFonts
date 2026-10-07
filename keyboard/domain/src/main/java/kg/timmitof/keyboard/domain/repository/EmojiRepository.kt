package kg.timmitof.keyboard.domain.repository

import kg.timmitof.keyboard.domain.model.EmojiCategory

interface EmojiRepository {

    suspend fun getEmojiCategories(): List<EmojiCategory>

    suspend fun getEmojiVariants(): Map<String, List<String>>

    suspend fun getPreferredVariants(): Map<String, String>

    suspend fun setPreferredVariant(base: String, variant: String): Map<String, String>

    suspend fun getRecentEmojis(): List<String>

    suspend fun addRecentEmoji(emoji: String): List<String>

    suspend fun searchEmojis(query: String): List<String>

    suspend fun prefetchSearchIndex()
}