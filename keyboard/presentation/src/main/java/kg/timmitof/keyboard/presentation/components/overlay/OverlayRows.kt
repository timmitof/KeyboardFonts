package kg.timmitof.keyboard.presentation.components.overlay

import kg.timmitof.core.ui.plainClickable
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposableTarget
import androidx.compose.runtime.ComposableTargetMarker
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kg.timmitof.keyboard.presentation.theme.KFTheme

@DslMarker
annotation class KeyboardOverlayDsl

@Stable
internal sealed interface OverlayRow {
    @get:DrawableRes
    val iconRes: Int
    val title: String

    data class Toggle(
        @param:DrawableRes override val iconRes: Int,
        override val title: String,
        val subtitle: String? = null,
        val isChecked: Boolean,
        val onCheckedChange: (Boolean) -> Unit,
    ) : OverlayRow

    data class Segmented(
        @param:DrawableRes override val iconRes: Int,
        override val title: String,
        val options: List<String>,
        val selectedIndex: Int,
        val onSelect: (Int) -> Unit,
    ) : OverlayRow
}

@KeyboardOverlayDsl
internal class OverlayRowsScope {

    private val rows = mutableListOf<OverlayRow>()

    fun rows(): List<OverlayRow> = rows

    fun toggle(
        @DrawableRes iconRes: Int,
        title: String,
        isChecked: Boolean,
        subtitle: String? = null,
        onCheckedChange: (Boolean) -> Unit,
    ) {
        rows += OverlayRow.Toggle(iconRes, title, subtitle, isChecked, onCheckedChange)
    }

    fun segmented(
        @DrawableRes iconRes: Int,
        title: String,
        options: List<String>,
        selectedIndex: Int,
        onSelect: (Int) -> Unit,
    ) {
        rows += OverlayRow.Segmented(iconRes, title, options, selectedIndex, onSelect)
    }
}

@Composable
internal fun OverlayRows(
    modifier: Modifier = Modifier,
    content: OverlayRowsScope.() -> Unit,
) {
    val rows = OverlayRowsScope().apply(content).rows()

    Column(
        modifier = modifier
            .padding(horizontal = 8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        rows.forEach { row -> OverlayRowItem(row = row) }
    }
}

@Composable
private fun OverlayRowItem(row: OverlayRow) {
    val clickModifier = when (row) {
        is OverlayRow.Toggle -> Modifier.plainClickable { row.onCheckedChange(!row.isChecked) }

        is OverlayRow.Segmented -> Modifier
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = RowMinHeight)
            .clip(RowShape)
            .then(clickModifier)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(row.iconRes),
            contentDescription = null,
            tint = KFTheme.color.overlayIconColor,
            modifier = Modifier.size(21.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = row.title,
                fontSize = 14.sp,
                color = KFTheme.color.overlayTitleColor,
                maxLines = 1,
            )
            (row as? OverlayRow.Toggle)?.subtitle?.let { subtitle ->
                Text(
                    modifier = Modifier.padding(top = 2.dp),
                    text = subtitle,
                    fontSize = 11.sp,
                    color = KFTheme.color.overlaySubtitleColor,
                    maxLines = 1,
                )
            }
        }

        OverlayRowControl(row = row)
    }
}

@Composable
private fun RowScope.OverlayRowControl(row: OverlayRow) = when (row) {
    is OverlayRow.Toggle -> OverlaySwitch(isChecked = row.isChecked)

    is OverlayRow.Segmented -> OverlaySegmentedControl(
        options = row.options,
        selectedIndex = row.selectedIndex,
        onSelect = row.onSelect,
    )
}

/** Минимум, а не фиксированная высота: строка с подзаголовком выше обычной. */
private val RowMinHeight = 44.dp
private val RowShape = RoundedCornerShape(12.dp)
