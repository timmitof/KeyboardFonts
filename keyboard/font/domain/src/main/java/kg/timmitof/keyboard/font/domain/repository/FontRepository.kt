package kg.timmitof.keyboard.font.domain.repository

import kg.timmitof.keyboard.font.domain.model.KeyboardFont

interface FontRepository {

    fun getFonts(): List<KeyboardFont>

    suspend fun getSelectedFont(): KeyboardFont

    suspend fun setSelectedFont(id: String)
}
