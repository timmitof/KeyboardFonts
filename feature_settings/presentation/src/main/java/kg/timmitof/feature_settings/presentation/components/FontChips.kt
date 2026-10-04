package kg.timmitof.feature_settings.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kg.timmitof.core.ui.theme.appColors
import kg.timmitof.keyboard.font.domain.model.KeyboardFont

@Composable
internal fun FontChips(
    fonts: List<KeyboardFont>,
    selected: KeyboardFont,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        fonts.forEach { font ->
            FontChip(font = font, isSelected = font.id == selected.id)
        }
    }
}

@Composable
private fun FontChip(font: KeyboardFont, isSelected: Boolean) {
    val tones = MaterialTheme.appColors.brand
    val sample = remember(font) { font.apply(FontSample) }
    val outline = MaterialTheme.colorScheme.onBackground.copy(alpha = OutlineAlpha)

    Text(
        modifier = Modifier
            .height(32.dp)
            .clip(CircleShape)
            .background(if (isSelected) tones.container else Color.Transparent)
            .border(width = 1.dp, color = if (isSelected) Color.Transparent else outline, shape = CircleShape)
            .padding(horizontal = 11.dp)
            .wrapContentHeight(Alignment.CenterVertically),
        text = sample,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = if (isSelected) tones.onContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
    )
}

private const val FontSample = "Abc"
private const val OutlineAlpha = 0.14f
