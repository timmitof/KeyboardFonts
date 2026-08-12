package kg.timmitof.keyboard.presentation.screens.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kg.timmitof.keyboard.domain.model.KeyboardLayout
import kg.timmitof.keyboard.presentation.components.KeyRowSpacing
import kg.timmitof.keyboard.presentation.components.KeyboardRows
import kg.timmitof.keyboard.presentation.components.emoji.EmojiPanel
import kg.timmitof.keyboard.presentation.components.emoji.EmojiSearchBar
import kg.timmitof.keyboard.presentation.components.topbar.KeyboardTopBar
import kg.timmitof.keyboard.presentation.insets.LocalKeyboardInsets
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardEvent
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardLayer
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardState
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

/** Каркас клавиатуры: фон + переключение между слоями */
@Composable
private fun KeyboardContent(
    state: State<KeyboardState>,
    onEvent: (KeyboardEvent) -> Unit
) {
    val insets = LocalKeyboardInsets.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(KFTheme.color.keyboardBackground)
            .padding(start = insets.left, end = insets.right, bottom = insets.bottom)
            .padding(horizontal = 3.dp, vertical = 8.dp)
    ) {
        when (state.value.layer) {
            KeyboardLayer.EMOJI -> EmojiLayer(
                state = state,
                onEvent = onEvent
            )

            KeyboardLayer.EMOJI_SEARCH -> EmojiSearchLayer(
                layout = state.value.keyboardLayout,
                state = state,
                onEvent = onEvent
            )

            else -> LettersLayer(
                state = state,
                onEvent = onEvent
            )
        }
    }
}

@Composable
private fun LettersLayer(
    state: State<KeyboardState>,
    onEvent: (KeyboardEvent) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        KeyboardTopBar(state = state, onEvent = onEvent)

        // Половину зазора добавит верхний ряд клавиш.
        Spacer(modifier = Modifier.height(KeyRowSpacing / 2))

        KeyboardRows(
            layout = state.value.keyboardLayout,
            state = state,
            onEvent = onEvent
        )
    }
}

/** Слой эмодзи */
@Composable
private fun EmojiLayer(
    state: State<KeyboardState>,
    onEvent: (KeyboardEvent) -> Unit
) {
    EmojiPanel(
        categories = state.value.emojiCategories,
        recentEmojis = state.value.recentEmojis,
        selectedLanguage = state.value.activeLanguage,
        onEvent = onEvent,
        emojiVariants = state.value.emojiVariants,
        preferredVariants = state.value.preferredEmojiVariants
    )
}

/** Слой поиска эмодзи */
@Composable
private fun EmojiSearchLayer(
    layout: KeyboardLayout,
    state: State<KeyboardState>,
    onEvent: (KeyboardEvent) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        EmojiSearchBar(
            query = state.value.emojiSearchQuery,
            results = state.value.emojiSearchResults,
            selectionChars = state.value.emojiSearchSelection,
            onEvent = onEvent,
            emojiVariants = state.value.emojiVariants,
            preferredVariants = state.value.preferredEmojiVariants
        )

        // Половину зазора добавит верхний ряд клавиш.
        Spacer(modifier = Modifier.height(KeyRowSpacing / 2))

        KeyboardRows(
            layout = layout,
            state = state,
            onEvent = onEvent
        )
    }
}