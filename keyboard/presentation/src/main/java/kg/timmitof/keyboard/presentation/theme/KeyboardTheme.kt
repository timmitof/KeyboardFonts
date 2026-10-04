package kg.timmitof.keyboard.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import kg.timmitof.keyboard.domain.model.KeyColorTarget
import kg.timmitof.keyboard.domain.model.KeyboardSettings
import kg.timmitof.keyboard.domain.model.KeyboardThemeMode

private val keyboardDefaultLightColorScheme = KeyboardLightColor()
private val keyboardDefaultDarkColorScheme = KeyboardDarkColor()

private val LocalKeyOutlined = staticCompositionLocalOf { false }

/** `null` у цвета — берётся из темы. */
@Immutable
data class KeyboardAppearance(
    val isDark: Boolean = false,
    val keyColor: Color? = null,
    val specialKeyColor: Color? = null,
    val enterColor: Color? = null,
    val isKeyOutlined: Boolean = false,
)

/** Цвет пользователя главнее палитры фона, палитра — главнее темы. */
fun KeyboardSettings.appearance(isSystemDark: Boolean): KeyboardAppearance {
    val palette = background.palette()
    val special = specialKeyColor?.toColor() ?: palette?.special

    return KeyboardAppearance(
        isDark = palette?.isDark ?: when (theme) {
            KeyboardThemeMode.AUTO -> isSystemDark
            KeyboardThemeMode.LIGHT -> false
            KeyboardThemeMode.DARK -> true
        },
        keyColor = keyColor?.toColor() ?: palette?.key,
        specialKeyColor = special,
        enterColor = enterColor?.toColor() ?: palette?.enter ?: special,
        isKeyOutlined = isKeyOutlineEnabled,
    )
}

/** Цвет, который получит клавиша при «Авто»: его показывает первый кружок в настройках. */
fun KeyboardSettings.autoKeyColor(target: KeyColorTarget, isSystemDark: Boolean): Color {
    val scheme = colorSchemeOf(copy(keyColor = null, specialKeyColor = null, enterColor = null).appearance(isSystemDark))
    return when (target) {
        KeyColorTarget.KEY -> scheme.keyButtonBackground
        KeyColorTarget.SPECIAL -> scheme.keySpecialButtonBackground
        KeyColorTarget.ENTER -> scheme.keyEnterBackground
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun KeyboardTheme(
    appearance: KeyboardAppearance = KeyboardAppearance(isDark = isSystemInDarkTheme()),
    content: @Composable () -> Unit
) {
    val colorScheme = remember(appearance) { colorSchemeOf(appearance) }

    CompositionLocalProvider(
        LocalKeyboardColorScheme provides colorScheme,
        LocalKeyOutlined provides appearance.isKeyOutlined,
    ) {
        MaterialTheme(
            motionScheme = MotionScheme.expressive(),
            content = content
        )
    }
}

/** Цвета клавиатуры вне её композиции — для миниатюр и рамок в приложении. */
fun KeyboardAppearance.colorScheme(): KeyboardColorScheme = colorSchemeOf(this)

private fun colorSchemeOf(appearance: KeyboardAppearance): KeyboardColorScheme {
    val base = if (appearance.isDark) keyboardDefaultDarkColorScheme else keyboardDefaultLightColorScheme
    val hasCustomKeys = appearance.keyColor != null ||
        appearance.specialKeyColor != null ||
        appearance.enterColor != null
    if (!hasCustomKeys) return base

    return CustomKeyColors(
        base = base,
        key = appearance.keyColor,
        special = appearance.specialKeyColor,
        enter = appearance.enterColor,
    )
}

/** Подменяет только клавиши: текст на самом фоне (шрифты, подсказки) остаётся от основы темы. */
private class CustomKeyColors(
    base: KeyboardColorScheme,
    key: Color?,
    special: Color?,
    enter: Color?,
) : KeyboardColorScheme by base {
    override val keyButtonBackground = key ?: base.keyButtonBackground
    override val keyLabelColor = key?.let(::contentColorOn) ?: base.keyLabelColor
    override val keyButtonPressedBackground = key?.let { lerp(it, keyLabelColor, PressedTint) }
        ?: base.keyButtonPressedBackground
    override val keySpecialButtonBackground = special ?: base.keySpecialButtonBackground
    override val keySpecialLabelColor = special?.let(::contentColorOn) ?: base.keySpecialLabelColor
    override val keyEnterBackground = enter ?: base.keyEnterBackground
    override val keyEnterTextColor = enter?.let(::contentColorOn) ?: base.keyEnterTextColor
    override val keyEnterPressedBackground = enter?.copy(alpha = EnterPressedAlpha) ?: base.keyEnterPressedBackground
}

private fun Long.toColor() = Color(toInt())

private const val EnterPressedAlpha = 0.8f
private const val PressedTint = 0.14f

/** Цвет клавиш выбирает пользователь, поэтому подпись подбираем по яркости фона клавиши. */
fun contentColorOn(background: Color): Color =
    if (background.luminance() > 0.5f) Color(0xFF1B1D21) else Color.White

object KFTheme {

    val color: KeyboardColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalKeyboardColorScheme.current

    val isKeyOutlined: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalKeyOutlined.current
}
