package kg.timmitof.keyboard.presentation.screens.keyboard.delegates

import kg.timmitof.keyboard.domain.model.KeyboardLanguage
import kg.timmitof.keyboard.presentation.screens.keyboard.KeyboardSyntax
import kg.timmitof.keyboard.presentation.screens.keyboard.states.ComposingText
import kg.timmitof.keyboard.presentation.screens.keyboard.states.DetectionRequest
import kg.timmitof.keyboard.presentation.screens.keyboard.states.LanguageDetectionRules
import kg.timmitof.keyboard.presentation.screens.keyboard.states.InlineEditor
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardOverlay
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardSideEffect
import kg.timmitof.keyboard.presentation.screens.keyboard.states.TranslationRequest
import kg.timmitof.keyboard.presentation.screens.keyboard.states.TranslatorState
import kg.timmitof.keyboard.presentation.screens.keyboard.states.TranslatorStatus
import kg.timmitof.keyboard.presentation.screens.keyboard.states.TranslatorSummary
import kg.timmitof.keyboard.translate.domain.model.DetectedLanguage
import kg.timmitof.keyboard.translate.domain.model.TranslationLanguage
import kg.timmitof.keyboard.translate.domain.model.TranslationModels
import kg.timmitof.keyboard.translate.domain.model.TranslationResult
import kg.timmitof.keyboard.translate.domain.repository.LanguageDetectionRepository
import kg.timmitof.keyboard.translate.domain.repository.TranslationDownloadRepository
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
    private val downloadRepository: TranslationDownloadRepository,
    private val detectionRepository: LanguageDetectionRepository,
) {

    /** Модели на телефоне и в очереди: панель и лист выбора обновляются сами, где бы ни шла загрузка. */
    val models: Flow<TranslationModels> = downloadRepository.observeModels()

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

    /**
     * Язык набранного — после паузы подольше, чем перевод: определять каждое слово незачем,
     * а на половине фразы ML Kit ошибается чаще.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun detections(requests: Flow<DetectionRequest?>): Flow<Pair<DetectionRequest, List<DetectedLanguage>>> =
        requests
            .distinctUntilChanged()
            .transformLatest { request ->
                if (request == null) return@transformLatest
                delay(DETECTION_DELAY_MILLIS)
                emit(request to detectionRepository.detect(request.text))
            }

    /** Подсказка «Новое: переводчик» висит в верхней строке, пока переводчик ни разу не открыли. */
    suspend fun KeyboardSyntax.loadHint() {
        val isPending = !preferencesRepository.getPreferences().isHintShown
        if (state.isTranslatorHintPending != isPending) reduce { state.copy(isTranslatorHintPending = isPending) }
    }

    /**
     * Сводка для быстрых настроек — та же пара, что откроет «文A»: последний язык перевода
     * или язык по умолчанию. Скачанные — по алфавиту.
     */
    suspend fun KeyboardSyntax.loadSummary() {
        val preferences = preferencesRepository.getPreferences()
        val source = sourceOf(state.activeLanguage)
        val target = preferences.targetCode?.let(modelRepository::language)
            ?: defaultTarget(source, layoutsOf(state.languages))
        val offline = modelRepository.downloadedCodes()
            .mapNotNull(modelRepository::language)
            .map(TranslationLanguage::name)
            .sorted()

        val summary = TranslatorSummary(
            sourceName = if (preferences.isAutoSource) null else source?.name ?: state.activeLanguage?.displayName,
            targetName = target.name,
            offlineNames = offline,
        )
        if (state.translatorSummary != summary) reduce { state.copy(translatorSummary = summary) }
    }

    /** Черновик поля приложения закрывает вызывающий: дальше ввод пойдёт в панель. */
    suspend fun KeyboardSyntax.open() {
        if (state.translator != null || !state.allowsTranslation) return

        val preferences = preferencesRepository.getPreferences()
        val layouts = layoutsOf(state.languages)
        val source = sourceOf(state.activeLanguage)
        val target = preferences.targetCode?.let(modelRepository::language) ?: defaultTarget(source, layouts)
        val models = TranslationModels(downloaded = modelRepository.downloadedCodes())
        val before = state.textContext.before

        reduce {
            state.copy(
                translator = TranslatorState(
                    source = source,
                    target = target,
                    languages = modelRepository.languages(),
                    layouts = layouts,
                    models = models,
                    recentTargets = preferences.recentTargets.mapNotNull(modelRepository::language),
                    isAutoSource = preferences.isAutoSource,
                    needsLeadingSpace = before.isNotEmpty() && !before.last().isWhitespace(),
                ).withDistinctPair(previousSource = null),
                isFontsExpanded = false,
                keyboardOverlay = null,
                suggestions = emptyList(),
                suggestionsWord = "",
                autoCorrection = null,
                keptWord = null,
                isTranslatorHintPending = false,
            )
        }
        if (!preferences.isHintShown) preferencesRepository.setHintShown()
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
                keyboardOverlay = state.keyboardOverlay.takeUnless { it?.isTranslator == true },
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

    /**
     * Клавиатуру скрыли («Назад» закрывает её сразу, вместе с панелью). Перевод в поле уже зафиксировал
     * сервис — здесь только убираем панель и отпускаем модель.
     */
    suspend fun KeyboardSyntax.dismiss() {
        if (state.translator == null) return

        reduce {
            state.copy(
                translator = null,
                composing = ComposingText(),
                keyboardOverlay = state.keyboardOverlay.takeUnless { it?.isTranslator == true },
            )
        }
        release()
    }

    fun release() {
        translationRepository.release()
        detectionRepository.release()
    }

    /**
     * Результат определения устарел (текст или раскладка уже другие) — не применяем.
     * В обычном режиме — подсказка сменить раскладку, в автоматическом — исходный язык перевода.
     */
    suspend fun KeyboardSyntax.applyDetection(request: DetectionRequest, detected: List<DetectedLanguage>) {
        val translator = state.translator ?: return
        if (state.translatorDetectionRequest != request) return

        if (translator.isAutoSource) {
            val language = LanguageDetectionRules.confidentCode(detected)?.let(modelRepository::language)
            // Не понять — оставляем прежний: исходный язык не должен мигать на каждой паузе.
            updateTranslator { it.copy(autoSource = language ?: it.autoSource, suggestedLayout = null) }
        } else {
            val layout = LanguageDetectionRules.suggestLayout(detected, state.activeLanguage, state.languages, request.text)
            updateTranslator { it.copy(suggestedLayout = layout) }
        }
    }

    /** «Оставить как есть»: эту раскладку больше не предлагаем, пока поле не очистят. */
    suspend fun KeyboardSyntax.dismissDetection() {
        updateTranslator { it.copy(dismissedLayoutCode = it.suggestedLayout?.code, suggestedLayout = null) }
    }

    /** «Определять автоматически»: исходный язык — по тексту, раскладка не меняется. */
    suspend fun KeyboardSyntax.selectAutoSource() {
        preferencesRepository.setAutoSource(true)
        reduce {
            val translator = state.translator ?: return@reduce state
            state.copy(
                translator = translator.copy(isAutoSource = true, suggestedLayout = null, status = TranslatorStatus.READY),
                keyboardOverlay = null,
            )
        }
    }

    /** Выбрали язык вручную — автоопределение выключается, исходный снова равен раскладке. */
    suspend fun KeyboardSyntax.disableAutoSource() {
        if (state.translator?.isAutoSource != true) return

        preferencesRepository.setAutoSource(false)
        updateTranslator { it.copy(isAutoSource = false, autoSource = null) }
    }

    /**
     * Загрузка идёт в фоне (WorkManager), поэтому её исход панель узнаёт отсюда: пара скачалась — переводим
     * набранное заново; пропала из очереди нескачанной — загрузка не удалась.
     */
    suspend fun KeyboardSyntax.applyModels(models: TranslationModels) {
        val translator = state.translator ?: return
        if (translator.models == models) return

        updateTranslator { it.withModels(models) }
    }

    private fun TranslatorState.withModels(models: TranslationModels): TranslatorState {
        val isDownloadedNow = pairCodes.all { it in models.downloaded }
        val isQueuedNow = pairCodes.any { it in models.downloading }
        // «Не скачалось» — только если загрузка была в очереди и пропала из неё: сразу после нажатия
        // очередь может ещё не обновиться.
        val wasQueued = pairCodes.any { it in this.models.downloading }

        val status = when {
            isDownloadedNow -> if (status == TranslatorStatus.FAILED) status else TranslatorStatus.READY
            isQueuedNow -> TranslatorStatus.DOWNLOADING
            status == TranslatorStatus.DOWNLOADING && wasQueued -> TranslatorStatus.DOWNLOAD_FAILED
            else -> status
        }
        val revision = if (isDownloadedNow && !isPairDownloaded) revision + 1 else revision
        return copy(models = models, status = status, revision = revision)
    }

    /** Перевод устаревшего запроса не пишем: пока он считался, текст в панели уже другой. */
    suspend fun KeyboardSyntax.applyTranslation(request: TranslationRequest, result: TranslationResult) {
        val translator = state.translator ?: return
        if (translator.request != request) return

        when (result) {
            is TranslationResult.Success -> {
                writeToField(translator.target, translator.needsLeadingSpace, result.text)
                updateTranslator {
                    it.copy(translation = result.text, translatedFor = request, status = TranslatorStatus.READY)
                }
            }

            // Скачана ли пара, панель знает по моделям — здесь только отмечаем, что запрос обработан.
            TranslationResult.ModelMissing -> updateTranslator { it.copy(translatedFor = request) }

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
        val hasTranslation = current.translation.isNotBlank() && current.notice == null &&
                current.translatedFor == current.request
        if (current.editor.text.isNotBlank() && !hasTranslation) return

        postSideEffect(KeyboardSideEffect.Input.FinishComposing)
        postSideEffect(KeyboardSideEffect.Input.PerformEditorAction)
        updateTranslator {
            it.copy(editor = InlineEditor(), translation = "", translatedFor = null, needsLeadingSpace = false)
                .withoutDetection()
        }
    }

    /** «×» в поле: стирает набранное, а пустой запрос уберёт перевод и из поля приложения. */
    suspend fun KeyboardSyntax.clear() {
        updateTranslator { it.copy(editor = InlineEditor(), status = TranslatorStatus.READY).withoutDetection() }
    }

    suspend fun KeyboardSyntax.retry() {
        updateTranslator { it.copy(status = TranslatorStatus.READY, revision = it.revision + 1) }
    }

    suspend fun KeyboardSyntax.selectTarget(language: TranslationLanguage) {
        if (state.translator == null) return

        preferencesRepository.setTarget(language.code)
        reduce {
            val translator = state.translator ?: return@reduce state
            state.copy(
                translator = translator.copy(
                    target = language,
                    status = TranslatorStatus.READY,
                    languageSearch = null,
                ).withRecent(language),
                keyboardOverlay = null,
            )
        }
    }

    /**
     * «Найти язык»: лист закрывается, чтобы были видны клавиши, а вместо панели — строка поиска.
     * Черновик панели закрывает вызывающий: дальше ввод идёт в запрос.
     */
    suspend fun KeyboardSyntax.openSearch() {
        reduce {
            val translator = state.translator ?: return@reduce state
            state.copy(
                translator = translator.copy(languageSearch = InlineEditor(), isFontPickerOpen = false),
                keyboardOverlay = null,
            )
        }
    }

    suspend fun KeyboardSyntax.closeSearch() {
        updateTranslator { it.copy(languageSearch = null) }
    }

    /** Enter в поиске — первый найденный язык, как самый подходящий. */
    suspend fun KeyboardSyntax.selectFirstFound() {
        val language = state.translator?.searchResults?.firstOrNull()
        if (language == null) closeSearch() else selectTarget(language)
    }

    /** «↓» в списке: качаем, не меняя пару, — язык пригодится позже, в том числе без сети. */
    suspend fun KeyboardSyntax.downloadLanguage(language: TranslationLanguage) {
        if (language.code in (state.translator?.models?.downloaded ?: return)) return

        downloadRepository.download(listOf(language.code))
    }

    suspend fun KeyboardSyntax.setFontPickerOpen(isOpen: Boolean) {
        updateTranslator { it.copy(isFontPickerOpen = isOpen) }
    }

    /** Сменили шрифт — готовый перевод в поле перерисовываем сразу, не дожидаясь нового набора. */
    suspend fun KeyboardSyntax.restyle() {
        val translator = state.translator ?: return
        if (translator.translation.isEmpty()) return

        writeToField(translator.target, translator.needsLeadingSpace, translator.translation)
    }

    /**
     * Модели качаются один раз — в фоне, с уведомлением, и переживают закрытие клавиатуры.
     * Исход придёт через [applyModels].
     */
    suspend fun KeyboardSyntax.download() {
        val translator = state.translator ?: return
        if (translator.source == null || translator.status == TranslatorStatus.DOWNLOADING) return

        downloadRepository.download(translator.pairCodes)
        updateTranslator { it.copy(status = TranslatorStatus.DOWNLOADING) }
    }

    /**
     * Языки местами: раскладку переключает вызывающий ([TranslatorState.swapLayout]), здесь — пара и текст.
     * Готовый перевод становится набранным текстом и переводится обратно.
     */
    suspend fun KeyboardSyntax.swapped(previousSource: TranslationLanguage) {
        preferencesRepository.setTarget(previousSource.code)
        updateTranslator {
            val text = it.translation.ifEmpty { it.editor.text }
            it.withRecent(previousSource).copy(
                source = sourceOf(state.activeLanguage),
                target = previousSource,
                editor = InlineEditor(text = text),
                translation = "",
                translatedFor = null,
                status = TranslatorStatus.READY,
            )
        }
    }

    /**
     * Сменили раскладку (или список включённых языков) — исходный язык перевода следует за ней.
     * Совпал с целевым — пара меняется местами: с английской раскладки переводим на прежний язык.
     */
    suspend fun KeyboardSyntax.syncSource() {
        val translator = state.translator ?: return
        val source = sourceOf(state.activeLanguage)
        val layouts = layoutsOf(state.languages)
        if (source == translator.source && layouts == translator.layouts) return

        // Раскладку сменили — прежняя подсказка «другой язык» больше не к месту.
        val updated = translator.copy(source = source, layouts = layouts, status = TranslatorStatus.READY, suggestedLayout = null)
            .withDistinctPair(previousSource = translator.source)
        if (updated.target != translator.target) preferencesRepository.setTarget(updated.target.code)
        val final = if (updated.target != translator.target) updated.withRecent(updated.target) else updated
        updateTranslator { final.copy(editor = it.editor, models = it.models, languageSearch = it.languageSearch) }
    }

    /** Новое сообщение — язык определяем с чистого листа. */
    private fun TranslatorState.withoutDetection(): TranslatorState =
        copy(suggestedLayout = null, dismissedLayoutCode = null, autoSource = null)

    /** Тот же порядок, что пишет хранилище: свежий первым, без повторов, не больше [MAX_RECENT]. */
    private fun TranslatorState.withRecent(language: TranslationLanguage): TranslatorState =
        copy(recentTargets = (listOf(language) + recentTargets.filter { it.code != language.code }).take(MAX_RECENT))

    /** Исходный и целевой совпали — целевым становится прежний исходный (или язык по умолчанию). */
    private fun TranslatorState.withDistinctPair(previousSource: TranslationLanguage?): TranslatorState {
        if (source == null || source.code != target.code) return this

        val target = previousSource?.takeIf { it.code != source.code } ?: defaultTarget(source, layouts)
        return copy(target = target)
    }

    private suspend fun translate(request: TranslationRequest): TranslationResult =
        translationRepository.translate(request.text, request.sourceCode, request.targetCode)

    /** Перевод — областью компоновки: следующий перевод заменит её целиком. Пишется мимо [input] — это поле приложения. */
    private suspend fun KeyboardSyntax.writeToField(
        target: TranslationLanguage,
        needsLeadingSpace: Boolean,
        translation: String,
    ) {
        val styled = state.translationFont(target).apply(translation)
        val text = if (needsLeadingSpace && styled.isNotEmpty()) " $styled" else styled
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

    /** Языки перевода, на которых можно печатать: из них выбирают исходный и с ними меняют пару местами. */
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
        const val DETECTION_DELAY_MILLIS = 500L
        const val ENGLISH = "en"
        const val MAX_RECENT = 4
    }
}
