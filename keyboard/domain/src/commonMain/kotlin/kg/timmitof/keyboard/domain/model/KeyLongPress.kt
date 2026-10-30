package kg.timmitof.keyboard.domain.model

sealed class LongPressAction {
    data class Symbols(val symbols: List<KeyCharacter>) : LongPressAction()
    data object Microphone : LongPressAction()
}

/** Регистр выбирается при вводе, а не при отрисовке: иначе при нескольких одновременных нажатиях все буквы шли бы в старом регистре. */
data class KeyCharacter(
    val labelLower: String,
    val labelUpper: String = labelLower
) {
    fun text(isUpperCase: Boolean): String = if (isUpperCase) labelUpper else labelLower
}
