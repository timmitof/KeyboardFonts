package kg.timmitof.keyboard.presentation.screens.keyboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kg.timmitof.keyboard.domain.model.KeyboardKey
import kg.timmitof.keyboard.domain.model.KeyboardLayout
import kg.timmitof.keyboard.presentation.components.BackspaceKeyButton
import kg.timmitof.keyboard.presentation.components.EnterKeyButton
import kg.timmitof.keyboard.presentation.components.KeyboardKeyButton
import kg.timmitof.keyboard.presentation.components.ShiftKeyButton
import kg.timmitof.keyboard.presentation.components.SpaceKeyButton
import kg.timmitof.keyboard.presentation.components.SpecialKeyButton
import kg.timmitof.keyboard.presentation.components.emoji.EmojiPanel
import kg.timmitof.keyboard.presentation.components.emoji.EmojiSearchBar
import kg.timmitof.keyboard.presentation.theme.KFTheme
import org.orbitmvi.orbit.compose.collectAsState

/** Высота зоны клавиш: 4 ряда по 48dp + 3 промежутка по 8dp. */
private val KeyboardBodyHeight = 48.dp * 4 + 8.dp * 3

@Composable
internal fun KeyboardFontsScreen(viewModel: KeyboardViewModel) {
    val state = viewModel.collectAsState()

    KeyboardContent(
        state = state,
        onEvent = viewModel::onEvent
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun KeyboardContent(
    state: State<KeyboardState>,
    onEvent: (KeyboardEvent) -> Unit
) {
    val motionScheme = MaterialTheme.motionScheme

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(KFTheme.color.keyboardBackground)
            .padding(horizontal = 6.dp, vertical = 8.dp)
    ) {
        AnimatedContent(
            targetState = state.value.layer to state.value.keyboardLayout,
            transitionSpec = {
                (fadeIn(motionScheme.fastEffectsSpec()) +
                        slideInVertically(motionScheme.fastSpatialSpec()) { it / 10 })
                    .togetherWith(fadeOut(motionScheme.fastEffectsSpec()))
            },
            label = "keyboard-layer"
        ) { (layer, layout) ->
            when (layer) {
                KeyboardLayer.EMOJI -> EmojiPanel(
                    categories = state.value.emojiCategories,
                    recentEmojis = state.value.recentEmojis,
                    onEmojiSelect = { onEvent(KeyboardEvent.OnEmojiSelect(it)) },
                    onSearchClick = { onEvent(KeyboardEvent.OnEmojiSearchOpen) },
                    onAbcClick = { onEvent(KeyboardEvent.OnAbcSwitch) },
                    onBackspaceClick = { onEvent(KeyboardEvent.OnBackspace) },
                    modifier = Modifier.height(KeyboardBodyHeight)
                )

                KeyboardLayer.EMOJI_SEARCH -> Column(modifier = Modifier.fillMaxWidth()) {
                    EmojiSearchBar(
                        query = state.value.emojiSearchQuery,
                        results = state.value.emojiSearchResults,
                        selectionChars = state.value.emojiSearchSelection,
                        onQueryChange = { onEvent(KeyboardEvent.OnEmojiSearchQueryChange(it)) },
                        onEmojiSelect = { onEvent(KeyboardEvent.OnEmojiSelect(it)) },
                        onClose = { onEvent(KeyboardEvent.OnEmojiSearchClose) },
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    KeyboardRows(
                        layout = layout,
                        state = state,
                        onEvent = onEvent
                    )
                }

                else -> KeyboardRows(
                    layout = layout,
                    state = state,
                    onEvent = onEvent
                )
            }
        }
    }
}

@Composable
private fun KeyboardRows(
    layout: KeyboardLayout,
    state: State<KeyboardState>,
    onEvent: (KeyboardEvent) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        layout.rows.forEach { row ->
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
                            isCapsLock = state.value.isCapsLock,
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

                        is KeyboardKey.SymbolsAltSwitch -> SpecialKeyButton(
                            label = key.label,
                            weight = key.weight,
                            onClick = { onEvent(KeyboardEvent.OnSymbolsAltSwitch) }
                        )

                        is KeyboardKey.AbcSwitch -> SpecialKeyButton(
                            label = "ABC",
                            weight = key.weight,
                            onClick = { onEvent(KeyboardEvent.OnAbcSwitch) }
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