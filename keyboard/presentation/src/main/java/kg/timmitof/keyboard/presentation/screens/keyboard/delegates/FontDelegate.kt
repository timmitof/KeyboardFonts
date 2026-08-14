package kg.timmitof.keyboard.presentation.screens.keyboard.delegates

import kg.timmitof.keyboard.font.domain.model.KeyboardFont
import kg.timmitof.keyboard.font.domain.repository.FontRepository
import kg.timmitof.keyboard.presentation.screens.keyboard.KeyboardSyntax

internal class FontDelegate(
    private val fontRepository: FontRepository,
) {

    /** Загружает каталог шрифтов и сохранённый выбор в состояние. */
    suspend fun KeyboardSyntax.loadFonts() {
        val fonts = fontRepository.getFonts()
        val selected = if (state.settings.isFontRemembered) {
            fontRepository.getSelectedFont()
        } else {
            KeyboardFont.Default
        }
        reduce { state.copy(fonts = fonts, selectedFont = selected) }
    }

    /**
     * Смена шрифта: сохраняет выбор и обновляет состояние.
     * Повторный тап по уже выбранному шрифту сбрасывает стилизацию на дефолтную.
     */
    suspend fun KeyboardSyntax.selectFont(font: KeyboardFont) {
        val current = state.selectedFont
        val target = when {
            font.id != current.id -> font
            current.isDefault -> return
            else -> state.fonts.firstOrNull(KeyboardFont::isDefault) ?: KeyboardFont.Default
        }

        fontRepository.setSelectedFont(target.id)
        reduce { state.copy(selectedFont = target) }
    }

    /**
     * Разворачивает карусель шрифтов на всю верхнюю панель или сворачивает её в кнопку «Aa».
     */
    suspend fun KeyboardSyntax.setFontsExpanded(expanded: Boolean) {
        val target = expanded && state.allowsFonts
        if (state.isFontsExpanded != target) {
            reduce { state.copy(isFontsExpanded = target) }
        }
    }

    /**
     * Возврат к обычному шрифту в начале сессии ввода.
     *
     * Нужен, когда «Запоминать выбранный шрифт» выключено: выбор действует
     * до конца текущего сообщения и не переносится в следующее поле.
     */
    suspend fun KeyboardSyntax.forgetFontIfNeeded() {
        if (state.settings.isFontRemembered || state.selectedFont.isDefault) return

        val default = state.fonts.firstOrNull(KeyboardFont::isDefault) ?: KeyboardFont.Default
        reduce { state.copy(selectedFont = default) }
    }
}
