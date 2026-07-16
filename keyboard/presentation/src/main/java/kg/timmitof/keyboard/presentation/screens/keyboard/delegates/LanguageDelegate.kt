package kg.timmitof.keyboard.presentation.screens.keyboard.delegates

import kg.timmitof.keyboard.domain.model.KeyboardLanguage
import kg.timmitof.keyboard.domain.repository.LanguageRepository
import kg.timmitof.keyboard.presentation.screens.keyboard.KeyboardSyntax

internal class LanguageDelegate(
    private val languageRepository: LanguageRepository,
    private val layerDelegate: LayerDelegate,
) {

    /** Загружает локали и сохранённый выбор в состояние. */
    suspend fun KeyboardSyntax.loadLanguages() {
        val languages = languageRepository.getLanguages()
        val selected = languageRepository.getSelectedLanguage()
        reduce { state.copy(languages = languages, selectedLanguage = selected) }
    }

    /** Смена локали: сохраняет выбор и перезагружает раскладку языкозависимого слоя. */
    suspend fun KeyboardSyntax.selectLanguage(language: KeyboardLanguage) {
        if (language == state.selectedLanguage || language !in state.languages) return

        languageRepository.setSelectedLanguage(language.code)
        reduce { state.copy(selectedLanguage = language) }

        if (state.layer.usesLanguageLayout) {
            with(layerDelegate) { applyLayer(state.layer) }
        }
    }
}
