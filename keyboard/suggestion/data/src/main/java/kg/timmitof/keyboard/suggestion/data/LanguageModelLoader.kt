package kg.timmitof.keyboard.suggestion.data

import kg.timmitof.keyboard.data.AssetTextLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LanguageModelLoader @Inject constructor(
    private val assetTextLoader: AssetTextLoader,
) {

    private val models = ConcurrentHashMap<String, LanguageModel>(2)

    private val mutex = Mutex()

    internal suspend fun load(languageCode: String): LanguageModel =
        models[languageCode] ?: mutex.withLock {
            models[languageCode] ?: read(languageCode).also { models[languageCode] = it }
        }

    private suspend fun read(languageCode: String): LanguageModel = withContext(Dispatchers.Default) {
        val words = assetTextLoader.loadText("$DIRECTORY/$languageCode$DICTIONARY_EXTENSION")
            ?: return@withContext LanguageModel.Empty

        val bigrams = assetTextLoader.loadText("$DIRECTORY/$languageCode$BIGRAMS_EXTENSION")
        val dictionary = WordDictionary.parse(words)

        LanguageModel(
            dictionary = dictionary,
            bigrams = bigrams?.let(BigramTable::parse) ?: BigramTable.Empty,
            spellCorrector = SpellCorrector.build(dictionary),
        )
    }

    private companion object {
        const val DIRECTORY = "dictionaries"
        const val DICTIONARY_EXTENSION = ".dict"
        const val BIGRAMS_EXTENSION = ".bigrams"
    }
}
