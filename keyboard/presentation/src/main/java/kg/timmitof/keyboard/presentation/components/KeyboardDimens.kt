package kg.timmitof.keyboard.presentation.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/** Высота ряда — она же высота зоны нажатия клавиши (вместе с её зазорами). */
internal val KeyRowHeight = 56.dp

/** Видимый вертикальный зазор между рядами. */
internal val KeyRowSpacing = 8.dp

/** Видимый горизонтальный зазор между клавишами в ряду. */
internal val KeySpacing = 6.dp

/** Скругление видимой части клавиши. */
internal val KeyShape = RoundedCornerShape(10.dp)
