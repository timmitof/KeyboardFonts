package kg.timmitof.keyboard.presentation.components.keys

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kg.timmitof.core.ui.holdPickerClickable
import kg.timmitof.keyboard.domain.model.KeyboardFont
import kg.timmitof.keyboard.domain.model.LongPressAction
import kg.timmitof.keyboard.presentation.theme.KFTheme
import kotlin.math.roundToInt

/** Кегль основной метки: одиночный символ и слово вроде `.com` или `пауза`. */
private val SingleGlyphSize = 23.sp
private val WordLabelSize = 15.sp

/**
 * Кегль меток телефонной раскладки.
 */
private val LargeLabelSize = 26.sp
private val LargeWordLabelSize = 19.sp
private val SubLabelSize = 11.5.sp

/** Зазор между меткой и подписью. */
private val SubLabelSpacing = 4.dp

/** Отступ пары цифра + буквы от краёв клавиши. */
private val KeyLabelPadding = 6.dp

/** Подпись приглушена — она подсказка, а не то, что вводится. */
private const val SubLabelAlpha = 0.5f

/** Кегль подсказки в углу клавиши. */
private val HintSize = 10.sp

internal fun String.isWordLabel(): Boolean = codePointCount(0, length) > 1

@Composable
internal fun RowScope.KeyboardKeyButton(
    modifier: Modifier = Modifier,
    label: String,
    isUpperCase: Boolean = false,
    weight: Float,
    subLabel: String? = null,
    hint: String? = null,
    isLargeLabel: Boolean = false,
    hasSubLabels: Boolean = false,
    output: String? = null,
    isSpecial: Boolean = false,
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
    val typed = output ?: label

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
        background = if (isSpecial) {
            KFTheme.color.keySpecialButtonBackground
        } else {
            KFTheme.color.keyButtonBackground
        },
        shadowColor = KFTheme.color.keyButtonShadow,
        interactionSource = interactionSource,
        customGestures = if (symbols.isEmpty()) null else { source ->
            Modifier.holdPickerClickable(
                interactionSource = source,
                onTap = { onClick(typed) },
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
        onClick = { onClick(typed) }
    ) {
        hint?.let { KeyHint(hint = it) }

        KeyLabel(
            label = displayLabel,
            subLabel = subLabel ?: "".takeIf { hasSubLabels && !isSpecial },
            isLarge = isLargeLabel,
            color = if (isSpecial) KFTheme.color.keySpecialTextColor else KFTheme.color.keyTextColor
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

@Composable
private fun KeyLabel(
    label: String,
    subLabel: String?,
    isLarge: Boolean,
    color: Color,
) {
    if (subLabel == null) {
        Text(
            text = label,
            fontSize = remember(label, isLarge) { label.labelFontSize(isLarge) },
            fontWeight = FontWeight.Medium,
            color = color,
            maxLines = 1,
            letterSpacing = 0.5.sp
        )
        return
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = KeyLabelPadding),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.weight(1f).alignByBaseline(),
            text = label,
            fontSize = remember(label, isLarge) { label.labelFontSize(isLarge) },
            fontWeight = FontWeight.Medium,
            color = color,
            textAlign = TextAlign.End,
            maxLines = 1,
            softWrap = false,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.width(SubLabelSpacing))
        Text(
            modifier = Modifier.weight(1f).alignByBaseline(),
            text = subLabel,
            fontSize = SubLabelSize,
            fontWeight = FontWeight.Medium,
            color = color.copy(alpha = SubLabelAlpha),
            textAlign = TextAlign.Start,
            maxLines = 1,
            softWrap = false,
            letterSpacing = 0.3.sp
        )
    }
}

@Composable
private fun BoxScope.KeyHint(hint: String) {
    Text(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(top = 3.dp, end = 3.dp),
        text = hint,
        fontSize = HintSize,
        color = KFTheme.color.keySpecialTextColor,
        style = TextStyle(
            platformStyle = PlatformTextStyle(includeFontPadding = false),
            lineHeight = HintSize,
            lineHeightStyle = LineHeightStyle(
                alignment = LineHeightStyle.Alignment.Top,
                trim = LineHeightStyle.Trim.Both
            )
        )
    )
}

private fun String.labelFontSize(large: Boolean = false): TextUnit = when {
    isWordLabel() -> if (large) LargeWordLabelSize else WordLabelSize
    large -> LargeLabelSize
    else -> SingleGlyphSize
}
