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

    /** Собранные раскладки: нижний ряд и цифры доклеиваются при каждой смене слоя или настроек, а результат тот же. */
    private val cache = HashMap<LayoutKey, KeyboardLayout>()

    private data class LayoutKey(
        val layoutName: String,
        val bottomRowVariant: String?,
        val hasDigitsRow: Boolean,
    )

    private suspend fun KeyboardState.resolveLayout(layer: KeyboardLayer): KeyboardLayout? {
        fieldType.layoutName
            ?.takeIf { layer == KeyboardLayer.LETTERS }
            ?.let { return keyboardLayoutRepository.getLayout(it) }

        val layoutName = (if (layer.usesLanguageLayout) activeLanguage?.code else layer.fixedLayoutName)
            ?: return null
        val isLetters = layer == KeyboardLayer.LETTERS
        val key = LayoutKey(
            layoutName = layoutName,
            bottomRowVariant = bottomRowVariant.takeIf { isLetters },
            hasDigitsRow = isLetters && settings.isDigitsRowEnabled,
        )
        cache[key]?.let { return it }

        val layout = keyboardLayoutRepository.getLayout(layoutName) ?: return null
        if (!isLetters) return layout.also { cache[key] = it }

        val bottomRow = key.bottomRowVariant?.let { keyboardLayoutRepository.getBottomRow(it) }
        val letters = bottomRow?.let(layout::withBottomRow) ?: layout

        return (if (key.hasDigitsRow) letters.withDigitsRow() else letters).also { cache[key] = it }
    }

    suspend fun preloadLayouts(languageCodes: List<String>) {
        (languageCodes + KeyboardLayer.entries.mapNotNull { it.fixedLayoutName })
            .distinct()
            .forEach { keyboardLayoutRepository.getLayout(it) }
    }
}
