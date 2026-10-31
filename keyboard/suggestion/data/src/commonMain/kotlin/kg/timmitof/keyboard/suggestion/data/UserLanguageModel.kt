package kg.timmitof.keyboard.suggestion.data

import kg.timmitof.keyboard.suggestion.data.concurrent.ConcurrentMap
import kg.timmitof.keyboard.suggestion.data.concurrent.ConcurrentQueue
import kg.timmitof.keyboard.suggestion.data.concurrent.ConcurrentSet
import kg.timmitof.keyboard.suggestion.data.concurrent.ConcurrentSortedSet
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized

/**
 * Что клавиатура выучила у пользователя: частоты слов, частоты пар, недавние слова
 * (последние живут только до перезапуска), слова, отвергнувшие автозамену, и слова, похожие на опечатку.
 *
 * Структуры конкурентные: потеря счётчика в гонке не страшна, а блокировки на пути ввода недопустимы.
 */
internal class UserLanguageModel {

    private val words = ConcurrentMap<String, Int>()

    private val pairs = ConcurrentMap<String, ConcurrentMap<String, Int>>()

    // Отсортированные ключи: префиксный поиск — это диапазон, а не проход по всем словам.
    private val sortedWords = ConcurrentSortedSet<String>()

    private val recentLock = SynchronizedObject()

    private val recent = ArrayDeque<String>()

    private val recentSet = HashSet<String>()

    // Порядок нужен только для вытеснения самых старых отказов при переполнении.
    private val rejected = ConcurrentSet<String>()
    private val rejectedOrder = ConcurrentQueue<String>()

    // Новые слова в одной правке от частого словарного: «известными» становятся не сразу.
    private val suspicious = ConcurrentSet<String>()

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
            val followers = pairs.getOrPut(previous) { ConcurrentMap(FOLLOWERS_CAPACITY) }
            followers[word] = (followers[word] ?: 0) + 1
        }

        remember(word)
        trim()

        return word !in suspicious
    }

    /** Откатывает один [learn]: счётчик не уходит ниже нуля, обнулённые слово и пара удаляются. */
    fun unlearn(previous: String, word: String) {
        if (words.computeIfPresent(word) { count -> (count - 1).takeIf { it > 0 } } == null) {
            forgetWord(word)
        }

        if (previous.isEmpty()) return
        val followers = pairs[previous] ?: return
        followers.computeIfPresent(word) { count -> (count - 1).takeIf { it > 0 } }
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

    fun wordFrequencies(): Map<String, Int> = words.asMap().toMap()

    fun knows(word: String): Boolean =
        countOf(word) >= if (word in suspicious) SUSPICIOUS_KNOWN_THRESHOLD else KNOWN_THRESHOLD

    fun followersOf(previous: String): Map<String, Int> = pairs[previous]?.asMap().orEmpty()

    fun pairCount(previous: String, word: String): Int = pairs[previous]?.get(word) ?: 0

    fun wordsWithPrefix(prefix: String): List<String> {
        if (prefix.isEmpty()) return sortedWords.asSet().filter { it.isNotEmpty() }
        // Верхняя граница — префикс с увеличенным последним символом.
        val upper = prefix.dropLast(1) + (prefix.last() + 1)
        return sortedWords.between(prefix, upper)
            .filter { it.length > prefix.length && it.startsWith(prefix) }
    }

    /** Топ-[limit] по счётчику за один проход, без сортировки всех слов. */
    fun frequentWords(limit: Int): List<String> {
        if (limit <= 0) return emptyList()
        val top = ArrayList<Pair<String, Int>>(limit + 1)
        for (entry in words.asMap().entries) {
            if (top.size == limit && entry.value <= top.last().second) continue
            val position = top.indexOfFirst { it.second < entry.value }.takeIf { it >= 0 } ?: top.size
            top.add(position, entry.key to entry.value)
            if (top.size > limit) top.removeAt(top.lastIndex)
        }
        return top.map { it.first }
    }

    fun isRecent(word: String): Boolean = synchronized(recentLock) { word in recentSet }

    fun export(): List<String> = buildList(words.size + pairs.size + rejected.size + suspicious.size) {
        words.asMap().forEach { (word, count) -> add("$WORD_MARK\t$word\t$count") }
        pairs.asMap().forEach { (previous, followers) ->
            followers.asMap().forEach { (word, count) -> add("$PAIR_MARK\t$previous\t$word\t$count") }
        }
        // Отказы — от старых к новым: после загрузки вытесняться будут те же самые.
        rejectedOrder.asIterable().forEach { word -> if (word in rejected) add("$REJECTED_MARK\t$word") }
        suspicious.asSet().forEach { word -> add("$SUSPICIOUS_MARK\t$word") }
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
                        pairs.getOrPut(columns[1]) { ConcurrentMap(FOLLOWERS_CAPACITY) }[columns[2]] = it
                    }

                columns.size == 2 && columns[0] == REJECTED_MARK -> reject(columns[1])

                columns.size == 2 && columns[0] == SUSPICIOUS_MARK -> suspicious.add(columns[1])
            }
        }
        // Пометка без самого слова ничего не значит.
        suspicious.retainAll(words.asMap().keys)
    }

    private fun forgetWord(word: String) {
        sortedWords.remove(word)
        suspicious.remove(word)
    }

    private fun remember(word: String) = synchronized(recentLock) {
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
            words.asMap().forEach { entry ->
                val halved = entry.value / 2
                if (halved > 0) {
                    words[entry.key] = halved
                } else {
                    words.remove(entry.key)
                    forgetWord(entry.key)
                }
            }
        }
        if (words.size > MAX_WORDS) {
            // Все счётчики уже единицы: отсекаем лишнее принудительно.
            words.asMap().keys.take(words.size - MAX_WORDS).forEach { key ->
                words.remove(key)
                forgetWord(key)
            }
        }

        rounds = 0
        while (pairs.size > MAX_PAIRS && rounds++ < MAX_TRIM_ROUNDS) {
            pairs.asMap().forEach { (previous, followers) ->
                followers.asMap().forEach { (word, count) ->
                    val halved = count / 2
                    if (halved > 0) followers[word] = halved else followers.remove(word)
                }
                if (followers.isEmpty()) pairs.remove(previous)
            }
        }
        if (pairs.size > MAX_PAIRS) {
            pairs.asMap().keys.take(pairs.size - MAX_PAIRS).forEach { pairs.remove(it) }
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

        const val FOLLOWERS_CAPACITY = 8

        const val WORD_MARK = "w"
        const val PAIR_MARK = "p"
        const val REJECTED_MARK = "r"
        const val SUSPICIOUS_MARK = "s"
    }
}
