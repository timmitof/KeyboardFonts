package kg.timmitof.keyboard.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import kg.timmitof.keyboard.presentation.components.KeyboardKeyButton
import kg.timmitof.keyboard.presentation.model.KeyboardEvent
import kg.timmitof.keyboard.presentation.model.KeyboardKey

typealias KeyboardLayout = List<List<KeyboardKey>>

@Composable
internal fun KeyboardFontsScreen(
    modifier: Modifier = Modifier,
    layout: KeyboardLayout,
    fontSize: TextUnit = TextUnit.Unspecified,
    onEvent: (KeyboardEvent) -> Unit
) {
    BoxWithConstraints(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(4.dp)
    ) {
        val keyHeight = maxHeight / layout.size

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            layout.forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(keyHeight),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    row.forEach { key ->
                        KeyboardKeyButton(
                            key = key,
                            fontSize = fontSize,
                            onClick = { onEvent(KeyboardEvent.OnKeyClick(it)) },
                            modifier = Modifier.weight(key.weight)
                        )
                    }
                }
            }
        }
    }
}