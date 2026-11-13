package kg.timmitof.keyboard.presentation.screens.keyboard.delegates

import kg.timmitof.keyboard.presentation.screens.keyboard.KeyboardSyntax
import kg.timmitof.keyboard.presentation.screens.keyboard.states.AutoCorrection
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardSideEffect
import kg.timmitof.keyboard.presentation.screens.keyboard.states.LanguageDetectionRules
import kg.timmitof.keyboard.presentation.screens.keyboard.states.SelectionTranslation
import kg.timmitof.keyboard.presentation.screens.keyboard.states.SelectionTranslationStatus
import kg.timmitof.keyboard.translate.domain.model.TranslationLanguage
import kg.timmitof.keyboard.translate.domain.model.TranslationResult
import kg.timmitof.keyboard.translate.domain.repository.LanguageDetectionRepository
import kg.timmitof.keyboard.translate.domain.repository.TranslationDownloadRepository
import kg.timmitof.keyboard.translate.domain.repository.TranslationModelRepository
import kg.timmitof.keyboard.translate.domain.repository.TranslationRepository
import kg.timmitof.keyboard.translate.domain.repository.TranslatorPreferencesRepository

/**
 * Перевод выделенного в поле приложения: выделенное заменяется переводом, а Backspace сразу после —
 * возвращает оригинал (как отмена автозамены).
 *
 * Куда переводить: выделен чужой текст — на язык раскладки (понять входящее), свой — на язык перевода
 * этого приложения (отправить собеседнику). Язык выделенного определяется один раз на нажатие.
 */
internal class SelectionTranslationDelegate(
    private val detectionRepository: LanguageDetectionRepository,
    private val translationRepository: TranslationRepository,
    private val modelRepository: TranslationModelRepository,
    private val downloadRepository: TranslationDownloadRepository,
    private val preferencesRepository: TranslatorPreferencesRepository,
    private val suggestionsDelegate: SuggestionsDelegate,
) {

    suspend fun KeyboardSyntax.translateSelection() {
        val text = state.textContext.selected.takeIf { it.isNotBlank() } ?: return
        if (!state.allowsTranslation || state.selectionTranslation?.isWorkingOn(text) == true) return

        setStatus(text, SelectionTranslationStatus.TRANSLATING)
        try {
            val pair = pairFor(text) ?: return setStatus(text, SelectionTranslationStatus.UNSUPPORTED)
            val (source, target) = pair

            // Языка нет на телефоне — ставим в загрузку (с уведомлением) и говорим об этом на кнопке.
            val missing = listOf(source.code, target.code) - modelRepository.downloadedCodes()
            if (missing.isNotEmpty()) {
                downloadRepository.download(missing)
                return setStatus(text, SelectionTranslationStatus.DOWNLOADING)
            }

            val result = translationRepository.translate(text, source.code, target.code)
            if (result !is TranslationResult.Success) return setStatus(text, SelectionTranslationStatus.FAILED)
            // Пока переводили, выделение сменили — заменять уже нечего.
            if (state.textContext.selected != text) return setStatus(text, null)

            replace(text, result.text)
        } finally {
            // Панель перевода закрыта — моделям в памяти делать нечего.
            if (state.translator == null) {
                detectionRepository.release()
                translationRepository.release()
            }
        }
    }

    /** Выделенное заменяется переводом; отмена — как у автозамены: Backspace сразу после вернёт оригинал. */
    private suspend fun KeyboardSyntax.replace(original: String, translation: String) {
        input(KeyboardSideEffect.Input.CommitText(translation))
        reduce {
            state.copy(
                autoCorrection = AutoCorrection(original = original, corrected = translation),
                selectionTranslation = null,
            )
        }
        with(suggestionsDelegate) { applyLocalEdit { it.appending(translation) } }
    }

    /** Чужой текст — на язык раскладки, свой — на язык перевода этого приложения (или английский). */
    private suspend fun KeyboardSyntax.pairFor(text: String): Pair<TranslationLanguage, TranslationLanguage>? {
        val layout = state.activeLanguage?.let { modelRepository.languageForLayout(it.code) }
        val detected = LanguageDetectionRules.confidentCode(
            detectionRepository.detect(text.take(LanguageDetectionRules.MAX_TEXT_LENGTH))
        )
        val source = (detected ?: layout?.code)?.let(modelRepository::language) ?: return null

        val target = if (layout != null && source.code != layout.code) {
            layout
        } else {
            val appTarget = preferencesRepository.getPreferences()
                .targetFor(state.fieldContext.packageName)
                ?.let(modelRepository::language)
            appTarget?.takeIf { it.code != source.code }
                ?: modelRepository.language(ENGLISH)?.takeIf { it.code != source.code }
                ?: return null
        }
        return source to target
    }

    private suspend fun KeyboardSyntax.setStatus(text: String, status: SelectionTranslationStatus?) {
        reduce { state.copy(selectionTranslation = status?.let { SelectionTranslation(text, it) }) }
    }

    private companion object {
        const val ENGLISH = "en"
    }
}
