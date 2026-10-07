package kg.timmitof.keyboard.suggestion.data

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.ConcurrentSkipListSet

/**
 * Что клавиатура выучила у пользователя: частоты слов, частоты пар, недавние слова
 * (последние живут только до перезапуска), слова, отвергнувшие автозамену, и слова, похожие на опечатку.
 *
 * Структуры конкурентные: потеря счётчика в гонке не страшна, а блокировки на пути ввода недопустимы.
 */
internal class UserLanguageModel {

    private val words = ConcurrentHashMap<String, Int>()

    private val pairs = ConcurrentHashMap<String, ConcurrentHashMap<String, Int>>()

    // Отсортированные ключи: префиксный поиск — это subSet, а не проход по всем словам.
    private val sortedWords = ConcurrentSkipListSet<String>()

    private val recent = ArrayDeque<String>()

    private val recentSet = HashSet<String>()

    // Порядок нужен только для вытеснения самых старых отказов при переполнении.
    private val rejected = ConcurrentHashMap.newKeySet<String>()
    private val rejectedOrder = ConcurrentLinkedQueue<String>()

    // Новые слова в одной правке от частого словарного: «известными» становятся не сразу.
    private val suspicious = ConcurrentHashMap.newKeySet<String>()

    /**
     * @param isSuspicious слово похоже на опечатку. Помечается только новое слово: выученное раньше
     * не разжалуем, а осознанный ввод (`false`) пометку снимает. После [SUSPICIOUS_KNOWN_THRESHOLD]
     * повторов пометка снимается сама — опечатку столько раз подряд не повторяют.
     * @return можно ли отдавать слово в индекс опечаток (подозрительное — нельзя).
     */
    fun learn(previous: String, word: String, isSuspicious: Boolean = false): Boolean {
        val count = words.merge(word, 1, Int::plus) ?: 1
        sortedWords.add(word)

        when {
            !isSuspicious -> suspicious.remove(word)
            count == 1 -> suspicious.add(word)
            count >= SUSPICIOUS_KNOWN_THRESHOLD -> suspicious.remove(word)
        }

        if (previous.isNotEmpty()) {
            val followers = pairs.getOrPut(previous) { ConcurrentHashMap(8) }
            followers[word] = (followers[word] ?: 0) + 1
        }

        remember(word)
        trim()

        return word !in suspicious
    }

    /** Откатывает один [learn]: счётчик не уходит ниже нуля, обнулённые слово и пара удаляются. */
    fun unlearn(previous: String, word: String) {
        if (words.computeIfPresent(word) { _, count -> (count - 1).takeIf { it > 0 } } == null) {
            forgetWord(word)
        }

        if (previous.isEmpty()) return
        val followers = pairs[previous] ?: return
        followers.computeIfPresent(word) { _, count -> (count - 1).takeIf { it > 0 } }
        if (followers.isEmpty()) pairs.remove(previous, followers)
    }

    /** Набранное слово, ради которого отменили автозамену: больше его не исправляем. */
    fun reject(word: String) {
        if (!rejected.add(word)) return
        rejectedOrder.add(word)
        while (rejected.size > MAX_REJECTED) {
            rejectedOrder.poll()?.let(rejected::remove) ?: break
        }
    }

    fun hasRejected(word: String): Boolean = word in rejected

    fun isSuspicious(word: String): Boolean = word in suspicious

    fun countOf(word: String): Int = words[word] ?: 0

    fun wordFrequencies(): Map<String, Int> = words.toMap()

    fun knows(word: String): Boolean =
        countOf(word) >= if (word in suspicious) SUSPICIOUS_KNOWN_THRESHOLD else KNOWN_THRESHOLD

    fun followersOf(previous: String): Map<String, Int> = pairs[previous].orEmpty()

    fun pairCount(previous: String, word: String): Int = pairs[previous]?.get(word) ?: 0

    fun wordsWithPrefix(prefix: String): List<String> {
        if (prefix.isEmpty()) return sortedWords.filter { it.isNotEmpty() }
        // Верхняя граница — префикс с увеличенным последним символом.
        val upper = prefix.dropLast(1) + (prefix.last() + 1)
        return sortedWords.subSet(prefix, false, upper, false)
            .filter { it.length > prefix.length && it.startsWith(prefix) }
    }

    /** Топ-[limit] по счётчику за один проход, без сортировки всех слов. */
    fun frequentWords(limit: Int): List<String> {
        if (limit <= 0) return emptyList()
        val top = ArrayList<Map.Entry<String, Int>>(limit + 1)
        for (entry in words.entries) {
            if (top.size == limit && entry.value <= top.last().value) continue
            val position = top.indexOfFirst { it.value < entry.value }.takeIf { it >= 0 } ?: top.size
            top.add(position, java.util.AbstractMap.SimpleEntry(entry.key, entry.value))
            if (top.size > limit) top.removeAt(top.lastIndex)
        }
        return top.map { it.key }
    }

    fun isRecent(word: String): Boolean = synchronized(recent) { word in recentSet }

    fun export(): List<String> = buildList(words.size + pairs.size + rejected.size + suspicious.size) {
        words.entries.forEach { (word, count) -> add("$WORD_MARK\t$word\t$count") }
        pairs.entries.forEach { (previous, followers) ->
            followers.entries.forEach { (word, count) -> add("$PAIR_MARK\t$previous\t$word\t$count") }
        }
        // Отказы — от старых к новым: после загрузки вытесняться будут те же самые.
        rejectedOrder.forEach { word -> if (word in rejected) add("$REJECTED_MARK\t$word") }
        suspicious.forEach { word -> add("$SUSPICIOUS_MARK\t$word") }
    }

    /** Строки с незнакомой меткой пропускаются: старые файлы без отказов и пометок читаются как раньше. */
    fun restore(lines: Sequence<String>) {
        lines.forEach { line ->
            val columns = line.split('\t')
            when {
                columns.size == 3 && columns[0] == WORD_MARK ->
                    columns[2].toIntOrNull()?.let {
                        words[columns[1]] = it
                        sortedWords.add(columns[1])
                    }

                columns.size == 4 && columns[0] == PAIR_MARK ->
                    columns[3].toIntOrNull()?.let {
                        pairs.getOrPut(columns[1]) { ConcurrentHashMap(8) }[columns[2]] = it
                    }

                columns.size == 2 && columns[0] == REJECTED_MARK -> reject(columns[1])

                columns.size == 2 && columns[0] == SUSPICIOUS_MARK -> suspicious.add(columns[1])
            }
        }
        // Пометка без самого слова ничего не значит.
        suspicious.retainAll(words.keys)
    }

    private fun forgetWord(word: String) {
        sortedWords.remove(word)
        suspicious.remove(word)
    }

    private fun remember(word: String) = synchronized(recent) {
        if (!recentSet.add(word)) recent.remove(word)
        recent.addFirst(word)
        while (recent.size > MAX_RECENT) recentSet.remove(recent.removeLast())
    }

    /**
     * Вместо выбрасывания «хвоста» делит счётчики пополам: редкие слова отмирают, частые остаются.
     * Одного деления может не хватить (все счётчики большие), поэтому повторяем, пока размер не упадёт.
     */
    private fun trim() {
        var rounds = 0
        while (words.size > MAX_WORDS && rounds++ < MAX_TRIM_ROUNDS) {
            words.entries.forEach { entry ->
                val halved = entry.value / 2
                if (halved > 0) {
                    entry.setValue(halved)
                } else {
                    words.remove(entry.key)
                    forgetWord(entry.key)
                }
            }
        }
        if (words.size > MAX_WORDS) {
            // Все счётчики уже единицы: отсекаем лишнее принудительно.
            words.keys.take(words.size - MAX_WORDS).forEach { key ->
                words.remove(key)
                forgetWord(key)
            }
        }

        rounds = 0
        while (pairs.size > MAX_PAIRS && rounds++ < MAX_TRIM_ROUNDS) {
            pairs.entries.forEach { (previous, followers) ->
                followers.entries.forEach { follower ->
                    val halved = follower.value / 2
                    if (halved > 0) follower.setValue(halved) else followers.remove(follower.key)
                }
                if (followers.isEmpty()) pairs.remove(previous)
            }
        }
        if (pairs.size > MAX_PAIRS) {
            pairs.keys.take(pairs.size - MAX_PAIRS).forEach { pairs.remove(it) }
        }
    }

    private companion object {
        const val KNOWN_THRESHOLD = 2

        /** Опечатку пять раз подряд не повторяют, а своё слово набирают легко. */
        const val SUSPICIOUS_KNOWN_THRESHOLD = 5

        const val MAX_WORDS = 4000
        const val MAX_PAIRS = 4000
        const val MAX_RECENT = 60
        const val MAX_REJECTED = 300
        const val MAX_TRIM_ROUNDS = 12

        const val WORD_MARK = "w"
        const val PAIR_MARK = "p"
        const val REJECTED_MARK = "r"
        const val SUSPICIOUS_MARK = "s"
    }
}
