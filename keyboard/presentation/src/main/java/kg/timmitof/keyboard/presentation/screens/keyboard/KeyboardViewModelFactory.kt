package kg.timmitof.keyboard.presentation.screens.keyboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kg.timmitof.core.analytics.Analytics
import kg.timmitof.keyboard.clipboard.domain.repository.ClipboardRepository
import kg.timmitof.keyboard.domain.repository.EmojiRepository
import kg.timmitof.keyboard.font.domain.repository.FontRepository
import kg.timmitof.keyboard.domain.repository.KeyboardLayoutRepository
import kg.timmitof.keyboard.domain.repository.KeyboardSettingsRepository
import kg.timmitof.keyboard.domain.repository.LanguageRepository
import kg.timmitof.keyboard.suggestion.domain.repository.SuggestionRepository
import kg.timmitof.keyboard.presentation.screens.keyboard.delegates.ClipboardDelegate
import kg.timmitof.keyboard.presentation.screens.keyboard.delegates.EmojiDelegate
import kg.timmitof.keyboard.presentation.screens.keyboard.delegates.FieldContextDelegate
import kg.timmitof.keyboard.presentation.screens.keyboard.delegates.FontDelegate
import kg.timmitof.keyboard.presentation.screens.keyboard.delegates.LanguageDelegate
import kg.timmitof.keyboard.presentation.screens.keyboard.delegates.LayerDelegate
import kg.timmitof.keyboard.presentation.screens.keyboard.delegates.SettingsDelegate
import kg.timmitof.keyboard.presentation.screens.keyboard.delegates.SuggestionsDelegate
import kg.timmitof.keyboard.presentation.screens.keyboard.delegates.TextInputDelegate
import kg.timmitof.keyboard.presentation.screens.keyboard.delegates.UsageTracker

class KeyboardViewModelFactory(
    private val keyboardLayoutRepository: KeyboardLayoutRepository,
    private val emojiRepository: EmojiRepository,
    private val languageRepository: LanguageRepository,
    private val fontRepository: FontRepository,
    private val suggestionRepository: SuggestionRepository,
    private val keyboardSettingsRepository: KeyboardSettingsRepository,
    private val clipboardRepository: ClipboardRepository,
    private val analytics: Analytics,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(KeyboardViewModel::class.java)) {
            val layerDelegate = LayerDelegate(keyboardLayoutRepository)
            val emojiDelegate = EmojiDelegate(emojiRepository, layerDelegate)
            val suggestionsDelegate = SuggestionsDelegate(suggestionRepository)
            val usageTracker = UsageTracker(analytics)

            @Suppress("UNCHECKED_CAST")
            return KeyboardViewModel(
                layerDelegate = layerDelegate,
                textInputDelegate = TextInputDelegate(layerDelegate, emojiDelegate, suggestionsDelegate, usageTracker),
                emojiDelegate = emojiDelegate,
                languageDelegate = LanguageDelegate(languageRepository, layerDelegate),
                fontDelegate = FontDelegate(fontRepository),
                fieldContextDelegate = FieldContextDelegate(layerDelegate),
                suggestionsDelegate = suggestionsDelegate,
                settingsDelegate = SettingsDelegate(keyboardSettingsRepository),
                clipboardDelegate = ClipboardDelegate(clipboardRepository),
                usageTracker = usageTracker,
                analytics = analytics,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
