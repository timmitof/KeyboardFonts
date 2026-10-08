package kg.timmitof.core.ui.components.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kg.timmitof.core.ui.R
import kg.timmitof.core.ui.components.reorder.rememberReorderState
import kg.timmitof.core.ui.theme.appColors

/**
 * Карточка-список с перестановкой за ручку справа (панель шрифтов, языки).
 * Список стоит внутри прокрутки экрана, поэтому своя прокрутка выключена, а высота задана заранее:
 * у всех строк она одинаковая — [rowHeight].
 *
 * Пока строку тащат, порядок живёт локально; наружу ([onReorder]) уходит один раз, по отпусканию.
 *
 * @param onClick нажатие на строку; `null` — строка не нажимается.
 * @param row содержимое строки слева от ручки.
 */
@Composable
fun <T : Any> SettingsReorderList(
    items: List<T>,
    key: (T) -> Any,
    onReorder: (List<T>) -> Unit,
    modifier: Modifier = Modifier,
    rowHeight: Dp = SettingsListRowHeight,
    isClickEnabled: Boolean = true,
    onClick: ((T) -> Unit)? = null,
    row: @Composable RowScope.(T) -> Unit,
) {
    // Один объект на весь экран: жесты строк захватывают его в pointerInput и не должны терять.
    val order = remember { mutableStateListOf<T>().apply { addAll(items) } }
    val listState = rememberLazyListState()
    val reorder = rememberReorderState(listState) { from, to -> order.add(to, order.removeAt(from)) }

    LaunchedEffect(items) {
        if (reorder.draggedKey == null && order != items) {
            order.clear()
            order.addAll(items)
        }
    }

    val currentOnReorder by rememberUpdatedState(onReorder)
    val currentOnClick by rememberUpdatedState(onClick)
    val haptic = LocalHapticFeedback.current
    val finishDrag = {
        reorder.onDragEnd()
        currentOnReorder(order.toList())
    }

    SettingsListCard(modifier = modifier) {
        LazyColumn(
            modifier = Modifier.height(rowHeight * order.size),
            state = listState,
            userScrollEnabled = false,
        ) {
            itemsIndexed(order, key = { _, item -> key(item) }) { index, item ->
                val itemKey = key(item)
                val isDragged = reorder.isDragged(itemKey)
                val elevation by animateFloatAsState(
                    targetValue = if (isDragged) DraggedElevation else 0f,
                    animationSpec = spring(stiffness = 900f),
                    label = "reorderRowElevation",
                )

                SettingsListRow(
                    height = rowHeight,
                    hasDivider = index > 0 && !isDragged,
                    onClick = currentOnClick?.let { click -> { click(item) } },
                    isClickEnabled = isClickEnabled,
                    modifier = Modifier
                        .animateItem(
                            fadeInSpec = null,
                            fadeOutSpec = null,
                            placementSpec = if (isDragged) null else spring(stiffness = 600f),
                        )
                        .zIndex(if (isDragged) 1f else 0f)
                        .graphicsLayer {
                            translationY = if (isDragged) reorder.draggedOffset else 0f
                            shadowElevation = elevation
                            shape = DraggedRowShape
                            clip = elevation > 0f
                        },
                ) {
                    row(item)
                    DragHandle(
                        modifier = Modifier.pointerInput(itemKey) {
                            detectDragGestures(
                                onDragStart = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    reorder.onDragStart(itemKey)
                                },
                                onDragEnd = finishDrag,
                                onDragCancel = finishDrag,
                                onDrag = { change, amount ->
                                    change.consume()
                                    reorder.onDrag(amount.y)
                                },
                            )
                        },
                    )
                }
            }
        }
    }
}

/**
 * Карточка-список строк одинакового вида без перестановки (скрытые шрифты, выбор из каталога).
 *
 * @param onClick нажатие на строку; `null` — строка не нажимается.
 */
@Composable
fun <T : Any> SettingsCheckList(
    items: List<T>,
    key: (T) -> Any,
    modifier: Modifier = Modifier,
    rowHeight: Dp = SettingsListRowHeight,
    onClick: ((T) -> Unit)? = null,
    row: @Composable RowScope.(T) -> Unit,
) {
    val currentOnClick by rememberUpdatedState(onClick)

    SettingsListCard(modifier = modifier) {
        Column {
            items.forEachIndexed { index, item ->
                key(key(item)) {
                    SettingsListRow(
                        height = rowHeight,
                        hasDivider = index > 0,
                        onClick = currentOnClick?.let { click -> { click(item) } },
                    ) {
                        row(item)
                    }
                }
            }
        }
    }
}

/** Квадратный флажок строки списка: отмечен — заливка бренда с галочкой, нет — контур. */
@Composable
fun SettingsCheckbox(
    isChecked: Boolean,
    modifier: Modifier = Modifier,
) {
    val tones = MaterialTheme.appColors.brand
    val background by animateColorAsState(
        targetValue = if (isChecked) tones.solid else Color.Transparent,
        animationSpec = spring(stiffness = 900f),
        label = "settingsCheckbox",
    )

    Box(
        modifier = modifier
            .size(22.dp)
            .clip(CheckboxShape)
            .drawBehind { drawRect(background) }
            .then(
                if (isChecked) Modifier else Modifier.border(2.dp, MaterialTheme.colorScheme.outline, CheckboxShape)
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (isChecked) {
            Icon(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = null,
                tint = tones.onSolid,
                modifier = Modifier.size(13.dp),
            )
        }
    }
}

@Composable
private fun SettingsListCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = SettingsCardShape,
        color = MaterialTheme.appColors.card,
        content = content,
    )
}

@Composable
private fun SettingsListRow(
    height: Dp,
    hasDivider: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    isClickEnabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    val card = MaterialTheme.appColors.card
    val divider = MaterialTheme.colorScheme.onBackground.copy(alpha = DividerAlpha)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(card)
            .drawBehind {
                if (hasDivider) {
                    drawLine(divider, Offset.Zero, Offset(size.width, 0f), strokeWidth = 1.dp.toPx())
                }
            }
            .then(if (onClick != null) Modifier.clickable(enabled = isClickEnabled, onClick = onClick) else Modifier)
            .padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

@Composable
private fun DragHandle(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .padding(start = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_drag_handle),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(20.dp),
        )
    }
}

/** Высота строки как у списка шрифтов; строкам с подписью в две строки нужна [SettingsListTallRowHeight]. */
val SettingsListRowHeight = 46.dp
val SettingsListTallRowHeight = 58.dp

private val DraggedRowShape = RoundedCornerShape(12.dp)
private val CheckboxShape = RoundedCornerShape(6.dp)
private const val DraggedElevation = 16f
private const val DividerAlpha = 0.06f
