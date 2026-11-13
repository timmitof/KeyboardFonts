package kg.timmitof.keyboard.presentation.screens.keyboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kg.timmitof.keyboard.clipboard.domain.repository.ClipboardRepository
import kg.timmitof.keyboard.domain.repository.EmojiRepository
import kg.timmitof.keyboard.font.domain.repository.FontRepository
import kg.timmitof.keyboard.domain.repository.KeyboardLayoutRepository
import kg.timmitof.keyboard.domain.repository.KeyboardSettingsRepository
import kg.timmitof.keyboard.domain.repository.LanguageRepository
import kg.timmitof.keyboard.suggestion.domain.repository.SuggestionRepository
import kg.timmitof.keyboard.suggestion.domain.repository.TypingStatsRepository
import kg.timmitof.keyboard.presentation.screens.keyboard.delegates.ClipTranslationDelegate
import kg.timmitof.keyboard.presentation.screens.keyboard.delegates.ClipboardDelegate
import kg.timmitof.keyboard.presentation.screens.keyboard.delegates.EmojiDelegate
import kg.timmitof.keyboard.presentation.screens.keyboard.delegates.FieldContextDelegate
import kg.timmitof.keyboard.presentation.screens.keyboard.delegates.FontDelegate
import kg.timmitof.keyboard.presentation.screens.keyboard.delegates.LanguageDelegate
import kg.timmitof.keyboard.presentation.screens.keyboard.delegates.LayerDelegate
import kg.timmitof.keyboard.presentation.screens.keyboard.delegates.SettingsDelegate
import kg.timmitof.keyboard.presentation.screens.keyboard.delegates.SuggestionsDelegate
import kg.timmitof.keyboard.presentation.screens.keyboard.delegates.TextInputDelegate
import kg.timmitof.keyboard.presentation.screens.keyboard.delegates.TranslatorDelegate
import kg.timmitof.keyboard.domain.repository.FeatureAccessRepository
import kg.timmitof.keyboard.translate.domain.repository.LanguageDetectionRepository
import kg.timmitof.keyboard.translate.domain.repository.TranslationDownloadRepository
import kg.timmitof.keyboard.translate.domain.repository.TranslationModelRepository
import kg.timmitof.keyboard.translate.domain.repository.TranslationRepository
import kg.timmitof.keyboard.translate.domain.repository.TranslatorPreferencesRepository

class KeyboardViewModelFactory(
    private val keyboardLayoutRepository: KeyboardLayoutRepository,
    private val emojiRepository: EmojiRepository,
    private val languageRepository: LanguageRepository,
    private val fontRepository: FontRepository,
    private val suggestionRepository: SuggestionRepository,
    private val typingStatsRepository: TypingStatsRepository,
    private val keyboardSettingsRepository: KeyboardSettingsRepository,
    private val clipboardRepository: ClipboardRepository,
    private val translationRepository: TranslationRepository,
    private val translationModelRepository: TranslationModelRepository,
    private val translatorPreferencesRepository: TranslatorPreferencesRepository,
    private val translationDownloadRepository: TranslationDownloadRepository,
    private val languageDetectionRepository: LanguageDetectionRepository,
    private val featureAccessRepository: FeatureAccessRepository,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(KeyboardViewModel::class.java)) {
            val layerDelegate = LayerDelegate(keyboardLayoutRepository)
            val emojiDelegate = EmojiDelegate(emojiRepository, layerDelegate)
            val suggestionsDelegate = SuggestionsDelegate(suggestionRepository, typingStatsRepository)

            @Suppress("UNCHECKED_CAST")
            return KeyboardViewModel(
                layerDelegate = layerDelegate,
                textInputDelegate = TextInputDelegate(layerDelegate, suggestionsDelegate),
                emojiDelegate = emojiDelegate,
                languageDelegate = LanguageDelegate(languageRepository, layerDelegate),
                fontDelegate = FontDelegate(fontRepository),
                fieldContextDelegate = FieldContextDelegate(layerDelegate),
                suggestionsDelegate = suggestionsDelegate,
                settingsDelegate = SettingsDelegate(keyboardSettingsRepository, featureAccessRepository),
                clipboardDelegate = ClipboardDelegate(clipboardRepository),
                clipTranslationDelegate = ClipTranslationDelegate(
                    clipboardRepository = clipboardRepository,
                    detectionRepository = languageDetectionRepository,
                    translationRepository = translationRepository,
                    modelRepository = translationModelRepository,
                ),
                translatorDelegate = TranslatorDelegate(
                    translationRepository = translationRepository,
                    modelRepository = translationModelRepository,
                    preferencesRepository = translatorPreferencesRepository,
                    downloadRepository = translationDownloadRepository,
                    detectionRepository = languageDetectionRepository,
                ),
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
