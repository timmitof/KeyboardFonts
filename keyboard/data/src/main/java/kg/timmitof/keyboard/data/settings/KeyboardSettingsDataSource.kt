package kg.timmitof.keyboard.data.settings

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kg.timmitof.keyboard.data.language.keyboardPreferences
import kg.timmitof.keyboard.domain.model.KeyColorTarget
import kg.timmitof.keyboard.domain.model.KeyboardBackground
import kg.timmitof.keyboard.domain.model.toKey
import kg.timmitof.keyboard.domain.model.KeyboardHeight
import kg.timmitof.keyboard.domain.model.KeyboardSettings
import kg.timmitof.keyboard.domain.model.KeyboardSoundPack
import kg.timmitof.keyboard.domain.model.KeyboardThemeMode
import kg.timmitof.keyboard.domain.model.KeyboardToggle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/** Ключи берутся из [KeyboardToggle] — новая настройка не требует правок хранилища. */
@Singleton
class KeyboardSettingsDataSource @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    /** Ключи считаются один раз: `booleanPreferencesKey` на каждое чтение — лишняя работа. */
    private val toggleKeys: Map<KeyboardToggle, Preferences.Key<Boolean>> =
        KeyboardToggle.entries.associateWith { booleanPreferencesKey(it.key) }

    /** Сбойное чтение не должно валить клавиатуру — откатываемся к значениям по умолчанию. */
    private val preferences: Flow<Preferences> = context.keyboardPreferences.data
        .catch { error -> if (error is IOException) emit(emptyPreferences()) else throw error }

    fun observe(): Flow<KeyboardSettings> = preferences.map(::toSettings)

    suspend fun get(): KeyboardSettings = toSettings(preferences.first())

    suspend fun setToggle(toggle: KeyboardToggle, enabled: Boolean) {
        context.keyboardPreferences.edit { prefs ->
            prefs[toggleKeys.getValue(toggle)] = enabled
        }
    }

    suspend fun setTheme(mode: KeyboardThemeMode) {
        context.keyboardPreferences.edit { prefs -> prefs[THEME_KEY] = mode.key }
    }

    suspend fun setHeight(height: KeyboardHeight) {
        context.keyboardPreferences.edit { prefs -> prefs[HEIGHT_KEY] = height.key }
    }

    suspend fun setKeyColor(target: KeyColorTarget, argb: Long?) {
        val key = target.preferenceKey
        context.keyboardPreferences.edit { prefs ->
            if (argb == null) prefs.remove(key) else prefs[key] = argb
        }
    }

    suspend fun setSoundPack(pack: KeyboardSoundPack) {
        context.keyboardPreferences.edit { prefs -> prefs[SOUND_PACK_KEY] = pack.key }
    }

    suspend fun setSoundVolume(volume: Float) {
        context.keyboardPreferences.edit { prefs -> prefs[SOUND_VOLUME_KEY] = volume.coerceIn(0f, 1f) }
    }

    /** Одной записью с цветами клавиш: иначе клавиатура на кадр покажет новый фон со старыми клавишами. */
    suspend fun setBackground(background: KeyboardBackground) {
        context.keyboardPreferences.edit { prefs ->
            background.toKey()?.let { prefs[BACKGROUND_KEY] = it } ?: prefs.remove(BACKGROUND_KEY)
            if (background is KeyboardBackground.Photo) prefs[BACKGROUND_PHOTO_KEY] = background.toKey().orEmpty()
            prefs.remove(KEY_COLOR_KEY)
            prefs.remove(SPECIAL_KEY_COLOR_KEY)
        }
    }

    private fun toSettings(prefs: Preferences) = KeyboardSettings(
        flags = KeyboardToggle.entries.associateWith { toggle ->
            prefs[toggleKeys.getValue(toggle)] ?: toggle.default
        },
        theme = KeyboardThemeMode.of(prefs[THEME_KEY]),
        height = KeyboardHeight.of(prefs[HEIGHT_KEY]),
        keyColor = prefs[KEY_COLOR_KEY],
        specialKeyColor = prefs[SPECIAL_KEY_COLOR_KEY],
        enterColor = prefs[ENTER_COLOR_KEY],
        soundPack = KeyboardSoundPack.of(prefs[SOUND_PACK_KEY]),
        soundVolume = prefs[SOUND_VOLUME_KEY] ?: KeyboardSettings.DEFAULT_SOUND_VOLUME,
        background = KeyboardBackground.of(prefs[BACKGROUND_KEY]),
        backgroundPhoto = KeyboardBackground.of(prefs[BACKGROUND_PHOTO_KEY]) as? KeyboardBackground.Photo,
    )

    private companion object {
        val THEME_KEY = stringPreferencesKey("keyboard_theme")

        val HEIGHT_KEY = stringPreferencesKey("keyboard_height")

        val ENTER_COLOR_KEY = longPreferencesKey("enter_key_color")

        val KEY_COLOR_KEY = longPreferencesKey("key_color")

        val SPECIAL_KEY_COLOR_KEY = longPreferencesKey("special_key_color")

        val SOUND_PACK_KEY = stringPreferencesKey("key_sound_pack")

        val SOUND_VOLUME_KEY = floatPreferencesKey("key_sound_volume")

        val BACKGROUND_KEY = stringPreferencesKey("keyboard_background")

        val BACKGROUND_PHOTO_KEY = stringPreferencesKey("keyboard_background_last_photo")

        val KeyColorTarget.preferenceKey
            get() = when (this) {
                KeyColorTarget.KEY -> KEY_COLOR_KEY
                KeyColorTarget.SPECIAL -> SPECIAL_KEY_COLOR_KEY
                KeyColorTarget.ENTER -> ENTER_COLOR_KEY
            }
    }
}
