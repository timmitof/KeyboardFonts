package kg.timmitof.feature_settings.domain.interactor

import kg.timmitof.feature_settings.domain.model.SettingsSummary
import kg.timmitof.keyboard.clipboard.domain.model.ClipboardBoard
import kg.timmitof.keyboard.domain.model.BackgroundPhoto
import kg.timmitof.keyboard.domain.model.KeyColorTarget
import kg.timmitof.keyboard.domain.model.KeyboardBackground
import kg.timmitof.keyboard.domain.model.KeyboardHeight
import kg.timmitof.keyboard.domain.model.KeyboardLanguages
import kg.timmitof.keyboard.domain.model.KeyboardSettings
import kg.timmitof.keyboard.domain.model.KeyboardSoundPack
import kg.timmitof.keyboard.domain.model.KeyboardThemeMode
import kg.timmitof.keyboard.domain.model.KeyboardToggle
import kg.timmitof.keyboard.domain.model.PhotoCrop
import kg.timmitof.keyboard.font.domain.model.FontPanel
import kotlinx.coroutines.flow.Flow

interface SettingsInteractor {

    fun observeSettings(): Flow<KeyboardSettings>

    suspend fun setToggle(toggle: KeyboardToggle, enabled: Boolean)

    suspend fun setTheme(mode: KeyboardThemeMode)

    suspend fun setHeight(height: KeyboardHeight)

    suspend fun setKeyColor(target: KeyColorTarget, argb: Long?)

    suspend fun setSoundPack(pack: KeyboardSoundPack)

    suspend fun setSoundVolume(volume: Float)

    suspend fun setBackground(background: KeyboardBackground)

    fun observePhotos(): Flow<List<BackgroundPhoto>>

    suspend fun getPhoto(id: Long): BackgroundPhoto?

    suspend fun addPhoto(uri: String): BackgroundPhoto

    /** Сохраняет кадр и сразу делает фото фоном. */
    suspend fun applyPhoto(photo: BackgroundPhoto, crop: PhotoCrop)

    /** Если удаляют фото, которое стоит фоном, клавиатура возвращается к фону темы. */
    suspend fun deletePhoto(id: Long)

    suspend fun getSummary(): SettingsSummary

    /** Каталог, включённые языки и выбранный; клавиатура переключает только включённые, в их порядке. */
    fun observeLanguages(): Flow<KeyboardLanguages>

    /** Пустой список игнорируется — последний язык удалить нельзя. Выученные слова удалённого языка остаются. */
    suspend fun setEnabledLanguages(codes: List<String>)

    fun observeFontPanel(): Flow<FontPanel>

    suspend fun setPanelFonts(ids: List<String>)

    suspend fun resetFontPanel()

    fun observeClipboard(): Flow<ClipboardBoard>

    suspend fun clearRecentClipboard()
}
