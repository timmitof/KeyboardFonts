package kg.timmitof.keyboard.presentation.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import kg.timmitof.keyboard.domain.model.KeyboardLanguage
import kg.timmitof.keyboard.font.domain.model.KeyboardFont
import kg.timmitof.keyboard.presentation.screens.keyboard.rememberSlice
import kg.timmitof.keyboard.presentation.screens.keyboard.states.EnterAction
import kg.timmitof.keyboard.presentation.screens.keyboard.states.ShiftState
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

/** Всё, что клавиши читают из состояния; data class, чтобы срез сравнивался по значению. */
@Immutable
private data class KeyRowsSlice(
    val shiftState: ShiftState,
    val font: KeyboardFont,
    val languages: List<KeyboardLanguage>,
    val language: KeyboardLanguage?,
    val isLanguageSlideEnabled: Boolean,
    val enterAction: EnterAction,
)

@Composable
internal fun KeyboardRows(
    layout: KeyboardLayout,
    state: State<KeyboardState>,
    onEvent: (KeyboardEvent) -> Unit
) {
    val metrics = LocalKeyboardMetrics.current
    val slice by state.rememberSlice {
        KeyRowsSlice(
            shiftState = it.shiftState,
            font = it.activeFont,
            languages = it.languages,
            language = it.activeLanguage,
            isLanguageSlideEnabled = it.fieldType.allowsLanguageSlide,
            enterAction = it.displayedEnterAction,
        )
    }

    val rows = remember(layout, metrics.hasHideKey) {
        if (metrics.hasHideKey) layout.rows.withHideKey() else layout.rows
    }
    val density = LocalDensity.current
    // Ряды ниже — подписи меньше: sp масштабируются, dp-размеры клавиш не трогаем.
    val labelDensity = remember(density, metrics.labelScale) {
        Density(density.density, density.fontScale * metrics.labelScale)
    }

    CompositionLocalProvider(LocalDensity provides labelDensity) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val halfWidth = metrics.splitHalfWidth(maxWidth)
            if (halfWidth == null) {
                FullRows(rows, metrics.rowHeight, slice, layout, onEvent)
            } else {
                SplitRows(rows, metrics.rowHeight, halfWidth, slice, layout, onEvent)
            }
        }
    }
}

@Composable
private fun FullRows(
    rows: List<List<KeyboardKey>>,
    rowHeight: Dp,
    slice: KeyRowsSlice,
    layout: KeyboardLayout,
    onEvent: (KeyboardEvent) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(rowHeight),
                verticalAlignment = Alignment.CenterVertically
            ) {
                row.forEach { key ->
                    KeyboardKeySlot(
                        key = key,
                        slice = slice,
                        isLargeLabel = layout.largeLabels,
                        hasSubLabels = layout.hasSubLabels,
                        onEvent = onEvent
                    )
                }
            }
        }
    }
}

/** Половины прижаты к краям; внешние края рядов ровные, а недостающая ширина уходит к середине. */
@Composable
private fun SplitRows(
    rows: List<List<KeyboardKey>>,
    rowHeight: Dp,
    halfWidth: Dp,
    slice: KeyRowsSlice,
    layout: KeyboardLayout,
    onEvent: (KeyboardEvent) -> Unit,
) {
    val halves = remember(rows) { rows.splitInHalves() }
    val unit = remember(halves) {
        halves.maxOf { maxOf(it.left.totalWeight(), it.right.totalWeight()) }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        halves.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(rowHeight),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HalfRow(row.left, unit, halfWidth, fillerAtStart = false, slice, layout, onEvent)
                Spacer(modifier = Modifier.weight(1f))
                HalfRow(row.right, unit, halfWidth, fillerAtStart = true, slice, layout, onEvent)
            }
        }
    }
}

@Composable
private fun HalfRow(
    keys: List<KeyboardKey>,
    unit: Float,
    width: Dp,
    fillerAtStart: Boolean,
    slice: KeyRowsSlice,
    layout: KeyboardLayout,
    onEvent: (KeyboardEvent) -> Unit,
) {
    val filler = unit - keys.totalWeight()

    Row(
        modifier = Modifier
            .width(width)
            .fillMaxHeight(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (fillerAtStart && filler > 0f) Spacer(modifier = Modifier.weight(filler))
        keys.forEach { key ->
            KeyboardKeySlot(
                key = key,
                slice = slice,
                isLargeLabel = layout.largeLabels,
                hasSubLabels = layout.hasSubLabels,
                onEvent = onEvent
            )
        }
        if (!fillerAtStart && filler > 0f) Spacer(modifier = Modifier.weight(filler))
    }
}

private fun List<KeyboardKey>.totalWeight(): Float = sumOf { it.weight.toDouble() }.toFloat()

@Composable
private fun RowScope.KeyboardKeySlot(
    key: KeyboardKey,
    slice: KeyRowsSlice,
    isLargeLabel: Boolean,
    hasSubLabels: Boolean,
    onEvent: (KeyboardEvent) -> Unit
) {
    when (key) {
        is KeyboardKey.Character -> KeyboardKeyButton(
            key = key,
            isUpperCase = slice.shiftState.isUpperCase(),
            isLargeLabel = isLargeLabel,
            hasSubLabels = hasSubLabels,
            font = slice.font,
            onInput = { onEvent(KeyboardEvent.OnKeySelect(it)) }
        )

        is KeyboardKey.Shift -> ShiftKeyButton(
            weight = key.weight,
            shiftState = slice.shiftState,
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
            languages = slice.languages,
            selectedLanguage = slice.language,
            isLanguageSlideEnabled = slice.isLanguageSlideEnabled,
            onLanguageSelect = { onEvent(KeyboardEvent.OnLanguageSelect(it)) },
            onCursorMove = { horizontal, vertical ->
                onEvent(KeyboardEvent.OnCursorMove(horizontal, vertical))
            },
            onCursorModeChange = { onEvent(KeyboardEvent.OnCursorModeChange(it)) },
            onClick = { onEvent(KeyboardEvent.OnSpace) }
        )

        is KeyboardKey.Enter -> EnterKeyButton(
            weight = key.weight,
            enterAction = slice.enterAction,
            onClick = { onEvent(KeyboardEvent.OnEnter) }
        )

        is KeyboardKey.EmojiSwitch -> SpecialIconKeyButton(
            iconRes = R.drawable.ic_emoji_key,
            contentDescription = "Emoji",
            weight = key.weight,
            onClick = { onEvent(KeyboardEvent.OnEmojiSwitch) }
        )

        is KeyboardKey.HideKeyboard -> SpecialIconKeyButton(
            iconRes = R.drawable.ic_keyboard_hide,
            contentDescription = stringResource(R.string.key_hide_keyboard),
            weight = key.weight,
            onClick = { onEvent(KeyboardEvent.OnHideKeyboard) }
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

private fun KeyboardKey.switchAction(): Pair<String, KeyboardEvent>? = when (this) {
    is KeyboardKey.SymbolsSwitch -> "123" to KeyboardEvent.OnSymbolsSwitch
    is KeyboardKey.SymbolsAltSwitch -> label to KeyboardEvent.OnSymbolsAltSwitch
    is KeyboardKey.AbcSwitch -> "ABC" to KeyboardEvent.OnAbcSwitch
    else -> null
}
