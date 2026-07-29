package kg.timmitof.feature_home.presentation.screens

import kg.timmitof.core.ui.base.BaseEvent
import kg.timmitof.core.ui.base.BaseSideEffect
import kg.timmitof.core.ui.base.BaseState
import kg.timmitof.feature_home.domain.model.KeyboardSetupModel
import kg.timmitof.feature_home.domain.model.KeyboardSetupStep
import kg.timmitof.feature_home.domain.model.TemplateModel

/**
 * `HomeState` represents the current UI state of the **Home** screen.
 *
 * @property keyboardSetup состояние подключения клавиатуры — от него зависит вся инструкция.
 */
data class HomeState(
    val templateList: List<TemplateModel> = emptyList(),
    val keyboardSetup: KeyboardSetupModel = KeyboardSetupModel()
): BaseState()

/** Screen-specific one-off effects */
sealed class HomeSideEffect : BaseSideEffect.UiSideEffect() {

}

/** All actions coming from the UI */
sealed class HomeEvent : BaseEvent.UiEvent() {
    data class BackgroundSelected(val backgroundPath: String) : HomeEvent()

    /** Экран вернулся на передний план — перепроверяем статус клавиатуры. */
    data object KeyboardSetupChecked : HomeEvent()

    /** Нажата кнопка шага инструкции. */
    data class SetupStepClicked(val step: KeyboardSetupStep) : HomeEvent()
}
