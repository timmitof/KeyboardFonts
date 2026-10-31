package kg.timmitof.keyboard.presentation.components.keys

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle

/** Лёгкий отклик, как у системной клавиатуры. В клавиатуре iOS работает только с «Полным доступом». */
internal actual class KeyHaptics {

    private val generator = UIImpactFeedbackGenerator(style = UIImpactFeedbackStyle.UIImpactFeedbackStyleLight).apply { prepare() }

    actual fun keyTap() {
        generator.impactOccurred()
        // Следующее нажатие тоже без задержки: генератор держим «прогретым».
        generator.prepare()
    }
}

@Composable
internal actual fun rememberKeyHaptics(): KeyHaptics = remember { KeyHaptics() }
