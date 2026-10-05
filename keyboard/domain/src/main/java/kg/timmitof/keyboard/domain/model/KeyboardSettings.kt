package kg.timmitof.keyboard.domain.model

/** Новая настройка — одна запись здесь: хранилище и экран настроек собираются по списку. [parent] выключен — дочерний не действует. */
enum class KeyboardToggle(
    val key: String,
    val default: Boolean,
    val parent: KeyboardToggle? = null,
) {
    SUGGESTIONS("t9_enabled", default = true),

    AUTO_CORRECT("autocorrect_enabled", default = true, parent = SUGGESTIONS),

    SPACE_COMMITS("space_commits_suggestion", default = false, parent = SUGGESTIONS),

    NEXT_WORD_PREDICTION("next_word_prediction", default = true, parent = SUGGESTIONS),

    LEARN_FROM_INPUT("learn_from_input", default = false, parent = SUGGESTIONS),

    STYLED_FONTS("styled_fonts_enabled", default = true),

    REMEMBER_FONT("remember_font", default = true, parent = STYLED_FONTS),

    DIGITS_ROW("digits_row", default = false),

    VIBRATION("key_vibration", default = true),

    SOUND("key_sound", default = false),

    KEY_PREVIEW("key_preview", default = true),

    KEY_OUTLINE("key_outline", default = false),

    /** Действует только в альбомном режиме и на планшете. */
    SPLIT_KEYBOARD("split_keyboard", default = true);
}

enum class KeyboardThemeMode(val key: String) {
    AUTO("auto"),
    LIGHT("light"),
    DARK("dark");

    companion object {
        val Default = AUTO

        fun of(key: String?): KeyboardThemeMode = entries.firstOrNull { it.key == key } ?: Default
    }
}

enum class KeyboardHeight(val key: String, val scale: Float) {
    S("s", 0.86f),
    M("m", 1f),
    L("l", 1.14f),
    XL("xl", 1.28f);

    companion object {
        val Default = M

        fun of(key: String?): KeyboardHeight = entries.firstOrNull { it.key == key } ?: Default
    }
}

enum class KeyboardSoundPack(val key: String) {
    SYSTEM("system"),
    MECHANICAL("mechanical"),
    BUBBLE("bubble"),
    TYPEWRITER("typewriter");

    companion object {
        val Default = SYSTEM

        fun of(key: String?): KeyboardSoundPack = entries.firstOrNull { it.key == key } ?: Default
    }
}

enum class KeyColorTarget { KEY, SPECIAL, ENTER }

data class KeyboardSettings(
    private val flags: Map<KeyboardToggle, Boolean> = emptyMap(),
    val theme: KeyboardThemeMode = KeyboardThemeMode.Default,
    val height: KeyboardHeight = KeyboardHeight.Default,
    /** Цвета клавиш, ARGB; `null` — «Авто»: из палитры фона, а без фона — из темы. */
    val keyColor: Long? = null,
    val specialKeyColor: Long? = null,
    val enterColor: Long? = null,
    val soundPack: KeyboardSoundPack = KeyboardSoundPack.Default,
    /** 0..1, доля от системной громкости. */
    val soundVolume: Float = DEFAULT_SOUND_VOLUME,
    val background: KeyboardBackground = KeyboardBackground.Default,
) {

    fun keyColor(target: KeyColorTarget): Long? = when (target) {
        KeyColorTarget.KEY -> keyColor
        KeyColorTarget.SPECIAL -> specialKeyColor
        KeyColorTarget.ENTER -> enterColor
    }

    operator fun get(toggle: KeyboardToggle): Boolean = flags[toggle] ?: toggle.default

    /** Выключенный родитель гасит всю ветку, хотя дочерние сохраняют своё положение. */
    fun isOn(toggle: KeyboardToggle): Boolean =
        get(toggle) && toggle.parent?.let(::isOn) != false

    val isSuggestionsEnabled: Boolean get() = isOn(KeyboardToggle.SUGGESTIONS)

    val isAutoCorrectEnabled: Boolean get() = isOn(KeyboardToggle.AUTO_CORRECT)

    val isSpaceCommitsEnabled: Boolean get() = isOn(KeyboardToggle.SPACE_COMMITS)

    val isNextWordPredictionEnabled: Boolean get() = isOn(KeyboardToggle.NEXT_WORD_PREDICTION)

    val isLearningEnabled: Boolean get() = isOn(KeyboardToggle.LEARN_FROM_INPUT)

    val isFontsPanelEnabled: Boolean get() = isOn(KeyboardToggle.STYLED_FONTS)

    val isFontRemembered: Boolean get() = isOn(KeyboardToggle.REMEMBER_FONT)

    val isDigitsRowEnabled: Boolean get() = isOn(KeyboardToggle.DIGITS_ROW)

    val isVibrationEnabled: Boolean get() = isOn(KeyboardToggle.VIBRATION)

    val isSoundEnabled: Boolean get() = isOn(KeyboardToggle.SOUND)

    val isKeyPreviewEnabled: Boolean get() = isOn(KeyboardToggle.KEY_PREVIEW)

    val isKeyOutlineEnabled: Boolean get() = isOn(KeyboardToggle.KEY_OUTLINE)

    val isSplitEnabled: Boolean get() = isOn(KeyboardToggle.SPLIT_KEYBOARD)

    companion object {
        const val DEFAULT_SOUND_VOLUME = 0.6f
    }
}
