package kg.timmitof.keyboard.suggestion.data.concurrent

/*
 * Конкурентные коллекции модели подсказок. Поиск идёт на каждое нажатие, обучение — в фоне, а блокировки
 * на пути ввода недопустимы: на Android это `java.util.concurrent` без блокировок, на iOS — коллекции под замком.
 * Обход — через `asMap`/`asSet`: на Android это живое представление (как у `ConcurrentHashMap`), на iOS — снимок.
 */

internal expect class ConcurrentMap<K : Any, V : Any>(initialCapacity: Int = DEFAULT_CAPACITY) {

    val size: Int

    fun isEmpty(): Boolean

    operator fun get(key: K): V?

    operator fun set(key: K, value: V)

    /** Прежнее значение ключа. */
    fun put(key: K, value: V): V?

    fun remove(key: K): V?

    /** Удаляет, только если значение ключа всё ещё [value]. */
    fun remove(key: K, value: V): Boolean

    fun clear()

    /** Как `ConcurrentHashMap.merge`: новое значение или `null`, если [remapping] удалил ключ. */
    fun merge(key: K, value: V, remapping: (V, V) -> V?): V?

    /** Как `ConcurrentHashMap.computeIfPresent`: `null` от [remapping] удаляет ключ. */
    fun computeIfPresent(key: K, remapping: (V) -> V?): V?

    /** Атомарно: в гонке [defaultValue] может вызваться дважды, но в карте останется одно значение. */
    fun getOrPut(key: K, defaultValue: () -> V): V

    fun asMap(): Map<K, V>
}

internal expect class ConcurrentSet<T : Any>() {

    val size: Int

    fun isEmpty(): Boolean

    operator fun contains(element: T): Boolean

    fun add(element: T): Boolean

    fun remove(element: T): Boolean

    fun retainAll(elements: Collection<T>): Boolean

    fun asSet(): Set<T>
}

internal expect class ConcurrentSortedSet<T : Comparable<T>>() {

    fun add(element: T): Boolean

    fun remove(element: T): Boolean

    fun asSet(): Set<T>

    /** Элементы строго между [from] и [to]. */
    fun between(from: T, to: T): Set<T>
}

internal expect class ConcurrentQueue<T : Any>() {

    fun add(element: T)

    fun poll(): T?

    fun asIterable(): Iterable<T>
}

internal const val DEFAULT_CAPACITY = 16
