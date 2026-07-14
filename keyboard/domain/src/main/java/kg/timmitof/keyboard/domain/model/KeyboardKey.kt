package kg.timmitof.keyboard.domain.model

sealed class KeyboardKey {
    abstract val weight: Float

    data class Character(
        override val weight: Float,
        val labelLower: String,
        val labelUpper: String,
        val longPress: LongPressAction? = null
    ) : KeyboardKey()

    data class Space(
        override val weight: Float
    ) : KeyboardKey()

    data class Enter(
        override val weight: Float
    ) : KeyboardKey()

    data class Shift(
        override val weight: Float
    ) : KeyboardKey()

    data class Backspace(
        override val weight: Float
    ) : KeyboardKey()

    data class SymbolsSwitch(
        override val weight: Float
    ) : KeyboardKey()

    data class SymbolsAltSwitch(
        override val weight: Float,
        val label: String
    ) : KeyboardKey()

    data class AbcSwitch(
        override val weight: Float
    ) : KeyboardKey()

    data class EmojiSwitch(
        override val weight: Float
    ) : KeyboardKey()
}