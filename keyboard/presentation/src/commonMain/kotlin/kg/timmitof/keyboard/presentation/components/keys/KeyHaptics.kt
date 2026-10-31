package kg.timmitof.keyboard.presentation.components.keys

import androidx.compose.runtime.Composable

/** Вибрация нажатия клавиши — системная, со своей силой у каждой платформы. */
internal expect class KeyHaptics {

    fun keyTap()
}

@Composable
internal expect fun rememberKeyHaptics(): KeyHaptics
