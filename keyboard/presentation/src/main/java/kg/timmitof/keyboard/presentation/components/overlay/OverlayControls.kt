package kg.timmitof.keyboard.presentation.components.overlay

import kg.timmitof.core.ui.plainClickable
import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kg.timmitof.keyboard.presentation.R
import kg.timmitof.keyboard.presentation.theme.KFTheme

private val SwitchSize = 52.dp to 32.dp
private val SwitchShape = RoundedCornerShape(16.dp)
private val SegmentedShape = RoundedCornerShape(14.dp)
private val SegmentShape = RoundedCornerShape(11.dp)
private val IconButtonSize = 32.dp

private val ControlSpring = spring<Color>(stiffness = Spring.StiffnessMediumLow)

@Composable
internal fun OverlaySwitch(
    isChecked: Boolean,
    modifier: Modifier = Modifier,
) {
    val (width, height) = SwitchSize
    val track by animateColorAsState(
        targetValue = if (isChecked) KFTheme.color.overlayAccent else KFTheme.color.overlayControlTrack,
        animationSpec = ControlSpring,
        label = "switchTrack"
    )
    val thumbColor by animateColorAsState(
        targetValue = if (isChecked) {
            KFTheme.color.overlayAccentTextColor
        } else {
            KFTheme.color.overlaySubtitleColor
        },
        animationSpec = ControlSpring,
        label = "switchThumb"
    )
    val thumbSize by animateDpAsState(
        targetValue = if (isChecked) 24.dp else 16.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "switchThumbSize"
    )
    val outline = KFTheme.color.overlaySubtitleColor

    Box(
        modifier = modifier
            .size(width = width, height = height)
            .clip(SwitchShape)
            .background(track)
            .then(if (isChecked) Modifier else Modifier.border(2.dp, outline, SwitchShape))
            .padding(horizontal = 4.dp),
        contentAlignment = if (isChecked) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .size(thumbSize)
                .clip(CircleShape)
                .background(thumbColor)
        )
    }
}

@Composable
internal fun OverlaySegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(SegmentedShape)
            .background(KFTheme.color.overlayControlTrack)
            .padding(3.dp),
    ) {
        options.forEachIndexed { index, option ->
            Segment(
                label = option,
                isSelected = index == selectedIndex,
                onClick = { onSelect(index) },
            )
        }
    }
}

@Composable
private fun Segment(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val background by animateColorAsState(
        targetValue = if (isSelected) KFTheme.color.overlayAccent else Color.Transparent,
        animationSpec = ControlSpring,
        label = "segmentBackground"
    )
    val content by animateColorAsState(
        targetValue = if (isSelected) {
            KFTheme.color.overlayAccentTextColor
        } else {
            KFTheme.color.overlayControlLabelColor
        },
        animationSpec = ControlSpring,
        label = "segmentContent"
    )

    Text(
        modifier = Modifier
            .clip(SegmentShape)
            .background(background)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = !isSelected,
                onClick = onClick,
            )
            .padding(horizontal = 11.dp, vertical = 7.dp),
        text = label,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = content,
        maxLines = 1,
    )
}

@Composable
internal fun OverlayIconButton(
    @DrawableRes iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(IconButtonSize)
            .clip(CircleShape)
            .plainClickable(onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(iconRes),
            contentDescription = contentDescription,
            tint = KFTheme.color.overlayIconColor,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
internal fun OverlayActionRow(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(40.dp)
            .clip(CircleShape)
            .background(KFTheme.color.overlayActionBackground)
            .plainClickable(onClick)
            .padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val content = KFTheme.color.overlayActionTextColor

        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_open_in_app),
            contentDescription = null,
            tint = content,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = content,
            modifier = Modifier.weight(1f),
            maxLines = 1,
        )
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_overlay_chevron),
            contentDescription = null,
            tint = content,
            modifier = Modifier.size(18.dp)
        )
    }
}
