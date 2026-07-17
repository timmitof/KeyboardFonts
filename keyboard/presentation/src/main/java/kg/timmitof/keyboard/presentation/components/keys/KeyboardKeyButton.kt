package kg.timmitof.keyboard.presentation.components.keys

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import kg.timmitof.keyboard.domain.model.KeyboardFont
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
    font: KeyboardFont = KeyboardFont.Default,
    onClick: (char: String) -> Unit
) {
    val symbols = remember(longPress, isUpperCase) {
        (longPress as? LongPressAction.Symbols)?.symbols
            ?.map { if (isUpperCase) it.labelUpper else it.labelLower }
            .orEmpty()
    }
    val displayLabel = remember(label, font) { font.apply(label) }
    val displaySymbols = remember(symbols, font) { symbols.map(font::apply) }

    val cellWidthPx = with(LocalDensity.current) { LongPressSymbolCellSize.toPx() }
    var isPickerVisible by remember { mutableStateOf(false) }
    var pickOffsetPx by remember { mutableFloatStateOf(0f) }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val selectedIndexFor: (Float) -> Int = { offsetPx ->
        (offsetPx / cellWidthPx).roundToInt().coerceIn(0, symbols.lastIndex)
    }

    KeyBase(
        modifier = modifier.weight(weight).fillMaxHeight(),
        background = KFTheme.color.keyButtonBackground,
        shadowColor = KFTheme.color.keyButtonShadow,
        interactionSource = interactionSource,
        customGestures = if (symbols.isEmpty()) null else { source ->
            Modifier.holdPickerClickable(
                interactionSource = source,
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
                text = font.apply(if (isUpperCase) hint.labelUpper else hint.labelLower),
                fontSize = 9.sp,
                color = KFTheme.color.keySpecialTextColor,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 2.dp, end = 3.dp)
            )
        }
        Text(
            text = displayLabel,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = KFTheme.color.keyTextColor,
            letterSpacing = 0.5.sp
        )

        when {
            isPickerVisible -> LongPressSymbolsPicker(
                symbols = displaySymbols,
                selectedIndex = { selectedIndexFor(pickOffsetPx) },
                onDismiss = { isPickerVisible = false }
            )

            isPressed -> LongPressSymbolsPicker(
                symbols = listOf(displayLabel),
                selectedIndex = { 0 },
                onDismiss = {}
            )
        }
    }
}
