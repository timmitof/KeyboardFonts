package kg.timmitof.keyboard.font.domain.model

/**
 * Алфавит, с которым стиль реально работает. Стилизованных кириллических алфавитов в Unicode нет,
 * поэтому стили с подстановкой из математических блоков отмечены только [LATIN].
 */
enum class FontScript {
    LATIN,
    CYRILLIC;

    companion object {
        val ALL: Set<FontScript> = values().toSet()
    }
}
