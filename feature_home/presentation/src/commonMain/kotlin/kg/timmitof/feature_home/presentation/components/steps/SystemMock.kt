package kg.timmitof.feature_home.presentation.components.steps

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kg.timmitof.core.ui.theme.appColors

@Immutable
internal sealed interface MockRow {
    val label: String

    val poke: String?

    data class Toggle(override val label: String, val checked: Boolean, override val poke: String?) : MockRow

    data class Radio(override val label: String, val selected: Boolean, override val poke: String?) : MockRow
}

@SetupDsl
class SystemMockScope internal constructor() {

    private val rows = mutableListOf<MockRow>()

    internal fun rows(): List<MockRow> = rows

    fun toggle(label: String, checked: Boolean, poke: String? = null) {
        rows += MockRow.Toggle(label, checked, poke)
    }

    fun radio(label: String, selected: Boolean, poke: String? = null) {
        rows += MockRow.Radio(label, selected, poke)
    }
}

/** Картинка, а не контролы: строки не нажимаются, нажимать нужно в системе. */
@Composable
fun SystemMock(
    caption: String,
    modifier: Modifier = Modifier,
    content: SystemMockScope.() -> Unit,
) {
    val rows = SystemMockScope().apply(content).rows()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MockShape)
            .background(MaterialTheme.appColors.cardMuted)
            .padding(start = 6.dp, end = 6.dp, top = 4.dp, bottom = 6.dp),
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            text = caption,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.outline,
        )
        rows.forEach { row -> MockRowItem(row) }
    }
}

@Composable
private fun MockRowItem(row: MockRow) {
    val colors = MaterialTheme.appColors
    val isHighlighted = row.poke != null

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .clip(RowShape)
            .then(
                if (isHighlighted) {
                    Modifier
                        .background(colors.card)
                        .border(2.dp, colors.brand.solid, RowShape)
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (row is MockRow.Radio) MockRadio(selected = row.selected)

        Text(
            text = row.label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.weight(1f))

        row.poke?.let { poke -> PokeChip(text = poke) }
        if (row is MockRow.Toggle) MockSwitch(checked = row.checked)
    }
}

@Composable
private fun PokeChip(text: String) {
    val tones = MaterialTheme.appColors.brand

    Text(
        modifier = Modifier
            .clip(PokeShape)
            .background(tones.container)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        color = tones.solid,
    )
}

@Composable
private fun MockSwitch(checked: Boolean) {
    val scheme = MaterialTheme.colorScheme
    val track = if (checked) scheme.primary else scheme.surfaceVariant
    val thumb = if (checked) scheme.onPrimary else scheme.outline

    Box(
        modifier = Modifier
            .size(width = 40.dp, height = 24.dp)
            .clip(CircleShape)
            .background(track)
            .then(if (checked) Modifier else Modifier.border(2.dp, scheme.outline, CircleShape))
            .padding(horizontal = 4.dp),
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .size(if (checked) 16.dp else 12.dp)
                .clip(CircleShape)
                .background(thumb)
        )
    }
}

@Composable
private fun MockRadio(selected: Boolean) {
    val scheme = MaterialTheme.colorScheme
    val ring = if (selected) scheme.primary else scheme.outline

    Box(
        modifier = Modifier
            .size(18.dp)
            .border(2.dp, ring, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(if (selected) ring else Color.Transparent)
        )
    }
}

private val MockShape = RoundedCornerShape(14.dp)
private val RowShape = RoundedCornerShape(10.dp)
private val PokeShape = RoundedCornerShape(10.dp)
