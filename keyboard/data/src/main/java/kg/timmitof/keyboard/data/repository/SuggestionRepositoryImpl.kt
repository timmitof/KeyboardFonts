package kg.timmitof.keyboard.data.repository

import kg.timmitof.keyboard.data.suggestion.LanguageModelLoader
import kg.timmitof.keyboard.data.suggestion.SuggestionEngine
import kg.timmitof.keyboard.data.suggestion.UserDictionaryStore
import kg.timmitof.keyboard.data.suggestion.UserLanguageModel
import kg.timmitof.keyboard.data.suggestion.isLearnable
import kg.timmitof.keyboard.data.suggestion.toDictionaryForm
import kg.timmitof.keyboard.domain.model.SuggestionRequest
import kg.timmitof.keyboard.domain.model.WordSuggestion
import kg.timmitof.keyboard.domain.repository.SuggestionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Подсказки слов поверх словаря языка и личной модели пользователя.
 *
 * Личная модель живёт в памяти весь сеанс клавиатуры и сбрасывается на диск
 * с задержкой: обучение идёт на каждом пробеле, а запись файла — раз в несколько
 * секунд после того, как пользователь перестал печатать.
 */
@Singleton
class SuggestionRepositoryImpl @Inject constructor(
    private val languageModelLoader: LanguageModelLoader,
    private val userDictionaryStore: UserDictionaryStore,
    private val suggestionEngine: SuggestionEngine,
) : SuggestionRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val userModels = ConcurrentHashMap<String, UserLanguageModel>()

    private val mutex = Mutex()

    private val pendingSaves = MutableSharedFlow<String>(extraBufferCapacity = SAVE_BUFFER)

    init {
        observeSaves()
    }

    override suspend fun suggest(request: SuggestionRequest): List<WordSuggestion> =
        withContext(Dispatchers.Default) {
            suggestionEngine.suggest(
                request = request,
                model = languageModelLoader.load(request.languageCode),
                user = userModel(request.languageCode),
            )
        }

    override suspend fun learn(languageCode: String, previousWord: String, word: String) {
        val learned = word.toDictionaryForm()
        if (!learned.isLearnable()) return

        val previous = previousWord.toDictionaryForm().takeIf { it.isLearnable() }.orEmpty()

        userModel(languageCode).learn(previous, learned)
        pendingSaves.tryEmit(languageCode)
    }

    override suspend fun prefetch(languageCode: String) {
        languageModelLoader.load(languageCode)
        userModel(languageCode)
    }

    private suspend fun userModel(languageCode: String): UserLanguageModel =
        userModels[languageCode] ?: mutex.withLock {
            userModels[languageCode] ?: userDictionaryStore.load(languageCode)
                .also { userModels[languageCode] = it }
        }

    @OptIn(FlowPreview::class)
    private fun observeSaves() {
        pendingSaves
            .debounce(SAVE_DELAY_MILLIS)
            .onEach { languageCode ->
                userModels[languageCode]?.let { userDictionaryStore.save(languageCode, it) }
            }
            .launchIn(scope)
    }

    private companion object {
        const val SAVE_DELAY_MILLIS = 4_000L
        const val SAVE_BUFFER = 8
    }
}
