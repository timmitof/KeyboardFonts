package kg.timmitof.keyboard.presentation.components.topbar

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kg.timmitof.keyboard.presentation.R
import kg.timmitof.keyboard.presentation.components.KeySupport
import kg.timmitof.keyboard.presentation.theme.KFTheme

/** Высота элементов верхней панели. */
private val ControlHeight = 32.dp

private val ControlShape = RoundedCornerShape(ControlHeight / 2)

/**
 * Свёрнутая карусель шрифтов: кнопка с текущим стилем.
 */
@Composable
internal fun FontToggleButton(
    preview: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(ControlHeight)
            .clip(ControlShape)
            .pillSurface()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = preview,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = KFTheme.color.keyTextColor,
            maxLines = 1
        )
        Text(
            text = "▾",
            fontSize = 11.sp,
            color = KFTheme.color.keyTextColor.copy(alpha = 0.5f)
        )
    }
}

/** Круглая кнопка-иконка панели: свернуть клавиатуру, закрыть карусель. */
@Composable
internal fun TopBarIconButton(
    @DrawableRes iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(ControlHeight)
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(iconRes),
            contentDescription = contentDescription,
            tint = KFTheme.color.keySpecialTextColor,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
internal fun NoticePill(
    @StringRes textRes: Int,
    modifier: Modifier = Modifier,
) {
    val isRestriction = textRes != R.string.field_notice_multiline

    Row(
        modifier = modifier
            .height(26.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(KFTheme.color.noticeBackground)
            .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isRestriction) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_field_lock),
                contentDescription = null,
                tint = KFTheme.color.noticeTextColor,
                modifier = Modifier.size(12.dp)
            )
        }
        Text(
            text = stringResource(textRes),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = KFTheme.color.noticeTextColor,
            maxLines = 1
        )
    }
}

@Composable
private fun Modifier.pillSurface(): Modifier {
    val surface = KFTheme.color.keyButtonBackground
    val support = KFTheme.color.keyButtonShadow
    val radius = ControlHeight / 2

    return drawBehind {
        val supportPx = KeySupport.toPx()
        val corner = CornerRadius(radius.toPx())
        val capSize = Size(size.width, size.height - supportPx)

        drawRoundRect(support, topLeft = Offset(0f, supportPx), size = capSize, cornerRadius = corner)
        drawRoundRect(surface, topLeft = Offset.Zero, size = capSize, cornerRadius = corner)
    }
}
