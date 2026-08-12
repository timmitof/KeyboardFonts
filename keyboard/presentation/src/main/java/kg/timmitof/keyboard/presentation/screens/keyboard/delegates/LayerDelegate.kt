package kg.timmitof.keyboard.presentation.screens.keyboard.delegates

import kg.timmitof.keyboard.domain.model.KeyboardLayout
import kg.timmitof.keyboard.domain.repository.KeyboardLayoutRepository
import kg.timmitof.keyboard.presentation.screens.keyboard.KeyboardSyntax
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardLayer
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardState

internal class LayerDelegate(
    private val keyboardLayoutRepository: KeyboardLayoutRepository,
) {

    suspend fun KeyboardSyntax.applyLayer(layer: KeyboardLayer) {
        val layout = state.resolveLayout(layer) ?: state.keyboardLayout

        reduce { state.copy(layer = layer, keyboardLayout = layout) }
    }

    suspend fun KeyboardSyntax.toggleSymbolsAlt() {
        val next = if (state.layer == KeyboardLayer.SYMBOLS) {
            KeyboardLayer.SYMBOLS_ALT
        } else {
            KeyboardLayer.SYMBOLS
        }
        applyLayer(next)
    }

    /**
     * Раскладка слоя для текущего поля.
     */
    private suspend fun KeyboardState.resolveLayout(layer: KeyboardLayer): KeyboardLayout? {
        fieldType.layoutName
            ?.takeIf { layer == KeyboardLayer.LETTERS }
            ?.let { return keyboardLayoutRepository.getLayout(it) }

        val layoutName = if (layer.usesLanguageLayout) activeLanguage?.code else layer.fixedLayoutName
        val layout = layoutName?.let { keyboardLayoutRepository.getLayout(it) } ?: return null

        if (layer != KeyboardLayer.LETTERS) return layout

        val bottomRow = bottomRowVariant?.let { keyboardLayoutRepository.getBottomRow(it) }
        return bottomRow?.let(layout::withBottomRow) ?: layout
    }

    /** Прогревает кэш раскладок (языковых и фиксированных), чтобы переключение было мгновенным. */
    suspend fun preloadLayouts(languageCodes: List<String>) {
        (languageCodes + KeyboardLayer.entries.mapNotNull { it.fixedLayoutName })
            .distinct()
            .forEach { keyboardLayoutRepository.getLayout(it) }
    }
}
