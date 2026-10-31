package kg.timmitof.keyboard.presentation.components.keys

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import kg.timmitof.keyboard.domain.model.KeyboardSettings
import kg.timmitof.keyboard.domain.model.KeyboardSoundPack
import kg.timmitof.keyboard.presentation.sound.KeySound
import kg.timmitof.keyboard.presentation.sound.KeySoundPlayer
import kg.timmitof.keyboard.presentation.sound.rememberKeySoundPlayer

/** Настройки отклика лежат в [LocalKeyFeedback], а не в параметрах каждой клавиши. */
@Immutable
internal class KeyFeedback(
    private val haptics: KeyHaptics? = null,
    private val soundPlayer: KeySoundPlayer? = null,
    private val isVibrationEnabled: Boolean = false,
    private val isSoundEnabled: Boolean = false,
    private val soundPack: KeyboardSoundPack = KeyboardSoundPack.Default,
    private val soundVolume: Float = 0f,
    val isPreviewEnabled: Boolean = true,
) {

    fun onKeyPress(sound: KeySound = KeySound.STANDARD) {
        if (isVibrationEnabled) {
            haptics?.keyTap()
        }
        if (isSoundEnabled) {
            soundPlayer?.play(sound, soundPack, soundVolume)
        }
    }
}

internal val LocalKeyFeedback = staticCompositionLocalOf { KeyFeedback() }

@Composable
internal fun rememberKeyFeedback(settings: KeyboardSettings): KeyFeedback {
    val haptics = rememberKeyHaptics()
    val soundPlayer = rememberKeySoundPlayer()

    // Набор грузим сразу при включении, иначе первое нажатие прозвучит в тишине.
    LaunchedEffect(soundPlayer, settings.isSoundEnabled, settings.soundPack) {
        if (settings.isSoundEnabled) soundPlayer.prepare(settings.soundPack)
    }

    return remember(haptics, soundPlayer, settings) {
        KeyFeedback(
            haptics = haptics,
            soundPlayer = soundPlayer,
            isVibrationEnabled = settings.isVibrationEnabled,
            isSoundEnabled = settings.isSoundEnabled,
            soundPack = settings.soundPack,
            soundVolume = settings.soundVolume,
            isPreviewEnabled = settings.isKeyPreviewEnabled,
        )
    }
}
