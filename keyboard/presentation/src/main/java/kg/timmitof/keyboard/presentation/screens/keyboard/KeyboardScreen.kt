package kg.timmitof.keyboard.presentation.screens.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import kg.timmitof.keyboard.domain.model.KeyboardLayout
import kg.timmitof.keyboard.presentation.components.KeyboardKeyButton
import kg.timmitof.keyboard.presentation.theme.KFTheme
import kotlin.collections.orEmpty

@Composable
internal fun KeyboardFontsScreen(
    modifier: Modifier = Modifier,
    layout: KeyboardLayout,
    fontSize: TextUnit = TextUnit.Unspecified,
    onEvent: (KeyboardEvent) -> Unit
) {
    Box(modifier = modifier, contentAlignment = Alignment.BottomCenter) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxHeight(0.25f)
                .fillMaxWidth()
                .background(KFTheme.color.keyboardBackground)
                .padding(4.dp)
        ) {
            val rows = layout.rows.orEmpty()
                .filterNotNull()
                .map { row -> row.filterNotNull() }
                .filter { it.isNotEmpty() }

            if (rows.isEmpty()) return@BoxWithConstraints

            val keyHeight = maxHeight / rows.size
            val keyWidth = maxWidth / (rows.first().size + 1)

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(5.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                rows.forEach { row ->
                    Row(
                        modifier = Modifier.height(keyHeight),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        row.forEach { key ->
                            val label = key.labelLower ?: ""

                            KeyboardKeyButton(
                                key = label,
                                fontSize = fontSize,
                                onClick = { onEvent(KeyboardEvent.OnKeySelect(key)) },
                                modifier = Modifier
                                    .width(keyWidth * key.weight)
                                    .fillMaxHeight()
                            )
                        }
                    }
                }
            }
        }
    }
}