package kg.timmitof.keyboard.presentation.components.emoji

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import kg.timmitof.keyboard.domain.model.EmojiCategory
import kg.timmitof.keyboard.presentation.R

/**
 * Секция сплошной сетки эмодзи.
 */
@Immutable
internal data class EmojiSection(
    val id: String,
    val icon: String,
    @field:StringRes val titleRes: Int,
    val emojis: List<String>,
)

/** Собирает секции панели. */
internal fun buildEmojiSections(
    categories: List<EmojiCategory>,
    recentEmojis: List<String>,
): List<EmojiSection> = buildList {
    if (recentEmojis.isNotEmpty()) {
        add(
            EmojiSection(
                id = RecentSectionId,
                icon = "🕐",
                titleRes = R.string.emoji_category_recent,
                emojis = recentEmojis
            )
        )
    }
    categories.forEach { category ->
        add(
            EmojiSection(
                id = category.id,
                icon = category.icon,
                titleRes = category.titleRes(),
                emojis = category.emojis
            )
        )
    }
}

internal const val RecentSectionId = "recent"

@StringRes
private fun EmojiCategory.titleRes(): Int = when (id) {
    "smileys" -> R.string.emoji_category_smileys
    "people" -> R.string.emoji_category_people
    "animals" -> R.string.emoji_category_animals
    "food" -> R.string.emoji_category_food
    "activities" -> R.string.emoji_category_activities
    "travel" -> R.string.emoji_category_travel
    "objects" -> R.string.emoji_category_objects
    "symbols" -> R.string.emoji_category_symbols
    "flags" -> R.string.emoji_category_flags
    else -> R.string.emoji_category_symbols
}
