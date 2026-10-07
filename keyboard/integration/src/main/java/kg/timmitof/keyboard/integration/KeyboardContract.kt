package kg.timmitof.keyboard.integration

import kotlinx.coroutines.flow.Flow

data class KeyboardState(
    val isEnabled: Boolean = false,
    val isSelected: Boolean = false
)

interface KeyboardContract {

    fun getKeyboardState(): KeyboardState

    fun observeKeyboardState(): Flow<KeyboardState>

    fun openKeyboardSettings()

    fun showKeyboardPicker()
}
