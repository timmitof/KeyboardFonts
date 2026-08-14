package kg.timmitof.keyboard.suggestion.data

import java.util.concurrent.ConcurrentHashMap

/**
 * То, чему клавиатура научилась у пользователя на одном языке.
 *
 * Три слоя памяти, от самого долгого к самому короткому:
 * 1. частота слов — что этот человек вообще пишет;
 * 2. частота пар слов — его обороты речи («буду через», «всё ок»);
 * 3. недавние слова — лексика текущего разговора, живёт только до перезапуска.
 *
 * Структуры конкурентные: подсказки считаются в фоне, а обучение идёт
 * из другой корутины — потеря одного счётчика в гонке ничего не портит,
 * а блокировки на пути ввода недопустимы.
 */
internal class UserLanguageModel {

    private val words = ConcurrentHashMap<String, Int>()

    private val pairs = ConcurrentHashMap<String, ConcurrentHashMap<String, Int>>()

    private val recent = ArrayDeque<String>()

    /** Запоминает слово и его связь с предыдущим. */
    fun learn(previous: String, word: String) {
        words[word] = (words[word] ?: 0) + 1

        if (previous.isNotEmpty()) {
            val followers = pairs.getOrPut(previous) { ConcurrentHashMap(8) }
            followers[word] = (followers[word] ?: 0) + 1
        }

        remember(word)
        trim()
    }

    fun countOf(word: String): Int = words[word] ?: 0

    /** Снимок частот для добавления личных слов в индекс исправления опечаток. */
    fun wordFrequencies(): Map<String, Int> = words.toMap()

    fun knows(word: String): Boolean = countOf(word) >= KNOWN_THRESHOLD

    fun followersOf(previous: String): Map<String, Int> = pairs[previous].orEmpty()

    fun pairCount(previous: String, word: String): Int = pairs[previous]?.get(word) ?: 0

    /** Слова пользователя, начинающиеся с [prefix]. */
    fun wordsWithPrefix(prefix: String): List<String> =
        words.keys.filter { it.length > prefix.length && it.startsWith(prefix) }

    /** Самые частые слова — запасной вариант, когда контекста нет совсем. */
    fun frequentWords(limit: Int): List<String> =
        words.entries.sortedByDescending { it.value }.take(limit).map { it.key }

    fun isRecent(word: String): Boolean = synchronized(recent) { word in recent }

    fun recentWords(): List<String> = synchronized(recent) { recent.toList() }

    /** Строки для сохранения на диск. */
    fun export(): List<String> = buildList(words.size + pairs.size) {
        words.entries.forEach { (word, count) -> add("$WORD_MARK\t$word\t$count") }
        pairs.entries.forEach { (previous, followers) ->
            followers.entries.forEach { (word, count) -> add("$PAIR_MARK\t$previous\t$word\t$count") }
        }
    }

    /** Восстанавливает модель из строк [export]. */
    fun restore(lines: Sequence<String>) {
        lines.forEach { line ->
            val columns = line.split('\t')
            when {
                columns.size == 3 && columns[0] == WORD_MARK ->
                    columns[2].toIntOrNull()?.let { words[columns[1]] = it }

                columns.size == 4 && columns[0] == PAIR_MARK ->
                    columns[3].toIntOrNull()?.let {
                        pairs.getOrPut(columns[1]) { ConcurrentHashMap(8) }[columns[2]] = it
                    }
            }
        }
    }

    private fun remember(word: String) = synchronized(recent) {
        recent.remove(word)
        recent.addFirst(word)
        while (recent.size > MAX_RECENT) recent.removeLast()
    }

    /**
     * Держит модель в разумном размере: вместо выбрасывания «хвоста» делит
     * все счётчики пополам — редкие слова отмирают, частые остаются.
     */
    private fun trim() {
        if (words.size > MAX_WORDS) {
            words.entries.forEach { entry ->
                val halved = entry.value / 2
                if (halved > 0) entry.setValue(halved) else words.remove(entry.key)
            }
        }
        if (pairs.size > MAX_PAIRS) {
            pairs.entries.forEach { (previous, followers) ->
                followers.entries.forEach { follower ->
                    val halved = follower.value / 2
                    if (halved > 0) follower.setValue(halved) else followers.remove(follower.key)
                }
                if (followers.isEmpty()) pairs.remove(previous)
            }
        }
    }

    private companion object {
        /** Со скольких повторов слово считается «своим» и не исправляется. */
        const val KNOWN_THRESHOLD = 2

        const val MAX_WORDS = 4000
        const val MAX_PAIRS = 4000
        const val MAX_RECENT = 60

        const val WORD_MARK = "w"
        const val PAIR_MARK = "p"
    }
}
