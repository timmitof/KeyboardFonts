package kg.timmitof.build_logic.convention.dictionaries

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.Locale
import java.util.zip.GZIPInputStream
import kotlin.math.ln
import kotlin.math.roundToInt

/**
 * Частотный список → `dictionaries/<code>.dict`: `слово<TAB>частота`, частота — логарифм числа вхождений
 * в шкале 1..1000 (самое частое слово — 1000), строки отсортированы для бинарного поиска.
 * Шкала та же, что у прежних словарей: под неё подобраны веса `SuggestionEngine`.
 *
 * Если у языка задан список словоформ, рядом пишется `<code>.forms` — фильтр Блума ([WordFormsFilter]).
 * Рядом всегда пишется `<code>.spell` — индекс опечаток по номерам строк словаря ([SpellIndexWriter]).
 */
@CacheableTask
abstract class GenerateDictionariesTask : DefaultTask() {

    @get:Input
    abstract val languages: ListProperty<DictionarySpec>

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NAME_ONLY)
    abstract val sources: ConfigurableFileCollection

    /** Ручные словари в ассетах: если рядом есть исходник, они бы молча столкнулись при слиянии ассетов. */
    @get:Internal
    abstract val staticAssets: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun generate() {
        val output = outputDirectory.get().asFile.resolve(DIRECTORY)
        output.deleteRecursively()
        output.mkdirs()

        val sourcesByName = sources.files.associateBy(File::getName)

        languages.get().forEach { spec ->
            val source = sourcesByName[spec.source]?.takeIf(File::isFile) ?: return@forEach

            val static = staticAssets.get().asFile.resolve("$DIRECTORY/${spec.code}$EXTENSION")
            if (static.exists()) {
                throw GradleException(
                    "Словарь ${spec.code} собирается из ${spec.source}, но в ассетах лежит ещё и $static — удалите его."
                )
            }

            val forms = spec.forms?.let { name ->
                val file = sourcesByName[name]?.takeIf(File::isFile)
                if (file == null) logger.warn("dictionaries: ${spec.code} — нет файла словоформ $name, собираю без фильтра")
                file?.let { spec.readForms(it) }
            }

            val dictionary = spec.rank(source, forms)
            output.resolve("${spec.code}$EXTENSION").writeText(dictionary.text, Charsets.UTF_8)
            logger.lifecycle(
                "dictionaries: ${spec.code} — ${dictionary.words.size} слов из ${spec.source}, " +
                        "отброшено похожих на опечатки: ${dictionary.typos}"
            )

            val spell = SpellIndexWriter(dictionary.words, spec.alphabet)
                .writeTo(output.resolve("${spec.code}$SPELL_EXTENSION"))
            logger.lifecycle(
                "dictionaries: ${spec.code} — индекс опечаток: ${spell.postings} записей в ${spell.buckets} корзинах, " +
                        "${spell.bytes / 1024} КБ"
            )

            if (forms != null) {
                forms.writeTo(output.resolve("${spec.code}$FORMS_EXTENSION"))
                logger.lifecycle(
                    "dictionaries: ${spec.code} — фильтр словоформ: ${forms.size} форм из ${spec.forms}, " +
                            "${forms.byteSize / 1024} КБ"
                )
            }
        }
    }

    /** Размер фильтра зависит от числа форм, поэтому файл читается дважды: сначала счёт, потом заполнение. */
    private fun DictionarySpec.readForms(file: File): WordFormsFilter {
        var count = 0
        forEachForm(file) { count++ }
        return WordFormsFilter(count).also { filter -> forEachForm(file, filter::add) }
    }

    /** Формы приводятся к виду словаря и проходят тот же отбор, что и слова частотного списка. */
    private inline fun DictionarySpec.forEachForm(file: File, action: (String) -> Unit) {
        val folding = folding()
        val input = file.inputStream().buffered().let { if (file.name.endsWith(".gz")) GZIPInputStream(it) else it }
        input.bufferedReader(Charsets.UTF_8).useLines { lines ->
            lines.forEach { line ->
                val form = normalize(line.trim(), folding)
                if (accepts(form)) action(form)
            }
        }
    }

    /** @param words слова в порядке строк [text] — по этим номерам ссылается индекс опечаток. */
    private class Dictionary(val text: String, val words: List<String>, val typos: Int)

    private fun DictionarySpec.rank(source: File, forms: WordFormsFilter?): Dictionary {
        val folding = folding()
        val counts = HashMap<String, Long>(maxWords * 4)

        source.forEachLine(Charsets.UTF_8) { raw ->
            val line = raw.trimEnd('\r')
            val space = line.lastIndexOf(' ')
            if (space <= 0) return@forEachLine

            val count = line.substring(space + 1).toLongOrNull()?.takeIf { it > 0 } ?: return@forEachLine
            val word = normalize(line.substring(0, space), folding)

            if (accepts(word)) counts.merge(word, count, Long::plus)
        }
        if (counts.isEmpty()) throw GradleException("dictionaries: в $source нет ни одного подходящего слова")

        val ranked = counts.entries
            .sortedWith(compareByDescending<Map.Entry<String, Long>> { it.value }.thenBy { it.key })
        val scale = ln(ranked.first().value.toDouble()).coerceAtLeast(1.0)
        fun scoreOf(count: Long): Int = (MAX_SCORE * ln(count.toDouble()) / scale).roundToInt().coerceIn(1, MAX_SCORE)

        val filter = TypoFilter(
            ranked.asSequence()
                .takeWhile { scoreOf(it.value) >= typoNeighborMinScore }
                .map { it.key }
                .toList(),
            validForms = forms,
        )

        // Место отброшенной опечатки занимает следующее по частоте слово — размер словаря сохраняется.
        val kept = ArrayList<Map.Entry<String, Long>>(maxWords)
        var typos = 0
        for (entry in ranked) {
            if (kept.size == maxWords) break
            if (kept.size >= trustedWords && filter.isTypo(entry.key)) {
                typos++
            } else {
                kept += entry
            }
        }

        // Сортировка по кодам символов — ровно так сравнивает WordDictionary.
        val sorted = kept.sortedBy { it.key }
        val text = sorted
            .joinToString(separator = "\n", postfix = "\n") { (word, count) -> "$word\t${scoreOf(count)}" }

        return Dictionary(text = text, words = sorted.map { it.key }, typos = typos)
    }

    private fun DictionarySpec.folding(): Map<Char, Char> = folds.chunked(2).associate { it[0] to it[1] }

    private fun normalize(word: String, folding: Map<Char, Char>): String {
        val lower = word.lowercase(Locale.ROOT)
        if (folding.isEmpty()) return lower
        return buildString(lower.length) { lower.forEach { append(folding[it] ?: it) } }
    }

    /** Апостроф допустим только внутри слова (`don't`), одиночные буквы — только из белого списка. */
    private fun DictionarySpec.accepts(word: String): Boolean = when {
        word.isEmpty() || word.length > maxLength -> false
        word.length == 1 -> word[0] in singleLetters
        !word.first().isLetter() || !word.last().isLetter() -> false
        else -> word.all { it in alphabet }
    }

    companion object {
        const val DIRECTORY = "dictionaries"
        const val EXTENSION = ".dict"
        const val FORMS_EXTENSION = ".forms"
        const val SPELL_EXTENSION = ".spell"

        private const val MAX_SCORE = 1000
    }
}
