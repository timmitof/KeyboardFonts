package kg.timmitof.keyboard.font.data.repository

import kg.timmitof.keyboard.data.font.FontCatalog
import kg.timmitof.keyboard.font.data.SelectedFontDataSource
import kg.timmitof.keyboard.font.domain.model.KeyboardFont
import kg.timmitof.keyboard.font.domain.repository.FontRepository
import javax.inject.Inject

class FontRepositoryImpl @Inject constructor(
    private val selectedFontDataSource: SelectedFontDataSource,
) : FontRepository {

    override fun getFonts(): List<KeyboardFont> = FontCatalog.fonts

    override suspend fun getSelectedFont(): KeyboardFont {
        val savedId = selectedFontDataSource.get()
        return FontCatalog.fonts.firstOrNull { it.id == savedId } ?: FontCatalog.fonts.first()
    }

    override suspend fun setSelectedFont(id: String) = selectedFontDataSource.set(id)
}
