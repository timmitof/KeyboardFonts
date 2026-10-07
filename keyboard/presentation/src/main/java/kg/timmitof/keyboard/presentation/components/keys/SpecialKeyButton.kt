package kg.timmitof.keyboard.presentation.components.keys

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kg.timmitof.keyboard.presentation.theme.KFTheme

@Composable
internal fun RowScope.SpecialKeyButton(
    label: String,
    weight: Float,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    SpecialKeyBase(weight = weight, modifier = modifier, onClick = onClick) {
        Text(
            text = label,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Medium,
            color = KFTheme.color.keySpecialLabelColor
        )
    }
}

@Composable
internal fun RowScope.SpecialIconKeyButton(
    @DrawableRes iconRes: Int,
    contentDescription: String,
    weight: Float,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    SpecialKeyBase(weight = weight, modifier = modifier, onClick = onClick) {
        Icon(
            imageVector = ImageVector.vectorResource(iconRes),
            contentDescription = contentDescription,
            tint = KFTheme.color.keySpecialLabelColor,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun RowScope.SpecialKeyBase(
    weight: Float,
    modifier: Modifier,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    KeyBase(
        modifier = modifier.weight(weight).fillMaxHeight(),
        background = KFTheme.color.keySpecialButtonBackground,
        shadowColor = KFTheme.color.keyButtonShadow,
        onClick = onClick,
        content = content
    )
}
