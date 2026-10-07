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
        postSideEffect(KeyboardSideEffect.Input.CommitText(emoji))

        val updatedRecent = emojiRepository.addRecentEmoji(emoji)
        reduce { state.copy(recentEmojis = updatedRecent) }
    }

    suspend fun KeyboardSyntax.selectVariant(base: String, variant: String) {
        postSideEffect(KeyboardSideEffect.Input.CommitText(variant))

        val updatedPreferred = emojiRepository.setPreferredVariant(base, variant)
        val updatedRecent = emojiRepository.addRecentEmoji(variant)
        reduce {
            state.copy(
                preferredEmojiVariants = updatedPreferred,
                recentEmojis = updatedRecent
            )
        }
    }

    /** Слой переключается сразу, данные догружаются: панель не ждёт чтения каталога. */
    suspend fun KeyboardSyntax.openEmojiPanel() {
        with(layerDelegate) { applyLayer(KeyboardLayer.EMOJI) }

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
    }

    suspend fun KeyboardSyntax.openSearch() {
        reduce { state.copy(emojiSearchQuery = "", emojiSearchResults = emptyList()) }
        with(layerDelegate) { applyLayer(KeyboardLayer.EMOJI_SEARCH) }
    }

    suspend fun KeyboardSyntax.updateSearchQuery(query: String) {
        // Запрос показываем сразу, результаты — когда посчитаются и только если запрос не успели сменить.
        reduce { state.copy(emojiSearchQuery = query, emojiSearchSelection = 0) }

        val results = emojiRepository.searchEmojis(query)
        reduce {
            if (state.emojiSearchQuery == query) state.copy(emojiSearchResults = results) else state
        }
    }

    suspend fun prefetchVariants() {
        emojiRepository.getEmojiVariants()
    }

    /** Зовём при открытии панели, а не на старте: не нагружаем тех, кто эмодзи не открывает. */
    suspend fun prefetchSearchIndex() {
        emojiRepository.prefetchSearchIndex()
    }
}
