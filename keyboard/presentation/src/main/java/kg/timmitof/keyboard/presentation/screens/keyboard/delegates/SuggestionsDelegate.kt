package kg.timmitof.keyboard.presentation.screens.keyboard.delegates

import kg.timmitof.keyboard.suggestion.domain.model.SuggestionRequest
import kg.timmitof.keyboard.suggestion.domain.model.TextContext
import kg.timmitof.keyboard.suggestion.domain.model.WordSuggestion
import kg.timmitof.keyboard.suggestion.domain.repository.SuggestionRepository
import kg.timmitof.keyboard.presentation.screens.keyboard.KeyboardSyntax
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardSideEffect
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardState
import kg.timmitof.keyboard.presentation.screens.keyboard.states.ShiftState
import kg.timmitof.keyboard.presentation.screens.keyboard.states.isUpperCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.mapLatest

/**
 * Т9. Единственный вход — снимок текста вокруг курсора от сервиса.
 * Расчёт идёт в потоке: [mapLatest] отменяет устаревший, пока разбирается словарь, и ввод не ждёт.
 */
internal class SuggestionsDelegate(
    private val suggestionRepository: SuggestionRepository,
) {

    private val requests = MutableStateFlow<SuggestionRequest?>(null)

    /**
     * Пауза в несколько кадров: при быстром наборе промежуточные слова не видны, а словарь перебирать дорого.
     * Вместе с результатом идёт запрос — по нему видно, для какого слова подсказки посчитаны.
     */
    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val suggestions: Flow<Pair<SuggestionRequest, List<WordSuggestion>>> = requests
        .filterNotNull()
        .debounce(CALCULATION_DELAY_MILLIS)
        .mapLatest { request -> request to suggestionRepository.suggest(request) }

    suspend fun KeyboardSyntax.applyTextContext(context: TextContext) = updateContext(context)

    /** Клавиатура сама изменила текст — обновляем снимок сразу; ответ поля придёт позже и лишь подтвердит. */
    suspend fun KeyboardSyntax.applyLocalEdit(edit: (TextContext) -> TextContext) =
        updateContext(edit(state.textContext))

    private suspend fun KeyboardSyntax.updateContext(context: TextContext) {
        if (state.textContext == context) return

        reduce { state.copy(textContext = context, shiftState = state.autoShift(context)) }
        requestSuggestions()
    }

    /** Первая подсказка сворачивает карусель шрифтов; открытая пользователем вручную остаётся. */
    suspend fun KeyboardSyntax.applySuggestions(
        request: SuggestionRequest,
        suggestions: List<WordSuggestion>,
    ) {
        val word = request.context.composingWord
        if (state.suggestions == suggestions && state.suggestionsWord == word) return

        val collapseFonts = state.suggestions.isEmpty() && suggestions.isNotEmpty()
        reduce {
            state.copy(
                suggestions = suggestions,
                suggestionsWord = word,
                isFontsExpanded = state.isFontsExpanded && !collapseFonts,
            )
        }
        markComposingCorrection(suggestions)
    }

    private suspend fun KeyboardSyntax.markComposingCorrection(suggestions: List<WordSuggestion>) {
        val composing = state.composing
        if (!composing.isActive) return

        val hasCorrection = suggestions.any(WordSuggestion::isAutoCorrect)
        if (composing.hasCorrection == hasCorrection) return

        postSideEffect(KeyboardSideEffect.Input.SetComposingText(composing.text, hasCorrection))
        reduce { state.copy(composing = composing.copy(hasCorrection = hasCorrection)) }
    }

    suspend fun KeyboardSyntax.requestSuggestions() {
        val request = state.suggestionRequest()
        requests.value = request

        if (request == null && state.suggestions.isNotEmpty()) {
            reduce { state.copy(suggestions = emptyList(), suggestionsWord = "") }
        }
    }

    /** Если фоновый расчёт не успел, досчитываем здесь: лучше подождать пару мс, чем пропустить автозамену. */
    suspend fun awaitCorrection(state: KeyboardState): WordSuggestion? {
        if (state.hasFreshSuggestions) return state.pendingAutoCorrect

        val request = state.suggestionRequest() ?: return null
        return suggestionRepository.suggest(request).firstOrNull { it.isAutoCorrect }
    }

    private fun KeyboardState.suggestionRequest(): SuggestionRequest? {
        val languageCode = activeLanguage?.code ?: return null
        if (!allowsSuggestions) return null

        // Слово ещё не начато — это предсказание следующего, а его можно выключить.
        if (textContext.composingWord.isEmpty() && !settings.isNextWordPredictionEnabled) return null

        return SuggestionRequest(
            languageCode = languageCode,
            context = textContext,
            isShifted = shiftState.isUpperCase(),
            allowsAutoCorrect = fieldType.allowsAutoCorrect && settings.isAutoCorrectEnabled,
        )
    }

    suspend fun learnWord(state: KeyboardState, word: String) {
        val languageCode = state.activeLanguage?.code ?: return
        if (!state.allowsSuggestions || !state.settings.isLearningEnabled) return

        suggestionRepository.learn(
            languageCode = languageCode,
            previousWord = state.textContext.previousWord,
            word = word,
        )
    }

    suspend fun prefetch(languageCode: String) = suggestionRepository.prefetch(languageCode)

    /** Сбрасывает выученное на диск при завершении сессии ввода. */
    suspend fun flush() = suggestionRepository.flush()

    /**
     * Shift решаем только на границе слова: внутри слова он мог быть поднят вручную (имя), Caps Lock не сбрасываем никогда.
     */
    private fun KeyboardState.autoShift(context: TextContext): ShiftState = when {
        shiftState == ShiftState.CAPS_LOCK || !fieldType.autoCapitalize -> shiftState
        context.composingWord.isNotEmpty() -> shiftState
        context.isSentenceStart -> ShiftState.ACTIVE
        else -> ShiftState.DISABLED
    }

    private companion object {
        const val CALCULATION_DELAY_MILLIS = 45L
    }
}
