package kg.timmitof.keyboard.domain.model

enum class KeyType {
    CHARACTER,
    SHIFT,
    BACKSPACE,
    SYMBOLS_SWITCH,
    SYMBOLS_ALT_SWITCH,
    ABC_SWITCH,
    EMOJI_SWITCH,
    SPACE,
    ENTER,
    SPACER;

    companion object {
        fun fromString(value: String): KeyType? =
            entries.firstOrNull { it.name == value }
    }
}

enum class KeyLongPressType {
    SYMBOLS,
    MICROPHONE;

    companion object {
        fun fromString(value: String): KeyLongPressType? =
            entries.firstOrNull { it.name == value }
    }
}