package kg.timmitof.keyboard.presentation.sound

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import kg.timmitof.keyboard.domain.model.KeyboardSoundPack
import kg.timmitof.keyboard.presentation.resources.Res
import kotlinx.cinterop.ExperimentalForeignApi
import org.jetbrains.compose.resources.ExperimentalResourceApi
import platform.AVFAudio.AVAudioPlayer
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryAmbient
import platform.AVFAudio.setActive
import platform.Foundation.NSURL
import platform.UIKit.UIDevice

/**
 * Свои наборы — [AVAudioPlayer] на каждый звук, подготовленный заранее. Системный набор — `playInputClick`:
 * это щелчок самой iOS, он учитывает настройку «Щелчки клавиатуры» (звучит только в окне клавиатуры).
 */
@OptIn(ExperimentalForeignApi::class, ExperimentalResourceApi::class)
actual class KeySoundPlayer {

    private var loadedPack: KeyboardSoundPack? = null
    private var players: Map<KeySound, AVAudioPlayer> = emptyMap()

    actual fun prepare(pack: KeyboardSoundPack) {
        if (pack == loadedPack) return
        stopAll()
        if (pack != KeyboardSoundPack.SYSTEM) useAmbientSession()

        players = pack.soundFiles.mapNotNull { (sound, path) ->
            val url = NSURL.URLWithString(Res.getUri(path)) ?: return@mapNotNull null
            val player = AVAudioPlayer(contentsOfURL = url, error = null)
            player.prepareToPlay()
            sound to player
        }.toMap()
        loadedPack = pack
    }

    actual fun play(sound: KeySound, pack: KeyboardSoundPack, volume: Float) {
        if (volume <= 0f) return

        if (pack == KeyboardSoundPack.SYSTEM) {
            UIDevice.currentDevice.playInputClick()
            return
        }

        prepare(pack)
        val player = players[sound] ?: return
        player.volume = volume
        // Быстрый набор: звук начинается заново, а не ждёт конца предыдущего.
        player.currentTime = 0.0
        player.play()
    }

    actual fun release() {
        stopAll()
        loadedPack = null
    }

    private fun stopAll() {
        players.values.forEach { it.stop() }
        players = emptyMap()
    }

    /** Ambient: щелчки не прерывают музыку пользователя и стихают вместе с переключателем «Без звука». */
    private fun useAmbientSession() {
        val session = AVAudioSession.sharedInstance()
        session.setCategory(AVAudioSessionCategoryAmbient, error = null)
        session.setActive(true, error = null)
    }
}

@Composable
actual fun rememberKeySoundPlayer(): KeySoundPlayer {
    val player = remember { KeySoundPlayer() }
    DisposableEffect(player) { onDispose { player.release() } }
    return player
}
