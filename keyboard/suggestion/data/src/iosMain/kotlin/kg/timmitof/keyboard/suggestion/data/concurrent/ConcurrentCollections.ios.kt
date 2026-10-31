package kg.timmitof.keyboard.suggestion.data.concurrent

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized

internal actual class ConcurrentMap<K : Any, V : Any> actual constructor(initialCapacity: Int) {

    private val lock = SynchronizedObject()

    private val map = HashMap<K, V>(initialCapacity)

    actual val size: Int get() = synchronized(lock) { map.size }

    actual fun isEmpty(): Boolean = synchronized(lock) { map.isEmpty() }

    actual operator fun get(key: K): V? = synchronized(lock) { map[key] }

    actual operator fun set(key: K, value: V) {
        synchronized(lock) { map[key] = value }
    }

    actual fun put(key: K, value: V): V? = synchronized(lock) { map.put(key, value) }

    actual fun remove(key: K): V? = synchronized(lock) { map.remove(key) }

    actual fun remove(key: K, value: V): Boolean = synchronized(lock) {
        if (map[key] == value) map.remove(key) != null else false
    }

    actual fun clear() {
        synchronized(lock) { map.clear() }
    }

    actual fun merge(key: K, value: V, remapping: (V, V) -> V?): V? = synchronized(lock) {
        val current = map[key]
        val merged = if (current == null) value else remapping(current, value)
        if (merged == null) map.remove(key) else map[key] = merged
        merged
    }

    actual fun computeIfPresent(key: K, remapping: (V) -> V?): V? = synchronized(lock) {
        val current = map[key] ?: return@synchronized null
        val computed = remapping(current)
        if (computed == null) map.remove(key) else map[key] = computed
        computed
    }

    actual fun getOrPut(key: K, defaultValue: () -> V): V = synchronized(lock) { map.getOrPut(key, defaultValue) }

    actual fun asMap(): Map<K, V> = synchronized(lock) { HashMap(map) }
}

internal actual class ConcurrentSet<T : Any> actual constructor() {

    private val lock = SynchronizedObject()

    private val set = HashSet<T>()

    actual val size: Int get() = synchronized(lock) { set.size }

    actual fun isEmpty(): Boolean = synchronized(lock) { set.isEmpty() }

    actual operator fun contains(element: T): Boolean = synchronized(lock) { element in set }

    actual fun add(element: T): Boolean = synchronized(lock) { set.add(element) }

    actual fun remove(element: T): Boolean = synchronized(lock) { set.remove(element) }

    actual fun retainAll(elements: Collection<T>): Boolean = synchronized(lock) { set.retainAll(elements.toSet()) }

    actual fun asSet(): Set<T> = synchronized(lock) { HashSet(set) }
}

/** Отсортированный список с бинарным поиском: выученных слов тысячи, вставка сдвигом дешевле дерева. */
internal actual class ConcurrentSortedSet<T : Comparable<T>> actual constructor() {

    private val lock = SynchronizedObject()

    private val items = ArrayList<T>()

    actual fun add(element: T): Boolean = synchronized(lock) {
        val index = items.binarySearch(element)
        if (index >= 0) return@synchronized false
        items.add(-index - 1, element)
        true
    }

    actual fun remove(element: T): Boolean = synchronized(lock) {
        val index = items.binarySearch(element)
        if (index < 0) return@synchronized false
        items.removeAt(index)
        true
    }

    actual fun asSet(): Set<T> = synchronized(lock) { LinkedHashSet(items) }

    actual fun between(from: T, to: T): Set<T> = synchronized(lock) {
        val start = items.binarySearch(from).let { if (it >= 0) it + 1 else -it - 1 }
        val end = items.binarySearch(to).let { if (it >= 0) it else -it - 1 }
        if (start >= end) emptySet() else LinkedHashSet(items.subList(start, end))
    }
}

internal actual class ConcurrentQueue<T : Any> actual constructor() {

    private val lock = SynchronizedObject()

    private val queue = ArrayDeque<T>()

    actual fun add(element: T) {
        synchronized(lock) { queue.addLast(element) }
    }

    actual fun poll(): T? = synchronized(lock) { queue.removeFirstOrNull() }

    actual fun asIterable(): Iterable<T> = synchronized(lock) { queue.toList() }
}
