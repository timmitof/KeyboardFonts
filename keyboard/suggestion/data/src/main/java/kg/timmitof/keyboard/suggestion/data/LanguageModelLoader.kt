package kg.timmitof.keyboard.suggestion.data

import kg.timmitof.keyboard.data.AssetTextLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LanguageModelLoader @Inject constructor(
    private val assetTextLoader: AssetTextLoader,
) {

    private val models = ConcurrentHashMap<String, LanguageModel>(2)

    private val mutex = Mutex()

    private val indexScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    internal suspend fun load(languageCode: String): LanguageModel =
        models[languageCode] ?: mutex.withLock {
            models[languageCode] ?: read(languageCode).also { models[languageCode] = it }
        }

    private suspend fun read(languageCode: String): LanguageModel = coroutineScope {
        val wordsText = async(Dispatchers.IO) {
            assetTextLoader.loadText("$DIRECTORY/$languageCode$DICTIONARY_EXTENSION")
        }
        val bigramsText = async(Dispatchers.IO) {
            assetTextLoader.loadText("$DIRECTORY/$languageCode$BIGRAMS_EXTENSION")
        }

        val formsBytes = async(Dispatchers.IO) {
            assetTextLoader.loadBytes("$DIRECTORY/$languageCode$FORMS_EXTENSION")
        }

        val words = wordsText.await() ?: return@coroutineScope LanguageModel.Empty

        val dictionary = async(Dispatchers.Default) { WordDictionary.parse(words) }
        val bigrams = async(Dispatchers.Default) {
            bigramsText.await()?.let(BigramTable::parse) ?: BigramTable.Empty
        }
        // Списка форм может и не быть (английский) — тогда язык работает без фильтра.
        val forms = async(Dispatchers.Default) {
            formsBytes.await()?.let(WordForms::parse) ?: WordForms.Empty
        }
        val parsed = dictionary.await()

        LanguageModel(
            dictionary = parsed,
            bigrams = bigrams.await(),
            // Свой scope: индекс должен пережить вызвавшую корутину.
            spellIndex = indexScope.async { SpellCorrector.build(parsed) },
            forms = forms.await(),
        )
    }

    private companion object {
        const val DIRECTORY = "dictionaries"
        const val DICTIONARY_EXTENSION = ".dict"
        const val BIGRAMS_EXTENSION = ".bigrams"
        const val FORMS_EXTENSION = ".forms"
    }
}
