package kg.timmitof.keyboard.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kg.timmitof.keyboard.domain.model.KeyboardKey
import kg.timmitof.keyboard.domain.model.KeyboardLayout
import kg.timmitof.keyboard.presentation.R
import kg.timmitof.keyboard.presentation.components.keys.BackspaceKeyButton
import kg.timmitof.keyboard.presentation.components.keys.EnterKeyButton
import kg.timmitof.keyboard.presentation.components.keys.KeyboardKeyButton
import kg.timmitof.keyboard.presentation.components.keys.ShiftKeyButton
import kg.timmitof.keyboard.presentation.components.keys.SpaceKeyButton
import kg.timmitof.keyboard.presentation.components.keys.SpecialIconKeyButton
import kg.timmitof.keyboard.presentation.components.keys.SpecialKeyButton
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardEvent
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardState
import kg.timmitof.keyboard.presentation.screens.keyboard.states.isUpperCase

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
    val rowHeight = LocalKeyRowHeight.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        layout.rows.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(rowHeight),
                verticalAlignment = Alignment.CenterVertically
            ) {
                row.forEach { key ->
                    KeyboardKeySlot(
                        key = key,
                        state = state,
                        isLargeLabel = layout.largeLabels,
                        hasSubLabels = layout.hasSubLabels,
                        onEvent = onEvent
                    )
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
    isLargeLabel: Boolean,
    hasSubLabels: Boolean,
    onEvent: (KeyboardEvent) -> Unit
) {
    when (key) {
        is KeyboardKey.Character -> KeyboardKeyButton(
            key = key,
            isUpperCase = state.value.shiftState.isUpperCase(),
            isLargeLabel = isLargeLabel,
            hasSubLabels = hasSubLabels,
            font = state.value.activeFont,
            onInput = { onEvent(KeyboardEvent.OnKeySelect(it)) }
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
            languages = state.value.languages,
            selectedLanguage = state.value.activeLanguage,
            isLanguageSlideEnabled = state.value.fieldType.allowsLanguageSlide,
            onLanguageSelect = { onEvent(KeyboardEvent.OnLanguageSelect(it)) },
            onCursorMove = { horizontal, vertical ->
                onEvent(KeyboardEvent.OnCursorMove(horizontal, vertical))
            },
            onCursorModeChange = { onEvent(KeyboardEvent.OnCursorModeChange(it)) },
            onClick = { onEvent(KeyboardEvent.OnSpace) }
        )

        is KeyboardKey.Enter -> EnterKeyButton(
            weight = key.weight,
            enterAction = state.value.displayedEnterAction,
            onClick = { onEvent(KeyboardEvent.OnEnter) }
        )

        is KeyboardKey.EmojiSwitch -> SpecialIconKeyButton(
            iconRes = R.drawable.ic_emoji_key,
            contentDescription = "Emoji",
            weight = key.weight,
            onClick = { onEvent(KeyboardEvent.OnEmojiSwitch) }
        )

        is KeyboardKey.Spacer -> Spacer(modifier = Modifier.weight(key.weight))

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
    else -> null
}
