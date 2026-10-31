package kg.timmitof.core.ui.components.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kg.timmitof.core.ui.theme.AccentRole
import kg.timmitof.core.ui.theme.appColors

private val SegmentedShape = RoundedCornerShape(14.dp)
private val SegmentShape = RoundedCornerShape(11.dp)
private val BadgeShape = RoundedCornerShape(8.dp)
private val PillShape = RoundedCornerShape(50)

/** Вместо ползущего индикатора анимируются цвета сегментов: варианты разной ширины, измерений не нужно. */
@Composable
fun SettingsSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
) {
    Row(
        modifier = modifier
            .clip(SegmentedShape)
            .background(MaterialTheme.appColors.cardMuted)
            .padding(3.dp),
    ) {
        options.forEachIndexed { index, option ->
            Segment(
                label = option,
                isSelected = index == selectedIndex,
                isEnabled = isEnabled,
                onClick = { onSelect(index) },
            )
        }
    }
}

@Composable
private fun Segment(
    label: String,
    isSelected: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit,
) {
    val background by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.appColors.selected
        } else {
            MaterialTheme.appColors.cardMuted
        },
        animationSpec = spring(stiffness = 700f),
        label = "segmentBackground"
    )
    val content by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.appColors.onSelected
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = spring(stiffness = 700f),
        label = "segmentContent"
    )

    Text(
        modifier = Modifier
            .clip(SegmentShape)
            .background(background)
            .clickable(enabled = isEnabled && !isSelected, onClick = onClick)
            .padding(horizontal = 11.dp, vertical = 7.dp),
        text = label,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Medium,
        color = content,
        maxLines = 1,
    )
}

/** Плашка вместо контрола: функция уже на экране, но ещё не работает. */
@Composable
fun SettingsSoonBadge(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier
            .clip(SegmentShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
    )
}

/** Короткий код в плашке слева от строки: язык (`RU`), счётчик. */
@Composable
fun SettingsBadge(
    text: String,
    modifier: Modifier = Modifier,
) {
    val tones = MaterialTheme.appColors.hint

    Box(
        modifier = modifier
            .size(width = 34.dp, height = 28.dp)
            .clip(BadgeShape)
            .background(tones.container),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = tones.onContainer,
            maxLines = 1,
        )
    }
}

/**
 * Кнопка-пилюля в цветах роли: «Добавить язык» во всю ширину ([isCompact] = false)
 * или маленькая «Добавить» в конце строки списка.
 */
@Composable
fun SettingsPillButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: Painter? = null,
    role: AccentRole = AccentRole.HINT,
    isCompact: Boolean = false,
) {
    val tones = MaterialTheme.appColors[role]

    Row(
        modifier = modifier
            .height(if (isCompact) 32.dp else 46.dp)
            .clip(PillShape)
            .background(tones.container)
            .clickable(onClick = onClick)
            .padding(horizontal = if (isCompact) 13.dp else 18.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon?.let {
            Icon(
                painter = it,
                contentDescription = null,
                tint = tones.onContainer,
                modifier = Modifier.size(17.dp),
            )
        }
        Text(
            text = label,
            fontSize = if (isCompact) 12.5.sp else 14.sp,
            fontWeight = FontWeight.Medium,
            color = tones.onContainer,
            maxLines = 1,
        )
    }
}
