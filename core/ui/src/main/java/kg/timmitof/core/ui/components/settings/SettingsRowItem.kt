package kg.timmitof.core.ui.components.settings

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kg.timmitof.core.ui.R
import kg.timmitof.core.ui.theme.appColors

private val RowMinHeight = 54.dp

private val NestedStartPadding = 44.dp
private val RowHorizontalPadding = 14.dp

private val ConnectorOffset = 24.dp
private val ConnectorWidth = 2.dp
private const val ConnectorAlpha = 0.28f

private const val DisabledAlpha = 0.4f

private val BadgeShape = RoundedCornerShape(8.dp)

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
                start = if (row.isNested) NestedStartPadding else RowHorizontalPadding,
                end = RowHorizontalPadding,
                top = 9.dp,
                bottom = 9.dp,
            ),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        row.icon?.let { icon -> RowIcon(icon = icon, alpha = { alpha }) }
        (row as? SettingsRow.Info)?.badge?.let { badge -> RowBadge(text = badge) }

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
            .size(22.dp)
            .graphicsLayer { this.alpha = alpha() },
    )
}

@Composable
private fun RowBadge(text: String) {
    val tones = MaterialTheme.appColors.hint

    Box(
        modifier = Modifier
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

@Composable
private fun RowTitles(row: SettingsRow, alpha: () -> Float) {
    Text(
        text = row.title,
        style = MaterialTheme.typography.bodyLarge,
        fontSize = 15.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.graphicsLayer { this.alpha = alpha() },
    )

    row.description?.let { description ->
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier
                .padding(top = 1.dp)
                .graphicsLayer { this.alpha = alpha() },
        )
    }
}

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
                painter = painterResource(R.drawable.ic_chevron_right),
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

        is SettingsRow.Slider, is SettingsRow.Info -> Unit
    }
}

private fun Modifier.nestedConnector(isNested: Boolean, color: () -> Color): Modifier =
    if (!isNested) this else drawBehind {
        drawRect(
            color = color(),
            topLeft = Offset(ConnectorOffset.toPx(), 0f),
            size = Size(ConnectorWidth.toPx(), size.height),
        )
    }
