package kg.timmitof.keyboard.presentation.screens.keyboard.delegates

import kg.timmitof.keyboard.domain.model.KeyboardLanguage
import kg.timmitof.keyboard.domain.model.KeyboardLanguages
import kg.timmitof.keyboard.domain.repository.LanguageRepository
import kg.timmitof.keyboard.presentation.screens.keyboard.KeyboardSyntax
import kotlinx.coroutines.flow.Flow

/** Список языков правит экран настроек в другом процессе, поэтому он приходит потоком, как настройки. */
internal class LanguageDelegate(
    private val languageRepository: LanguageRepository,
    private val layerDelegate: LayerDelegate,
) {

    val languages: Flow<KeyboardLanguages> = languageRepository.observeLanguages()

    suspend fun KeyboardSyntax.loadLanguages() {
        val languages = languageRepository.getLanguages()

        reduce {
            val loaded = state.copy(
                languages = languages.enabled,
                selectedLanguage = languages.selected,
                latinLanguage = languages.latin,
            )
            // Поле могло открыться раньше, чем загрузились раскладки.
            loaded.copy(fieldLanguage = loaded.latinLanguageFor(loaded.fieldType))
        }
    }

    /**
     * Переключаем только включённые языки. Удалили выбранный — репозиторий уже выбрал первый включённый;
     * ручной выбор в поле (отменённая подмена на латиницу) сохраняется, пока выбранный язык тот же.
     * @return сменился ли язык ввода — тогда нужны новые подсказки.
     */
    suspend fun KeyboardSyntax.applyLanguages(languages: KeyboardLanguages): Boolean {
        if (state.languages == languages.enabled && state.selectedLanguage == languages.selected &&
            state.latinLanguage == languages.latin
        ) return false

        val previous = state.activeLanguage
        val isSelectionChanged = state.selectedLanguage != languages.selected

        reduce {
            val updated = state.copy(
                languages = languages.enabled,
                selectedLanguage = languages.selected,
                latinLanguage = languages.latin,
            )
            val keepsManualChoice = !isSelectionChanged && state.fieldLanguage == null
            updated.copy(fieldLanguage = if (keepsManualChoice) null else updated.latinLanguageFor(updated.fieldType))
        }

        if (state.activeLanguage == previous) return false

        if (state.layer.usesLanguageLayout) {
            with(layerDelegate) { applyLayer(state.layer) }
        }
        return true
    }

    /** Ручной выбор отменяет автоматическую подмену раскладки в поле (адрес, пароль). */
    suspend fun KeyboardSyntax.selectLanguage(language: KeyboardLanguage) {
        if (language == state.activeLanguage || language !in state.languages) return

        languageRepository.setSelectedLanguage(language.code)
        reduce { state.copy(selectedLanguage = language, fieldLanguage = null) }

        if (state.layer.usesLanguageLayout) {
            with(layerDelegate) { applyLayer(state.layer) }
        }
    }
}
