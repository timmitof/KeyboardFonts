package kg.timmitof.keyboard.presentation.insets

import android.view.View
import android.view.ViewTreeObserver
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal class KeyboardInsetsTracker(private val view: View) {

    private val insetsState = mutableStateOf(KeyboardInsets())
    val insets: State<KeyboardInsets> get() = insetsState

    private val viewLocation = IntArray(2)
    private var trackedTreeObserver: ViewTreeObserver? = null
    private val layoutListener = ViewTreeObserver.OnGlobalLayoutListener { measure() }

    fun startTracking() {
        stopTracking()
        trackedTreeObserver = view.viewTreeObserver.also { it.addOnGlobalLayoutListener(layoutListener) }
        measure()
    }

    fun stopTracking() {
        trackedTreeObserver
            ?.takeIf(ViewTreeObserver::isAlive)
            ?.removeOnGlobalLayoutListener(layoutListener)
        trackedTreeObserver = null
    }

    fun measure() {
        val rootInsets = ViewCompat.getRootWindowInsets(view) ?: return
        val root = view.rootView
        // До первого layout размеры нулевые - мерить нечего, отступ добавит следующий проход.
        if (view.width == 0 || view.height == 0) return

        val systemPanels = INSET_TYPES.fold(Insets.NONE) { panels, type ->
            Insets.max(panels, rootInsets.getInsets(type))
        }

        view.getLocationInWindow(viewLocation)
        val density = view.resources.displayMetrics.density

        insetsState.value = KeyboardInsets(
            left = missing(systemPanels.left, reserved = viewLocation[0], density = density),
            right = missing(
                panel = systemPanels.right,
                reserved = root.width - (viewLocation[0] + view.width),
                density = density
            ),
            bottom = missing(
                panel = systemPanels.bottom,
                reserved = root.height - (viewLocation[1] + view.height),
                density = density
            )
        )
    }

    private fun missing(panel: Int, reserved: Int, density: Float): Dp =
        ((panel - reserved).coerceAtLeast(0) / density).dp

    private companion object {
        val INSET_TYPES = listOf(
            WindowInsetsCompat.Type.navigationBars(),
            WindowInsetsCompat.Type.systemBars(),
            WindowInsetsCompat.Type.tappableElement()
        )
    }
}
