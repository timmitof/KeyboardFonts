package kg.timmitof.keyboard.domain.model

sealed class KeyboardKey {
    abstract val weight: Float

    abstract fun withWeight(weight: Float): KeyboardKey

    data class Character(
        override val weight: Float,
        val labelLower: String,
        val labelUpper: String,
        val subLabel: String? = null,
        val hint: String? = null,
        val output: String? = null,
        val isSpecial: Boolean = false,
        val longPress: LongPressAction? = null
    ) : KeyboardKey() {
        override fun withWeight(weight: Float) = copy(weight = weight)
    }

    data class Space(
        override val weight: Float
    ) : KeyboardKey() {
        override fun withWeight(weight: Float) = copy(weight = weight)
    }

    data class Enter(
        override val weight: Float
    ) : KeyboardKey() {
        override fun withWeight(weight: Float) = copy(weight = weight)
    }

    data class Shift(
        override val weight: Float
    ) : KeyboardKey() {
        override fun withWeight(weight: Float) = copy(weight = weight)
    }

    data class Backspace(
        override val weight: Float
    ) : KeyboardKey() {
        override fun withWeight(weight: Float) = copy(weight = weight)
    }

    data class SymbolsSwitch(
        override val weight: Float
    ) : KeyboardKey() {
        override fun withWeight(weight: Float) = copy(weight = weight)
    }

    data class SymbolsAltSwitch(
        override val weight: Float,
        val label: String
    ) : KeyboardKey() {
        override fun withWeight(weight: Float) = copy(weight = weight)
    }

    data class AbcSwitch(
        override val weight: Float
    ) : KeyboardKey() {
        override fun withWeight(weight: Float) = copy(weight = weight)
    }

    data class EmojiSwitch(
        override val weight: Float
    ) : KeyboardKey() {
        override fun withWeight(weight: Float) = copy(weight = weight)
    }

    /** Скрыть клавиатуру: на планшете системная «назад» далеко от пальцев. В JSON-раскладках её нет. */
    data class HideKeyboard(
        override val weight: Float
    ) : KeyboardKey() {
        override fun withWeight(weight: Float) = copy(weight = weight)
    }

    data class Spacer(
        override val weight: Float
    ) : KeyboardKey() {
        override fun withWeight(weight: Float) = copy(weight = weight)
    }
}