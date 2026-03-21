package kg.timmitof.keyboard.presentation.screens.keyboard

import kg.timmitof.core.ui.base.BaseSideEffect
import kg.timmitof.core.ui.base.BaseViewModel
import kg.timmitof.keyboard.domain.model.KeyboardKey
import kg.timmitof.keyboard.domain.repository.KeyboardLayoutRepository
import org.orbitmvi.orbit.syntax.Syntax

internal class KeyboardViewModel(
    private val keyboardLayoutRepository: KeyboardLayoutRepository
) : BaseViewModel<KeyboardState, KeyboardSideEffect, KeyboardEvent>(KeyboardState()) {

    override fun onEvent(event: KeyboardEvent) {
        when (event) {
            is KeyboardEvent.OnKeySelect -> handleKeySelect(event.char)
            is KeyboardEvent.OnShift -> handleShift()
            is KeyboardEvent.OnBackspace -> handleBackspace()
            is KeyboardEvent.OnSpace -> handleSpace()
            is KeyboardEvent.OnEnter -> handleEnter()
            is KeyboardEvent.OnSymbolsSwitch -> handleSymbolsSwitch()
            is KeyboardEvent.OnEmojiSwitch -> handleEmojiSwitch()
        }
    }

    override suspend fun Syntax<KeyboardState, BaseSideEffect>.onBootstrap() {
        val keyboardLayout = keyboardLayoutRepository.getLayout("en_us")
        reduce { state.copy(keyboardLayout = keyboardLayout) }
    }

    private fun handleKeySelect(char: String) = intent {
         postSideEffect(KeyboardSideEffect.CommitText(char))

        if (state.isUpperCase && !state.isCapsLock) {
            reduce { state.copy(isUpperCase = false) }
        }
    }

    private fun handleShift() = intent {
        reduce {
            when {
                state.isCapsLock -> state.copy(isUpperCase = false, isCapsLock = false)
                state.isUpperCase -> state.copy(isCapsLock = true)
                else -> state.copy(isUpperCase = true)
            }
        }
    }

    private fun handleBackspace() = intent {
        // postSideEffect(KeyboardSideEffect.DeleteBackward)
    }

    private fun handleSpace() = intent {
        // postSideEffect(KeyboardSideEffect.CommitText(" "))
    }

    private fun handleEnter() = intent {
        // postSideEffect(KeyboardSideEffect.PerformEditorAction)
    }

    private fun handleSymbolsSwitch() = intent {
        // postSideEffect(KeyboardSideEffect.SwitchToSymbols)
    }

    private fun handleEmojiSwitch() = intent {
        // postSideEffect(KeyboardSideEffect.SwitchToEmoji)
    }
}