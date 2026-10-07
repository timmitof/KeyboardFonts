package kg.timmitof.keyboard.font.data.repository

import kg.timmitof.keyboard.data.font.FontCatalog
import kg.timmitof.keyboard.font.data.FontPanelDataSource
import kg.timmitof.keyboard.font.data.SelectedFontDataSource
import kg.timmitof.keyboard.font.domain.model.FontPanel
import kg.timmitof.keyboard.font.domain.model.KeyboardFont
import kg.timmitof.keyboard.font.domain.repository.FontRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FontRepositoryImpl @Inject constructor(
    private val selectedFontDataSource: SelectedFontDataSource,
    private val panelDataSource: FontPanelDataSource,
) : FontRepository {

    override fun observePanel(): Flow<FontPanel> =
        panelDataSource.observe().distinctUntilChanged().map(::toPanel)

    override suspend fun setPanelFonts(ids: List<String>) = panelDataSource.set(ids)

    override suspend fun resetPanel() = panelDataSource.clear()

    override suspend fun getSelectedFont(): KeyboardFont {
        val savedId = selectedFontDataSource.get()
        return FontCatalog.fonts.firstOrNull { it.id == savedId } ?: FontCatalog.fonts.first()
    }

    override suspend fun setSelectedFont(id: String) = selectedFontDataSource.set(id)

    /** Неизвестные id (шрифт убрали из каталога) пропускаем; пустая панель — значит, показываем всё. */
    private fun toPanel(ids: List<String>?): FontPanel {
        val catalog = FontCatalog.fonts
        val byId = catalog.associateBy(KeyboardFont::id)
        val visible = ids.orEmpty().mapNotNull(byId::get).distinct()
        if (visible.isEmpty()) return FontPanel(visible = catalog)

        val visibleIds = visible.mapTo(HashSet(), KeyboardFont::id)
        return FontPanel(
            visible = visible,
            hidden = catalog.filterNot { it.id in visibleIds },
            isCustom = visible != catalog,
        )
    }
}
