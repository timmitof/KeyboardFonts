package kg.timmitof.keyboard.presentation.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/** Высота ряда — она же высота зоны нажатия клавиши (вместе с её зазорами). */
internal val KeyRowHeight = 60.dp

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

/** Высота верхней панели: шрифты, подсказки поля и кнопка «свернуть». */
internal val TopBarHeight = 40.dp

/** Высота зоны клавиш: 4 ряда вплотную — промежутки входят в высоту самих рядов. */
internal val KeyboardBodyHeight = KeyRowHeight * 4

/**
 * Полная высота содержимого клавиатуры.
 */
internal val KeyboardContentHeight = TopBarHeight + KeyRowSpacing / 2 + KeyboardBodyHeight
