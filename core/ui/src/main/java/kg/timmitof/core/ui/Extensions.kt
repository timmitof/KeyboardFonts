package kg.timmitof.core.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

fun Context.showToast(message: String) =
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

fun Modifier.backspaceHoldSlideClickable(
    interactionSource: MutableInteractionSource,
    slideStep: Dp = 12.dp,
    holdDelayMillis: Long = 500L,
    holdRepeatIntervalMillis: Long = 300L,
    holdMinRepeatIntervalMillis: Long = 80L,
    holdAccelerationFactor: Float = 0.8f,
    onPress: () -> Unit = {},
    onTap: () -> Unit,
    onHold: () -> Unit,
    onSlideChange: (steps: Int) -> Unit,
    onSlideFinish: (steps: Int) -> Unit,
): Modifier = composed {
    val currentOnPress by rememberUpdatedState(onPress)
    val currentOnTap by rememberUpdatedState(onTap)
    val currentOnHold by rememberUpdatedState(onHold)
    val currentOnSlideChange by rememberUpdatedState(onSlideChange)
    val currentOnSlideFinish by rememberUpdatedState(onSlideFinish)

    pointerInput(interactionSource) {
        val stepPx = slideStep.toPx()

        coroutineScope {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                currentOnPress()
                val press = PressInteraction.Press(down.position)
                launch { interactionSource.emit(press) }

                var holdFired = false
                val holdJob = launch {
                    delay(holdDelayMillis)
                    holdFired = true
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

fun Modifier.spaceCursorClickable(
    interactionSource: MutableInteractionSource,
    holdDelayMillis: Long = 280L,
    cursorStepX: Dp = 8.dp,
    cursorStepY: Dp = 32.dp,
    onPress: () -> Unit = {},
    onTap: () -> Unit,
    onSlideStart: () -> Unit,
    onSlideChange: (offsetPx: Float) -> Unit,
    onSlideFinish: (offsetPx: Float) -> Unit,
    onCursorStart: () -> Unit,
    onCursorMove: (horizontal: Int, vertical: Int) -> Unit,
    onCursorEnd: () -> Unit,
): Modifier = composed {
    val currentOnPress by rememberUpdatedState(onPress)
    val currentOnTap by rememberUpdatedState(onTap)
    val currentOnSlideStart by rememberUpdatedState(onSlideStart)
    val currentOnSlideChange by rememberUpdatedState(onSlideChange)
    val currentOnSlideFinish by rememberUpdatedState(onSlideFinish)
    val currentOnCursorStart by rememberUpdatedState(onCursorStart)
    val currentOnCursorMove by rememberUpdatedState(onCursorMove)
    val currentOnCursorEnd by rememberUpdatedState(onCursorEnd)

    pointerInput(interactionSource) {
        val stepXPx = cursorStepX.toPx()
        val stepYPx = cursorStepY.toPx()

        coroutineScope {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                currentOnPress()
                val press = PressInteraction.Press(down.position)
                launch { interactionSource.emit(press) }

                var totalDx = 0f
                var totalDy = 0f
                var isSliding = false
                var cursorMode = false

                var anchorX = 0f
                var anchorY = 0f
                var lastStepX = 0
                var lastStepY = 0

                val holdJob = launch {
                    delay(holdDelayMillis)
                    if (!isSliding) {
                        cursorMode = true
                        anchorX = totalDx
                        anchorY = totalDy
                        currentOnCursorStart()
                    }
                }

                while (true) {
                    val event = awaitPointerEvent()
                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                    if (!change.pressed) break

                    val delta = change.positionChange()
                    totalDx += delta.x
                    totalDy += delta.y

                    if (!cursorMode && !isSliding && abs(totalDx) > viewConfiguration.touchSlop) {
                        isSliding = true
                        holdJob.cancel()
                        currentOnSlideStart()
                    }

                    when {
                        cursorMode -> {
                            change.consume()
                            val stepX = ((totalDx - anchorX) / stepXPx).roundToInt()
                            val stepY = ((totalDy - anchorY) / stepYPx).roundToInt()
                            val dx = stepX - lastStepX
                            val dy = stepY - lastStepY
                            if (dx != 0 || dy != 0) {
                                lastStepX = stepX
                                lastStepY = stepY
                                currentOnCursorMove(dx, dy)
                            }
                        }

                        isSliding -> {
                            change.consume()
                            currentOnSlideChange(totalDx)
                        }
                    }
                }

                holdJob.cancel()

                when {
                    cursorMode -> currentOnCursorEnd()
                    isSliding -> currentOnSlideFinish(totalDx)
                    else -> currentOnTap()
                }

                launch { interactionSource.emit(PressInteraction.Release(press)) }
            }
        }
    }
}

/** [onHoldStart] = `null` — у клавиши нет вариантов, зажатие ничего не делает. */
fun Modifier.keyClickable(
    interactionSource: MutableInteractionSource,
    holdDelayMillis: Long = 350L,
    onPress: () -> Unit = {},
    onTap: () -> Unit,
    onHoldStart: (() -> Unit)? = null,
    onPickChange: ((offsetPx: Float) -> Unit)? = null,
    onPickFinish: ((offsetPx: Float) -> Unit)? = null,
): Modifier = composed {
    val currentOnPress by rememberUpdatedState(onPress)
    val currentOnTap by rememberUpdatedState(onTap)
    val currentOnHoldStart by rememberUpdatedState(onHoldStart)
    val currentOnPickChange by rememberUpdatedState(onPickChange)
    val currentOnPickFinish by rememberUpdatedState(onPickFinish)

    pointerInput(interactionSource) {
        coroutineScope {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                down.consume()
                currentOnPress()
                val press = PressInteraction.Press(down.position)
                launch { interactionSource.emit(press) }

                var holdFired = false
                val holdJob = currentOnHoldStart?.let { onHold ->
                    launch {
                        delay(holdDelayMillis.milliseconds)
                        holdFired = true
                        onHold()
                    }
                }

                var totalDx = 0f

                while (true) {
                    val event = awaitPointerEvent()
                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                    if (!change.pressed) break

                    totalDx += change.positionChange().x
                    if (holdFired) {
                        change.consume()
                        currentOnPickChange?.invoke(totalDx)
                    }
                }

                holdJob?.cancel()

                if (holdFired) currentOnPickFinish?.invoke(totalDx) else currentOnTap()

                launch { interactionSource.emit(PressInteraction.Release(press)) }
            }
        }
    }
}

/** Клик без индикации; interactionSource запоминается внутри, не нужно создавать его на каждом месте вызова. */
fun Modifier.plainClickable(onClick: () -> Unit): Modifier = composed {
    clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
    )
}

fun Modifier.interceptTouches(): Modifier = pointerInput(Unit) {
    awaitPointerEventScope {
        while (true) awaitPointerEvent()
    }
}
