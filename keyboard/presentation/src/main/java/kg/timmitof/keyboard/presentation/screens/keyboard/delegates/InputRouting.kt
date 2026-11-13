package kg.timmitof.keyboard.presentation.screens.keyboard.delegates

import kg.timmitof.keyboard.presentation.screens.keyboard.KeyboardSyntax
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardSideEffect

/**
 * Ввод клавиш: в поле внутри клавиатуры ([KeyboardState.inlineTarget] — поиск эмодзи, панель перевода),
 * иначе — в поле приложения. Делегаты набора шлют правки только сюда, поэтому всё, что умеет клавиатура,
 * работает и во внутренних полях. Перевод в поле приложения пишет сам переводчик, мимо этой функции.
 */
internal suspend fun KeyboardSyntax.input(effect: KeyboardSideEffect.Input) {
    if (state.inlineTarget == null) {
        postSideEffect(effect)
        return
    }

    reduce { state.editInline(effect) }
}
