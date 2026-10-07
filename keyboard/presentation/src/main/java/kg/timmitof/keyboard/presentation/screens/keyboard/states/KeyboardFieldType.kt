package kg.timmitof.keyboard.presentation.screens.keyboard.states

import androidx.annotation.StringRes
import kg.timmitof.keyboard.presentation.R

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
    @field:StringRes val noticeRes: Int? = null,
) {
    TEXT,

    EMAIL(
        bottomRowVariant = "email",
        allowsFonts = false,
        allowsAutoCorrect = false,
        autoCapitalize = false,
        requiresLatinLayout = true,
        noticeRes = R.string.fonts_not_available,
    ),

    PASSWORD(
        bottomRowVariant = "password",
        allowsFonts = false,
        allowsSuggestions = false,
        allowsAutoCorrect = false,
        allowsLanguageSlide = true,
        requiresLatinLayout = true,
        autoCapitalize = false,
        noticeRes = R.string.field_notice_password,
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
