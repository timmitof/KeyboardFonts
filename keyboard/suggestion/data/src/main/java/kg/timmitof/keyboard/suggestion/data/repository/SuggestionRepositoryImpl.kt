package kg.timmitof.keyboard.suggestion.data.repository

import kg.timmitof.keyboard.suggestion.data.LanguageModelLoader
import kg.timmitof.keyboard.suggestion.data.SuggestionEngine
import kg.timmitof.keyboard.suggestion.data.UserDictionaryStore
import kg.timmitof.keyboard.suggestion.data.UserLanguageModel
import kg.timmitof.keyboard.suggestion.data.isLearnable
import kg.timmitof.keyboard.suggestion.data.toDictionaryForm
import kg.timmitof.keyboard.suggestion.domain.model.SuggestionRequest
import kg.timmitof.keyboard.suggestion.domain.model.WordSuggestion
import kg.timmitof.keyboard.suggestion.domain.repository.SuggestionRepository
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

@Singleton
class SuggestionRepositoryImpl @Inject constructor(
    private val languageModelLoader: LanguageModelLoader,
    private val userDictionaryStore: UserDictionaryStore,
    private val suggestionEngine: SuggestionEngine,
) : SuggestionRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val userModels = ConcurrentHashMap<String, UserLanguageModel>()

    private val mutex = Mutex()

    // SymSpell — изменяемый индекс; личные слова добавляем в него один раз на язык.
    private val modelMutex = Mutex()

    private val syncedUserModels = ConcurrentHashMap.newKeySet<String>()

    private val pendingSaves = MutableSharedFlow<String>(extraBufferCapacity = SAVE_BUFFER)

    init {
        observeSaves()
    }

    override suspend fun suggest(request: SuggestionRequest): List<WordSuggestion> {
        val user = userModel(request.languageCode)
        val model = languageModel(request.languageCode, user)

        return withContext(Dispatchers.Default) {
            suggestionEngine.suggest(
                request = request,
                model = model,
                user = user,
            )
        }
    }

    override suspend fun learn(languageCode: String, previousWord: String, word: String) {
        val learned = word.toDictionaryForm()
        if (!learned.isLearnable()) return

        val previous = previousWord.toDictionaryForm().takeIf { it.isLearnable() }.orEmpty()

        val user = userModel(languageCode)
        val model = languageModel(languageCode, user)

        user.learn(previous, learned)
        model.spellCorrector.addWord(learned, USER_WORD_FREQUENCY)
        pendingSaves.tryEmit(languageCode)
    }

    override suspend fun prefetch(languageCode: String) {
        val user = userModel(languageCode)
        languageModel(languageCode, user)
    }

    private suspend fun userModel(languageCode: String): UserLanguageModel =
        userModels[languageCode] ?: mutex.withLock {
            userModels[languageCode] ?: userDictionaryStore.load(languageCode)
                .also { userModels[languageCode] = it }
        }

    private suspend fun languageModel(
        languageCode: String,
        user: UserLanguageModel,
    ) = languageModelLoader.load(languageCode).also { model ->
        modelMutex.withLock {
            if (syncedUserModels.add(languageCode)) {
                user.wordFrequencies().forEach { (word, count) ->
                    model.spellCorrector.addWord(
                        word = word,
                        frequency = (count.toLong() * USER_WORD_FREQUENCY)
                            .coerceAtMost(Int.MAX_VALUE.toLong())
                            .toInt(),
                    )
                }
            }
        }
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
        const val USER_WORD_FREQUENCY = 500
    }
}
