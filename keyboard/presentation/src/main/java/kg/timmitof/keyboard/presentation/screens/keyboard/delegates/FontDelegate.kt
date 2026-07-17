package kg.timmitof.keyboard.presentation.screens.keyboard.delegates

import kg.timmitof.keyboard.domain.model.KeyboardFont
import kg.timmitof.keyboard.domain.repository.FontRepository
import kg.timmitof.keyboard.presentation.screens.keyboard.KeyboardSyntax

internal class FontDelegate(
    private val fontRepository: FontRepository,
) {

    /** Загружает каталог шрифтов и сохранённый выбор в состояние. */
    suspend fun KeyboardSyntax.loadFonts() {
        val fonts = fontRepository.getFonts()
        val selected = fontRepository.getSelectedFont()
        reduce { state.copy(fonts = fonts, selectedFont = selected) }
    }

    /** Смена шрифта: сохраняет выбор и обновляет состояние. */
    suspend fun KeyboardSyntax.selectFont(font: KeyboardFont) {
        if (font.id == state.selectedFont.id) return

        fontRepository.setSelectedFont(font.id)
        reduce { state.copy(selectedFont = font) }
    }
}
