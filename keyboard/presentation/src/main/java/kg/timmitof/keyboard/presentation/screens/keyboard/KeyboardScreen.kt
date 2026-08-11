package kg.timmitof.keyboard.presentation.screens.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kg.timmitof.keyboard.presentation.R
import kg.timmitof.keyboard.domain.model.KeyboardLayout
import kg.timmitof.keyboard.presentation.components.FontsCarousel
import kg.timmitof.keyboard.presentation.components.KeyRowHeight
import kg.timmitof.keyboard.presentation.components.KeyRowSpacing
import kg.timmitof.keyboard.presentation.components.KeyboardRows
import kg.timmitof.keyboard.presentation.components.TopStripHeight
import kg.timmitof.keyboard.presentation.components.emoji.EmojiPanel
import kg.timmitof.keyboard.presentation.components.emoji.EmojiSearchBar
import kg.timmitof.keyboard.presentation.insets.LocalKeyboardInsets
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardEvent
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardLayer
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardState
import kg.timmitof.keyboard.presentation.theme.KFTheme
import org.orbitmvi.orbit.compose.collectAsState

/** Высота зоны клавиш: 4 ряда вплотную — промежутки входят в высоту самих рядов. */
private val KeyboardBodyHeight = KeyRowHeight * 4

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
        // В режиме трекпада (зажат пробел) верхнюю панель отдаём под подсказку.
        if (state.value.isCursorMode) {
            CursorModeHint(modifier = Modifier.fillMaxWidth())
        } else {
            FontsCarousel(
                fonts = state.value.fonts,
                selectedFontId = state.value.selectedFont.id,
                onFontSelect = { onEvent(KeyboardEvent.OnFontSelect(it)) },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Половину зазора добавит верхний ряд клавиш — здесь нужна только вторая половина.
        Spacer(modifier = Modifier.height(KeyRowSpacing / 2))

        KeyboardRows(
            layout = state.value.keyboardLayout,
            state = state,
            onEvent = onEvent
        )
    }
}

@Composable
private fun CursorModeHint(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.height(TopStripHeight),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.cursor_mode_hint),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = KFTheme.color.keySpecialTextColor,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

/** Слой эмодзи: панель категорий с недавними и нижней навигацией */
@Composable
private fun EmojiLayer(
    state: State<KeyboardState>,
    onEvent: (KeyboardEvent) -> Unit
) {
    EmojiPanel(
        categories = state.value.emojiCategories,
        recentEmojis = state.value.recentEmojis,
        onEvent = onEvent,
        modifier = Modifier.height(KeyboardBodyHeight),
        emojiVariants = state.value.emojiVariants,
        preferredVariants = state.value.preferredEmojiVariants
    )
}

/** Слой поиска эмодзи: строка поиска с результатами + буквенная раскладка */
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

        // Половину зазора добавит верхний ряд клавиш — здесь нужна только вторая половина.
        Spacer(modifier = Modifier.height(KeyRowSpacing / 2))

        KeyboardRows(
            layout = layout,
            state = state,
            onEvent = onEvent
        )
    }
}