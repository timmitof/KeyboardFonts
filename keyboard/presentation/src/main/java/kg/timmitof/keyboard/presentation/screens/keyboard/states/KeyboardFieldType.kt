package kg.timmitof.keyboard.presentation.screens.keyboard.states

import androidx.annotation.StringRes
import kg.timmitof.keyboard.presentation.R

/**
 * Тип поля ввода — единственный источник правды о том, чем клавиатура отличается
 * от базовой в этом поле.
 *
 * Всё, что меняется, объявлено здесь декларативно.
 *
 * @param layoutName собственная раскладка вместо буквенной (цифры, телефон).
 * @param bottomRowVariant вариант нижнего ряда из `layouts/bottom_rows.json`.
 * @param allowsFonts применять ли выбранный Unicode-шрифт к вводу.
 * @param allowsSuggestions разрешены ли подсказки слов (Т9).
 * @param allowsAutoCorrect можно ли исправлять слово при вводе пробела.
 * @param allowsLanguageSlide можно ли менять язык слайдом по пробелу.
 * @param requiresLatinLayout поле открывается на латинице, даже если выбрана другая
 * раскладка; сменить язык вручную при этом можно.
 * @param autoCapitalize поднимать ли Shift в начале ввода.
 * @param noticeRes плашка-пояснение в верхней панели.
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

    /** Раскладка типа занимает всю клавиатуру. */
    val hasOwnLayout: Boolean get() = layoutName != null
}
