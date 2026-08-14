package kg.timmitof.keyboard.presentation.components.keys

import android.media.AudioManager
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import kg.timmitof.keyboard.domain.model.KeyboardSettings

/**
 * Отклик клавиатуры на нажатие: вибрация, звук и всплывающая шапка над клавишей.
 *
 * Настройки не тянутся в каждую клавишу через параметры — они лежат в
 * [LocalKeyFeedback]: отклик нужен всем клавишам сразу и не влияет на то,
 * что клавиша вводит.
 */
@Immutable
internal class KeyFeedback(
    private val view: View? = null,
    private val audioManager: AudioManager? = null,
    private val isVibrationEnabled: Boolean = false,
    private val isSoundEnabled: Boolean = false,
    /** Показывать ли шапку над нажатой клавишей. */
    val isPreviewEnabled: Boolean = true,
) {

    /** Клавишу нажали — отзываемся до того, как символ уйдёт в поле. */
    fun onKeyPress() {
        if (isVibrationEnabled) {
            view?.performHapticFeedback(
                HapticFeedbackConstants.KEYBOARD_TAP,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING,
            )
        }
        if (isSoundEnabled) {
            audioManager?.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD)
        }
    }
}

/** Отклик по умолчанию — молчаливый: без него превью клавиш всё равно работает. */
internal val LocalKeyFeedback = staticCompositionLocalOf { KeyFeedback() }

@Composable
internal fun rememberKeyFeedback(settings: KeyboardSettings): KeyFeedback {
    val view = LocalView.current
    val context = LocalContext.current
    val audioManager = remember(context) { context.getSystemService(AudioManager::class.java) }

    return remember(view, audioManager, settings) {
        KeyFeedback(
            view = view,
            audioManager = audioManager,
            isVibrationEnabled = settings.isVibrationEnabled,
            isSoundEnabled = settings.isSoundEnabled,
            isPreviewEnabled = settings.isKeyPreviewEnabled,
        )
    }
}
