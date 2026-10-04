package kg.timmitof.feature_home.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kg.timmitof.core.ui.theme.appColors

/**
 * Поле пробы: фокус ставится сразу, поэтому живая клавиатура уже открыта —
 * остаётся нажимать на стили над клавишами.
 */
@Composable
internal fun TryFontsField(
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    var text by rememberSaveable { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val colors = MaterialTheme.appColors

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    val textStyle = TextStyle(
        fontSize = 19.sp,
        color = MaterialTheme.colorScheme.onBackground,
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 54.dp)
            .clip(FieldShape)
            .background(colors.card)
            .border(2.dp, colors.brand.solid, FieldShape)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        BasicTextField(
            value = text,
            onValueChange = { text = it },
            textStyle = textStyle,
            cursorBrush = SolidColor(colors.brand.solid),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
        )
        if (text.isEmpty()) {
            Text(text = placeholder, style = textStyle.copy(color = MaterialTheme.colorScheme.outline))
        }
    }
}

/**
 * Подсказка над клавиатурой: стрелка указывает вниз, на строку шрифтов —
 * первое, что стоит попробовать.
 */
@Composable
internal fun CoachMark(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme

    Box(modifier = modifier.padding(bottom = ArrowSize / 2)) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 32.dp, y = ArrowSize / 2)
                .size(ArrowSize)
                .rotate(45f)
                .clip(RoundedCornerShape(2.dp))
                .background(scheme.inverseSurface)
        )
        Column(
            modifier = Modifier
                .widthIn(max = 260.dp)
                .clip(CoachShape)
                .background(scheme.inverseSurface)
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            Text(
                text = title,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.appColors.hintOnInverse,
            )
            Text(
                modifier = Modifier.padding(top = 2.dp),
                text = body,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = scheme.inverseOnSurface,
            )
        }
    }
}

private val ArrowSize = 14.dp
private val FieldShape = RoundedCornerShape(16.dp)
private val CoachShape = RoundedCornerShape(14.dp)
