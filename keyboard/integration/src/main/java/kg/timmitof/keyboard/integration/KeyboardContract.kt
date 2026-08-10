package kg.timmitof.keyboard.integration

import kotlinx.coroutines.flow.Flow

data class KeyboardState(
    val isEnabled: Boolean = false,
    val isSelected: Boolean = false
)

/**
 * Контракт для app-части: узнать состояние IME и отправить пользователя в нужный системный экран.
 *
 * Единственное место, где app-слой знает о системных настройках клавиатуры.
 */
interface KeyboardContract {

    /** Разовое чтение состояния подключения. */
    fun getKeyboardState(): KeyboardState

    /**
     * Поток состояния подключения.
     */
    fun observeKeyboardState(): Flow<KeyboardState>

    /** Открыть системный экран «Экранная клавиатура» — там включается наша IME. */
    fun openKeyboardSettings()

    /** Показать системный диалог выбора текущей клавиатуры. */
    fun showKeyboardPicker()
}
