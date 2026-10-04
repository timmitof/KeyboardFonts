package kg.timmitof.feature_settings.data.interactor

import kg.timmitof.feature_settings.domain.interactor.SettingsInteractor
import kg.timmitof.feature_settings.domain.model.SettingsSummary
import kg.timmitof.keyboard.clipboard.domain.model.ClipboardBoard
import kg.timmitof.keyboard.clipboard.domain.repository.ClipboardRepository
import kg.timmitof.keyboard.domain.model.KeyboardHeight
import kg.timmitof.keyboard.domain.model.KeyboardSettings
import kg.timmitof.keyboard.domain.model.KeyboardThemeMode
import kg.timmitof.keyboard.domain.model.KeyboardToggle
import kg.timmitof.keyboard.domain.repository.KeyboardLayoutRepository
import kg.timmitof.keyboard.domain.repository.KeyboardSettingsRepository
import kg.timmitof.keyboard.domain.repository.LanguageRepository
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
) : SettingsInteractor {

    override fun observeSettings(): Flow<KeyboardSettings> =
        keyboardSettingsRepository.observeSettings()

    override suspend fun setToggle(toggle: KeyboardToggle, enabled: Boolean) =
        keyboardSettingsRepository.setToggle(toggle, enabled)

    override suspend fun setTheme(mode: KeyboardThemeMode) =
        keyboardSettingsRepository.setTheme(mode)

    override suspend fun setHeight(height: KeyboardHeight) =
        keyboardSettingsRepository.setHeight(height)

    override suspend fun getSummary(): SettingsSummary {
        val keyboardState = keyboardContract.getKeyboardState()
        val selectedLanguage = languageRepository.getSelectedLanguage()

        return SettingsSummary(
            languages = languageRepository.getLanguages(),
            selectedLanguage = selectedLanguage,
            fonts = fontRepository.getFonts(),
            selectedFont = fontRepository.getSelectedFont(),
            previewLayout = keyboardLayoutRepository.getLayout(selectedLanguage.code),
            isKeyboardReady = keyboardState.isEnabled && keyboardState.isSelected,
        )
    }

    override fun observeClipboard(): Flow<ClipboardBoard> =
        clipboardRepository.observeBoard()

    override suspend fun clearRecentClipboard() =
        clipboardRepository.clearRecent()
}
