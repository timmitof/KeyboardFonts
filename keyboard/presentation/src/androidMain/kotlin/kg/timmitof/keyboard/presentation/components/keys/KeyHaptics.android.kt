package kg.timmitof.keyboard.presentation.components.keys

import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

internal actual class KeyHaptics(private val view: View) {

    actual fun keyTap() {
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }
}

@Composable
internal actual fun rememberKeyHaptics(): KeyHaptics {
    val view = LocalView.current
    return remember(view) { KeyHaptics(view) }
}
