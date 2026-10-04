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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kg.timmitof.core.ui.theme.appColors

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

/**
 * Действие в заголовке секции — например, «Очистить недавние» справа от названия.
 */
@Immutable
data class SettingsSectionAction(
    val label: String,
    val isEnabled: Boolean = true,
    val onClick: () -> Unit,
)

/**
 * Секция настроек: необязательный заголовок + карточка со строками.
 *
 * Порядок секций на экране можно менять свободно — каждая замкнута на себя.
 *
 * ```
 * SettingsSection(title = "Ввод текста") {
 *     toggle(title = "Подсказки слов", checked = state.t9, icon = icon) { … }
 *     toggle(title = "Автокоррекция", checked = state.autoCorrect, isNested = true) { … }
 * }
 * ```
 *
 * @param title подпись над карточкой; `null` — карточка без заголовка (внутри вкладки).
 * @param action действие справа от заголовка.
 */
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

/**
 * Заголовок над карточкой или произвольным блоком вкладки: метка темы, а не строка списка.
 */
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

/** Карточка секции: строки вплотную, между ними — тонкая линия. */
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

/** Скругление карточек вкладок — общее для строк настроек и плиток. */
val SettingsCardShape = RoundedCornerShape(20.dp)

private val ActionShape = RoundedCornerShape(8.dp)
private val SettingsDividerThickness = 1.dp
private const val DividerAlpha = 0.06f
