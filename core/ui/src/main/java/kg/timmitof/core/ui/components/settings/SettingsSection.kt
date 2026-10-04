package kg.timmitof.core.ui.components.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kg.timmitof.core.ui.theme.appColors

@DslMarker
annotation class SettingsDsl

/** Строки описываются данными; иконки — готовые [Painter]: DSL намеренно не композабельный. */
@SettingsDsl
class SettingsSectionScope internal constructor() {

    private val rows = mutableListOf<SettingsRow>()

    internal fun rows(): List<SettingsRow> = rows

    fun row(row: SettingsRow) {
        rows += row
    }

    fun toggle(
        title: String,
        checked: Boolean,
        description: String? = null,
        icon: Painter? = null,
        isNested: Boolean = false,
        isEnabled: Boolean = true,
        onCheckedChange: (Boolean) -> Unit,
    ) = row(
        SettingsRow.Toggle(
            title = title,
            description = description,
            icon = icon,
            isNested = isNested,
            isEnabled = isEnabled,
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    )

    fun navigation(
        title: String,
        description: String? = null,
        icon: Painter? = null,
        value: String? = null,
        isNested: Boolean = false,
        isEnabled: Boolean = true,
        onClick: () -> Unit,
    ) = row(
        SettingsRow.Navigation(
            title = title,
            description = description,
            icon = icon,
            isNested = isNested,
            isEnabled = isEnabled,
            value = value,
            onClick = onClick,
        )
    )

    fun segmented(
        title: String,
        options: List<String>,
        selectedIndex: Int,
        description: String? = null,
        icon: Painter? = null,
        isNested: Boolean = false,
        isEnabled: Boolean = true,
        onSelect: (Int) -> Unit,
    ) = row(
        SettingsRow.Segmented(
            title = title,
            description = description,
            icon = icon,
            isNested = isNested,
            isEnabled = isEnabled,
            options = options,
            selectedIndex = selectedIndex,
            onSelect = onSelect,
        )
    )

    fun slider(
        title: String,
        value: Float,
        description: String? = null,
        icon: Painter? = null,
        valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
        steps: Int = 0,
        valueLabel: String? = null,
        isNested: Boolean = false,
        isEnabled: Boolean = true,
        onValueChangeFinished: (() -> Unit)? = null,
        onValueChange: (Float) -> Unit,
    ) = row(
        SettingsRow.Slider(
            title = title,
            description = description,
            icon = icon,
            isNested = isNested,
            isEnabled = isEnabled,
            value = value,
            valueRange = valueRange,
            steps = steps,
            valueLabel = valueLabel,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
        )
    )

    fun colors(
        title: String,
        colors: List<Color>,
        selected: Color?,
        description: String? = null,
        icon: Painter? = null,
        isNested: Boolean = false,
        isEnabled: Boolean = true,
        onPickCustom: (() -> Unit)? = null,
        autoColor: Color? = null,
        autoLabel: String = "",
        onAuto: () -> Unit = {},
        onSelect: (Color) -> Unit,
    ) = row(
        SettingsRow.Colors(
            title = title,
            description = description,
            icon = icon,
            isNested = isNested,
            isEnabled = isEnabled,
            colors = colors,
            selected = selected,
            onSelect = onSelect,
            onPickCustom = onPickCustom,
            autoColor = autoColor,
            autoLabel = autoLabel,
            onAuto = onAuto,
        )
    )

    fun info(
        title: String,
        description: String? = null,
        badge: String? = null,
        icon: Painter? = null,
        isNested: Boolean = false,
        isEnabled: Boolean = true,
    ) = row(
        SettingsRow.Info(
            title = title,
            description = description,
            icon = icon,
            isNested = isNested,
            isEnabled = isEnabled,
            badge = badge,
        )
    )

    fun soon(
        title: String,
        badge: String,
        description: String? = null,
        icon: Painter? = null,
        isNested: Boolean = false,
    ) = row(
        SettingsRow.Soon(
            title = title,
            description = description,
            icon = icon,
            isNested = isNested,
            badge = badge,
        )
    )
}

@Immutable
data class SettingsSectionAction(
    val label: String,
    val isEnabled: Boolean = true,
    val onClick: () -> Unit,
)

/** [title] = `null` — карточка без заголовка (внутри вкладки). */
@Composable
fun SettingsSection(
    modifier: Modifier = Modifier,
    title: String? = null,
    action: SettingsSectionAction? = null,
    content: SettingsSectionScope.() -> Unit,
) {
    val rows = SettingsSectionScope().apply(content).rows()
    if (rows.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        if (title != null) {
            SettingsSectionHeader(title = title, action = action)
        }
        SettingsCard(rows = rows)
    }
}

@Composable
fun SettingsSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: SettingsSectionAction? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 2.dp, end = 2.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        action?.let {
            Text(
                modifier = Modifier
                    .clip(ActionShape)
                    .clickable(enabled = it.isEnabled, onClick = it.onClick)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                    .graphicsLayer { alpha = if (it.isEnabled) 1f else 0.4f },
                text = it.label,
                style = MaterialTheme.typography.labelLarge,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
fun SettingsSectionFooter(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier.padding(start = 2.dp, end = 2.dp, top = 8.dp),
        text = text,
        style = MaterialTheme.typography.bodySmall,
        fontSize = 12.5.sp,
        color = MaterialTheme.colorScheme.outline,
    )
}

@Composable
private fun SettingsCard(rows: List<SettingsRow>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = SettingsCardShape,
        color = MaterialTheme.appColors.card,
    ) {
        Column {
            rows.forEachIndexed { index, row ->
                if (index > 0) {
                    HorizontalDivider(
                        thickness = SettingsDividerThickness,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = DividerAlpha),
                    )
                }
                SettingsRowItem(row = row)
            }
        }
    }
}

val SettingsCardShape = RoundedCornerShape(20.dp)

private val ActionShape = RoundedCornerShape(8.dp)
private val SettingsDividerThickness = 1.dp
private const val DividerAlpha = 0.06f
