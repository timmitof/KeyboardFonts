package kg.timmitof.keyboard.presentation.screens.keyboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kg.timmitof.keyboard.domain.repository.EmojiRepository
import kg.timmitof.keyboard.domain.repository.FontRepository
import kg.timmitof.keyboard.domain.repository.KeyboardLayoutRepository
import kg.timmitof.keyboard.domain.repository.LanguageRepository
import kg.timmitof.keyboard.domain.repository.SuggestionRepository
import kg.timmitof.keyboard.presentation.screens.keyboard.delegates.EmojiDelegate
import kg.timmitof.keyboard.presentation.screens.keyboard.delegates.FieldContextDelegate
import kg.timmitof.keyboard.presentation.screens.keyboard.delegates.FontDelegate
import kg.timmitof.keyboard.presentation.screens.keyboard.delegates.LanguageDelegate
import kg.timmitof.keyboard.presentation.screens.keyboard.delegates.LayerDelegate
import kg.timmitof.keyboard.presentation.screens.keyboard.delegates.SuggestionsDelegate
import kg.timmitof.keyboard.presentation.screens.keyboard.delegates.TextInputDelegate

class KeyboardViewModelFactory(
    private val keyboardLayoutRepository: KeyboardLayoutRepository,
    private val emojiRepository: EmojiRepository,
    private val languageRepository: LanguageRepository,
    private val fontRepository: FontRepository,
    private val suggestionRepository: SuggestionRepository,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(KeyboardViewModel::class.java)) {
            val layerDelegate = LayerDelegate(keyboardLayoutRepository)
            val emojiDelegate = EmojiDelegate(emojiRepository, layerDelegate)
            val suggestionsDelegate = SuggestionsDelegate(suggestionRepository)

            @Suppress("UNCHECKED_CAST")
            return KeyboardViewModel(
                layerDelegate = layerDelegate,
                textInputDelegate = TextInputDelegate(layerDelegate, emojiDelegate, suggestionsDelegate),
                emojiDelegate = emojiDelegate,
                languageDelegate = LanguageDelegate(languageRepository, layerDelegate),
                fontDelegate = FontDelegate(fontRepository),
                fieldContextDelegate = FieldContextDelegate(layerDelegate),
                suggestionsDelegate = suggestionsDelegate,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
