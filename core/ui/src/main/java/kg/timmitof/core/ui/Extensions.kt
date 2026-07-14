package kg.timmitof.core.ui

import android.content.Context
import android.os.SystemClock
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer

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

/**
 * Эффект карусели для элемента LazyRow/LazyColumn: чем ближе центр элемента
 * к краю вьюпорта, тем сильнее он уменьшается и растворяется — до самого
 * минимума в момент выхода за край
 *
 * @param listState состояние ленивого списка, в котором находится элемент.
 * @param index индекс элемента в списке.
 * @param minScale масштаб элемента на краю вьюпорта.
 * @param minAlpha прозрачность элемента на краю вьюпорта.
 */
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

/**
 * Метод для задержки
 */
@Composable
inline fun debounced(crossinline onClick: () -> Unit, debounceTime: Long = 1000L): () -> Unit {
    var lastTimeClicked by remember { mutableLongStateOf(0L) }
    val onClickLambda: () -> Unit = {
        val now = SystemClock.uptimeMillis()
        if (now - lastTimeClicked > debounceTime) {
            onClick()
        }
        lastTimeClicked = now
    }
    return onClickLambda
}