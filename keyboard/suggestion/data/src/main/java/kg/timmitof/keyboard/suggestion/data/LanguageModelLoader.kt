package kg.timmitof.keyboard.suggestion.data

import android.os.SystemClock
import android.util.Log
import kg.timmitof.keyboard.data.AssetTextLoader
import kotlinx.coroutines.Dispatchers
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

    internal suspend fun load(languageCode: String): LanguageModel =
        models[languageCode] ?: mutex.withLock {
            models[languageCode] ?: read(languageCode).also { models[languageCode] = it }
        }

    private suspend fun read(languageCode: String): LanguageModel = coroutineScope {
        val startedAt = SystemClock.elapsedRealtime()

        val wordsText = async(Dispatchers.IO) {
            assetTextLoader.loadText("$DIRECTORY/$languageCode$DICTIONARY_EXTENSION")
        }
        val bigramsText = async(Dispatchers.IO) {
            assetTextLoader.loadText("$DIRECTORY/$languageCode$BIGRAMS_EXTENSION")
        }
        val formsBytes = async(Dispatchers.IO) {
            assetTextLoader.loadBytes("$DIRECTORY/$languageCode$FORMS_EXTENSION")
        }
        // Индекс опечаток не разбирается — только отображается в память, пока парсится словарь.
        val spellBuffer = async(Dispatchers.IO) {
            assetTextLoader.loadBuffer("$DIRECTORY/$languageCode$SPELL_EXTENSION")
        }

        val words = wordsText.await() ?: return@coroutineScope LanguageModel.empty()

        val dictionary = async(Dispatchers.Default) { WordDictionary.parse(words) }
        val bigrams = async(Dispatchers.Default) {
            bigramsText.await()?.let(BigramTable::parse) ?: BigramTable.Empty
        }
        // Списка форм может и не быть (английский) — тогда язык работает без фильтра.
        val forms = async(Dispatchers.Default) {
            formsBytes.await()?.let(WordForms::parse) ?: WordForms.Empty
        }
        val parsed = dictionary.await()

        // Нет файла или он от другого словаря — исправления только по выученным словам.
        val spellIndex = spellBuffer.await()?.let { DictionarySpellIndex.open(it, parsed) }
        if (spellIndex == null) Log.w(TAG, "$languageCode: нет подходящего индекса опечаток")

        LanguageModel(
            dictionary = parsed,
            bigrams = bigrams.await(),
            spellCorrector = SpellCorrector.create(parsed, spellIndex),
            forms = forms.await(),
        ).also {
            Log.d(TAG, "$languageCode: модель загружена за ${SystemClock.elapsedRealtime() - startedAt} мс")
        }
    }

    private companion object {
        const val TAG = "LanguageModelLoader"

        const val DIRECTORY = "dictionaries"
        const val DICTIONARY_EXTENSION = ".dict"
        const val BIGRAMS_EXTENSION = ".bigrams"
        const val FORMS_EXTENSION = ".forms"
        const val SPELL_EXTENSION = ".spell"
    }
}
