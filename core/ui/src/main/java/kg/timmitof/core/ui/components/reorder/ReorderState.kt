package kg.timmitof.core.ui.components.reorder

import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue

/** Перетаскивание строк [LazyListState] по ключам; слоты берутся из layoutInfo, высота строк может быть любой. */
@Stable
class ReorderState internal constructor(
    private val listState: LazyListState,
    private val onMove: State<(from: Int, to: Int) -> Unit>,
) {

    var draggedKey by mutableStateOf<Any?>(null)
        private set

    private var dragDelta by mutableFloatStateOf(0f)
    private var startOffset by mutableIntStateOf(0)

    // После onMove список перестраивается только к следующему кадру; до этого старые слоты врут.
    private var awaitedIndex: Int? = null

    /** Читать в draw-фазе (graphicsLayer): меняется на каждое движение пальца. */
    val draggedOffset: Float
        get() = draggedItem()?.let { startOffset + dragDelta - it.offset } ?: 0f

    fun isDragged(key: Any): Boolean = draggedKey == key

    fun onDragStart(key: Any) {
        val item = itemOf(key) ?: return
        draggedKey = key
        startOffset = item.offset
        dragDelta = 0f
        awaitedIndex = null
    }

    fun onDrag(dy: Float) {
        val dragged = draggedItem() ?: return
        dragDelta += dy

        awaitedIndex?.let { if (dragged.index != it) return }
        awaitedIndex = null

        val middle = (startOffset + dragDelta + dragged.size / 2f).toInt()
        val target = listState.layoutInfo.visibleItemsInfo.firstOrNull {
            it.key != dragged.key && middle in it.offset until it.offset + it.size
        } ?: return

        onMove.value(dragged.index, target.index)
        awaitedIndex = target.index
    }

    fun onDragEnd() {
        draggedKey = null
        dragDelta = 0f
        startOffset = 0
        awaitedIndex = null
    }

    private fun draggedItem(): LazyListItemInfo? = draggedKey?.let(::itemOf)

    private fun itemOf(key: Any): LazyListItemInfo? =
        listState.layoutInfo.visibleItemsInfo.firstOrNull { it.key == key }
}

@Composable
fun rememberReorderState(
    listState: LazyListState,
    onMove: (from: Int, to: Int) -> Unit,
): ReorderState {
    val currentOnMove = rememberUpdatedState(onMove)
    return remember(listState) { ReorderState(listState, currentOnMove) }
}
