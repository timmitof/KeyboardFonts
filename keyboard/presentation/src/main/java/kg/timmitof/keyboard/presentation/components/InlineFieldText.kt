package kg.timmitof.keyboard.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kg.timmitof.keyboard.presentation.theme.KFTheme
import kotlinx.coroutines.delay

/**
 * Текст поля, которое живёт внутри клавиатуры (поиск эмодзи, панель перевода): курсор всегда в конце,
 * длинный текст обрезается слева — видно то, что набираешь. Пустое поле показывает [hint].
 *
 * @param selectionChars сколько символов в конце выделено (Backspace со слайдом).
 */
@Composable
internal fun InlineFieldText(
    text: String,
    hint: String,
    modifier: Modifier = Modifier,
    selectionChars: Int = 0,
) {
    val textColor = KFTheme.color.keyLabelColor
    val hasSelection = selectionChars in 1..text.length

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (text.isEmpty()) {
            BlinkingCaret(color = textColor, resetKey = text)
            Text(
                text = hint,
                fontSize = 14.sp,
                color = textColor.copy(alpha = HintAlpha),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            return@Row
        }

        Text(
            modifier = Modifier.weight(1f, fill = false),
            text = if (hasSelection) {
                buildAnnotatedString {
                    append(text.substring(0, text.length - selectionChars))
                    withStyle(SpanStyle(background = textColor.copy(alpha = 0.25f))) {
                        append(text.substring(text.length - selectionChars))
                    }
                }
            } else {
                AnnotatedString(text)
            },
            fontSize = 14.sp,
            color = textColor,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.StartEllipsis
        )
        if (!hasSelection) {
            BlinkingCaret(color = textColor, resetKey = text)
        }
    }
}

/** Мигание читается только при отрисовке: курсор не рекомпозирует поле дважды в секунду. */
@Composable
private fun BlinkingCaret(
    color: Color,
    resetKey: String,
    modifier: Modifier = Modifier,
) {
    var visible by remember { mutableStateOf(true) }

    LaunchedEffect(resetKey) {
        visible = true
        while (true) {
            delay(CaretBlinkMillis)
            visible = !visible
        }
    }

    Box(
        modifier = modifier
            .padding(horizontal = 1.dp)
            .width(2.dp)
            .height(18.dp)
            .drawBehind { if (visible) drawRect(color) }
    )
}

private const val CaretBlinkMillis = 500L

private const val HintAlpha = 0.6f
