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
import androidx.compose.ui.graphics.luminance
import kg.timmitof.keyboard.domain.model.KeyboardSettings
import kg.timmitof.keyboard.domain.model.KeyboardThemeMode

private val keyboardDefaultLightColorScheme = KeyboardLightColor()
private val keyboardDefaultDarkColorScheme = KeyboardDarkColor()

private val LocalKeyOutlined =staticCompositionLocalOf { false }

@Immutable
data class KeyboardAppearance(
    val isDark: Boolean = false,
    val enterColor: Color? = null,
    val isKeyOutlined: Boolean = false,
)

fun KeyboardSettings.appearance(isSystemDark: Boolean) = KeyboardAppearance(
    isDark = when (theme) {
        KeyboardThemeMode.AUTO -> isSystemDark
        KeyboardThemeMode.LIGHT -> false
        KeyboardThemeMode.DARK -> true
    },
    enterColor = enterColor?.let { Color(it.toInt()) },
    isKeyOutlined = isKeyOutlineEnabled,
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun KeyboardTheme(
    appearance: KeyboardAppearance = KeyboardAppearance(isDark = isSystemInDarkTheme()),
    content: @Composable () -> Unit
) {
    val colorScheme = remember(appearance.isDark, appearance.enterColor) {
        colorSchemeOf(appearance.isDark, appearance.enterColor)
    }

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

private fun colorSchemeOf(isDark: Boolean, enterColor: Color?): KeyboardColorScheme {
    if (enterColor == null) {
        return if (isDark) keyboardDefaultDarkColorScheme else keyboardDefaultLightColorScheme
    }

    val onEnter = contentColorOn(enterColor)
    val pressed = enterColor.copy(alpha = EnterPressedAlpha)
    return if (isDark) {
        KeyboardDarkColor(
            keyEnterBackground = enterColor,
            keyEnterTextColor = onEnter,
            keyEnterPressedBackground = pressed,
        )
    } else {
        KeyboardLightColor(
            keyEnterBackground = enterColor,
            keyEnterTextColor = onEnter,
            keyEnterPressedBackground = pressed,
        )
    }
}

private const val EnterPressedAlpha = 0.8f

/** Цвет Enter выбирает пользователь, поэтому иконку подбираем по яркости фона. */
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
