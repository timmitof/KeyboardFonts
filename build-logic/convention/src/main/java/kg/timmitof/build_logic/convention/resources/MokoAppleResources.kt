package kg.timmitof.build_logic.convention.resources

import java.io.File

/** Доводка iOS-ресурсов, которые генерирует moko-resources. */
internal object MokoAppleResources {

    private const val BASE = "Base.lproj"
    private const val STRINGS_FILE = "Localizable.strings"

    private val KEY = Regex("^\"((?:[^\"\\\\]|\\\\.)*)\"\\s*=")

    /**
     * На Android строки без перевода берутся из `values/`, а на iOS локаль без ключа показывает сам ключ.
     * В каждую локаль дописываются недостающие строки из Base — непереведённые названия и образцы текста.
     */
    fun fillMissingFromBase(outputResDir: File) {
        val base = File(outputResDir, "$BASE/$STRINGS_FILE").takeIf(File::exists) ?: return
        val baseLines = base.readLines().mapNotNull { line -> keyOf(line)?.let { it to line } }

        outputResDir.listFiles { file -> file.isDirectory && file.extension == "lproj" && file.name != BASE }
            .orEmpty()
            .map { File(it, STRINGS_FILE) }
            .filter(File::exists)
            .forEach { strings ->
                val present = strings.readLines().mapNotNull(::keyOf).toSet()
                val missing = baseLines.filter { (key, _) -> key !in present }.map { (_, line) -> line }
                if (missing.isNotEmpty()) strings.appendText(missing.joinToString("\n", prefix = "\n"))
            }
    }

    private fun keyOf(line: String): String? = KEY.find(line)?.groupValues?.get(1)
}
