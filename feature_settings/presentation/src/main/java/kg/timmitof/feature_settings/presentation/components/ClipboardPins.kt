package kg.timmitof.feature_settings.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kg.timmitof.core.ui.R as UiR
import kg.timmitof.core.ui.theme.appColors
import kg.timmitof.keyboard.clipboard.domain.model.ClipboardEntry

@Composable
internal fun ClipboardPins(
    entries: List<ClipboardEntry>,
    emptyText: String,
    modifier: Modifier = Modifier,
) {
    if (entries.isEmpty()) {
        Text(
            modifier = modifier
                .fillMaxWidth()
                .clip(PinShape)
                .background(MaterialTheme.appColors.card)
                .padding(horizontal = 14.dp, vertical = 14.dp),
            text = emptyText,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            color = MaterialTheme.colorScheme.outline,
        )
        return
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        entries.chunked(Columns).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { entry ->
                    PinCard(entry = entry, modifier = Modifier.weight(1f))
                }
                // Неполный последний ряд не растягивает карточку на всю ширину.
                repeat(Columns - row.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun PinCard(entry: ClipboardEntry, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .heightIn(min = 62.dp)
            .clip(PinShape)
            .background(MaterialTheme.appColors.card)
            .padding(start = 12.dp, end = 12.dp, top = 7.dp, bottom = 10.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Icon(
                painter = painterResource(UiR.drawable.ic_pin),
                contentDescription = null,
                tint = MaterialTheme.appColors.success.solid,
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = entry.text,
                fontSize = 13.sp,
                lineHeight = 17.sp,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private const val Columns = 2
private val PinShape = RoundedCornerShape(14.dp)
