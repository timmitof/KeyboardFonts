package kg.timmitof.keyboard.integration

/**
 * Контракт для app-части: узнать состояние IME и отправить пользователя в нужный системный экран.
 *
 * Единственное место, где app-слой знает о системных настройках клавиатуры.
 */
interface KeyboardContract {

    /** Клавиатура включена в списке доступных методов ввода (шаг 1). */
    fun isKeyboardEnabled(): Boolean

    /** Клавиатура выбрана текущим методом ввода (шаг 2). */
    fun isKeyboardSelected(): Boolean

    /** Открыть системный экран «Экранная клавиатура» — там включается наша IME. */
    fun openKeyboardSettings()

    /** Показать системный диалог выбора текущей клавиатуры. */
    fun showKeyboardPicker()
}
