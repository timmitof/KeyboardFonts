package kg.timmitof.keyboard.presentation.screens.keyboard

import kg.timmitof.core.ui.base.BaseSideEffect
import kg.timmitof.core.ui.base.BaseViewModel
import kg.timmitof.keyboard.domain.repository.KeyboardLayoutRepository
import org.orbitmvi.orbit.syntax.Syntax

class KeyboardViewModel(
    private val keyboardLayoutRepository: KeyboardLayoutRepository
) : BaseViewModel<KeyboardState, KeyboardSideEffect, KeyboardEvent>(KeyboardState()) {

    override fun onEvent(event: KeyboardEvent) {}

    override suspend fun Syntax<KeyboardState, BaseSideEffect>.onBootstrap() {
        val keyboardLayout = keyboardLayoutRepository.getLayout("en_us")
        reduce { state.copy(keyboardLayout = keyboardLayout) }
    }
}