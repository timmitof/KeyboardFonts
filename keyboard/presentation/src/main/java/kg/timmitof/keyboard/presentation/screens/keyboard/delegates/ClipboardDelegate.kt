package kg.timmitof.keyboard.presentation.screens.keyboard.delegates

import kg.timmitof.keyboard.domain.model.ClipboardBoard
import kg.timmitof.keyboard.domain.repository.ClipboardRepository
import kg.timmitof.keyboard.presentation.screens.keyboard.KeyboardSyntax
import kg.timmitof.keyboard.presentation.screens.keyboard.states.ClipboardAction
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardSideEffect
import kotlinx.coroutines.flow.Flow

internal class ClipboardDelegate(
    private val clipboardRepository: ClipboardRepository,
) {

    val board: Flow<ClipboardBoard> = clipboardRepository.observeBoard()

    suspend fun captureSystemClip() = clipboardRepository.captureSystemClip()

    suspend fun KeyboardSyntax.applyBoard(board: ClipboardBoard) {
        if (state.clipboard == board) return

        reduce { state.copy(clipboard = board) }
    }

    suspend fun KeyboardSyntax.paste(text: String) {
        postSideEffect(KeyboardSideEffect.Input.CommitText(text))
        reduce { state.copy(keyboardOverlay = null) }
    }

    suspend fun applyAction(action: ClipboardAction) = when (action) {
        is ClipboardAction.Pin -> clipboardRepository.setPinned(action.entry.id, action.isPinned)
        is ClipboardAction.Remove -> clipboardRepository.remove(action.entry.id)
        ClipboardAction.ClearRecent -> clipboardRepository.clearRecent()
    }
}
