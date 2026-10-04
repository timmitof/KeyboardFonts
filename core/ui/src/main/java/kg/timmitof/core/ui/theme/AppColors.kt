package kg.timmitof.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Роль цвета-помощника. У каждого цвета палитры одна роль — так цвет сам
 * подсказывает, что перед пользователем, и фиолетового на экране остаётся мало.
 */
enum class AccentRole {
    /** Фиолетовый: главные кнопки, текущий шаг, шрифты. */
    BRAND,

    /** Бирюзовый: «готово» — подключено, шаг пройден, ввод и T9. */
    SUCCESS,

    /** Янтарный: подсказки, советы, объяснения — всё, что учит, а не требует действия. */
    HINT,

    /** Розовый: внешний вид клавиатуры. */
    APPEARANCE,
}

/**
 * Тона одной роли.
 *
 * @property container мягкая заливка плашки.
 * @property onContainer текст и иконки на [container].
 * @property solid насыщенный тон: кружок иконки, номер шага.
 * @property onSolid содержимое поверх [solid].
 */
@Immutable
data class AccentTones(
    val container: Color,
    val onContainer: Color,
    val solid: Color,
    val onSolid: Color,
)

/**
 * Цвета приложения сверх Material-схемы: роли-помощники и поверхности из дизайна.
 *
 * @property card карточки со строками настроек и активный шаг.
 * @property cardMuted приглушённые плашки: поле ввода, неактивные шаги, чипы.
 * @property selected выбранный чип или сегмент — контрастный, а не фиолетовый.
 * @property onSelected содержимое поверх [selected].
 * @property hintOnInverse янтарный акцент на инверсной поверхности — заголовок подсказки-пузыря.
 */
@Immutable
data class AppColors(
    val brand: AccentTones,
    val success: AccentTones,
    val hint: AccentTones,
    val appearance: AccentTones,
    val card: Color,
    val cardMuted: Color,
    val selected: Color,
    val onSelected: Color,
    val hintOnInverse: Color,
) {
    operator fun get(role: AccentRole): AccentTones = when (role) {
        AccentRole.BRAND -> brand
        AccentRole.SUCCESS -> success
        AccentRole.HINT -> hint
        AccentRole.APPEARANCE -> appearance
    }
}

internal val AppLightColors = AppColors(
    brand = AccentTones(
        container = Color(0xFFF7D8FF),
        onContainer = Color(0xFF2D0B3D),
        solid = Color(0xFF6D4BD8),
        onSolid = Color(0xFFFFFFFF),
    ),
    success = AccentTones(
        container = Color(0xFFCDEFE7),
        onContainer = Color(0xFF00382F),
        solid = Color(0xFF0F7B6C),
        onSolid = Color(0xFFFFFFFF),
    ),
    hint = AccentTones(
        container = Color(0xFFFFE3B8),
        onContainer = Color(0xFF3D2400),
        solid = Color(0xFF8A5200),
        onSolid = Color(0xFFFFFFFF),
    ),
    appearance = AccentTones(
        container = Color(0xFFFFDAD8),
        onContainer = Color(0xFF331111),
        solid = Color(0xFF815251),
        onSolid = Color(0xFFFFFFFF),
    ),
    card = Color(0xFFFFFFFF),
    cardMuted = Color(0xFFF5EBF3),
    selected = Color(0xFF1E1A1F),
    onSelected = Color(0xFFFFFFFF),
    hintOnInverse = Color(0xFFFFC266),
)

internal val AppDarkColors = AppColors(
    brand = AccentTones(
        container = Color(0xFF3A2350),
        onContainer = Color(0xFFF7D8FF),
        solid = Color(0xFFE3B7F3),
        onSolid = Color(0xFF442254),
    ),
    success = AccentTones(
        container = Color(0xFF10473F),
        onContainer = Color(0xFFB5F0E2),
        solid = Color(0xFF7FD8C4),
        onSolid = Color(0xFF00382F),
    ),
    hint = AccentTones(
        container = Color(0xFF3A2900),
        onContainer = Color(0xFFFFDDA8),
        solid = Color(0xFFFFC266),
        onSolid = Color(0xFF3D2400),
    ),
    appearance = AccentTones(
        container = Color(0xFF4E2C2B),
        onContainer = Color(0xFFFFDAD8),
        solid = Color(0xFFF5B7B5),
        onSolid = Color(0xFF4C2525),
    ),
    card = Color(0xFF231E23),
    cardMuted = Color(0xFF2D282E),
    selected = Color(0xFFE9E0E7),
    onSelected = Color(0xFF1E1A1F),
    hintOnInverse = Color(0xFF8A5200),
)

val LocalAppColors = staticCompositionLocalOf { AppLightColors }

/** Цвета приложения сверх Material-схемы — рядом с привычным `colorScheme`. */
val MaterialTheme.appColors: AppColors
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current
