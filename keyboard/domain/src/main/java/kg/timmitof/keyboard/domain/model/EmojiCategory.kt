package kg.timmitof.keyboard.domain.model

data class EmojiCategory(
    val id: String,
    val icon: String,
    val emojis: List<String>
)