package kg.timmitof.keyboard.presentation.screens.keyboard.delegates

import kg.timmitof.keyboard.domain.repository.EmojiRepository
import kg.timmitof.keyboard.presentation.screens.keyboard.KeyboardSyntax
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardLayer
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardSideEffect

internal class EmojiDelegate(
    private val emojiRepository: EmojiRepository,
    private val layerDelegate: LayerDelegate,
) {

    suspend fun KeyboardSyntax.selectEmoji(emoji: String) {
        postSideEffect(KeyboardSideEffect.CommitText(emoji))

        val updatedRecent = emojiRepository.addRecentEmoji(emoji)
        reduce { state.copy(recentEmojis = updatedRecent) }
    }

    /** Выбор варианта тона. */
    suspend fun KeyboardSyntax.selectVariant(base: String, variant: String) {
        postSideEffect(KeyboardSideEffect.CommitText(variant))

        val updatedPreferred = emojiRepository.setPreferredVariant(base, variant)
        val updatedRecent = emojiRepository.addRecentEmoji(variant)
        reduce {
            state.copy(
                preferredEmojiVariants = updatedPreferred,
                recentEmojis = updatedRecent
            )
        }
    }

    /** Открывает панель эмодзи, при первом входе подгружая каталог. */
    suspend fun KeyboardSyntax.openEmojiPanel() {
        if (state.emojiCategories.isEmpty()) {
            val categories = emojiRepository.getEmojiCategories()
            val variants = emojiRepository.getEmojiVariants()
            val preferred = emojiRepository.getPreferredVariants()
            reduce {
                state.copy(
                    emojiCategories = categories,
                    emojiVariants = variants,
                    preferredEmojiVariants = preferred
                )
            }
        }
        val recentEmojis = emojiRepository.getRecentEmojis()
        reduce { state.copy(recentEmojis = recentEmojis) }

        with(layerDelegate) { applyLayer(KeyboardLayer.EMOJI) }
    }

    /** Открывает поиск эмодзи с чистым запросом. */
    suspend fun KeyboardSyntax.openSearch() {
        reduce { state.copy(emojiSearchQuery = "", emojiSearchResults = emptyList()) }
        with(layerDelegate) { applyLayer(KeyboardLayer.EMOJI_SEARCH) }
    }

    /** Обновляет поисковый запрос и результаты; используется и вводом текста на слое поиска. */
    suspend fun KeyboardSyntax.updateSearchQuery(query: String) {
        val results = emojiRepository.searchEmojis(query)
        reduce {
            state.copy(
                emojiSearchQuery = query,
                emojiSearchResults = results,
                emojiSearchSelection = 0
            )
        }
    }

    /** Фоновый прогрев каталога вариантов тона. */
    suspend fun prefetchVariants() {
        emojiRepository.getEmojiVariants()
    }
}
