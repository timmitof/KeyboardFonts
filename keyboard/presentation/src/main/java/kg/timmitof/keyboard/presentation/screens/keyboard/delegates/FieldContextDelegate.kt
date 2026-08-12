package kg.timmitof.keyboard.presentation.screens.keyboard.delegates

import kg.timmitof.keyboard.presentation.screens.keyboard.KeyboardSyntax
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardFieldContext
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardLayer
import kg.timmitof.keyboard.presentation.screens.keyboard.states.ShiftState

/**
 * Подстраивает клавиатуру под поле ввода: раскладка, нижний ряд, шрифты, Shift.
 */
internal class FieldContextDelegate(
    private val layerDelegate: LayerDelegate,
) {

    suspend fun KeyboardSyntax.applyContext(context: KeyboardFieldContext) {
        if (state.fieldContext == context) return

        val typeChanged = state.fieldType != context.type

        reduce {
            state.copy(
                fieldContext = context,
                suggestions = emptyList(),
                autoCorrection = null,
                isFontsExpanded = state.isFontsExpanded && context.type.allowsFonts,
                shiftState = if (context.type.autoCapitalize) state.shiftState else ShiftState.DISABLED,
            )
        }

        if (typeChanged || state.layer == KeyboardLayer.LETTERS) {
            with(layerDelegate) { applyLayer(KeyboardLayer.LETTERS) }
        }
    }
}
