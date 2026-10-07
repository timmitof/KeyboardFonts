package kg.timmitof.feature_settings.presentation.components

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kg.timmitof.core.ui.R as UiR
import kg.timmitof.core.ui.components.reorder.rememberReorderState
import kg.timmitof.core.ui.components.settings.SettingsCardShape
import kg.timmitof.core.ui.theme.appColors
import kg.timmitof.feature_settings.presentation.R

@Immutable
data class FontItem(
    val id: String,
    val name: String,
    val sample: String,
)

/** Список внутри прокрутки экрана, поэтому прокрутку у него отключаем, а высоту задаём заранее. */
@Composable
internal fun VisibleFontsList(
    fonts: List<FontItem>,
    canHide: Boolean,
    onHide: (FontItem) -> Unit,
    onReorder: (List<FontItem>) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Один объект на весь экран: жесты строк захватывают его в pointerInput и не должны терять.
    val order = remember { mutableStateListOf<FontItem>().apply { addAll(fonts) } }
    val listState = rememberLazyListState()
    val reorder = rememberReorderState(listState) { from, to -> order.add(to, order.removeAt(from)) }

    LaunchedEffect(fonts) {
        if (reorder.draggedKey == null && order != fonts) {
            order.clear()
            order.addAll(fonts)
        }
    }

    val currentOnReorder by rememberUpdatedState(onReorder)
    val haptic = LocalHapticFeedback.current
    val finishDrag = {
        reorder.onDragEnd()
        currentOnReorder(order.toList())
    }

    FontsCard(modifier = modifier) {
        LazyColumn(
            modifier = Modifier.height(RowHeight * order.size),
            state = listState,
            userScrollEnabled = false,
        ) {
            itemsIndexed(order, key = { _, item -> item.id }) { index, item ->
                val isDragged = reorder.isDragged(item.id)
                val elevation by animateFloatAsState(
                    targetValue = if (isDragged) DraggedElevation else 0f,
                    animationSpec = spring(stiffness = 900f),
                    label = "fontRowElevation",
                )

                FontRow(
                    item = item,
                    isVisible = true,
                    isEnabled = canHide,
                    hasDivider = index > 0 && !isDragged,
                    onClick = { onHide(item) },
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
                            shape = RowShape
                            clip = elevation > 0f
                        },
                    handle = {
                        DragHandle(
                            modifier = Modifier.pointerInput(item.id) {
                                detectDragGestures(
                                    onDragStart = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        reorder.onDragStart(item.id)
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
                    },
                )
            }
        }
    }
}

@Composable
internal fun HiddenFontsList(
    fonts: List<FontItem>,
    onShow: (FontItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    FontsCard(modifier = modifier) {
        Column {
            fonts.forEachIndexed { index, item ->
                FontRow(
                    item = item,
                    isVisible = false,
                    isEnabled = true,
                    hasDivider = index > 0,
                    onClick = { onShow(item) },
                )
            }
        }
    }
}

@Composable
private fun FontsCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = SettingsCardShape,
        color = MaterialTheme.appColors.card,
        content = content,
    )
}

@Composable
private fun FontRow(
    item: FontItem,
    isVisible: Boolean,
    isEnabled: Boolean,
    hasDivider: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    handle: (@Composable () -> Unit)? = null,
) {
    val card = MaterialTheme.appColors.card
    val divider = MaterialTheme.colorScheme.onBackground.copy(alpha = DividerAlpha)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(RowHeight)
            .background(card)
            .drawBehind {
                if (hasDivider) {
                    drawLine(divider, Offset.Zero, Offset(size.width, 0f), strokeWidth = 1.dp.toPx())
                }
            }
            .clickable(enabled = isEnabled, onClick = onClick)
            .padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FontCheckbox(isChecked = isVisible)
        Text(
            modifier = Modifier.weight(1f),
            text = item.sample,
            fontSize = 18.sp,
            color = if (isVisible) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.outline,
            maxLines = 1,
        )
        Text(
            text = item.name,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.outline,
            maxLines = 1,
        )
        handle?.invoke()
    }
}

@Composable
private fun FontCheckbox(isChecked: Boolean) {
    val tones = MaterialTheme.appColors.brand
    val background by animateColorAsState(
        targetValue = if (isChecked) tones.solid else Color.Transparent,
        animationSpec = spring(stiffness = 900f),
        label = "fontCheckbox",
    )

    Box(
        modifier = Modifier
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
                painter = painterResource(UiR.drawable.ic_check),
                contentDescription = null,
                tint = tones.onSolid,
                modifier = Modifier.size(13.dp),
            )
        }
    }
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

private val RowHeight = 46.dp
private val RowShape = RoundedCornerShape(12.dp)
private val CheckboxShape = RoundedCornerShape(6.dp)
private const val DraggedElevation = 16f
private const val DividerAlpha = 0.06f
