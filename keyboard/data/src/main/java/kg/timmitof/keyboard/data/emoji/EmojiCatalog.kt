package kg.timmitof.keyboard.data.emoji

import android.graphics.Paint
import kg.timmitof.keyboard.data.emoji.catalog.ActivitiesEmoji
import kg.timmitof.keyboard.data.emoji.catalog.AnimalsEmoji
import kg.timmitof.keyboard.data.emoji.catalog.FlagsEmoji
import kg.timmitof.keyboard.data.emoji.catalog.FoodEmoji
import kg.timmitof.keyboard.data.emoji.catalog.ObjectsEmoji
import kg.timmitof.keyboard.data.emoji.catalog.PeopleEmoji
import kg.timmitof.keyboard.data.emoji.catalog.SmileysEmoji
import kg.timmitof.keyboard.data.emoji.catalog.SymbolsEmoji
import kg.timmitof.keyboard.data.emoji.catalog.TravelEmoji
import kg.timmitof.keyboard.domain.model.EmojiCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Статический каталог эмодзи, сгруппированный по категориям Unicode.
 *
 * Держим данные в Kotlin-объектах, а не в JSON-ассете: набор статичен,
 * парсинг не нужен, доступ мгновенный. Содержимое каждой категории —
 * в отдельном файле пакета `catalog`.
 */
internal object EmojiCatalog {

    /** Полный каталог — без оглядки на то, что умеет шрифт устройства. */
    private val allCategories: List<EmojiCategory> by lazy {
        listOf(
            EmojiCategory(id = "smileys", icon = "😀", emojis = SmileysEmoji.emojis),
            EmojiCategory(id = "people", icon = "👋", emojis = PeopleEmoji.emojis),
            EmojiCategory(id = "animals", icon = "🐻", emojis = AnimalsEmoji.emojis),
            EmojiCategory(id = "food", icon = "🍔", emojis = FoodEmoji.emojis),
            EmojiCategory(id = "activities", icon = "⚽", emojis = ActivitiesEmoji.emojis),
            EmojiCategory(id = "travel", icon = "🚗", emojis = TravelEmoji.emojis),
            EmojiCategory(id = "objects", icon = "💡", emojis = ObjectsEmoji.emojis),
            EmojiCategory(id = "symbols", icon = "❤️", emojis = SymbolsEmoji.emojis),
            EmojiCategory(id = "flags", icon = "🏁", emojis = FlagsEmoji.emojis)
        )
    }

    @Volatile
    private var supported: List<EmojiCategory>? = null

    /**
     * Категории без эмодзи, которых нет в шрифте устройства.
     *
     * Каталог собран по свежему Unicode, а старые прошивки часть символов
     * не знают — без фильтра они превратились бы в пустые квадраты.
     */
    suspend fun getCategories(): List<EmojiCategory> = withContext(Dispatchers.Default) {
        supported ?: buildSupported().also { supported = it }
    }

    private fun buildSupported(): List<EmojiCategory> {
        val paint = Paint()
        return allCategories.mapNotNull { category ->
            val emojis = category.emojis.distinct().filter { paint.canRender(it) }
            category.copy(emojis = emojis).takeIf { emojis.isNotEmpty() }
        }
    }

    /**
     * Умеет ли шрифт нарисовать эмодзи.
     *
     * Вариационный селектор — только пожелание «рисуй как эмодзи», и часть
     * шрифтов из-за него отвечает отказом, поэтому пробуем ещё раз без него.
     */
    private fun Paint.canRender(emoji: String): Boolean =
        hasGlyph(emoji) || hasGlyph(emoji.withoutVariationSelectors())

    private fun String.withoutVariationSelectors(): String =
        filterNot { it.code == TEXT_VARIATION || it.code == EMOJI_VARIATION }

    private const val TEXT_VARIATION = 0xFE0E
    private const val EMOJI_VARIATION = 0xFE0F
}
