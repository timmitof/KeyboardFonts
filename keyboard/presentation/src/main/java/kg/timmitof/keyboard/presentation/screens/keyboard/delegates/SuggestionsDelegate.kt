package kg.timmitof.keyboard.presentation.screens.keyboard.delegates

import kg.timmitof.keyboard.domain.model.SuggestionRequest
import kg.timmitof.keyboard.domain.model.TextContext
import kg.timmitof.keyboard.domain.model.WordSuggestion
import kg.timmitof.keyboard.domain.repository.SuggestionRepository
import kg.timmitof.keyboard.presentation.screens.keyboard.KeyboardSyntax
import kg.timmitof.keyboard.presentation.screens.keyboard.states.KeyboardState
import kg.timmitof.keyboard.presentation.screens.keyboard.states.ShiftState
import kg.timmitof.keyboard.presentation.screens.keyboard.states.isUpperCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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

    @OptIn(ExperimentalCoroutinesApi::class)
    val suggestions: Flow<List<WordSuggestion>> = requests
        .filterNotNull()
        .mapLatest(suggestionRepository::suggest)

    /** Новый снимок текста: пересчитать подсказки и поправить Shift. */
    suspend fun KeyboardSyntax.applyTextContext(context: TextContext) {
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
    suspend fun KeyboardSyntax.applySuggestions(suggestions: List<WordSuggestion>) {
        if (state.suggestions == suggestions) return

        val collapseFonts = state.suggestions.isEmpty() && suggestions.isNotEmpty()
        reduce {
            state.copy(
                suggestions = suggestions,
                isFontsExpanded = state.isFontsExpanded && !collapseFonts,
            )
        }
    }

    /** Пересобирает запрос под текущее состояние; в неподходящих полях — гасит подсказки. */
    suspend fun KeyboardSyntax.requestSuggestions() {
        val languageCode = state.selectedLanguage?.code
        if (languageCode == null || !state.allowsSuggestions) {
            requests.value = null
            if (state.suggestions.isNotEmpty()) reduce { state.copy(suggestions = emptyList()) }
            return
        }

        requests.value = SuggestionRequest(
            languageCode = languageCode,
            context = state.textContext,
            isShifted = state.shiftState.isUpperCase(),
            allowsAutoCorrect = state.fieldType.allowsAutoCorrect,
        )
    }

    /**
     * Запоминает законченное слово вместе с предыдущим — на этом
     * клавиатура и подстраивается под конкретного человека и разговор.
     */
    suspend fun learnWord(state: KeyboardState, word: String) {
        val languageCode = state.selectedLanguage?.code ?: return
        if (!state.allowsSuggestions) return

        suggestionRepository.learn(
            languageCode = languageCode,
            previousWord = state.textContext.previousWord,
            word = word,
        )
    }

    suspend fun prefetch(languageCode: String) = suggestionRepository.prefetch(languageCode)

    /**
     * Shift в начале предложения и его сброс внутри слова.
     *
     * Caps Lock — осознанный выбор пользователя, его не трогаем.
     */
    private fun KeyboardState.autoShift(context: TextContext): ShiftState = when {
        shiftState == ShiftState.CAPS_LOCK || !fieldType.autoCapitalize -> shiftState
        context.composingWord.isEmpty() && context.isSentenceStart -> ShiftState.ACTIVE
        else -> ShiftState.DISABLED
    }
}
