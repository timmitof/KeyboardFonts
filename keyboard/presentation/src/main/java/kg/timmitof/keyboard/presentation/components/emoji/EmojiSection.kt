package kg.timmitof.keyboard.presentation.components.emoji

import androidx.compose.runtime.Immutable
import dev.icerock.moko.resources.StringResource
import kg.timmitof.core.resources.MR
import kg.timmitof.core.resources.emoji_category_activities
import kg.timmitof.core.resources.emoji_category_animals
import kg.timmitof.core.resources.emoji_category_flags
import kg.timmitof.core.resources.emoji_category_food
import kg.timmitof.core.resources.emoji_category_objects
import kg.timmitof.core.resources.emoji_category_people
import kg.timmitof.core.resources.emoji_category_recent
import kg.timmitof.core.resources.emoji_category_smileys
import kg.timmitof.core.resources.emoji_category_symbols
import kg.timmitof.core.resources.emoji_category_travel
import kg.timmitof.keyboard.domain.model.EmojiCategory

@Immutable
internal data class EmojiSection(
    val id: String,
    val icon: String,
    val titleRes: StringResource,
    val emojis: List<String>,
)

internal fun buildEmojiSections(
    categories: List<EmojiCategory>,
    recentEmojis: List<String>,
): List<EmojiSection> = buildList {
    if (recentEmojis.isNotEmpty()) {
        add(
            EmojiSection(
                id = RecentSectionId,
                icon = "🕐",
                titleRes = MR.strings.emoji_category_recent,
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

private fun EmojiCategory.titleRes(): StringResource = when (id) {
    "smileys" -> MR.strings.emoji_category_smileys
    "people" -> MR.strings.emoji_category_people
    "animals" -> MR.strings.emoji_category_animals
    "food" -> MR.strings.emoji_category_food
    "activities" -> MR.strings.emoji_category_activities
    "travel" -> MR.strings.emoji_category_travel
    "objects" -> MR.strings.emoji_category_objects
    "symbols" -> MR.strings.emoji_category_symbols
    "flags" -> MR.strings.emoji_category_flags
    else -> MR.strings.emoji_category_symbols
}
