package kg.timmitof.feature_home.presentation.screens

import androidx.compose.runtime.Immutable
import kg.timmitof.core.ui.base.BaseEvent
import kg.timmitof.core.ui.base.BaseSideEffect
import kg.timmitof.core.ui.base.BaseState
import kg.timmitof.feature_home.domain.model.KeyboardSetupModel
import kg.timmitof.feature_home.domain.model.KeyboardSetupStep

/**
 * Состояние подключения клавиатуры.
 *
 * @property setup включена ли клавиатура и выбрана ли она — от этого зависит весь экран.
 */
@Immutable
data class OnboardingState(
    val setup: KeyboardSetupModel = KeyboardSetupModel(),
) : BaseState()

/** Screen-specific one-off effects */
sealed class OnboardingSideEffect : BaseSideEffect.UiSideEffect()

/** All actions coming from the UI */
sealed class OnboardingEvent : BaseEvent.UiEvent() {

    /** Экран вернулся на передний план — шаги засчитываются сами, без кнопки «Я сделал». */
    data object ScreenResumed : OnboardingEvent()

    /** Кнопка текущего шага: открыть системный экран. */
    data class StepActionClicked(val step: KeyboardSetupStep) : OnboardingEvent()

    /** «Готово, к настройке» после пробы. */
    data object DoneClicked : OnboardingEvent()
}
