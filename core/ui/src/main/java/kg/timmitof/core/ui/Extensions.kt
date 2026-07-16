package kg.timmitof.core.ui

import android.content.Context
import android.os.SystemClock
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Функция расширяющая [Context], для показа тоста
 */
fun Context.showToast(message: String) =
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

/**
 * Метод предотвращающий множественное нажатие
 */
fun Modifier.debounceClickable(
    debounceTime: Long = 1000L,
    onClick: () -> Unit
): Modifier {
    return this.composed {
        val clickable = debounced(debounceTime = debounceTime, onClick = { onClick() })
        this.clickable { clickable() }
    }
}

fun Modifier.carouselItemEffect(
    listState: LazyListState,
    index: Int,
    minScale: Float = 0f,
    minAlpha: Float = 0f,
): Modifier = graphicsLayer {
    val itemInfo = listState.layoutInfo.visibleItemsInfo
        .firstOrNull { it.index == index }
        ?: return@graphicsLayer

    if (itemInfo.size == 0) return@graphicsLayer

    val viewportStart = listState.layoutInfo.viewportStartOffset
    val viewportEnd = listState.layoutInfo.viewportEndOffset
    val itemStart = itemInfo.offset
    val itemEnd = itemInfo.offset + itemInfo.size

    val visiblePart = (minOf(itemEnd, viewportEnd) - maxOf(itemStart, viewportStart))
        .coerceIn(0, itemInfo.size)
    val fraction = visiblePart.toFloat() / itemInfo.size

    transformOrigin = when {
        itemStart < viewportStart -> TransformOrigin(1f, 0.5f)
        itemEnd > viewportEnd -> TransformOrigin(0f, 0.5f)
        else -> TransformOrigin.Center
    }

    alpha = minAlpha + (1f - minAlpha) * fraction
    scaleX = minScale + (1f - minScale) * fraction
    scaleY = scaleX
}

fun Modifier.backspaceHoldSlideClickable(
    interactionSource: MutableInteractionSource,
    slideStep: Dp = 12.dp,
    holdDelayMillis: Long = 500L,
    holdRepeatIntervalMillis: Long = 300L,
    holdMinRepeatIntervalMillis: Long = 80L,
    holdAccelerationFactor: Float = 0.8f,
    onTap: () -> Unit,
    onHold: () -> Unit,
    onSlideChange: (steps: Int) -> Unit,
    onSlideFinish: (steps: Int) -> Unit,
): Modifier = composed {
    val currentOnTap by rememberUpdatedState(onTap)
    val currentOnHold by rememberUpdatedState(onHold)
    val currentOnSlideChange by rememberUpdatedState(onSlideChange)
    val currentOnSlideFinish by rememberUpdatedState(onSlideFinish)

    pointerInput(interactionSource) {
        val stepPx = slideStep.toPx()

        coroutineScope {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                val press = PressInteraction.Press(down.position)
                launch { interactionSource.emit(press) }

                var holdFired = false
                val holdJob = launch {
                    delay(holdDelayMillis)
                    holdFired = true
                    // Повторяем срабатывание с ускорением, пока палец не отпущен
                    var interval = holdRepeatIntervalMillis
                    while (true) {
                        currentOnHold()
                        delay(interval)
                        interval = (interval * holdAccelerationFactor).toLong()
                            .coerceAtLeast(holdMinRepeatIntervalMillis)
                    }
                }

                var totalDx = 0f
                var isSliding = false
                var slideSteps = 0

                while (true) {
                    val event = awaitPointerEvent()
                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                    if (!change.pressed) break

                    totalDx += change.positionChange().x

                    // Пока «очистить всё» не сработало, движение переводит жест в режим выделения
                    if (!isSliding && !holdFired && abs(totalDx) > viewConfiguration.touchSlop) {
                        isSliding = true
                        holdJob.cancel()
                    }

                    if (isSliding) {
                        change.consume()
                        // Слайд влево увеличивает выделение назад от курсора
                        val steps = (-totalDx / stepPx).roundToInt().coerceAtLeast(0)
                        if (steps != slideSteps) {
                            slideSteps = steps
                            currentOnSlideChange(steps)
                        }
                    }
                }

                holdJob.cancel()

                when {
                    isSliding -> currentOnSlideFinish(slideSteps)
                    !holdFired -> currentOnTap()
                }

                launch { interactionSource.emit(PressInteraction.Release(press)) }
            }
        }
    }
}

fun Modifier.slidePickerClickable(
    interactionSource: MutableInteractionSource,
    onTap: () -> Unit,
    onSlideStart: () -> Unit,
    onSlideChange: (offsetPx: Float) -> Unit,
    onSlideFinish: (offsetPx: Float) -> Unit,
): Modifier = composed {
    val currentOnTap by rememberUpdatedState(onTap)
    val currentOnSlideStart by rememberUpdatedState(onSlideStart)
    val currentOnSlideChange by rememberUpdatedState(onSlideChange)
    val currentOnSlideFinish by rememberUpdatedState(onSlideFinish)

    pointerInput(interactionSource) {
        coroutineScope {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                val press = PressInteraction.Press(down.position)
                launch { interactionSource.emit(press) }

                var totalDx = 0f
                var isSliding = false

                while (true) {
                    val event = awaitPointerEvent()
                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                    if (!change.pressed) break

                    totalDx += change.positionChange().x

                    if (!isSliding && abs(totalDx) > viewConfiguration.touchSlop) {
                        isSliding = true
                        currentOnSlideStart()
                    }

                    if (isSliding) {
                        change.consume()
                        currentOnSlideChange(totalDx)
                    }
                }

                if (isSliding) currentOnSlideFinish(totalDx) else currentOnTap()

                launch { interactionSource.emit(PressInteraction.Release(press)) }
            }
        }
    }
}

fun Modifier.holdPickerClickable(
    interactionSource: MutableInteractionSource,
    holdDelayMillis: Long = 350L,
    onTap: () -> Unit,
    onHoldStart: () -> Unit,
    onPickChange: (offsetPx: Float) -> Unit,
    onPickFinish: (offsetPx: Float) -> Unit,
): Modifier = composed {
    val currentOnTap by rememberUpdatedState(onTap)
    val currentOnHoldStart by rememberUpdatedState(onHoldStart)
    val currentOnPickChange by rememberUpdatedState(onPickChange)
    val currentOnPickFinish by rememberUpdatedState(onPickFinish)

    pointerInput(interactionSource) {
        coroutineScope {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                val press = PressInteraction.Press(down.position)
                launch { interactionSource.emit(press) }

                var holdFired = false
                val holdJob = launch {
                    delay(holdDelayMillis)
                    holdFired = true
                    currentOnHoldStart()
                }

                var totalDx = 0f
                var movedBeforeHold = false

                while (true) {
                    val event = awaitPointerEvent()
                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                    if (!change.pressed) break

                    totalDx += change.positionChange().x

                    when {
                        holdFired -> {
                            change.consume()
                            currentOnPickChange(totalDx)
                        }

                        !movedBeforeHold && abs(totalDx) > viewConfiguration.touchSlop -> {
                            movedBeforeHold = true
                            holdJob.cancel()
                        }
                    }
                }

                holdJob.cancel()

                if (holdFired) currentOnPickFinish(totalDx) else currentOnTap()

                launch { interactionSource.emit(PressInteraction.Release(press)) }
            }
        }
    }
}

/**
 * Метод для задержки
 */
@Composable
inline fun debounced(crossinline onClick: () -> Unit, debounceTime: Long = 1000L): () -> Unit {
    var lastTimeClicked by remember { mutableLongStateOf(0L) }
    val onClickLambda: () -> Unit = {
        val now = SystemClock.uptimeMillis()
        if (now - lastTimeClicked > debounceTime) {
            lastTimeClicked = now
            onClick()
        }
    }
    return onClickLambda
}