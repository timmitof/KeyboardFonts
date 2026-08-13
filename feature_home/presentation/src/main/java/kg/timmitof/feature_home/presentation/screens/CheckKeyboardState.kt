package kg.timmitof.feature_home.presentation.screens

import kg.timmitof.core.ui.base.BaseEvent
import kg.timmitof.core.ui.base.BaseSideEffect
import kg.timmitof.core.ui.base.BaseState
import kg.timmitof.feature_home.domain.model.KeyboardSetupModel
import kg.timmitof.feature_home.domain.model.KeyboardSetupStep
import kg.timmitof.feature_home.domain.model.TemplateModel

/**
 * Состояние экрана проверки клавиатуры.
 *
 * @property keyboardSetup состояние подключения клавиатуры — от него зависит вся инструкция.
 */
data class CheckKeyboardState(
    val templateList: List<TemplateModel> = emptyList(),
    val keyboardSetup: KeyboardSetupModel = KeyboardSetupModel()
) : BaseState()

/** Screen-specific one-off effects */
sealed class CheckKeyboardSideEffect : BaseSideEffect.UiSideEffect()

/** All actions coming from the UI */
sealed class CheckKeyboardEvent : BaseEvent.UiEvent() {

    /** Экран вернулся на передний план — перепроверяем статус клавиатуры. */
    data object KeyboardSetupChecked : CheckKeyboardEvent()

    /** Нажата кнопка шага инструкции. */
    data class SetupStepClicked(val step: KeyboardSetupStep) : CheckKeyboardEvent()

    /** Стрелка «назад» в верхней панели. */
    data object BackClicked : CheckKeyboardEvent()
}
