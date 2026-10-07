package kg.timmitof.feature_settings.presentation.screens.crop

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kg.timmitof.core.navigation.graphs.SettingsGraph
import kg.timmitof.core.ui.base.BaseSideEffect
import kg.timmitof.core.ui.base.BaseViewModel
import kg.timmitof.feature_settings.domain.interactor.SettingsInteractor
import kg.timmitof.keyboard.domain.model.PhotoCrop
import org.orbitmvi.orbit.syntax.Syntax
import javax.inject.Inject

@HiltViewModel
class BackgroundCropViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val settingsInteractor: SettingsInteractor,
) : BaseViewModel<BackgroundCropState, BackgroundCropSideEffect, BackgroundCropEvent>(BackgroundCropState()) {

    private val photoId = savedStateHandle.toRoute<SettingsGraph.BackgroundCropScreen>().photoId

    init {
        observeSettings()
    }

    override fun onEvent(event: BackgroundCropEvent) {
        when (event) {
            is BackgroundCropEvent.DoneClicked -> save(event.crop)
            is BackgroundCropEvent.DeleteClicked -> delete()
            is BackgroundCropEvent.BackClicked -> navigateBack()
        }
    }

    override suspend fun Syntax<BackgroundCropState, BaseSideEffect>.onBootstrap() {
        val photo = settingsInteractor.getPhoto(photoId)
        if (photo == null) navigateBack() else reduce { state.copy(photo = photo) }
    }

    /** Рамка повторяет высоту клавиатуры из настроек, поэтому следим за ними. */
    private fun observeSettings() = intent {
        settingsInteractor.observeSettings().collect { settings ->
            reduce { state.copy(settings = settings) }
        }
    }

    private fun save(crop: PhotoCrop) = intent {
        val photo = state.photo ?: return@intent
        if (state.isSaving) return@intent

        reduce { state.copy(isSaving = true) }
        settingsInteractor.applyPhoto(photo, crop)
        navigateBack()
    }

    private fun delete() = intent {
        settingsInteractor.deletePhoto(photoId)
        navigateBack()
    }
}
