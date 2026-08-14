package kg.timmitof.core.ui.components.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val SegmentedShape = RoundedCornerShape(14.dp)
private val SegmentShape = RoundedCornerShape(11.dp)

/**
 * Выбор из двух-трёх вариантов.
 *
 * Вместо ползущего индикатора анимируются цвета самих сегментов: варианты
 * разной ширины, и перекрашивание не требует ни измерений, ни лишнего слоя.
 */
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
            .background(MaterialTheme.colorScheme.surfaceVariant)
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
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = spring(stiffness = 700f),
        label = "segmentBackground"
    )
    val content by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.onPrimary
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
