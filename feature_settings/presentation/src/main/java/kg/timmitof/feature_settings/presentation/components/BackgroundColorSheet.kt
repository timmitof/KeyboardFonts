package kg.timmitof.feature_settings.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kg.timmitof.core.ui.components.color.ColorSwatches
import kg.timmitof.core.ui.components.color.HueSlider
import kotlinx.coroutines.launch

/** Цвет сразу уходит в превью клавиатуры ([onPreview]); в настройки — только по «Применить» ([onApply]). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BackgroundColorSheet(
    initial: Color,
    title: String,
    toneLabel: String,
    cancelLabel: String,
    applyLabel: String,
    onPreview: (Color) -> Unit,
    onApply: (Color) -> Unit,
    onDismiss: () -> Unit,
) {
    // Черновик живёт только в шторке; наружу уходит превью, а в настройки — лишь по «Применить».
    var color by remember { mutableStateOf(initial) }
    val onChange = { picked: Color ->
        color = picked
        onPreview(picked)
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val closeThen = { action: () -> Unit ->
        scope.launch { sheetState.hide() }.invokeOnCompletion { action() }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground,
            )

            ColorSwatches(
                colors = BackgroundColors,
                selected = color,
                onSelect = onChange,
                swatchSize = 40.dp,
                columns = BackgroundColors.size / 2,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = toneLabel,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                // Свой оттенок — пастельный, как верхний ряд: на таком фоне клавиши читаются.
                HueSlider(
                    hue = { color.hue() },
                    onChange = { hue -> onChange(Color.hsv(hue, ToneSaturation, ToneValue)) },
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalButton(
                    modifier = Modifier.weight(1f),
                    onClick = { closeThen(onDismiss) },
                ) {
                    Text(text = cancelLabel)
                }
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = { closeThen { onApply(color) } },
                ) {
                    Text(text = applyLabel)
                }
            }
        }
    }
}

private fun Color.hue(): Float =
    FloatArray(3).also { android.graphics.Color.colorToHSV(toArgb(), it) }[0]

private const val ToneSaturation = 0.22f
private const val ToneValue = 0.96f

private val BackgroundColors = listOf(
    Color(0xFFE8E8E8),
    Color(0xFFF7D8FF),
    Color(0xFFCDEFE7),
    Color(0xFFFFE3B8),
    Color(0xFFFFDAD8),
    Color(0xFFD9E3FF),
    Color(0xFF1E1A1F),
    Color(0xFF2D0B3D),
    Color(0xFF10473F),
    Color(0xFF5C3A00),
    Color(0xFF4E2C2B),
    Color(0xFF1F2A44),
)
