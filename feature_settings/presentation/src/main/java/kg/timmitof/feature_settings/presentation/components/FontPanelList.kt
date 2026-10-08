package kg.timmitof.feature_settings.presentation.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import kg.timmitof.core.ui.components.settings.SettingsCheckList
import kg.timmitof.core.ui.components.settings.SettingsCheckbox
import kg.timmitof.core.ui.components.settings.SettingsReorderList

@Immutable
data class FontItem(
    val id: String,
    val name: String,
    val sample: String,
)

/** Шрифты на панели: нажатие прячет шрифт, ручка справа меняет порядок. */
@Composable
internal fun VisibleFontsList(
    fonts: List<FontItem>,
    canHide: Boolean,
    onHide: (FontItem) -> Unit,
    onReorder: (List<FontItem>) -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsReorderList(
        items = fonts,
        key = FontItem::id,
        onReorder = onReorder,
        modifier = modifier,
        isClickEnabled = canHide,
        onClick = onHide,
    ) { item ->
        FontRowContent(item = item, isVisible = true)
    }
}

@Composable
internal fun HiddenFontsList(
    fonts: List<FontItem>,
    onShow: (FontItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsCheckList(
        items = fonts,
        key = FontItem::id,
        modifier = modifier,
        onClick = onShow,
    ) { item ->
        FontRowContent(item = item, isVisible = false)
    }
}

@Composable
private fun RowScope.FontRowContent(item: FontItem, isVisible: Boolean) {
    SettingsCheckbox(isChecked = isVisible)
    Text(
        modifier = Modifier.weight(1f),
        text = item.sample,
        fontSize = 18.sp,
        color = if (isVisible) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.outline,
        maxLines = 1,
    )
    Text(
        text = item.name,
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.outline,
        maxLines = 1,
    )
}
