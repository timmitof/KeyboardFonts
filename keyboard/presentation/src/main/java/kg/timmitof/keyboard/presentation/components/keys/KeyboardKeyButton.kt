package kg.timmitof.keyboard.presentation.components.keys

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kg.timmitof.core.ui.holdPickerClickable
import kg.timmitof.keyboard.domain.model.LongPressAction
import kg.timmitof.keyboard.presentation.theme.KFTheme
import kotlin.math.roundToInt

@Composable
internal fun RowScope.KeyboardKeyButton(
    modifier: Modifier = Modifier,
    label: String,
    isUpperCase: Boolean = false,
    weight: Float,
    longPress: LongPressAction? = null,
    onClick: (char: String) -> Unit
) {
    val symbols = remember(longPress, isUpperCase) {
        (longPress as? LongPressAction.Symbols)?.symbols
            ?.map { if (isUpperCase) it.labelUpper else it.labelLower }
            .orEmpty()
    }

    val cellWidthPx = with(LocalDensity.current) { LongPressSymbolCellSize.toPx() }
    var isPickerVisible by remember { mutableStateOf(false) }
    var pickOffsetPx by remember { mutableFloatStateOf(0f) }

    val selectedIndexFor: (Float) -> Int = { offsetPx ->
        (offsetPx / cellWidthPx).roundToInt().coerceIn(0, symbols.lastIndex)
    }

    KeyBase(
        modifier = modifier.weight(weight).fillMaxHeight(),
        background = KFTheme.color.keyButtonBackground,
        shadowColor = KFTheme.color.keyButtonShadow,
        customGestures = if (symbols.isEmpty()) null else { interactionSource ->
            Modifier.holdPickerClickable(
                interactionSource = interactionSource,
                onTap = { onClick(label) },
                onHoldStart = {
                    pickOffsetPx = 0f
                    isPickerVisible = true
                },
                onPickChange = { pickOffsetPx = it },
                onPickFinish = { offsetPx ->
                    isPickerVisible = false
                    onClick(symbols[selectedIndexFor(offsetPx)])
                }
            )
        },
        onClick = { onClick(label) }
    ) {
        if (longPress is LongPressAction.Symbols) {
            val hint = longPress.symbols.first()
            Text(
                text = if (isUpperCase) hint.labelUpper else hint.labelLower,
                fontSize = 9.sp,
                color = KFTheme.color.keySpecialTextColor,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 2.dp, end = 3.dp)
            )
        }
        Text(
            text = label,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = KFTheme.color.keyTextColor,
            letterSpacing = 0.5.sp
        )

        if (isPickerVisible) {
            LongPressSymbolsPicker(
                symbols = symbols,
                selectedIndex = { selectedIndexFor(pickOffsetPx) },
                onDismiss = { isPickerVisible = false }
            )
        }
    }
}
