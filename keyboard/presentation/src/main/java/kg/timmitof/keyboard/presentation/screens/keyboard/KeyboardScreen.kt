package kg.timmitof.keyboard.presentation.screens.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kg.timmitof.keyboard.domain.model.KeyboardKey
import kg.timmitof.keyboard.presentation.components.BackspaceKeyButton
import kg.timmitof.keyboard.presentation.components.EnterKeyButton
import kg.timmitof.keyboard.presentation.components.KeyboardKeyButton
import kg.timmitof.keyboard.presentation.components.ShiftKeyButton
import kg.timmitof.keyboard.presentation.components.SpaceKeyButton
import kg.timmitof.keyboard.presentation.components.SpecialKeyButton
import kg.timmitof.keyboard.presentation.theme.KFTheme
import org.orbitmvi.orbit.compose.collectAsState

@Composable
internal fun KeyboardFontsScreen(viewModel: KeyboardViewModel) {
    val state = viewModel.collectAsState()

    KeyboardContent(
        state = state,
        onEvent = viewModel::onEvent
    )
}

@Composable
private fun KeyboardContent(
    state: State<KeyboardState>,
    onEvent: (KeyboardEvent) -> Unit
) {
    val rows = remember(state.value.keyboardLayout.rows) { state.value.keyboardLayout.rows }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(KFTheme.color.keyboardBackground)
            .padding(horizontal = 6.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            rows.forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    row.forEach { key ->
                        when (key) {
                            is KeyboardKey.Character -> KeyboardKeyButton(
                                label = if (state.value.isUpperCase) key.labelUpper else key.labelLower,
                                weight = key.weight,
                                longPress = key.longPress,
                                modifier = Modifier.weight(key.weight),
                                onClick = { onEvent(KeyboardEvent.OnKeySelect(it)) }
                            )

                            is KeyboardKey.Shift -> ShiftKeyButton(
                                weight = key.weight,
                                isActive = state.value.isUpperCase,
                                onClick = { onEvent(KeyboardEvent.OnShift) }
                            )

                            is KeyboardKey.Backspace -> BackspaceKeyButton(
                                weight = key.weight,
                                onClick = { onEvent(KeyboardEvent.OnBackspace) }
                            )

                            is KeyboardKey.Space -> SpaceKeyButton(
                                weight = key.weight,
                                modifier = Modifier.weight(key.weight),
                                onClick = { onEvent(KeyboardEvent.OnSpace) }
                            )

                            is KeyboardKey.Enter -> EnterKeyButton(
                                weight = key.weight,
                                onClick = { onEvent(KeyboardEvent.OnEnter) }
                            )

                            is KeyboardKey.SymbolsSwitch -> SpecialKeyButton(
                                label = "123",
                                weight = key.weight,
                                onClick = { onEvent(KeyboardEvent.OnSymbolsSwitch) }
                            )

                            is KeyboardKey.EmojiSwitch -> SpecialKeyButton(
                                label = "☺",
                                weight = key.weight,
                                onClick = { onEvent(KeyboardEvent.OnEmojiSwitch) }
                            )
                        }
                    }
                }
            }
        }
    }
}