package kg.timmitof.core.ui.components.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Ограничивает область видимости DSL секции — чужие receiver'ы внутрь не протекают. */
@DslMarker
annotation class SettingsDsl

/**
 * Сборщик строк секции.
 *
 * Строки описываются данными, а не разметкой: секция знает их порядок и сама
 * расставляет разделители, отступы вложенности и гашение выключенных веток.
 *
 * Иконки передаются готовыми ([Painter]) — DSL намеренно не композабельный,
 * чтобы список строк собирался одинаково при любой рекомпозиции.
 */
@SettingsDsl
class SettingsSectionScope internal constructor() {

    private val rows = mutableListOf<SettingsRow>()

    internal fun rows(): List<SettingsRow> = rows

    /** Добавить готовую строку — точка расширения для нестандартных случаев. */
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

/**
 * Секция настроек: заголовок + карточка со строками.
 *
 * Порядок секций на экране можно менять свободно — каждая замкнута на себя.
 *
 * ```
 * SettingsSection(title = "Ввод текста") {
 *     toggle(title = "Подсказки слов", checked = state.t9, icon = icon) { … }
 *     toggle(title = "Автокоррекция", checked = state.autoCorrect, isNested = true) { … }
 * }
 * ```
 */
@Composable
fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: SettingsSectionScope.() -> Unit,
) {
    val rows = SettingsSectionScope().apply(content).rows()
    if (rows.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        SettingsSectionHeader(title = title)
        SettingsCard(rows = rows)
    }
}

/** Заголовок секции — метка темы, а не отдельная строка списка. */
@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp),
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        letterSpacing = 0.9.sp,
        color = MaterialTheme.colorScheme.primary,
    )
}

/** Карточка секции: строки вплотную, между ними — тонкая линия. */
@Composable
private fun SettingsCard(rows: List<SettingsRow>) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column {
            rows.forEachIndexed { index, row ->
                if (index > 0) {
                    HorizontalDivider(
                        thickness = SettingsDividerThickness,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
                    )
                }
                SettingsRowItem(row = row)
            }
        }
    }
}

private val SettingsDividerThickness = 1.dp
