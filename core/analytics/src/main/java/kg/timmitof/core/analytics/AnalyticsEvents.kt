package kg.timmitof.core.analytics

/**
 * Все события приложения и клавиатуры — в одном месте, чтобы список совпадал с отчётами в Firebase.
 * Значения параметров — ключи настроек и идентификаторы, а не подписи: подписи переводятся и меняются.
 */
object AnalyticsEvents {

    fun screenView(screen: String) = event("screen_view", "screen_name" to screen)

    object Onboarding {
        /** [step] — enable или select. */
        fun stepClick(step: String) = event("onboarding_step_click", "step" to step)

        /** Шаг выполнен в системных настройках: клавиатура включена или выбрана. */
        fun stepDone(step: String) = event("onboarding_step_done", "step" to step)

        fun finish(isFromSettings: Boolean) = event("onboarding_finish", "from_settings" to isFromSettings)
    }

    object Settings {
        fun connectKeyboardClick() = event("keyboard_connect_click")

        fun tabOpen(tab: String) = event("studio_tab_open", "tab" to tab)

        fun toggle(setting: String, isEnabled: Boolean) =
            event("settings_toggle", "setting" to setting, "enabled" to isEnabled)

        fun theme(theme: String) = event("settings_theme", "option" to theme)

        fun height(height: String) = event("settings_height", "option" to height)

        /** [isCustom] — свой цвет, иначе «Авто». */
        fun keyColor(target: String, isCustom: Boolean) =
            event("settings_key_color", "target" to target, "custom" to isCustom)

        /** [type] — none, solid, pattern, photo; [value] — ключ узора (параметр option), для остальных пусто. */
        fun background(type: String, value: String) =
            event("settings_background", "type" to type, "option" to value)

        fun photoAdd() = event("settings_photo_add")

        fun soundPack(pack: String) = event("settings_sound_pack", "option" to pack)

        /** Громкость в процентах, с шагом 10. */
        fun soundVolume(percent: Int) = event("settings_sound_volume", "volume" to percent)

        fun fontPanel(count: Int) = event("settings_font_panel", "count" to count)

        fun fontPanelReset() = event("settings_font_panel_reset")

        fun languages(codes: List<String>) =
            event("settings_languages", "count" to codes.size, "languages" to codes.joinToString(","))

        fun clipboardClear() = event("settings_clipboard_clear")
    }

    object Keyboard {
        fun fontSelect(font: String) = event("keyboard_font_select", "font" to font)

        fun fontsOpen() = event("keyboard_fonts_open")

        fun languageSwitch(language: String) = event("keyboard_language_switch", "language" to language)

        fun emojiOpen() = event("keyboard_emoji_open")

        fun emojiSearch() = event("keyboard_emoji_search")

        /** [panel] — clipboard или quick_settings. */
        fun panelOpen(panel: String) = event("keyboard_panel_open", "panel" to panel)

        fun clipboardPaste(isPinned: Boolean) = event("keyboard_clipboard_paste", "pinned" to isPinned)

        fun quickSetting(setting: String, value: String) =
            event("keyboard_quick_setting", "setting" to setting, "option" to value)

        fun openApp() = event("keyboard_open_app")

        /** Итог одной сессии ввода (одно поле): только счётчики, сам текст не передаётся. */
        fun session(
            chars: Int,
            styledChars: Int,
            words: Int,
            suggestionsPicked: Int,
            autoCorrections: Int,
            autoCorrectionsUndone: Int,
            emojis: Int,
            clipboardPastes: Int,
            language: String,
            font: String,
            fieldType: String,
        ) = event(
            "keyboard_session",
            "chars" to chars,
            "styled_chars" to styledChars,
            "words" to words,
            "suggestions_picked" to suggestionsPicked,
            "autocorrections" to autoCorrections,
            "autocorrections_undone" to autoCorrectionsUndone,
            "emojis" to emojis,
            "clipboard_pastes" to clipboardPastes,
            "language" to language,
            "font" to font,
            "field_type" to fieldType,
        )
    }

    private fun event(name: String, vararg params: Pair<String, Any>) = AnalyticsEvent(name, params.toMap())
}

/** Свойства пользователя: последнее известное состояние настроек, по ним строятся срезы аудитории. */
object UserProperties {
    const val THEME = "kb_theme"
    const val HEIGHT = "kb_height"
    const val BACKGROUND = "kb_background"
    const val KEY_COLORS = "kb_key_colors"
    const val SOUND = "kb_sound"
    const val VIBRATION = "kb_vibration"
    const val SUGGESTIONS = "kb_suggestions"
    const val AUTO_CORRECT = "kb_autocorrect"
    const val LEARNING = "kb_learning"
    const val FONTS_PANEL = "kb_fonts_panel"
    const val DIGITS_ROW = "kb_digits_row"
    const val KEY_PREVIEW = "kb_key_preview"
    const val KEY_OUTLINE = "kb_key_outline"
    const val LANGUAGES = "kb_languages"
    const val LANGUAGE_COUNT = "kb_language_count"
    const val MAIN_LANGUAGE = "kb_main_language"
}
