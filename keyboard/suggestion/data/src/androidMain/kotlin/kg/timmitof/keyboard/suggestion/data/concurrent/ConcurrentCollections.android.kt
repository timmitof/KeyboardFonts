package kg.timmitof.keyboard.suggestion.data.concurrent

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.ConcurrentSkipListSet

internal actual class ConcurrentMap<K : Any, V : Any> actual constructor(initialCapacity: Int) {

    private val map = ConcurrentHashMap<K, V>(initialCapacity)

    actual val size: Int get() = map.size

    actual fun isEmpty(): Boolean = map.isEmpty()

    actual operator fun get(key: K): V? = map[key]

    actual operator fun set(key: K, value: V) {
        map[key] = value
    }

    actual fun put(key: K, value: V): V? = map.put(key, value)

    actual fun remove(key: K): V? = map.remove(key)

    actual fun remove(key: K, value: V): Boolean = map.remove(key, value)

    actual fun clear() = map.clear()

    actual fun merge(key: K, value: V, remapping: (V, V) -> V?): V? = map.merge(key, value, remapping)

    actual fun computeIfPresent(key: K, remapping: (V) -> V?): V? =
        map.computeIfPresent(key) { _, current -> remapping(current) }

    // ConcurrentMap.getOrPut из stdlib — через putIfAbsent.
    actual fun getOrPut(key: K, defaultValue: () -> V): V = map.getOrPut(key, defaultValue)

    actual fun asMap(): Map<K, V> = map
}

internal actual class ConcurrentSet<T : Any> actual constructor() {

    private val set: MutableSet<T> = ConcurrentHashMap.newKeySet()

    actual val size: Int get() = set.size

    actual fun isEmpty(): Boolean = set.isEmpty()

    actual operator fun contains(element: T): Boolean = element in set

    actual fun add(element: T): Boolean = set.add(element)

    actual fun remove(element: T): Boolean = set.remove(element)

    actual fun retainAll(elements: Collection<T>): Boolean = set.retainAll(elements)

    actual fun asSet(): Set<T> = set
}

internal actual class ConcurrentSortedSet<T : Comparable<T>> actual constructor() {

    private val set = ConcurrentSkipListSet<T>()

    actual fun add(element: T): Boolean = set.add(element)

    actual fun remove(element: T): Boolean = set.remove(element)

    actual fun asSet(): Set<T> = set

    actual fun between(from: T, to: T): Set<T> = set.subSet(from, false, to, false)
}

internal actual class ConcurrentQueue<T : Any> actual constructor() {

    private val queue = ConcurrentLinkedQueue<T>()

    actual fun add(element: T) {
        queue.add(element)
    }

    actual fun poll(): T? = queue.poll()

    actual fun asIterable(): Iterable<T> = queue
}
