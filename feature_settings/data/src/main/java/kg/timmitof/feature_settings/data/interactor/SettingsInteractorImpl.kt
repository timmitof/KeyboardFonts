package kg.timmitof.feature_settings.data.interactor

import kg.timmitof.feature_settings.domain.interactor.SettingsInteractor
import kg.timmitof.feature_settings.domain.model.SettingsSummary
import kg.timmitof.keyboard.clipboard.domain.model.ClipboardBoard
import kg.timmitof.keyboard.clipboard.domain.repository.ClipboardRepository
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
import kg.timmitof.keyboard.domain.repository.BackgroundPhotoRepository
import kg.timmitof.keyboard.domain.repository.KeyboardLayoutRepository
import kg.timmitof.keyboard.domain.repository.KeyboardSettingsRepository
import kg.timmitof.keyboard.domain.repository.LanguageRepository
import kg.timmitof.keyboard.font.domain.model.FontPanel
import kg.timmitof.keyboard.font.domain.repository.FontRepository
import kg.timmitof.keyboard.integration.KeyboardContract
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SettingsInteractorImpl @Inject constructor(
    private val keyboardSettingsRepository: KeyboardSettingsRepository,
    private val languageRepository: LanguageRepository,
    private val fontRepository: FontRepository,
    private val keyboardLayoutRepository: KeyboardLayoutRepository,
    private val clipboardRepository: ClipboardRepository,
    private val keyboardContract: KeyboardContract,
    private val photoRepository: BackgroundPhotoRepository,
) : SettingsInteractor {

    override fun observeSettings(): Flow<KeyboardSettings> =
        keyboardSettingsRepository.observeSettings()

    override suspend fun setToggle(toggle: KeyboardToggle, enabled: Boolean) =
        keyboardSettingsRepository.setToggle(toggle, enabled)

    override suspend fun setTheme(mode: KeyboardThemeMode) =
        keyboardSettingsRepository.setTheme(mode)

    override suspend fun setHeight(height: KeyboardHeight) =
        keyboardSettingsRepository.setHeight(height)

    override suspend fun setKeyColor(target: KeyColorTarget, argb: Long?) =
        keyboardSettingsRepository.setKeyColor(target, argb)

    override suspend fun setSoundPack(pack: KeyboardSoundPack) =
        keyboardSettingsRepository.setSoundPack(pack)

    override suspend fun setSoundVolume(volume: Float) =
        keyboardSettingsRepository.setSoundVolume(volume)

    override suspend fun setBackground(background: KeyboardBackground) =
        keyboardSettingsRepository.setBackground(background)

    override fun observePhotos(): Flow<List<BackgroundPhoto>> = photoRepository.observePhotos()

    override suspend fun getPhoto(id: Long): BackgroundPhoto? = photoRepository.getPhoto(id)

    override suspend fun addPhoto(uri: String): BackgroundPhoto = photoRepository.addPhoto(uri)

    override suspend fun applyPhoto(photo: BackgroundPhoto, crop: PhotoCrop) {
        photoRepository.setCrop(photo.id, crop)
        keyboardSettingsRepository.setBackground(KeyboardBackground.Photo(photo.copy(crop = crop)))
    }

    override suspend fun deletePhoto(id: Long) {
        val current = keyboardSettingsRepository.getSettings().background
        if ((current as? KeyboardBackground.Photo)?.photo?.id == id) {
            keyboardSettingsRepository.setBackground(KeyboardBackground.None)
        }
        photoRepository.deletePhoto(id)
    }

    override suspend fun getSummary(): SettingsSummary {
        val keyboardState = keyboardContract.getKeyboardState()
        val selectedLanguage = languageRepository.getLanguages().selected

        return SettingsSummary(
            selectedLanguage = selectedLanguage,
            selectedFont = fontRepository.getSelectedFont(),
            previewLayout = keyboardLayoutRepository.getLayout(selectedLanguage.layout),
            isKeyboardReady = keyboardState.isEnabled && keyboardState.isSelected,
        )
    }

    override fun observeLanguages(): Flow<KeyboardLanguages> =
        languageRepository.observeLanguages()

    override suspend fun setEnabledLanguages(codes: List<String>) =
        languageRepository.setEnabledLanguages(codes)

    override fun observeFontPanel(): Flow<FontPanel> =
        fontRepository.observePanel()

    override suspend fun setPanelFonts(ids: List<String>) =
        fontRepository.setPanelFonts(ids)

    override suspend fun resetFontPanel() =
        fontRepository.resetPanel()

    override fun observeClipboard(): Flow<ClipboardBoard> =
        clipboardRepository.observeBoard()

    override suspend fun clearRecentClipboard() =
        clipboardRepository.clearRecent()
}
