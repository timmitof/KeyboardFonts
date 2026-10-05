package kg.timmitof.core.ui.components.field

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Поле для пробы шрифтов: текст хранится локально, плейсхолдер показывает пример.
 * Внешний вид задаётся параметрами; [trailing] — необязательный хвост справа (например, кнопка).
 */
@Composable
fun ProbeTextField(
    placeholder: String,
    containerColor: Color,
    cursorColor: Color,
    textStyle: TextStyle,
    modifier: Modifier = Modifier,
    placeholderColor: Color = Color.Unspecified,
    shape: Shape = RoundedCornerShape(12.dp),
    contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp),
    borderWidth: Dp = 0.dp,
    borderColor: Color = Color.Unspecified,
    singleLine: Boolean = false,
    autoFocus: Boolean = false,
    focusRequester: FocusRequester = remember { FocusRequester() },
    trailing: (@Composable RowScope.() -> Unit)? = null,
) {
    var text by rememberSaveable { mutableStateOf("") }
    val textColor = MaterialTheme.colorScheme.onBackground
    val style = textStyle.copy(color = textColor)

    if (autoFocus) LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(containerColor)
            .then(
                if (borderWidth > 0.dp) Modifier.border(borderWidth, borderColor, shape) else Modifier
            )
            .padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.weight(1f)) {
            BasicTextField(
                value = text,
                onValueChange = { text = it },
                singleLine = singleLine,
                textStyle = style,
                cursorBrush = SolidColor(cursorColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
            )
            if (text.isEmpty()) {
                Text(
                    text = placeholder,
                    style = if (placeholderColor.isSpecified) style.copy(color = placeholderColor) else style,
                    maxLines = if (singleLine) 1 else Int.MAX_VALUE,
                )
            }
        }
        trailing?.invoke(this)
    }
}
