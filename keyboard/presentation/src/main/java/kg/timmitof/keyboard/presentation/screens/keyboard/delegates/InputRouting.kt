package kg.timmitof.keyboard.presentation.screens.keyboard.delegates

import kg.timmitof.keyboard.presentation.screens.keyboard.KeyboardSyntax
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardSideEffect

/**
 * Ввод клавиш: в поле панели перевода, если она открыта, иначе — в поле приложения.
 * Делегаты набора шлют правки только сюда, поэтому всё, что умеет клавиатура, работает и в панели.
 * Перевод в поле приложения пишет сам переводчик, мимо этой функции.
 */
internal suspend fun KeyboardSyntax.input(effect: KeyboardSideEffect.Input) {
    if (state.translator == null) {
        postSideEffect(effect)
        return
    }

    reduce {
        val translator = state.translator ?: return@reduce state
        state.copy(translator = translator.copy(editor = translator.editor.apply(effect)))
    }
}
