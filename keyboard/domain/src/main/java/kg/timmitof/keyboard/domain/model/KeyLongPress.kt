package kg.timmitof.keyboard.domain.model

sealed class LongPressAction {
    data class Symbols(val symbols: List<LongPressCharacter>) : LongPressAction()
    data object Microphone : LongPressAction()
}

data class LongPressCharacter(
    val labelLower: String,
    val labelUpper: String = labelLower
)