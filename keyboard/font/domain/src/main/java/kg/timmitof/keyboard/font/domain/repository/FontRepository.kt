package kg.timmitof.keyboard.font.domain.repository

import kg.timmitof.keyboard.font.domain.model.FontPanel
import kg.timmitof.keyboard.font.domain.model.KeyboardFont
import kotlinx.coroutines.flow.Flow

interface FontRepository {

    /** Поток: панель настраивают в приложении, а клавиатура живёт в другом процессе. */
    fun observePanel(): Flow<FontPanel>

    suspend fun setPanelFonts(ids: List<String>)

    suspend fun resetPanel()

    suspend fun getSelectedFont(): KeyboardFont

    suspend fun setSelectedFont(id: String)
}
