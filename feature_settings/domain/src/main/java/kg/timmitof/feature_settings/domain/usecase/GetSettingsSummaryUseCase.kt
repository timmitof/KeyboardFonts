package kg.timmitof.feature_settings.domain.usecase

import kg.timmitof.feature_settings.domain.model.SettingsSummary
import kg.timmitof.keyboard.domain.model.KeyboardLanguage
import kg.timmitof.keyboard.font.domain.repository.FontRepository
import kg.timmitof.keyboard.domain.repository.LanguageRepository
import kg.timmitof.keyboard.integration.KeyboardContract

/**
 * Собирает значения для строк-переходов: раскладки, каталог шрифтов и статус клавиатуры.
 */
class GetSettingsSummaryUseCase(
    private val languageRepository: LanguageRepository,
    private val fontRepository: FontRepository,
    private val keyboardContract: KeyboardContract,
) {

    suspend operator fun invoke(): SettingsSummary {
        val keyboardState = keyboardContract.getKeyboardState()

        return SettingsSummary(
            languages = languageRepository.getLanguages().joinToString(
                separator = ", ",
                transform = KeyboardLanguage::shortName,
            ),
            fontsTotal = fontRepository.getFonts().size,
            isKeyboardReady = keyboardState.isEnabled && keyboardState.isSelected,
        )
    }
}
