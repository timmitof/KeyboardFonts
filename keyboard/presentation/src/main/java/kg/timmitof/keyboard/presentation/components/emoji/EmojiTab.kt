package kg.timmitof.keyboard.presentation.components.emoji

import kg.timmitof.keyboard.domain.model.EmojiCategory

/**
 * Таб карусели панели эмодзи.
 *
 * @property icon иконка таба в карусели.
 */
internal sealed interface EmojiTab {

    val icon: String

    data object Search : EmojiTab {
        override val icon: String = "🔍"
    }

    data object Recent : EmojiTab {
        override val icon: String = "🕐"
    }

    data class Category(val category: EmojiCategory) : EmojiTab {
        override val icon: String get() = category.icon
    }
}