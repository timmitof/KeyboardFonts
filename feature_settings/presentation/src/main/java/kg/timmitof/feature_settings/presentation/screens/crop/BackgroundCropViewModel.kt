package kg.timmitof.feature_settings.presentation.screens.crop

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kg.timmitof.core.navigation.graphs.SettingsGraph
import kg.timmitof.core.ui.base.BaseViewModel
import kg.timmitof.feature_settings.domain.interactor.SettingsInteractor
import kg.timmitof.feature_settings.presentation.R
import kg.timmitof.keyboard.domain.model.PhotoCrop
import javax.inject.Inject

@HiltViewModel
class BackgroundCropViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val settingsInteractor: SettingsInteractor,
    @param:ApplicationContext private val context: Context,
) : BaseViewModel<BackgroundCropState, BackgroundCropSideEffect, BackgroundCropEvent>(
    BackgroundCropState(uri = savedStateHandle.toRoute<SettingsGraph.BackgroundCropScreen>().uri)
) {

    init {
        observeSettings()
    }

    override fun onEvent(event: BackgroundCropEvent) {
        when (event) {
            is BackgroundCropEvent.DoneClicked -> save(event.crop)
            is BackgroundCropEvent.AnotherPhotoPicked -> intent { reduce { state.copy(uri = event.uri) } }
            is BackgroundCropEvent.BackClicked -> navigateBack()
        }
    }

    /** Рамка повторяет высоту клавиатуры из настроек, поэтому следим за ними. */
    private fun observeSettings() = intent {
        settingsInteractor.observeSettings().collect { settings ->
            reduce { state.copy(settings = settings) }
        }
    }

    private fun save(crop: PhotoCrop) = intent {
        if (state.isSaving) return@intent
        reduce { state.copy(isSaving = true) }

        runCatching { settingsInteractor.importBackgroundPhoto(state.uri, crop) }
            .onSuccess { navigateBack() }
            .onFailure {
                reduce { state.copy(isSaving = false) }
                showToast(context.getString(R.string.background_crop_error))
            }
    }
}
