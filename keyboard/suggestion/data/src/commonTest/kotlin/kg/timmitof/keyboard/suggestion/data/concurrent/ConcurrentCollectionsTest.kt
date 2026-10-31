package kg.timmitof.keyboard.suggestion.data.concurrent

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Реализации на Android и iOS разные — поведение должно совпадать с `ConcurrentHashMap`/`ConcurrentSkipListSet`. */
class ConcurrentCollectionsTest {

    @Test
    fun `merge складывает и удаляет ключ при null`() {
        val map = ConcurrentMap<String, Int>()

        assertEquals(1, map.merge("a", 1, Int::plus))
        assertEquals(3, map.merge("a", 2, Int::plus))
        assertNull(map.merge("a", 1) { _, _ -> null })
        assertTrue(map.isEmpty())
    }

    @Test
    fun `computeIfPresent не создает ключ и удаляет при null`() {
        val map = ConcurrentMap<String, Int>()

        assertNull(map.computeIfPresent("a") { it + 1 })
        assertNull(map["a"])

        map["a"] = 1
        assertEquals(2, map.computeIfPresent("a") { it + 1 })
        assertNull(map.computeIfPresent("a") { null })
        assertEquals(0, map.size)
    }

    @Test
    fun `remove по значению удаляет только совпавшее`() {
        val map = ConcurrentMap<String, Int>()
        map["a"] = 1

        assertFalse(map.remove("a", 2))
        assertTrue(map.remove("a", 1))
        assertNull(map.put("b", 1))
        assertEquals(1, map.put("b", 2))
    }

    @Test
    fun `getOrPut не перезаписывает значение`() {
        val map = ConcurrentMap<String, Int>()

        assertEquals(1, map.getOrPut("a") { 1 })
        assertEquals(1, map.getOrPut("a") { 2 })
    }

    @Test
    fun `between исключает обе границы`() {
        val set = ConcurrentSortedSet<String>()
        listOf("ка", "кат", "кот", "кит", "л").forEach { set.add(it) }

        assertFalse(set.add("кот"))
        assertEquals(listOf("кат", "кит", "кот"), set.between("ка", "кп").toList())
        assertEquals(emptyList(), set.between("м", "я").toList())

        assertTrue(set.remove("кит"))
        assertEquals(listOf("ка", "кат", "кот", "л"), set.asSet().toList())
    }

    @Test
    fun `очередь отдает элементы по порядку`() {
        val queue = ConcurrentQueue<String>()
        queue.add("a")
        queue.add("b")

        assertEquals(listOf("a", "b"), queue.asIterable().toList())
        assertEquals("a", queue.poll())
        assertEquals("b", queue.poll())
        assertNull(queue.poll())
    }
}
