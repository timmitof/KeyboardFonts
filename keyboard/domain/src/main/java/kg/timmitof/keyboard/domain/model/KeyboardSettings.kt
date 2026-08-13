package kg.timmitof.keyboard.domain.model

/**
 * Переключатель настроек клавиатуры: одна строка экрана — один ключ хранилища.
 *
 * Новая настройка добавляется одной записью здесь: и хранилище, и экран
 * настроек собираются по этому списку, руками ничего дописывать не нужно.
 *
 * @property key ключ в `keyboardPreferences` — рядом с `selected_font` и `selected_language`.
 * @property default значение до первого выбора пользователя.
 * @property parent родительский переключатель: пока он выключен, дочерний не действует.
 */
enum class KeyboardToggle(
    val key: String,
    val default: Boolean,
    val parent: KeyboardToggle? = null,
) {
    /** Подсказки слов (Т9) — строка над клавишами. */
    SUGGESTIONS("t9_enabled", default = true),

    /** Исправлять опечатки при вводе пробела. */
    AUTO_CORRECT("autocorrect_enabled", default = true, parent = SUGGESTIONS),

    /** Пробел принимает выделенную подсказку. */
    SPACE_COMMITS("space_commits_suggestion", default = true, parent = SUGGESTIONS),

    /** Предсказание следующего слова, когда новое ещё не начато. */
    NEXT_WORD_PREDICTION("next_word_prediction", default = true, parent = SUGGESTIONS),

    /** Учиться на своём тексте — личный словарь. */
    LEARN_FROM_INPUT("learn_from_input", default = true, parent = SUGGESTIONS),

    /** Панель со стилизованными шрифтами над клавишами. */
    STYLED_FONTS("styled_fonts_enabled", default = true),

    /** Запоминать выбранный шрифт между сессиями ввода. */
    REMEMBER_FONT("remember_font", default = true, parent = STYLED_FONTS),

    /** Отдельная строка цифр сверху. */
    DIGITS_ROW("digits_row", default = false),

    /** Вибрация при нажатии. */
    VIBRATION("key_vibration", default = true),

    /** Звук нажатия. */
    SOUND("key_sound", default = false),

    /** Всплывающий предпросмотр клавиши. */
    KEY_PREVIEW("key_preview", default = true);
}

/** Тема клавиатуры: следовать системе или зафиксировать светлую/тёмную. */
enum class KeyboardThemeMode(val key: String) {
    AUTO("auto"),
    LIGHT("light"),
    DARK("dark");

    companion object {
        val Default = AUTO

        fun of(key: String?): KeyboardThemeMode = entries.firstOrNull { it.key == key } ?: Default
    }
}

/**
 * Снимок всех настроек клавиатуры.
 *
 * [flags] только читается — экземпляр собирается хранилищем и дальше не меняется,
 * поэтому снимок можно считать неизменяемым.
 */
data class KeyboardSettings(
    private val flags: Map<KeyboardToggle, Boolean> = emptyMap(),
    val theme: KeyboardThemeMode = KeyboardThemeMode.Default,
) {

    /** Положение самого переключателя — таким его видит пользователь на экране. */
    operator fun get(toggle: KeyboardToggle): Boolean = flags[toggle] ?: toggle.default

    /**
     * Действует ли настройка прямо сейчас.
     *
     * Выключенный родитель гасит всю ветку: дочерние переключатели остаются
     * в своём положении, но на поведение клавиатуры уже не влияют.
     */
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
}
