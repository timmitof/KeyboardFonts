package kg.timmitof.keyboard.domain.model

/**
 * Категория эмодзи для панели эмодзи.
 *
 * @param id уникальный идентификатор категории.
 * @param icon эмодзи-иконка, отображаемая на табе категории.
 * @param emojis список эмодзи данной категории в порядке отображения.
 */
data class EmojiCategory(
    val id: String,
    val icon: String,
    val emojis: List<String>
)