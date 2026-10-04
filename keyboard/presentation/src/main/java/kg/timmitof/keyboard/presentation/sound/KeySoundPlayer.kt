package kg.timmitof.keyboard.presentation.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import androidx.annotation.RawRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kg.timmitof.keyboard.domain.model.KeyboardSoundPack
import kg.timmitof.keyboard.presentation.R

enum class KeySound { STANDARD, SPACE, DELETE, ENTER }

/** [SoundPool] — звук стартует без задержки на касание; наборы грузятся заранее, при смене набора. */
class KeySoundPlayer(context: Context) {

    private val appContext = context.applicationContext
    private val audioManager = appContext.getSystemService(AudioManager::class.java)

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(MaxStreams)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private var loadedPack: KeyboardSoundPack? = null
    private var soundIds: Map<KeySound, Int> = emptyMap()
    private val readyIds = HashSet<Int>()

    // Звук, запрошенный до окончания загрузки сэмпла (например, превью сразу после выбора набора).
    private var pending: Pair<Int, Float>? = null

    init {
        soundPool.setOnLoadCompleteListener { pool, id, status ->
            if (status != 0) return@setOnLoadCompleteListener
            readyIds += id
            pending?.takeIf { (pendingId, _) -> pendingId == id }?.let { (_, volume) ->
                pool.play(id, volume, volume, 1, 0, 1f)
                pending = null
            }
        }
    }

    fun prepare(pack: KeyboardSoundPack) {
        if (pack == loadedPack) return

        soundIds.values.forEach(soundPool::unload)
        readyIds.clear()
        pending = null
        soundIds = pack.sounds.mapValues { (_, res) -> soundPool.load(appContext, res, 1) }
        loadedPack = pack
    }

    fun play(sound: KeySound, pack: KeyboardSoundPack, volume: Float) {
        if (volume <= 0f) return

        // Системный щелчок: версия с громкостью не смотрит на системные «Звуки касания».
        if (pack == KeyboardSoundPack.SYSTEM) {
            audioManager?.playSoundEffect(sound.systemEffect, volume)
            return
        }

        prepare(pack)
        val id = soundIds[sound] ?: return
        if (id in readyIds) {
            soundPool.play(id, volume, volume, 1, 0, 1f)
        } else {
            pending = id to volume
        }
    }

    fun release() {
        soundPool.release()
        soundIds = emptyMap()
        readyIds.clear()
        pending = null
        loadedPack = null
    }

    private companion object {
        const val MaxStreams = 4
    }
}

@Composable
fun rememberKeySoundPlayer(): KeySoundPlayer {
    val context = LocalContext.current
    val player = remember(context) { KeySoundPlayer(context) }
    DisposableEffect(player) { onDispose { player.release() } }
    return player
}

private val KeySound.systemEffect: Int
    get() = when (this) {
        KeySound.STANDARD -> AudioManager.FX_KEYPRESS_STANDARD
        KeySound.SPACE -> AudioManager.FX_KEYPRESS_SPACEBAR
        KeySound.DELETE -> AudioManager.FX_KEYPRESS_DELETE
        KeySound.ENTER -> AudioManager.FX_KEYPRESS_RETURN
    }

private val KeyboardSoundPack.sounds: Map<KeySound, Int>
    get() = when (this) {
        KeyboardSoundPack.SYSTEM -> emptyMap()
        KeyboardSoundPack.MECHANICAL -> pack(
            R.raw.key_mech_standard, R.raw.key_mech_space, R.raw.key_mech_delete, R.raw.key_mech_enter,
        )
        KeyboardSoundPack.BUBBLE -> pack(
            R.raw.key_bubble_standard, R.raw.key_bubble_space, R.raw.key_bubble_delete, R.raw.key_bubble_enter,
        )
        KeyboardSoundPack.TYPEWRITER -> pack(
            R.raw.key_type_standard, R.raw.key_type_space, R.raw.key_type_delete, R.raw.key_type_enter,
        )
    }

private fun pack(
    @RawRes standard: Int,
    @RawRes space: Int,
    @RawRes delete: Int,
    @RawRes enter: Int,
) = mapOf(
    KeySound.STANDARD to standard,
    KeySound.SPACE to space,
    KeySound.DELETE to delete,
    KeySound.ENTER to enter,
)
