package kg.timmitof.keyboard.presentation.screens.keyboard.delegates

import kg.timmitof.keyboard.domain.model.KeyboardLanguage
import kg.timmitof.keyboard.presentation.screens.keyboard.KeyboardSyntax
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardOverlay
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardSideEffect
import kg.timmitof.keyboard.presentation.screens.keyboard.states.InlineEditor
import kg.timmitof.keyboard.presentation.screens.keyboard.states.TranslationRequest
import kg.timmitof.keyboard.presentation.screens.keyboard.states.TranslatorState
import kg.timmitof.keyboard.presentation.screens.keyboard.states.TranslatorStatus
import kg.timmitof.keyboard.translate.domain.model.TranslationLanguage
import kg.timmitof.keyboard.translate.domain.model.TranslationResult
import kg.timmitof.keyboard.translate.domain.repository.TranslationModelRepository
import kg.timmitof.keyboard.translate.domain.repository.TranslationRepository
import kg.timmitof.keyboard.translate.domain.repository.TranslatorPreferencesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.transformLatest

/**
 * Панель перевода: набор идёт в её поле ([TranslatorState.editor], см. [input]), а в поле приложения
 * через паузу в наборе попадает перевод — областью компоновки, чтобы следующий перевод заменил предыдущий.
 */
internal class TranslatorDelegate(
    private val translationRepository: TranslationRepository,
    private val modelRepository: TranslationModelRepository,
    private val preferencesRepository: TranslatorPreferencesRepository,
) {

    /** Скачанные языки: лист выбора и кнопка «Скачать» обновляются сами после загрузки. */
    val downloadedModels: Flow<Set<String>> = modelRepository.observeDownloaded()

    /**
     * Перевод через паузу после последней буквы: поле приложения не дёргается на каждое нажатие.
     * Новый запрос (и закрытие панели) отменяет недоделанный перевод старого.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun translations(requests: Flow<TranslationRequest?>): Flow<Pair<TranslationRequest, TranslationResult>> =
        requests
            .distinctUntilChanged()
            .transformLatest { request ->
                if (request == null) return@transformLatest
                if (request.text.isNotBlank()) delay(TRANSLATION_DELAY_MILLIS)
                emit(request to translate(request))
            }

    /** Черновик поля приложения закрывает вызывающий: дальше ввод пойдёт в панель. */
    suspend fun KeyboardSyntax.open() {
        if (state.translator != null || !state.allowsTranslation) return

        val preferences = preferencesRepository.getPreferences()
        val layouts = layoutsOf(state.languages)
        val source = sourceOf(state.activeLanguage)
        val target = preferences.targetCode?.let(modelRepository::language) ?: defaultTarget(source, layouts)
        val downloaded = modelRepository.downloadedCodes()
        val before = state.textContext.before

        reduce {
            state.copy(
                translator = TranslatorState(
                    source = source,
                    target = target,
                    languages = modelRepository.languages(),
                    layouts = layouts,
                    downloadedCodes = downloaded,
                    isStyled = preferences.isStyled,
                    needsLeadingSpace = before.isNotEmpty() && !before.last().isWhitespace(),
                ),
                isFontsExpanded = false,
                keyboardOverlay = null,
                suggestions = emptyList(),
                suggestionsWord = "",
                autoCorrection = null,
                keptWord = null,
            )
        }
    }

    /**
     * Перевод остаётся в поле приложения; снимок поля перечитываем — он всё это время был панельным.
     * Модель выгружаем из памяти: пока панель закрыта, она клавиатуре не нужна.
     */
    suspend fun KeyboardSyntax.close() {
        if (state.translator == null) return

        reduce {
            state.copy(
                translator = null,
                keyboardOverlay = state.keyboardOverlay.takeIf { it != KeyboardOverlay.TRANSLATION_LANGUAGES },
                suggestions = emptyList(),
                suggestionsWord = "",
                autoCorrection = null,
                keptWord = null,
            )
        }
        postSideEffect(KeyboardSideEffect.Input.FinishComposing)
        postSideEffect(KeyboardSideEffect.Input.RefreshTextContext)
        release()
    }

    fun release() = translationRepository.release()

    suspend fun KeyboardSyntax.applyDownloaded(codes: Set<String>) {
        if (state.translator?.downloadedCodes == codes) return

        updateTranslator { it.copy(downloadedCodes = codes) }
    }

    /** Перевод устаревшего запроса не пишем: пока он считался, текст в панели уже другой. */
    suspend fun KeyboardSyntax.applyTranslation(request: TranslationRequest, result: TranslationResult) {
        val translator = state.translator ?: return
        if (translator.request != request) return

        when (result) {
            is TranslationResult.Success -> {
                writeToField(translator, result.text)
                updateTranslator {
                    it.copy(translation = result.text, translatedFor = request, status = TranslatorStatus.READY)
                }
            }

            TranslationResult.ModelMissing -> updateTranslator {
                it.copy(translatedFor = request, status = TranslatorStatus.MODEL_MISSING)
            }

            TranslationResult.Failed -> updateTranslator {
                it.copy(translatedFor = request, status = TranslatorStatus.FAILED)
            }
        }
    }

    /**
     * Enter отправляет перевод. Не успел досчитаться — переводим сразу, ждать паузу незачем.
     * Перевода нет (язык не скачан) — ничего не отправляем, иначе набранное пропадёт.
     */
    suspend fun KeyboardSyntax.send() {
        val translator = state.translator ?: return
        val request = translator.request
        if (request != null && translator.isTranslating) {
            applyTranslation(request, translate(request))
        }

        val current = state.translator ?: return
        val hasTranslation = current.translation.isNotBlank() && current.status == TranslatorStatus.READY &&
                current.translatedFor == current.request
        if (current.editor.text.isNotBlank() && !hasTranslation) return

        postSideEffect(KeyboardSideEffect.Input.FinishComposing)
        postSideEffect(KeyboardSideEffect.Input.PerformEditorAction)
        updateTranslator {
            it.copy(editor = InlineEditor(), translation = "", translatedFor = null, needsLeadingSpace = false)
        }
    }

    suspend fun KeyboardSyntax.selectTarget(language: TranslationLanguage) {
        if (state.translator == null) return

        preferencesRepository.setTarget(language.code)
        reduce {
            val translator = state.translator ?: return@reduce state
            state.copy(
                translator = translator.copy(target = language, status = TranslatorStatus.READY),
                keyboardOverlay = null,
            )
        }
    }

    suspend fun KeyboardSyntax.toggleStyled() {
        val translator = state.translator ?: return
        val isStyled = !translator.isStyled

        preferencesRepository.setStyled(isStyled)
        updateTranslator { it.copy(isStyled = isStyled) }
        if (translator.translation.isNotEmpty()) writeToField(translator.copy(isStyled = isStyled), translator.translation)
    }

    /** Модели качаются с сети один раз; после этого тот же текст переводим заново. */
    suspend fun KeyboardSyntax.download() {
        val translator = state.translator ?: return
        val source = translator.source ?: return
        if (translator.status == TranslatorStatus.DOWNLOADING) return

        updateTranslator { it.copy(status = TranslatorStatus.DOWNLOADING) }
        val isDownloaded = modelRepository.download(listOf(source.code, translator.target.code))
        updateTranslator {
            it.copy(
                status = if (isDownloaded) TranslatorStatus.READY else TranslatorStatus.DOWNLOAD_FAILED,
                revision = it.revision + 1,
            )
        }
    }

    /**
     * Языки местами: раскладку переключает вызывающий ([TranslatorState.swapLayout]), здесь — пара и текст.
     * Готовый перевод становится набранным текстом и переводится обратно.
     */
    suspend fun KeyboardSyntax.swapped(previousSource: TranslationLanguage) {
        preferencesRepository.setTarget(previousSource.code)
        updateTranslator {
            val text = it.translation.ifEmpty { it.editor.text }
            it.copy(
                source = sourceOf(state.activeLanguage),
                target = previousSource,
                editor = InlineEditor(text = text),
                translation = "",
                translatedFor = null,
                status = TranslatorStatus.READY,
            )
        }
    }

    /** Сменили раскладку (или список включённых языков) — исходный язык перевода следует за ней. */
    suspend fun KeyboardSyntax.syncSource() {
        val translator = state.translator ?: return
        val source = sourceOf(state.activeLanguage)
        val layouts = layoutsOf(state.languages)
        if (source == translator.source && layouts == translator.layouts) return

        updateTranslator { it.copy(source = source, layouts = layouts, status = TranslatorStatus.READY) }
    }

    private suspend fun translate(request: TranslationRequest): TranslationResult =
        translationRepository.translate(request.text, request.sourceCode, request.targetCode)

    /** Перевод — областью компоновки: следующий перевод заменит её целиком. Пишется мимо [input] — это поле приложения. */
    private suspend fun KeyboardSyntax.writeToField(translator: TranslatorState, translation: String) {
        val styled = state.translationFont(translator.target, translator.isStyled).apply(translation)
        val text = if (translator.needsLeadingSpace && styled.isNotEmpty()) " $styled" else styled
        postSideEffect(KeyboardSideEffect.Input.SetComposingText(text))
    }

    private suspend fun KeyboardSyntax.updateTranslator(update: (TranslatorState) -> TranslatorState) {
        reduce {
            val translator = state.translator ?: return@reduce state
            state.copy(translator = update(translator))
        }
    }

    private fun sourceOf(language: KeyboardLanguage?): TranslationLanguage? =
        language?.let { modelRepository.languageForLayout(it.code) }

    /** Языки перевода, на которых можно печатать: только на них можно поменять пару местами. */
    private fun layoutsOf(languages: List<KeyboardLanguage>): Map<String, KeyboardLanguage> =
        languages.mapNotNull { language -> modelRepository.languageForLayout(language.code)?.let { it.code to language } }
            .toMap()

    /** По умолчанию — английский, а с английской раскладки — другой включённый язык. */
    private fun defaultTarget(
        source: TranslationLanguage?,
        layouts: Map<String, KeyboardLanguage>,
    ): TranslationLanguage {
        val candidates = listOfNotNull(modelRepository.language(ENGLISH)) + layouts.keys.mapNotNull(modelRepository::language)
        return candidates.firstOrNull { it.code != source?.code } ?: modelRepository.languages().first()
    }

    private companion object {
        const val TRANSLATION_DELAY_MILLIS = 300L
        const val ENGLISH = "en"
    }
}
