package kg.timmitof.keyboard.font.domain.repository

import kg.timmitof.keyboard.font.domain.model.KeyboardFont

interface FontRepository {

    /** Все доступные шрифты; первый — обычный (по умолчанию). */
    fun getFonts(): List<KeyboardFont>

    /** Текущий выбранный шрифт (сохранённый или обычный). */
    suspend fun getSelectedFont(): KeyboardFont

    /** Сохранить выбор шрифта по [id]. */
    suspend fun setSelectedFont(id: String)
}
