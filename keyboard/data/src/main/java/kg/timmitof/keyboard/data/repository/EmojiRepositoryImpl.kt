package kg.timmitof.keyboard.data.repository

import kg.timmitof.keyboard.data.emoji.EmojiCatalog
import kg.timmitof.keyboard.data.emoji.EmojiSearchIndex
import kg.timmitof.keyboard.data.emoji.EmojiVariantCatalog
import kg.timmitof.keyboard.data.emoji.PreferredEmojiVariantDataSource
import kg.timmitof.keyboard.data.emoji.RecentEmojiDataSource
import kg.timmitof.keyboard.domain.model.EmojiCategory
import kg.timmitof.keyboard.domain.repository.EmojiRepository
import javax.inject.Inject

class EmojiRepositoryImpl @Inject constructor(
    private val recentEmojiDataSource: RecentEmojiDataSource,
    private val preferredVariantDataSource: PreferredEmojiVariantDataSource,
    private val emojiSearchIndex: EmojiSearchIndex,
) : EmojiRepository {

    override suspend fun getEmojiCategories(): List<EmojiCategory> = EmojiCatalog.getCategories()

    override suspend fun getEmojiVariants(): Map<String, List<String>> =
        EmojiVariantCatalog.getVariants()

    override suspend fun getPreferredVariants(): Map<String, String> =
        preferredVariantDataSource.getAll()

    override suspend fun setPreferredVariant(base: String, variant: String): Map<String, String> =
        preferredVariantDataSource.setPreferred(base, variant)

    override suspend fun getRecentEmojis(): List<String> = recentEmojiDataSource.getRecent()

    override suspend fun addRecentEmoji(emoji: String): List<String> = recentEmojiDataSource.add(emoji)

    override suspend fun searchEmojis(query: String): List<String> = emojiSearchIndex.search(query)

    override suspend fun prefetchSearchIndex() = emojiSearchIndex.prefetch()
}