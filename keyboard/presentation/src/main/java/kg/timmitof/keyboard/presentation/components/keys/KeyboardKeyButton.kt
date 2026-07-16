package kg.timmitof.keyboard.presentation.components.keys

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kg.timmitof.keyboard.domain.model.LongPressAction
import kg.timmitof.keyboard.presentation.theme.KFTheme

@Composable
internal fun RowScope.KeyboardKeyButton(
    modifier: Modifier = Modifier,
    label: String,
    weight: Float,
    longPress: LongPressAction? = null,
    onClick: (char: String) -> Unit
) {
    KeyBase(
        modifier = modifier.weight(weight).fillMaxHeight(),
        background = KFTheme.color.keyButtonBackground,
        shadowColor = KFTheme.color.keyButtonShadow,
        onClick = { onClick(label) }
    ) {
        if (longPress is LongPressAction.Symbols) {
            Text(
                text = longPress.symbols.first(),
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
    }
}