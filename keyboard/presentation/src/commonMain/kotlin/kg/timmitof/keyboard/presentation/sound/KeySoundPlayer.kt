package kg.timmitof.keyboard.presentation.sound

import androidx.compose.runtime.Composable
import kg.timmitof.keyboard.domain.model.KeyboardSoundPack

enum class KeySound { STANDARD, SPACE, DELETE, ENTER }

/** Звук нажатий: наборы грузятся заранее, при смене набора, — звук стартует без задержки на касание. */
expect class KeySoundPlayer {

    fun prepare(pack: KeyboardSoundPack)

    fun play(sound: KeySound, pack: KeyboardSoundPack, volume: Float)

    fun release()
}

@Composable
expect fun rememberKeySoundPlayer(): KeySoundPlayer

/** Файлы набора в `composeResources/files/sounds`; у системного набора своих файлов нет — играет система. */
internal val KeyboardSoundPack.soundFiles: Map<KeySound, String>
    get() = when (this) {
        KeyboardSoundPack.SYSTEM -> emptyMap()
        KeyboardSoundPack.MECHANICAL -> packFiles("key_mech")
        KeyboardSoundPack.BUBBLE -> packFiles("key_bubble")
        KeyboardSoundPack.TYPEWRITER -> packFiles("key_type")
    }

private fun packFiles(prefix: String): Map<KeySound, String> = mapOf(
    KeySound.STANDARD to "files/sounds/${prefix}_standard.wav",
    KeySound.SPACE to "files/sounds/${prefix}_space.wav",
    KeySound.DELETE to "files/sounds/${prefix}_delete.wav",
    KeySound.ENTER to "files/sounds/${prefix}_enter.wav",
)
