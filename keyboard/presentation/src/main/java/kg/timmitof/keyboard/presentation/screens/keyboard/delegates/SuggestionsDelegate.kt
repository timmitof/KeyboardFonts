package kg.timmitof.keyboard.presentation.screens.keyboard.delegates

import kg.timmitof.keyboard.domain.model.SuggestionRequest
import kg.timmitof.keyboard.domain.model.TextContext
import kg.timmitof.keyboard.domain.model.WordSuggestion
import kg.timmitof.keyboard.domain.repository.SuggestionRepository
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
 * Т9: подсказки слов и всё, что клавиатура делает «сама».
 *
 * Единственный вход — снимок текста вокруг курсора, который присылает сервис
 * после каждой правки поля. От него зависят и подсказки, и автоматический Shift
 * в начале предложения.
 *
 * Расчёт подсказок вынесен в поток: пока идёт разбор словаря, пользователь
 * успевает нажать следующую клавишу, и [mapLatest] отменяет устаревший расчёт,
 * не заставляя ввод ждать.
 */
internal class SuggestionsDelegate(
    private val suggestionRepository: SuggestionRepository,
) {

    private val requests = MutableStateFlow<SuggestionRequest?>(null)

    /**
     * Подсказки считаются в фоне, с паузой в несколько кадров: при быстром наборе
     * промежуточные слова всё равно никто не увидит, а словарь перебирать дорого.
     *
     * Вместе с результатом идёт и запрос — по нему видно, для какого слова
     * подсказки посчитаны.
     */
    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val suggestions: Flow<Pair<SuggestionRequest, List<WordSuggestion>>> = requests
        .filterNotNull()
        .debounce(CALCULATION_DELAY_MILLIS)
        .mapLatest { request -> request to suggestionRepository.suggest(request) }

    /** Снимок текста от поля ввода: пересчитать подсказки и поправить Shift. */
    suspend fun KeyboardSyntax.applyTextContext(context: TextContext) = updateContext(context)

    /**
     * Клавиатура сама изменила текст и знает результат — обновляем снимок сразу.
     *
     * Ответ поля придёт позже отдельным событием и просто подтвердит то же самое,
     * а до тех пор подсказки и авто-Shift уже считаются по актуальному тексту.
     */
    suspend fun KeyboardSyntax.applyLocalEdit(edit: (TextContext) -> TextContext) =
        updateContext(edit(state.textContext))

    private suspend fun KeyboardSyntax.updateContext(context: TextContext) {
        if (state.textContext == context) return

        reduce { state.copy(textContext = context, shiftState = state.autoShift(context)) }
        requestSuggestions()
    }

    /**
     * Кладёт свежие подсказки в состояние.
     *
     * Первая же подсказка сворачивает карусель шрифтов: пока пользователь набирает
     * слово, полезнее видеть варианты. Если он открыл карусель сам — она останется,
     * потому что подсказки к этому моменту уже были показаны.
     */
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

    /**
     * Помечает черновик в поле как «будет исправлен»
     */
    private suspend fun KeyboardSyntax.markComposingCorrection(suggestions: List<WordSuggestion>) {
        val composing = state.composing
        if (!composing.isActive) return

        val hasCorrection = suggestions.any(WordSuggestion::isAutoCorrect)
        if (composing.hasCorrection == hasCorrection) return

        postSideEffect(KeyboardSideEffect.Input.SetComposingText(composing.text, hasCorrection))
        reduce { state.copy(composing = composing.copy(hasCorrection = hasCorrection)) }
    }

    /** Пересобирает запрос под текущее состояние; в неподходящих полях — гасит подсказки. */
    suspend fun KeyboardSyntax.requestSuggestions() {
        val request = state.suggestionRequest()
        requests.value = request

        if (request == null && state.suggestions.isNotEmpty()) {
            reduce { state.copy(suggestions = emptyList(), suggestionsWord = "") }
        }
    }

    /**
     * Автозамена для слова, которое сейчас заканчивается пробелом.
     *
     * Если фоновый расчёт за набором не успел, досчитываем прямо здесь: пробел
     * нажимают один раз на слово, и лучше подождать пару миллисекунд, чем
     * оставить человека дописывать слово, которое клавиатура и так знает.
     */
    suspend fun awaitCorrection(state: KeyboardState): WordSuggestion? {
        if (state.hasFreshSuggestions) return state.pendingAutoCorrect

        val request = state.suggestionRequest() ?: return null
        return suggestionRepository.suggest(request).firstOrNull { it.isAutoCorrect }
    }

    /** Запрос под текущее состояние; `null` — подсказки в этом поле не нужны. */
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

    /**
     * Запоминает законченное слово вместе с предыдущим — на этом
     * клавиатура и подстраивается под конкретного человека и разговор.
     */
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

    /**
     * Заглавная буква в начале предложения.
     *
     * Правило одно: клавиатура решает за пользователя только на **границе слова**.
     * Стоит курсор в начале предложения — Shift поднят, в середине — опущен.
     * Внутри уже начатого слова Shift не трогаем совсем: там он мог быть поднят
     * вручную, чтобы написать имя с большой буквы, и перебивать это нельзя.
     *
     * Caps Lock — тоже осознанный выбор, его не сбрасываем никогда.
     */
    private fun KeyboardState.autoShift(context: TextContext): ShiftState = when {
        shiftState == ShiftState.CAPS_LOCK || !fieldType.autoCapitalize -> shiftState
        context.composingWord.isNotEmpty() -> shiftState
        context.isSentenceStart -> ShiftState.ACTIVE
        else -> ShiftState.DISABLED
    }

    private companion object {
        /** Пауза перед расчётом: примерно три кадра, на глаз незаметно. */
        const val CALCULATION_DELAY_MILLIS = 45L
    }
}
