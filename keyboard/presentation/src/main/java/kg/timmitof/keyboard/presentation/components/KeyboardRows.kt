package kg.timmitof.keyboard.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kg.timmitof.keyboard.domain.model.KeyboardKey
import kg.timmitof.keyboard.domain.model.KeyboardLayout
import kg.timmitof.keyboard.presentation.components.keys.BackspaceKeyButton
import kg.timmitof.keyboard.presentation.components.keys.EnterKeyButton
import kg.timmitof.keyboard.presentation.components.keys.KeyboardKeyButton
import kg.timmitof.keyboard.presentation.components.keys.ShiftKeyButton
import kg.timmitof.keyboard.presentation.components.keys.SpaceKeyButton
import kg.timmitof.keyboard.presentation.components.keys.SpecialKeyButton
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardEvent
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardState
import kg.timmitof.keyboard.presentation.screens.keyboard.states.isUpperCase

/** Высота одного ряда клавиш. */
internal val KeyRowHeight = 48.dp

/** Вертикальный промежуток между рядами. */
internal val KeyRowSpacing = 8.dp

/** Горизонтальный промежуток между клавишами в ряду. */
internal val KeySpacing = 6.dp

/**
 * Ряды клавиш раскладки: чистая разметка, каждая клавиша
 * маппится на свою кнопку в [KeyboardKeySlot].
 */
@Composable
internal fun KeyboardRows(
    layout: KeyboardLayout,
    state: State<KeyboardState>,
    onEvent: (KeyboardEvent) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(KeyRowSpacing),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        layout.rows.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(KeyRowHeight),
                horizontalArrangement = Arrangement.spacedBy(KeySpacing),
                verticalAlignment = Alignment.CenterVertically
            ) {
                row.forEach { key ->
                    KeyboardKeySlot(key = key, state = state, onEvent = onEvent)
                }
            }
        }
    }
}

/** Маппинг [KeyboardKey] → соответствующая кнопка из components. */
@Composable
private fun RowScope.KeyboardKeySlot(
    key: KeyboardKey,
    state: State<KeyboardState>,
    onEvent: (KeyboardEvent) -> Unit
) {
    when (key) {
        is KeyboardKey.Character -> KeyboardKeyButton(
            label = if (state.value.shiftState.isUpperCase()) key.labelUpper else key.labelLower,
            isUpperCase = state.value.shiftState.isUpperCase(),
            weight = key.weight,
            longPress = key.longPress,
            onClick = { onEvent(KeyboardEvent.OnKeySelect(it)) }
        )

        is KeyboardKey.Shift -> ShiftKeyButton(
            weight = key.weight,
            shiftState = state.value.shiftState,
            onClick = { onEvent(KeyboardEvent.OnShift) }
        )

        is KeyboardKey.Backspace -> BackspaceKeyButton(
            weight = key.weight,
            onDeleteWord = { onEvent(KeyboardEvent.OnBackspaceDeleteWord) },
            onSelectChange = { onEvent(KeyboardEvent.OnBackspaceSelectChange(it)) },
            onSelectCommit = { onEvent(KeyboardEvent.OnBackspaceSelectCommit(it)) },
            onClick = { onEvent(KeyboardEvent.OnBackspace) }
        )

        is KeyboardKey.Space -> SpaceKeyButton(
            weight = key.weight,
            onClick = { onEvent(KeyboardEvent.OnSpace) }
        )

        is KeyboardKey.Enter -> EnterKeyButton(
            weight = key.weight,
            onClick = { onEvent(KeyboardEvent.OnEnter) }
        )

        else -> key.switchAction()?.let { (label, event) ->
            SpecialKeyButton(
                label = label,
                weight = key.weight,
                onClick = { onEvent(event) }
            )
        }
    }
}

/** Label и событие для клавиш-переключателей слоёв; null для остальных клавиш. */
private fun KeyboardKey.switchAction(): Pair<String, KeyboardEvent>? = when (this) {
    is KeyboardKey.SymbolsSwitch -> "123" to KeyboardEvent.OnSymbolsSwitch
    is KeyboardKey.SymbolsAltSwitch -> label to KeyboardEvent.OnSymbolsAltSwitch
    is KeyboardKey.AbcSwitch -> "ABC" to KeyboardEvent.OnAbcSwitch
    is KeyboardKey.EmojiSwitch -> "☺" to KeyboardEvent.OnEmojiSwitch
    else -> null
}
