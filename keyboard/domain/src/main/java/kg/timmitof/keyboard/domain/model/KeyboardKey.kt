package kg.timmitof.keyboard.domain.model

sealed class KeyboardKey {
    abstract val weight: Float

    /** Копия клавиши с другим весом — нужна для нормировки ряда под ширину раскладки. */
    abstract fun withWeight(weight: Float): KeyboardKey

    /**
     * Обычная клавиша.
     *
     * @param subLabel подпись рядом с основной меткой (буквы возле цифр на телефонной раскладке).
     * @param hint мелкая подсказка в верхнем углу клавиши (цифры над буквами верхнего ряда).
     * @param output что реально вводится, если это не сама метка (клавиша «пауза» вводит «,»).
     * @param isSpecial клавиша оформляется как служебная (серый фон), хотя вводит символ.
     */
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

    data class Spacer(
        override val weight: Float
    ) : KeyboardKey() {
        override fun withWeight(weight: Float) = copy(weight = weight)
    }
}