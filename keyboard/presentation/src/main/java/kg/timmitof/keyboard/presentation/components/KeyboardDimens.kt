package kg.timmitof.keyboard.presentation.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

/** Базовая высота ряда — она же высота зоны нажатия клавиши (вместе с её зазорами). */
internal val KeyRowHeight = 60.dp

/**
 * Высота ряда с учётом выбранной в настройках высоты клавиатуры.
 */
internal val LocalKeyRowHeight = staticCompositionLocalOf { KeyRowHeight }

/** Видимый вертикальный зазор между рядами. */
internal val KeyRowSpacing = 8.dp

/** Видимый горизонтальный зазор между клавишами в ряду. */
internal val KeySpacing = 6.dp

/** Радиус скругления видимой части клавиши. */
internal val KeyCornerRadius = 11.dp

/** Скругление видимой части клавиши. */
internal val KeyShape = RoundedCornerShape(KeyCornerRadius)

/**
 * Толщина «опоры» под клавишей.
 */
internal val KeySupport = 1.5.dp

/** Высота верхней панели: шрифты, подсказки поля и якоря справа. */
internal val TopBarHeight = 40.dp

/** Число рядов клавиш в любом слое — по нему считается высота зоны клавиш. */
internal const val KeyRowCount = 4
