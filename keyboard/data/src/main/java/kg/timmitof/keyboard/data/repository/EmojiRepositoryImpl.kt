package kg.timmitof.keyboard.data.repository

import kg.timmitof.keyboard.data.emoji.EmojiCatalog
import kg.timmitof.keyboard.data.emoji.EmojiNameIndex
import kg.timmitof.keyboard.data.emoji.RecentEmojiDataSource
import kg.timmitof.keyboard.domain.model.EmojiCategory
import kg.timmitof.keyboard.domain.repository.EmojiRepository
import javax.inject.Inject

class EmojiRepositoryImpl @Inject constructor(
    private val recentEmojiDataSource: RecentEmojiDataSource,
) : EmojiRepository {

    override suspend fun getEmojiCategories(): List<EmojiCategory> = EmojiCatalog.categories

    override suspend fun getRecentEmojis(): List<String> = recentEmojiDataSource.getRecent()

    override suspend fun addRecentEmoji(emoji: String): List<String> = recentEmojiDataSource.add(emoji)

    override suspend fun searchEmojis(query: String): List<String> = EmojiNameIndex.search(query)
}