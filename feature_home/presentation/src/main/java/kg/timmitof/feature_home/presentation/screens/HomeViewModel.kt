package kg.timmitof.feature_home.presentation.screens

import dagger.hilt.android.lifecycle.HiltViewModel
import kg.timmitof.core.domain.interactor.ProjectInteractor
import kg.timmitof.core.ui.base.BaseSideEffect
import kg.timmitof.core.ui.base.BaseViewModel
import org.orbitmvi.orbit.syntax.Syntax
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val projectInteractor: ProjectInteractor
): BaseViewModel<HomeState, HomeSideEffect, HomeEvent>(HomeState()) {
    override fun onEvent(event: HomeEvent) {}

    override suspend fun Syntax<HomeState, BaseSideEffect>.onBootstrap() {
        val templateList = projectInteractor.getAllTemplates()
        reduce { state.copy(templateList = templateList) }
    }
}