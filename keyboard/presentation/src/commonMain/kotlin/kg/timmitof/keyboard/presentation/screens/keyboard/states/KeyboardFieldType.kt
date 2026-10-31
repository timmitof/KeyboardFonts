package kg.timmitof.keyboard.presentation.screens.keyboard.states

import dev.icerock.moko.resources.StringResource
import kg.timmitof.core.resources.MR
import kg.timmitof.core.resources.field_notice_password
import kg.timmitof.core.resources.fonts_not_available

/**
 * Единственный источник правды о том, чем клавиатура в поле отличается от базовой.
 *
 * @param requiresLatinLayout поле открывается на латинице, даже если выбрана другая раскладка; язык можно сменить вручную.
 */
enum class KeyboardFieldType(
    val layoutName: String? = null,
    val bottomRowVariant: String? = null,
    val allowsFonts: Boolean = true,
    val allowsSuggestions: Boolean = true,
    val allowsAutoCorrect: Boolean = true,
    val allowsLanguageSlide: Boolean = true,
    val requiresLatinLayout: Boolean = false,
    val autoCapitalize: Boolean = true,
    val noticeRes: StringResource? = null,
) {
    TEXT,

    EMAIL(
        bottomRowVariant = "email",
        allowsFonts = false,
        allowsAutoCorrect = false,
        autoCapitalize = false,
        requiresLatinLayout = true,
        noticeRes = MR.strings.fonts_not_available,
    ),

    PASSWORD(
        bottomRowVariant = "password",
        allowsFonts = false,
        allowsSuggestions = false,
        allowsAutoCorrect = false,
        allowsLanguageSlide = true,
        requiresLatinLayout = true,
        autoCapitalize = false,
        noticeRes = MR.strings.field_notice_password,
    ),

    NUMBER(
        layoutName = "numeric",
        allowsFonts = false,
        allowsSuggestions = false,
        allowsAutoCorrect = false,
        allowsLanguageSlide = false,
        autoCapitalize = false,
    ),

    PHONE(
        layoutName = "phone",
        allowsFonts = false,
        allowsSuggestions = false,
        allowsAutoCorrect = false,
        allowsLanguageSlide = false,
        autoCapitalize = false,
    );

    val hasOwnLayout: Boolean get() = layoutName != null
}
