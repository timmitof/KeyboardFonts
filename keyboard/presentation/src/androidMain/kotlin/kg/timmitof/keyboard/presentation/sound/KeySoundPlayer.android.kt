package kg.timmitof.keyboard.presentation.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kg.timmitof.keyboard.domain.model.KeyboardSoundPack
import kg.timmitof.keyboard.presentation.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi

/** [SoundPool] — звук стартует без задержки на касание; наборы грузятся заранее, при смене набора. */
actual class KeySoundPlayer(context: Context) {

    private val appContext = context.applicationContext
    private val audioManager = appContext.getSystemService(AudioManager::class.java)

    // Пул создаётся при первом обращении: при выключенном звуке и системном наборе он не нужен.
    private val soundPoolDelegate = lazy {
        SoundPool.Builder()
            .setMaxStreams(MaxStreams)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .build()
            .also(::listenLoadComplete)
    }
    private val soundPool by soundPoolDelegate

    private var loadedPack: KeyboardSoundPack? = null
    private var soundIds: Map<KeySound, Int> = emptyMap()
    private val readyIds = HashSet<Int>()

    // Звук, запрошенный до окончания загрузки сэмпла (например, превью сразу после выбора набора).
    private var pending: Pair<Int, Float>? = null

    private fun listenLoadComplete(pool: SoundPool) {
        pool.setOnLoadCompleteListener { _, id, status ->
            if (status != 0) return@setOnLoadCompleteListener
            readyIds += id
            pending?.takeIf { (pendingId, _) -> pendingId == id }?.let { (_, volume) ->
                pool.play(id, volume, volume, 1, 0, 1f)
                pending = null
            }
        }
    }

    actual fun prepare(pack: KeyboardSoundPack) {
        if (pack == loadedPack) return
        // Системный набор играется через AudioManager: пул ради него не создаём.
        if (pack == KeyboardSoundPack.SYSTEM && !soundPoolDelegate.isInitialized()) {
            loadedPack = pack
            return
        }

        soundIds.values.forEach(soundPool::unload)
        readyIds.clear()
        pending = null
        soundIds = pack.soundFiles.mapNotNull { (sound, path) -> load(path)?.let { sound to it } }.toMap()
        loadedPack = pack
    }

    actual fun play(sound: KeySound, pack: KeyboardSoundPack, volume: Float) {
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

    actual fun release() {
        if (soundPoolDelegate.isInitialized()) soundPool.release()
        soundIds = emptyMap()
        readyIds.clear()
        pending = null
        loadedPack = null
    }

    /**
     * Звуки — общие ресурсы Compose, на Android это ассеты; wav не сжимается, поэтому файл открывается
     * дескриптором. Пул копирует дескриптор при загрузке, наш можно закрыть сразу.
     */
    @OptIn(ExperimentalResourceApi::class)
    private fun load(path: String): Int? = runCatching {
        val asset = Res.getUri(path).removePrefix(ANDROID_ASSET_URI)
        appContext.assets.openFd(asset).use { descriptor -> soundPool.load(descriptor, 1) }
    }.onFailure { error -> Log.w(TAG, "Не удалось загрузить звук $path", error) }.getOrNull()

    private companion object {
        const val MaxStreams = 4
        const val ANDROID_ASSET_URI = "file:///android_asset/"
        const val TAG = "KeySoundPlayer"
    }
}

@Composable
actual fun rememberKeySoundPlayer(): KeySoundPlayer {
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
