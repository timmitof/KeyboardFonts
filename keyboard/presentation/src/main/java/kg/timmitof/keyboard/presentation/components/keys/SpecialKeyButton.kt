package kg.timmitof.keyboard.presentation.components.keys

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kg.timmitof.keyboard.presentation.theme.KFTheme

@Composable
internal fun RowScope.SpecialKeyButton(
    label: String,
    weight: Float,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    KeyBase(
        modifier = modifier.weight(weight).fillMaxHeight(),
        background = KFTheme.color.keySpecialButtonBackground,
        shadowColor = KFTheme.color.keyButtonShadow,
        onClick = onClick
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = KFTheme.color.keySpecialTextColor
        )
    }
}