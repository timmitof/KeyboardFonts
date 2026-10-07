package kg.timmitof.keyboard.font.domain.model

/** [visible] — шрифты в панели клавиатуры в выбранном порядке, [hidden] — остальные по каталогу; [isCustom] — пользователь панель менял. */
data class FontPanel(
    val visible: List<KeyboardFont> = emptyList(),
    val hidden: List<KeyboardFont> = emptyList(),
    val isCustom: Boolean = false,
)
