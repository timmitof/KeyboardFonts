package kg.timmitof.feature_settings.presentation.screens.crop

import androidx.compose.runtime.Immutable
import kg.timmitof.core.ui.base.BaseEvent
import kg.timmitof.core.ui.base.BaseSideEffect
import kg.timmitof.core.ui.base.BaseState
import kg.timmitof.keyboard.domain.model.BackgroundPhoto
import kg.timmitof.keyboard.domain.model.KeyboardSettings
import kg.timmitof.keyboard.domain.model.PhotoCrop

@Immutable
data class BackgroundCropState(
    val photo: BackgroundPhoto? = null,
    val settings: KeyboardSettings = KeyboardSettings(),
    val isSaving: Boolean = false,
) : BaseState()

sealed class BackgroundCropSideEffect : BaseSideEffect.UiSideEffect()

sealed class BackgroundCropEvent : BaseEvent.UiEvent() {

    data class DoneClicked(val crop: PhotoCrop) : BackgroundCropEvent()

    data object DeleteClicked : BackgroundCropEvent()

    data object BackClicked : BackgroundCropEvent()
}
