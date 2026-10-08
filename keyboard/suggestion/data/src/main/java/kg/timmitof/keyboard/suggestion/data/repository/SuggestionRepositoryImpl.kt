package kg.timmitof.keyboard.suggestion.data.repository

import kg.timmitof.keyboard.suggestion.data.LanguageModel
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

    // Личные слова переносим в индекс опечаток модели один раз на язык, дальше он пополняется при обучении.
    private val syncedUserModels = ConcurrentHashMap.newKeySet<String>()

    // Отложенное сохранение у каждого языка своё: переход ru -> en не должен отменять запись ru.
    private val saveJobs = ConcurrentHashMap<String, Job>()

    private val dirtyLanguages = ConcurrentHashMap.newKeySet<String>()

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

    override suspend fun learn(
        languageCode: String,
        previousWord: String,
        word: String,
        isDeliberate: Boolean,
    ) {
        val learned = word.toDictionaryForm()
        if (!learned.isLearnable()) return

        val previous = previousWord.toDictionaryForm().takeIf { it.isLearnable() }.orEmpty()

        val user = userModel(languageCode)
        val model = languageModel(languageCode, user)

        // Отказ от автозамены — тоже осознанный выбор: такое слово не опечатка.
        val trusted = isDeliberate || user.hasRejected(learned)
        val isSuspicious = !trusted && model.isLikelyTypo(learned)
        if (user.learn(previous, learned, isSuspicious)) model.spellCorrector.addWord(learned)
        scheduleSave(languageCode)
    }

    override suspend fun rejectAutoCorrection(
        languageCode: String,
        previousWord: String,
        typed: String,
        corrected: String,
        wasLearned: Boolean,
    ) {
        val original = typed.toDictionaryForm()
        val user = userModel(languageCode)
        val model = languageModel(languageCode, user)
        val previous = previousWord.toDictionaryForm().takeIf { it.isLearnable() }.orEmpty()

        if (wasLearned) {
            val replacement = corrected.toDictionaryForm()
            user.unlearn(previous, replacement)
            // Забытое целиком слово уходит и из исправлений; словарные там и не лежали.
            if (user.countOf(replacement) == 0) model.spellCorrector.removeWord(replacement)
        }
        if (original.isNotEmpty()) user.reject(original)
        scheduleSave(languageCode)

        // Отвергнутое уже в отказах — выучится сразу, без проверки на опечатку.
        learn(languageCode, previousWord, typed)
    }

    override suspend fun prefetch(languageCode: String) {
        val user = userModel(languageCode)
        languageModel(languageCode, user)
    }

    override suspend fun flush() {
        saveJobs.values.forEach(Job::cancel)
        saveJobs.clear()

        dirtyLanguages.toList().forEach { languageCode ->
            if (dirtyLanguages.remove(languageCode)) save(languageCode)
        }
    }

    private suspend fun userModel(languageCode: String): UserLanguageModel =
        userModels[languageCode] ?: mutex.withLock {
            userModels[languageCode] ?: userDictionaryStore.load(languageCode)
                .also { userModels[languageCode] = it }
        }

    private suspend fun languageModel(
        languageCode: String,
        user: UserLanguageModel,
    ): LanguageModel = languageModelLoader.load(languageCode).also { model ->
        // add атомарен, мьютекс не нужен. Тысячи слов по десятку вариантов — в фоне: первая подсказка их не ждёт.
        if (syncedUserModels.add(languageCode)) {
            scope.launch {
                user.wordFrequencies().keys.forEach { word ->
                    if (!user.isSuspicious(word)) model.spellCorrector.addWord(word)
                }
            }
        }
    }

    private fun scheduleSave(languageCode: String) {
        dirtyLanguages.add(languageCode)
        saveJobs.put(
            languageCode,
            scope.launch {
                delay(SAVE_DELAY_MILLIS)
                if (dirtyLanguages.remove(languageCode)) save(languageCode)
            },
        )?.cancel()
    }

    // Запись не отменяем: флаг «грязный» уже снят, отмена потеряла бы выученное.
    private suspend fun save(languageCode: String) = withContext(NonCancellable) {
        userModels[languageCode]?.let { userDictionaryStore.save(languageCode, it) }
        Unit
    }

    private companion object {
        const val SAVE_DELAY_MILLIS = 4_000L
    }
}
