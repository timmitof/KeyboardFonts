package kg.timmitof.keyboard.suggestion.domain.model

/**
 * Буква черновика и куда по её клавише попал палец: смещение от центра в долях размера клавиши,
 * `-0.5..0.5` по каждой оси. Без точки касания (ввод из пикера, доступность) — центр.
 */
data class TypedKey(
    val char: Char,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
)

/** Точка на клавиатуре: `x` — в ширинах обычной клавиши, `y` — в высотах ряда. */
data class KeyPoint(val x: Float, val y: Float)

/**
 * Где на раскладке стоят буквы — для пространственной модели опечаток. Строится из самой раскладки,
 * поэтому соседи клавиш верны для любого языка каталога.
 *
 * Ряд растянут на всю ширину, как на экране: ширина клавиши — её вес в долях веса ряда.
 * Сравнивается по ссылке: геометрию кэшируют на раскладку.
 */
class KeyGeometry private constructor(private val keys: Map<Char, Bounds>) {

    fun centerOf(char: Char): KeyPoint? = keys[char]?.let { KeyPoint(it.centerX, it.centerY) }

    /** Точка касания [typed] на клавиатуре; `null`, если такой буквы на раскладке нет. */
    fun pointOf(typed: TypedKey): KeyPoint? = keys[typed.char]?.let { bounds ->
        KeyPoint(
            x = bounds.centerX + typed.offsetX * bounds.width,
            y = bounds.centerY + typed.offsetY,
        )
    }

    private class Bounds(val centerX: Float, val centerY: Float, val width: Float)

    @KeyGeometryDsl
    class RowBuilder internal constructor() {
        internal val slots = ArrayList<Pair<Char?, Float>>(12)

        /** Клавиша-буква; одна буква на нескольких клавишах — берётся первая. */
        fun key(char: Char, weight: Float) {
            slots += char to weight
        }

        /** Клавиша без буквы (Shift, Backspace) или пустое место: только занимает ширину. */
        fun gap(weight: Float) {
            slots += null to weight
        }
    }

    @KeyGeometryDsl
    class Builder internal constructor() {
        private val rows = ArrayList<List<Pair<Char?, Float>>>(5)

        fun row(block: RowBuilder.() -> Unit) {
            rows += RowBuilder().apply(block).slots
        }

        internal fun build(): KeyGeometry {
            // Единица ширины — клавиша самого «плотного» ряда, чтобы соседство по горизонтали было ~1.
            val columns = rows.maxOfOrNull { row -> row.sumOf { it.second.toDouble() } }?.toFloat() ?: 0f
            val keys = HashMap<Char, Bounds>(48)

            rows.forEachIndexed { rowIndex, row ->
                val total = row.sumOf { it.second.toDouble() }.toFloat()
                if (total <= 0f) return@forEachIndexed

                val scale = columns / total
                var start = 0f
                row.forEach { (char, weight) ->
                    val width = weight * scale
                    if (char != null && char !in keys) {
                        keys[char] = Bounds(centerX = start + width / 2, centerY = rowIndex + 0.5f, width = width)
                    }
                    start += width
                }
            }
            return KeyGeometry(keys)
        }
    }
}

@DslMarker
annotation class KeyGeometryDsl

/** `keyGeometry { row { key('й', 1f); gap(1.5f) } }` — ряды сверху вниз, клавиши слева направо. */
fun keyGeometry(block: KeyGeometry.Builder.() -> Unit): KeyGeometry =
    KeyGeometry.Builder().apply(block).build()
