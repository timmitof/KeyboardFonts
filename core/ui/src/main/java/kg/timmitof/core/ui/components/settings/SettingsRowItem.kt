package kg.timmitof.core.ui.components.settings

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Минимальная высота строки — палец попадает без прицеливания. */
private val RowMinHeight = 56.dp

/** Отступ вложенной строки: она сдвинута под родителя, а не просто подписана к нему. */
private val NestedStartPadding = 52.dp
private val RowStartPadding = 16.dp
private val RowEndPadding = 16.dp

/** Линия связи вложенной строки с родителем. */
private val ConnectorOffset = 30.dp
private val ConnectorWidth = 2.dp
private const val ConnectorAlpha = 0.28f

/** Насколько гаснет строка, отключённая родителем: видно, но трогать нечего. */
private const val DisabledAlpha = 0.4f

/**
 * Отрисовка одной [SettingsRow].
 *
 * Все пять типов делят общий каркас — иконка, текст, контрол справа, — поэтому
 * строки выглядят одинаково независимо от того, что стоит в конце.
 */
@Composable
internal fun SettingsRowItem(row: SettingsRow) {
    val alpha by animateFloatAsState(
        targetValue = if (row.isEnabled) 1f else DisabledAlpha,
        animationSpec = spring(stiffness = 900f),
        label = "rowAlpha"
    )

    val clickModifier = when {
        !row.isEnabled -> Modifier
        row is SettingsRow.Toggle -> Modifier.clickable { row.onCheckedChange(!row.checked) }
        row is SettingsRow.Navigation -> Modifier.clickable(onClick = row.onClick)
        else -> Modifier
    }

    val connectorColor = MaterialTheme.colorScheme.primary.copy(alpha = ConnectorAlpha)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(clickModifier)
            .nestedConnector(row.isNested) { connectorColor }
            .heightIn(min = RowMinHeight)
            .padding(
                start = if (row.isNested) NestedStartPadding else RowStartPadding,
                end = RowEndPadding,
                top = 11.dp,
                bottom = 11.dp,
            ),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        row.icon?.let { icon -> RowIcon(icon = icon, alpha = { alpha }) }

        Column(modifier = Modifier.weight(1f)) {
            RowTitles(row = row, alpha = { alpha })

            // Ползунок не помещается в строку — он занимает вторую строку под заголовком.
            (row as? SettingsRow.Slider)?.let { slider ->
                Slider(
                    value = slider.value,
                    onValueChange = slider.onValueChange,
                    valueRange = slider.valueRange,
                    steps = slider.steps,
                    enabled = slider.isEnabled,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }

        RowControl(row = row, alpha = { alpha })
    }
}

@Composable
private fun RowIcon(icon: Painter, alpha: () -> Float) {
    Icon(
        painter = icon,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .size(24.dp)
            .graphicsLayer { this.alpha = alpha() },
    )
}

@Composable
private fun RowTitles(row: SettingsRow, alpha: () -> Float) {
    Text(
        text = row.title,
        style = MaterialTheme.typography.bodyLarge,
        fontSize = 15.sp,
        fontWeight = FontWeight.Normal,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.graphicsLayer { this.alpha = alpha() },
    )

    row.description?.let { description ->
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier
                .padding(top = 2.dp)
                .graphicsLayer { this.alpha = alpha() },
        )
    }
}

/** Контрол в конце строки — по нему и различаются типы. */
@Composable
private fun RowScope.RowControl(row: SettingsRow, alpha: () -> Float) {
    when (row) {
        is SettingsRow.Toggle -> Switch(
            checked = row.checked,
            onCheckedChange = row.onCheckedChange,
            enabled = row.isEnabled,
        )

        is SettingsRow.Navigation -> {
            row.value?.let { value ->
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.graphicsLayer { this.alpha = alpha() },
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier
                    .size(18.dp)
                    .graphicsLayer { this.alpha = alpha() },
            )
        }

        is SettingsRow.Segmented -> SettingsSegmentedControl(
            options = row.options,
            selectedIndex = row.selectedIndex,
            onSelect = row.onSelect,
            isEnabled = row.isEnabled,
        )

        is SettingsRow.Soon -> SettingsSoonBadge(text = row.badge)

        is SettingsRow.Slider -> Unit
    }
}

/** Линия, связывающая вложенную строку с родительским переключателем. */
private fun Modifier.nestedConnector(isNested: Boolean, color: () -> Color): Modifier =
    if (!isNested) this else drawBehind {
        drawRect(
            color = color(),
            topLeft = Offset(ConnectorOffset.toPx(), 0f),
            size = Size(ConnectorWidth.toPx(), size.height),
        )
    }
