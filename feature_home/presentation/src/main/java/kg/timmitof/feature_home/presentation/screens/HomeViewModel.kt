package kg.timmitof.feature_home.presentation.screens

import kg.timmitof.core.ui.base.BaseViewModel

class HomeViewModel: BaseViewModel<HomeState, HomeSideEffect, HomeEvent>(HomeState()) {

    override fun onEvent(event: HomeEvent) {
    }
}