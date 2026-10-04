package kg.timmitof.keyboard.presentation.screens.keyboard.delegates

import kg.timmitof.keyboard.font.domain.model.KeyboardFont
import kg.timmitof.keyboard.font.domain.repository.FontRepository
import kg.timmitof.keyboard.presentation.screens.keyboard.KeyboardSyntax

internal class FontDelegate(
    private val fontRepository: FontRepository,
) {

    suspend fun KeyboardSyntax.loadFonts() {
        val fonts = fontRepository.getFonts()
        val selected = if (state.settings.isFontRemembered) {
            fontRepository.getSelectedFont()
        } else {
            KeyboardFont.Default
        }
        reduce { state.copy(fonts = fonts, selectedFont = selected) }
    }

    /** Повторный тап по выбранному шрифту сбрасывает его на дефолтный. */
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

    suspend fun KeyboardSyntax.setFontsExpanded(expanded: Boolean) {
        val target = expanded && state.allowsFonts
        if (state.isFontsExpanded != target) {
            reduce { state.copy(isFontsExpanded = target) }
        }
    }

    /** Нужен при выключенном «Запоминать выбранный шрифт»: выбор живёт до конца сообщения. */
    suspend fun KeyboardSyntax.forgetFontIfNeeded() {
        if (state.settings.isFontRemembered || state.selectedFont.isDefault) return

        val default = state.fonts.firstOrNull(KeyboardFont::isDefault) ?: KeyboardFont.Default
        reduce { state.copy(selectedFont = default) }
    }
}
