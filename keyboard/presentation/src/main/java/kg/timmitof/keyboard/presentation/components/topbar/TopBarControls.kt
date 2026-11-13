package kg.timmitof.keyboard.presentation.components.topbar

import kg.timmitof.core.ui.plainClickable
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kg.timmitof.keyboard.presentation.R
import kg.timmitof.keyboard.presentation.components.KeySupport
import kg.timmitof.keyboard.presentation.theme.KFTheme

private val ControlHeight = 32.dp

private val ControlShape = RoundedCornerShape(ControlHeight / 2)

private val AnchorSize = 40.dp

@Composable
internal fun FontToggleButton(
    preview: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopBarPill(label = preview, onClick = onClick, modifier = modifier, hasDropdown = true)
}

/**
 * Плашка-кнопка верхней строки в стиле клавиши (с опорой снизу).
 *
 * @param isActive включённое состояние (шрифт для перевода): плашка красится в цвет подсказки.
 * @param hasDropdown стрелка «▾» — по тапу откроется выбор.
 */
@Composable
internal fun TopBarPill(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    hasDropdown: Boolean = false,
    isEnabled: Boolean = true,
) {
    val content = when {
        isActive -> KFTheme.color.noticeTextColor
        else -> KFTheme.color.keyLabelColor
    }.let { if (isEnabled) it else it.copy(alpha = DisabledAlpha) }

    Row(
        modifier = modifier
            .height(ControlHeight)
            .clip(ControlShape)
            .then(if (isActive) Modifier.background(KFTheme.color.noticeBackground) else Modifier.pillSurface())
            .then(if (isEnabled) Modifier.plainClickable(onClick) else Modifier)
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = content,
            maxLines = 1
        )
        if (hasDropdown) {
            Text(
                text = "▾",
                fontSize = 11.sp,
                color = content.copy(alpha = 0.5f)
            )
        }
    }
}

private const val DisabledAlpha = 0.4f

@Composable
internal fun TopBarAnchorButton(
    @DrawableRes iconRes: Int,
    contentDescription: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val background by animateColorAsState(
        targetValue = if (isActive) KFTheme.color.noticeBackground else Color.Transparent,
        animationSpec = tween(180),
        label = "anchorBackground"
    )
    val tint by animateColorAsState(
        targetValue = if (isActive) KFTheme.color.noticeTextColor else KFTheme.color.keySpecialTextColor,
        animationSpec = tween(180),
        label = "anchorTint"
    )

    Box(
        modifier = modifier
            .size(AnchorSize)
            .clip(CircleShape)
            .background(background)
            .plainClickable(onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(iconRes),
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
internal fun NoticePill(
    @StringRes textRes: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(26.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(KFTheme.color.noticeBackground)
            .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_field_lock),
            contentDescription = null,
            tint = KFTheme.color.noticeTextColor,
            modifier = Modifier.size(12.dp)
        )
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
