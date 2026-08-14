package kg.timmitof.keyboard.domain.model

sealed class LongPressAction {
    data class Symbols(val symbols: List<KeyCharacter>) : LongPressAction()
    data object Microphone : LongPressAction()
}

/**
 * Символ клавиши в обоих регистрах.
 *
 * Регистр выбирается в момент ввода, а не при отрисовке клавиши: иначе при
 * нескольких одновременных нажатиях все клавиши отдали бы регистр, который
 * был на экране до первого нажатия.
 */
data class KeyCharacter(
    val labelLower: String,
    val labelUpper: String = labelLower
) {
    fun text(isUpperCase: Boolean): String = if (isUpperCase) labelUpper else labelLower
}
